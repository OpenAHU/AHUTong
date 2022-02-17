package com.ahu.ahutong.data.reptile.utils;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class VpnURL {
    public final static String URL_WPN_AHU_BASE = "https://wvpn.ahu.edu.cn/%s/%s";
    private final static String IV_KEY = "wrdvpnisthebest!";

    public static String getProxyUrl(String plaintUrl) throws Exception {
        URL url = new URL(plaintUrl);
        return String.format(URL_WPN_AHU_BASE, url.getProtocol(), encrypt(url.getAuthority()) + url.getPath());
    }

    public static String getPlaintUrl(String proxyUrl) throws Exception {
        URL url = new URL(proxyUrl);
        String path = url.getPath().substring(1);
        String[] split = path.split("/", 3);
        String decryptedHost = decrypt(split[1].substring(32));
        return String.format("%s://%s/%s?%s", split[0], decryptedHost, split[2], url.getQuery());
    }

    private static String encrypt(String plainText) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(IV_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = new SecretKeySpec(IV_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher instance = Cipher.getInstance("AES/CFB/NoPadding");
        instance.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] bytes = instance.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return byteToHex(IV_KEY.getBytes(StandardCharsets.UTF_8)) + byteToHex(bytes);
    }

    private static String decrypt(String srcText) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(IV_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = new SecretKeySpec(IV_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher instance = Cipher.getInstance("AES/CFB/NoPadding");
        instance.init(Cipher.DECRYPT_MODE, keySpec, iv);
        byte[] bytes = instance.doFinal(hexToByte(srcText));
        return new String(bytes);
    }

    private static byte[] hexToByte(String hex) {
        int m, n;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = (byte) intVal;
        }
        return ret;
    }


    private static String byteToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (byte aByte : bytes) {
            strHex = Integer.toHexString(aByte & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }
}
