package com.eveb.saasops.common.validator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.common.utils.StringUtil;
import com.eveb.saasops.modules.operate.dto.ActivityRuleDto;
import com.eveb.saasops.modules.operate.dto.JRegisterDto;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;

import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.Collections3;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 数据校验
 */
public abstract class Assert {

    public static void isBlank(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new R200Exception(message);
        }
    }

    public static void isNull(Object object, String message) {
        if (object == null) {
            throw new R200Exception(message);
        }
    }


    public static void isNotEmpty(List object, String message) {
        if (!Collections3.isNotEmpty(object)) {
            throw new R200Exception(message);
        }
    }

    public static void isNullOrEmpty(List list, String message) {
        if (null == list || list.size() == 0) {
            throw new R200Exception(message);
        }
    }

    public static void isQq(Object object, String message) {
        if (null == object || !object.toString().matches("[1-9][0-9]{0,14}")) {
            throw new R200Exception(message);
        }
    }

    public static void isWeChat(Object object, String message) {
//        if (null == object || !object.toString().matches("^[a-zA-Z]{1}[-_a-zA-Z0-9]{5,19}+$")) {
//            throw new R200Exception(message);
//        }
    }

    public static void isPInt(Object object, String message) {
        if (null == object || !object.toString().matches("^[1-9]*[1-9][0-9]*$")) {
            throw new R200Exception(message);
        }
    }

    public static void isPhone(Object object, String message) {
       /* if (null == object || !object.toString().matches("^1(3\\d|47|5((?!4)\\d)|7(0|1|[6-8])|8\\d)\\d{8,8}$")) {
            throw new R200Exception(message);
        }*/
        if (null == object || object.toString().length() !=11 || !object.toString().matches("^[1-9]*[1-9][0-9]*$")) {
            throw new R200Exception(message);
        }
    }

    public static void isNumeric(Object object, String message) {
        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$")) {
            throw new R200Exception(message);
        }
    }

    public static void isChina(Object object, String message) {
        if (null == object || !object.toString().matches("^[\\u4e00-\\u9fa5]{0,}$")) {
            throw new R200Exception(message);
        }
    }

    public static void isNumeric(Object object, String message, Integer max) {
        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$") || object.toString().length() > max) {
            throw new R200Exception(message);
        }
    }

    public static void isNumeric(BigDecimal object, String message, Integer max, BigDecimal minValue) {
        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$") || object.toString().length() > max || minValue.compareTo(object) == 1) {
            throw new R200Exception(message);
        }
    }

    public static void isNumericInterregional(Integer object, String message, Double minValue, Double maxValue) {

        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$") || minValue > object || object > maxValue) {
            throw new R200Exception(message);
        }
    }

    //不能大于某一个值
    public static void isMaxNum(BigDecimal object, String message, BigDecimal maxNum) {
        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$") || object.compareTo(maxNum) == 1) {
            throw new R200Exception(message);
        }
    }

    //第一个入参大于第二个入参
    public static void isMax(BigDecimal object, BigDecimal object1, String message) {
        if (object.compareTo(object1) == 1) {
            throw new R200Exception(message);
        }
    }

    public static void isCharacter(Object object, String message) {
        if (null == object || !object.toString().matches("[0-9a-zA-Z]+$")) {
            throw new R200Exception(message);
        }
        String a = object.toString().substring(0, 1);
        if (!a.matches("[a-zA-Z]+$")) {
            throw new R200Exception(message);
        }
    }

    public static void accEqualPwd(String loginName, String pwd, String message) {
        if (StringUtils.isEmpty(pwd) || loginName.equals(pwd)) {
            throw new R200Exception(message);
        }
    }

    public static void isAccount(Object object, String message) {

        if (object.toString().length() > 10 || object.toString().length() < 6) {
            throw new R200Exception(message);
        }
    }

    public static void isLenght(Object object, String message, int start, int end) {
        if (!org.springframework.util.StringUtils.isEmpty(object) && (object.toString().length() > end || object.toString().length() < start)) {
            throw new R200Exception(message);
        }
    }

    public static void isSiteCode(String siteCode, String message, int start, int end) {
        String format = "^[0-9a-zA-Z]{" + start + "," + end + "}$";
        if (!siteCode.matches(format)) {
            throw new R200Exception(message);
        }
    }

    public static void isBankCardNo(Object object, String message, int start, int end) {
        if (object.toString().length() < start || object.toString().length() > end) {
            throw new R200Exception(message);
        }
    }

    public static void message(String message) {
        throw new R200Exception(message);
    }

    public static void checkEmail(String email, String message) {
        String format = "\\w+@[\\w\\-]+\\.[a-z]+(\\.[a-z]+)?";
        if (!email.matches(format)) {
            throw new R200Exception(message);
        }
    }

    /**
     * 判断是否为汉字及.
     *
     * @param character
     * @param message
     */
    public static void chineseCharacter(String character, String message) {
        String format = "[\\u4e00-\\u9fa5-\\.]+$";
        if (!character.matches(format)) {
            throw new R200Exception(message);
        }
    }

    public static void checkmobile(String mobile, String message) {
        String format = "^1(3|4|5|7|8)\\d{9}$";
        if (!mobile.matches(format)) {
            throw new R200Exception(message);
        }
    }

    public static void isBigDecimalNum(BigDecimal object, String message) {
        if (new BigDecimal(object.intValue()).compareTo(object) != 0) {
            throw new R200Exception(message);
        }
    }

    /**
     * 判断是否包含有中文字符
     */
    public static void isContainChinaChar(String str, String message) {
        if (StringUtil.isNotEmpty(str)) {
            String format = "[\u4e00-\u9fa5]";
            Pattern p = Pattern.compile(format);
            if (p.matcher(str).find()) {
                throw new R200Exception(message);
            }
        }
    }
}
