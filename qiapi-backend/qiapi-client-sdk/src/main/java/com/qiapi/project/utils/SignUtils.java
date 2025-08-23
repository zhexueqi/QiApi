package com.qiapi.project.utils;


import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

import java.util.HashMap;

/**
 * @author zhexueqi
 * @ClassName SignUtils
 * @since 2024/8/2    18:33
 */
public class SignUtils {

    public static String getSign(String secretKey, HashMap<String, String> hashMap) {
        Digester digester = new Digester(DigestAlgorithm.SHA256);
        String content = hashMap.toString() + "." + secretKey;
        return digester.digestHex(content);
    }
}
