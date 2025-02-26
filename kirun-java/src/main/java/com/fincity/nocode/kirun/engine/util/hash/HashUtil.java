package com.fincity.nocode.kirun.engine.util.hash;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class HashUtil {

    private static final ConcurrentHashMap<String, MessageDigest> digestCache = new ConcurrentHashMap<>();

    public static <T> JsonElement createHash(T input, String algorithm) {
        try {
            MessageDigest md = getMessageDigest(algorithm);
            byte[] valueBytes = convertToBytes(input);
            byte[] hash = md.digest(valueBytes);
            return new JsonPrimitive(bytesToHex(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not supported: " + algorithm, e);
        }
    }

    private static <T> byte[] convertToBytes(T input) {
        if (input == null) {
            return new byte[0];
        }

        if (input instanceof Number) {
            if (input instanceof Double || input instanceof Float) {
                return doubleToBytes(((Number) input).doubleValue());
            }
            return longToBytes(((Number) input).longValue());
        }

        return input.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFFL);
            value >>= 8;
        }
        return result;
    }

    private static byte[] doubleToBytes(double value) {
        long bits = Double.doubleToLongBits(value);
        return longToBytes(bits);
    }

    private static MessageDigest getMessageDigest(String algorithm) throws NoSuchAlgorithmException {
        return digestCache.computeIfAbsent(algorithm, alg -> {
            try {
                return MessageDigest.getInstance(alg);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
