package com.qiapi.qiapigateway;

import cn.hutool.core.util.RandomUtil;
import com.qiapi.qiapicommon.model.entity.InterfaceInfo;
import com.qiapi.qiapicommon.model.entity.User;
import com.qiapi.qiapicommon.model.entity.UserInterfaceInfo;
import com.qiapi.qiapicommon.service.InnerInterfaceInfoService;
import com.qiapi.qiapicommon.service.InnerUserInterfaceInfoService;
import com.qiapi.qiapicommon.service.InnerUserService;
import com.qiapi.qiapicommon.service.InnerCreditService;
import com.qiapi.project.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 全局过滤
 *
 * @author zhexueqi
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference(check = false)
    private InnerUserService innerUserService;

    @DubboReference(check = false)
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference(check = false)
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @DubboReference(check = false)
    private InnerCreditService innerCreditService;

    private static final List<String> IP_WHITE_LIST = Collections.singletonList("127.0.0.1");

    // 路径白名单，这些路径不需要进行认证过滤
    private static final List<String> PATH_WHITE_LIST = Arrays.asList(
            "/user/register",
            "/user/login",
            "/user/login/wx_open",
            "/user/logout");

    // 平台业务接口路径，需要登录认证但不需要密钥签名
    private static final List<String> PLATFORM_API_PATHS = Arrays.asList(
            "/api/user",
            "/api/interfaceInfo",
            "/api/analysis");

    // 平台内部调试接口路径，支持基于Session的认证
    private static final List<String> INTERNAL_DEBUG_PATHS = Collections.singletonList(
            "/api/interfaceInfo/invoke");

    // 第三方API调用路径，需要完整的密钥认证
    private static final List<String> THIRD_PARTY_API_PATHS = Collections.singletonList(
            "/third-party");

    private static final String INTERFACE_HOST = "http://localhost:8101";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().value();
        String path = INTERFACE_HOST + requestPath;
        String method = Objects.requireNonNull(request.getMethod()).toString();
        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = Objects.requireNonNull(request.getLocalAddress()).getHostString();
        log.info("请求来源地址：" + sourceAddress);
        log.info("请求来源地址：" + request.getRemoteAddress());
        ServerHttpResponse response = exchange.getResponse();

        // 检查是否为白名单路径，如果是则直接放行
        if (isWhiteListPath(requestPath)) {
            log.info("白名单路径，直接放行：" + requestPath);
            return chain.filter(exchange);
        }
        // 检查是否为平台内部调试路径
        if (isInternalDebugPath(requestPath)) {
            log.info("平台内部调试路径，使用Session认证：" + requestPath);
            String userId = request.getHeaders().getFirst("userId");
            if (userId == null) {
                return handleNoAuth(response);
            }
            Long userIdLong = Long.parseLong(userId);
            return handleInternalDebug(exchange, chain, response, userIdLong);
        }

        // 检查是否为平台业务接口路径
        if (isPlatformApiPath(requestPath)) {
            log.info("平台业务接口，使用Session认证：" + requestPath);
            return handlePlatformApi(exchange, chain, request, response);
        }

        // 检查是否为第三方API调用路径
        if (isThirdPartyApiPath(requestPath)) {
            log.info("第三方API调用，使用密钥认证：" + requestPath);
            // 继续执行完整的密钥认证流程
        } else {
            // 其他路径默认放行
            log.info("其他路径，直接放行：" + requestPath);
            return chain.filter(exchange);
        }
        // 2. 访问控制 - 黑白名单
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        // 3. 用户鉴权（判断 ak、sk 是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        // 去数据库中查是否已分配给用户
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error", e);
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        assert nonce != null;
        if (Long.parseLong(nonce) > 10000L) {
            return handleNoAuth(response);
        }
        // 时间和当前时间不能超过 5 分钟
        long currentTime = System.currentTimeMillis() / 1000;
        final long FIVE_MINUTES = 60 * 5L;
        assert timestamp != null;
        if ((currentTime - Long.parseLong(timestamp)) >= FIVE_MINUTES) {
            return handleNoAuth(response);
        }
        // 查出 secretKey
        String secretKey = invokeUser.getSecretKey();
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("body", body);
        paramMap.put("accessKey", accessKey);
        paramMap.put("nonce", RandomUtil.randomNumbers(5));
        paramMap.put("timestamp", System.currentTimeMillis() / 1000 + "");
        String serverSign = SignUtils.getSign(body, paramMap);
        if (sign == null || !sign.equals(serverSign)) {
            return handleNoAuth(response);
        }
        // 4. 请求的模拟接口是否存在，以及请求方法是否匹配
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error", e);
        }
        if (interfaceInfo == null) {
            return handleNoAuth(response);
        }
        // 检查额度和调用次数
        UserInterfaceInfo userInterfaceInfo = innerUserInterfaceInfoService.getUserInterfaceInfo(interfaceInfo.getId(),
                invokeUser.getId());

        // 优先检查额度系统，如果额度充足就使用额度，否则检查旧的调用次数系统
        boolean hasCreditQuota = false;
        try {
            hasCreditQuota = innerCreditService.checkCreditSufficient(invokeUser.getId(), interfaceInfo.getId(), 1L);
        } catch (Exception e) {
            log.error("检查额度失败", e);
        }

        // 如果额度不足，再检查旧的调用次数系统
        if (!hasCreditQuota && (userInterfaceInfo == null || userInterfaceInfo.getLeftNum() < 1)) {
            return handleInvokeError(response);
        }

        // 5. 请求转发，调用模拟接口 + 响应日志
        // Mono<Void> filter = chain.filter(exchange);
        // return filter;

        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId(), hasCreditQuota);

    }

    /**
     * 处理响应
     *
     * @param exchange exchange
     * @param chain    chain
     * @return Mono<Void>
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId,
            long userId, boolean useCreditSystem) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7. 调用成功，扣减额度或调用次数
                                        try {
                                            if (useCreditSystem) {
                                                // 优先扣减额度
                                                boolean creditDeducted = innerCreditService.consumeCredit(userId,
                                                        interfaceInfoId, 1L);
                                                if (!creditDeducted) {
                                                    log.warn("额度扣减失败，尝试扣减调用次数");
                                                    innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                                }
                                            } else {
                                                // 使用旧的调用次数系统
                                                innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                            }
                                        } catch (Exception e) {
                                            log.error("扣减额度或调用次数失败", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);// 释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); // data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 检查请求路径是否在白名单中
     * 
     * @param requestPath 请求路径
     * @return 是否在白名单中
     */
    private boolean isWhiteListPath(String requestPath) {
        return PATH_WHITE_LIST.stream().anyMatch(whiteListPath -> {
            // 支持前缀匹配，例如 /user/login 可以匹配 /user/login 和 /user/login/xxx
            return requestPath.equals(whiteListPath) || requestPath.startsWith(whiteListPath + "/")
                    || requestPath.startsWith(whiteListPath + "?");
        });
    }

    /**
     * 检查是否为平台业务接口路径
     * 
     * @param requestPath 请求路径
     * @return 是否为平台业务接口
     */
    private boolean isPlatformApiPath(String requestPath) {
        return PLATFORM_API_PATHS.stream().anyMatch(requestPath::startsWith);
    }

    /**
     * 检查是否为第三方API调用路径
     * 
     * @param requestPath 请求路径
     * @return 是否为第三方API调用
     */
    private boolean isThirdPartyApiPath(String requestPath) {
        return THIRD_PARTY_API_PATHS.stream().anyMatch(requestPath::startsWith);
    }

    /**
     * 检查是否为平台内部调试路径
     * 
     * @param requestPath 请求路径
     * @return 是否为内部调试路径
     */
    private boolean isInternalDebugPath(String requestPath) {
        return INTERNAL_DEBUG_PATHS.stream().anyMatch(debugPath -> {
            return requestPath.equals(debugPath) || requestPath.startsWith(debugPath + "/")
                    || requestPath.startsWith(debugPath + "?");
        });
    }

    /**
     * 处理平台业务接口请求（基于Session认证）
     * 
     * @param exchange 请求交换对象
     * @param chain    过滤器链
     * @param request  请求对象
     * @param response 响应对象
     * @return Mono<Void>
     */
    private Mono<Void> handlePlatformApi(ServerWebExchange exchange, GatewayFilterChain chain,
            ServerHttpRequest request, ServerHttpResponse response) {
        try {
            // 获取Session信息（从 Cookie 中获取）
            String sessionId = getSessionFromCookie(request);
            if (sessionId == null) {
                log.warn("平台业务接口缺少Session信息");
                return handleNoAuth(response);
            }

            // 有Session就直接放行，由后端服务处理具体的用户认证
            log.info("平台业务接口请求，直接放行由后端服务处理");
            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("处理平台业务接口请求异常", e);
            return handleNoAuth(response);
        }
    }

    /**
     * 处理平台内部调试请求（基于Session认证）
     *
     * @param exchange   请求交换对象
     * @param chain      过滤器链
     * @param response   响应对象
     * @param userIdLong 用户ID
     * @return Mono<Void>
     */
    private Mono<Void> handleInternalDebug(ServerWebExchange exchange, GatewayFilterChain chain,
            ServerHttpResponse response, Long userIdLong) {
        try {
            log.info("平台内部调试请求，执行带额度扣减的响应处理 - 用户ID: {}", userIdLong);

            // 需要读取请求体来获取接口ID
            ServerHttpRequest request = exchange.getRequest();
            return DataBufferUtils.join(request.getBody())
                    .defaultIfEmpty(exchange.getResponse().bufferFactory().allocateBuffer(0))
                    .flatMap(dataBuffer -> {
                        try {
                            // 读取请求体
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            String requestBody = new String(bytes, StandardCharsets.UTF_8);

                            log.info("平台内部调试请求体：{}", requestBody);

                            // 解析请求体中的接口ID
                            Long interfaceId = parseInterfaceIdFromRequestBody(requestBody);

                            if (interfaceId != null) {
                                // 将接口ID和用户ID保存到exchange的attributes中，供响应阶段使用
                                exchange.getAttributes().put("interfaceInfoId", interfaceId);
                                exchange.getAttributes().put("userId", userIdLong);
                                log.info("从请求体中解析出接口ID: {}，用户ID: {}", interfaceId, userIdLong);
                            } else {
                                log.warn("无法从请求体中解析出接口ID");
                            }

                            // 重新构建请求，因为请求体已经被读取
                            DataBuffer newBuffer = exchange.getResponse().bufferFactory().wrap(bytes);

                            // 创建新的请求体
                            Flux<DataBuffer> newBody = Flux.just(newBuffer);

                            // 重新构建ServerHttpRequest，使用装饰器模式
                            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(request) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return newBody;
                                }

                                @Override
                                public HttpHeaders getHeaders() {
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.putAll(super.getHeaders());
                                    headers.set("Content-Length", String.valueOf(bytes.length));
                                    return headers;
                                }
                            };

                            // 创建新的exchange
                            ServerWebExchange newExchange = exchange.mutate()
                                    .request(newRequest)
                                    .build();

                            // 执行带额度扣减的响应处理
                            return handleInternalDebugResponse(newExchange, chain, userIdLong);

                        } catch (Exception e) {
                            log.error("解析请求体失败", e);
                            return handleInternalDebugResponse(exchange, chain, userIdLong);
                        }
                    });

        } catch (Exception e) {
            log.error("处理平台内部调试请求异常", e);
            return handleNoAuth(response);
        }
    }

    /**
     * 从 Cookie 中获取 Session ID
     * 
     * @param request 请求对象
     * @return Session ID
     */
    private String getSessionFromCookie(ServerHttpRequest request) {
        List<String> cookies = request.getHeaders().get("Cookie");
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }

        for (String cookie : cookies) {
            String[] cookiePairs = cookie.split(";");
            for (String pair : cookiePairs) {
                String[] keyValue = pair.trim().split("=", 2);
                if (keyValue.length == 2 && "SESSION".equals(keyValue[0].trim())) {
                    return keyValue[1].trim();
                }
            }
        }
        return null;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

    /**
     * 处理平台内部调试响应（带额度扣减）
     * 
     * @param exchange 请求交换对象
     * @param chain    过滤器链
     * @param userId   用户ID
     * @return Mono<Void>
     */
    private Mono<Void> handleInternalDebugResponse(ServerWebExchange exchange, GatewayFilterChain chain, Long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            // 装饰响应，增强能力
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    log.info("平台内部调试响应处理 - 用户ID: {}", userId);
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(
                                fluxBody.map(dataBuffer -> {
                                    // 检查响应状态
                                    HttpStatus statusCode = getStatusCode();
                                    if (statusCode == HttpStatus.OK) {
                                        // 调用成功，需要扣减额度
                                        try {
                                            // 从exchange的attributes中获取接口ID
                                            Long interfaceInfoId = (Long) exchange.getAttribute("interfaceInfoId");

                                            if (interfaceInfoId != null) {
                                                // 优先使用额度系统
                                                boolean hasCreditQuota = false;
                                                try {
                                                    hasCreditQuota = innerCreditService.checkCreditSufficient(userId,
                                                            interfaceInfoId, 1L);
                                                } catch (Exception e) {
                                                    log.error("检查额度失败", e);
                                                }

                                                if (hasCreditQuota) {
                                                    // 使用额度系统扣减
                                                    boolean creditDeducted = innerCreditService.consumeCredit(userId,
                                                            interfaceInfoId, 1L);
                                                    if (!creditDeducted) {
                                                        log.warn("平台内部调试：额度扣减失败，尝试扣减调用次数");
                                                        innerUserInterfaceInfoService.invokeCount(interfaceInfoId,
                                                                userId);
                                                    } else {
                                                        log.info("平台内部调试：额度扣减成功 - 用户ID: {}, 接口ID: {}", userId,
                                                                interfaceInfoId);
                                                    }
                                                } else {
                                                    // 使用旧的调用次数系统
                                                    log.info("平台内部调试：使用旧的调用次数系统 - 用户ID: {}, 接口ID: {}", userId,
                                                            interfaceInfoId);
                                                    innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                                }
                                            } else {
                                                log.warn("平台内部调试：无法从请求体中获取接口ID，跳过额度扣减");
                                            }
                                        } catch (Exception e) {
                                            log.error("平台内部调试：扣减额度或调用次数失败", e);
                                        }
                                    } else {
                                        log.warn("平台内部调试：调用失败，不扣减额度 - 状态码: {}", statusCode);
                                    }

                                    // 读取响应内容
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);

                                    // 打印日志
                                    String data = new String(content, StandardCharsets.UTF_8);
                                    log.info("平台内部调试响应结果：{}", data);

                                    return bufferFactory.wrap(content);
                                }));
                    }
                    return super.writeWith(body);
                }
            };

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        } catch (Exception e) {
            log.error("平台内部调试响应处理异常", e);
            return chain.filter(exchange);
        }
    }

    /**
     * 从请求体中解析接口ID
     * 
     * @param requestBody 请求体内容
     * @return 接口ID
     */
    private Long parseInterfaceIdFromRequestBody(String requestBody) {
        try {
            if (requestBody == null || requestBody.trim().isEmpty()) {
                log.warn("请求体为空，无法解析接口ID");
                return null;
            }

            // 简单的JSON解析，寻找 "id" 或 "interfaceId" 字段
            // 这是一个简单的实现，实际中应该使用JSON解析库
            String[] patterns = { "\"id\"\\s*:\\s*(\\d+)", "\"interfaceId\"\\s*:\\s*(\\d+)" };

            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(requestBody);
                if (m.find()) {
                    String idStr = m.group(1);
                    Long id = Long.parseLong(idStr);
                    log.info("从请求体中解析出接口ID: {}", id);
                    return id;
                }
            }

            log.warn("无法从请求体中解析出接口ID: {}", requestBody);
            return null;

        } catch (Exception e) {
            log.error("解析请求体中的接口ID失败", e);
            return null;
        }
    }
}