package com.ssms.common.utils;

public class Helper {

    public static String generateGravatarUrl(String email) {
        String hash = com.ssms.common.utils.MD5Util.md5Hex(email);
        return String.format("https://www.gravatar.com/avatar/%s.jpg?s=400&d=identicon", hash);
    }
}
