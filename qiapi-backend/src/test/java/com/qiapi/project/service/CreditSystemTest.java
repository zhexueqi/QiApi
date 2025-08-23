package com.qiapi.project.service;

import com.qiapi.project.model.vo.CreditBalanceVO;
import com.qiapi.project.model.vo.PointBalanceVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 额度系统单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class CreditSystemTest {

    @Resource
    private CreditService creditService;

    @Resource
    private PointService pointService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_INTERFACE_ID = 1L;

    @Test
    public void testInitUserPoints() {
        // 测试初始化用户积分账户
        boolean result = pointService.initUserPoints(TEST_USER_ID);
        assertTrue(result);

        // 检查积分余额
        PointBalanceVO balance = pointService.getPointBalance(TEST_USER_ID);
        assertNotNull(balance);
        assertEquals(100L, balance.getTotalPoints());
        assertEquals(100L, balance.getAvailablePoints());
        assertEquals(0L, balance.getFrozenPoints());
    }

    @Test
    public void testApplyCreditLimit() {
        // 测试申请免费额度
        boolean result = creditService.applyCreditLimit(TEST_USER_ID, TEST_INTERFACE_ID);
        assertTrue(result);

        // 检查额度余额
        List<CreditBalanceVO> balances = creditService.getCreditBalance(TEST_USER_ID);
        assertNotNull(balances);
        assertFalse(balances.isEmpty());

        CreditBalanceVO balance = balances.get(0);
        assertEquals(100L, balance.getTotalCredit());
        assertEquals(100L, balance.getRemainingCredit());
        assertEquals(0L, balance.getUsedCredit());
        assertTrue(balance.getFreeApplied());
    }

    @Test
    public void testConsumeCredit() {
        // 先申请免费额度
        creditService.applyCreditLimit(TEST_USER_ID, TEST_INTERFACE_ID);

        // 测试消费额度
        boolean result = creditService.consumeCredit(TEST_USER_ID, TEST_INTERFACE_ID, 10L);
        assertTrue(result);

        // 检查余额
        List<CreditBalanceVO> balances = creditService.getCreditBalance(TEST_USER_ID);
        CreditBalanceVO balance = balances.get(0);
        assertEquals(90L, balance.getRemainingCredit());
        assertEquals(10L, balance.getUsedCredit());
    }

    @Test
    public void testExchangePointsForCredit() {
        // 初始化积分账户
        pointService.initUserPoints(TEST_USER_ID);

        // 测试积分兑换额度（使用50积分兑换5次额度）
        boolean result = creditService.exchangePointsForCredit(TEST_USER_ID, TEST_INTERFACE_ID, 50L);
        assertTrue(result);

        // 检查积分余额
        PointBalanceVO pointBalance = pointService.getPointBalance(TEST_USER_ID);
        assertEquals(50L, pointBalance.getAvailablePoints());

        // 检查额度余额
        List<CreditBalanceVO> creditBalances = creditService.getCreditBalance(TEST_USER_ID);
        CreditBalanceVO creditBalance = creditBalances.get(0);
        assertEquals(5L, creditBalance.getRemainingCredit());
    }

    @Test
    public void testCheckCreditSufficient() {
        // 先申请免费额度
        creditService.applyCreditLimit(TEST_USER_ID, TEST_INTERFACE_ID);

        // 测试额度检查
        assertTrue(creditService.checkCreditSufficient(TEST_USER_ID, TEST_INTERFACE_ID, 50L));
        assertTrue(creditService.checkCreditSufficient(TEST_USER_ID, TEST_INTERFACE_ID, 100L));
        assertFalse(creditService.checkCreditSufficient(TEST_USER_ID, TEST_INTERFACE_ID, 101L));
    }

    @Test
    public void testPointOperations() {
        // 初始化积分账户
        pointService.initUserPoints(TEST_USER_ID);

        // 测试获得积分
        boolean earnResult = pointService.earnPoints(TEST_USER_ID, 50L, "测试获得积分");
        assertTrue(earnResult);

        PointBalanceVO balance = pointService.getPointBalance(TEST_USER_ID);
        assertEquals(150L, balance.getAvailablePoints());

        // 测试消费积分
        boolean spendResult = pointService.spendPoints(TEST_USER_ID, 30L, "测试消费积分");
        assertTrue(spendResult);

        balance = pointService.getPointBalance(TEST_USER_ID);
        assertEquals(120L, balance.getAvailablePoints());

        // 测试积分充足性检查
        assertTrue(pointService.checkPointsSufficient(TEST_USER_ID, 100L));
        assertFalse(pointService.checkPointsSufficient(TEST_USER_ID, 150L));
    }
}