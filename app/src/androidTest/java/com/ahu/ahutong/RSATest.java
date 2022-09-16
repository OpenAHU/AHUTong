package com.ahu.ahutong;

import android.util.Log;

import com.ahu.ahutong.utils.RSA;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class RSATest {
    @Test
    public void testEncrypt() throws Exception {
        String encrypt = RSA.encryptByPublicKey("123".getBytes(StandardCharsets.UTF_8));
        Log.i("encode", encrypt);
    }
}
