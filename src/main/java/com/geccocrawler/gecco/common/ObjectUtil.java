package com.geccocrawler.gecco.common;

import lombok.extern.apachecommons.CommonsLog;

import java.util.Arrays;

/**
 * @author ztcaoll222
 * Create time: 2019/2/20 14:19
 */
@CommonsLog
public class ObjectUtil {
    /**
     * 判空
     */
    public static boolean checkObj(Object obj) {
        return obj != null && !"".equals(obj.toString().trim());
    }

    /**
     * 对多个 obj 判空
     */
    public static boolean checkObj(Object... objs) {
        return Arrays.stream(objs).allMatch(ObjectUtil::checkObj);
    }
}
