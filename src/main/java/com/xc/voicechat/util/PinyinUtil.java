package com.xc.voicechat.util;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

@Slf4j
public class PinyinUtil {
   /**
    * 将中文汉字转换为拼音
    * @param chinese 中文字符串
    * @return 拼音字符串
    */
   public static String toPinyin(String chinese) {
       StringBuilder pinyin = new StringBuilder();
       HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
       format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
       format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
       char[] charArray = chinese.toCharArray();
       for (char c : charArray) {
           if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
               try {
                   pinyin.append(PinyinHelper.toHanyuPinyinStringArray(c, format)[0]);
               } catch (BadHanyuPinyinOutputFormatCombination e) {
                   log.error("中文转拼音出错", e);
               }
           } else {
               pinyin.append(c);
           }
       }
       return pinyin.toString();
   }


}