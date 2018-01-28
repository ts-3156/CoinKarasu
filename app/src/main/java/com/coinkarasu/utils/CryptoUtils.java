package com.coinkarasu.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CryptoUtils";

    public static String HMAC_SHA256Encode(String message) {
        return HMAC_SHA256Encode(String.valueOf(System.currentTimeMillis()), message);
    }

    public static String HMAC_SHA256Encode(String secretKey, String message) {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "hmacSHA256");

        Mac mac;
        try {
            mac = Mac.getInstance("hmacSHA256");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException e) {
            CKLog.e(TAG, e);
            return "";
        } catch (InvalidKeyException e) {
            CKLog.e(TAG, e);
            return "";
        }
        byte[] rawHmac = mac.doFinal(message.getBytes());

        return new String(encodeHex(rawHmac));
    }

    private static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    // org.apache.commons.codec.binary.Hex.encodeHex
    private static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return out;
    }
}
