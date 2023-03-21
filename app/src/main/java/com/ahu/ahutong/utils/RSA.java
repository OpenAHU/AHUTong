package com.ahu.ahutong.utils;


import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSA {
    public static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final int MAX_ENCRYPT_BLOCK = 117;
    private final static String RSA_ALGORITHM = "RSA";
    private final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCPX8Mg9x2+a8go7O7UdZ/kjZGY\n" +
            "JlF8XTFpeoPS+LPcS7I7BNMo72j3mUQSK6KOmHPpWmrbV9FoU6JzLQCP4T+v+Bvy\n" +
            "JgUrGFAYpRy/QUUI16O6X4dNhvd0snDcbDrS7WrRyZEpg1o84++kScqY6WgWjDiM\n" +
            "DJVPO6b2nl+Xf4F6KwIDAQAB";

    public static String encryptByPublicKey(byte[] data)
            throws Exception {
        byte[] keyBytes = Base64.decode(PUBLIC_KEY, Base64.DEFAULT);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }


}
