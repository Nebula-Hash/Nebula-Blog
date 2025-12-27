package com.nebula.boot4.utils;

import java.security.MessageDigest;

/**
 * 代码领取微信公众号【程序员Mars】
 *
 * @className: MD5Utils
 * @author: Nebula-Hash
 * @date: 2025/11/25 22:50
 */
public class MD5Utils {


    /**
     * MD5加密
     *
     * @param text text
     * @return String
     */
    public static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
