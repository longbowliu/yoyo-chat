package com.xc.voicechat.util;



public class WeatherUtils {

    //预处理地理位置
    public static String preprocessLocation(String location) {
        if (containsChinese(location)) {
            return PinyinUtil.toPinyin(location);
        }
        return location;
    }

    public static boolean containsChinese(String str) {
        return str.matches("[\\u4E00-\\u9FA5]+");
    }
}