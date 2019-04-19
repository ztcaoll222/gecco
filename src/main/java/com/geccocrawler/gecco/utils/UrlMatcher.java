package com.geccocrawler.gecco.utils;

import com.geccocrawler.gecco.config.GlobalConfig;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommonsLog
public class UrlMatcher {
    public static String replaceParams(String regex, String name, String value) {
        Map<String, String> map = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
        map.put(name, value);
        return replaceParams(regex, map);
    }

    public static String replaceParams(String srcUrl, Map<String, String> params) {
        return replaceRegexs(srcUrl, "\\{(.*?)\\}", params);
    }

    public static String replaceFields(String regex, String name, String value) {
        Map<String, String> map = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
        map.put(name, value);
        return replaceFields(regex, map);
    }

    public static String replaceFields(String srcUrl, Map<String, String> params) {
        return replaceRegexs(srcUrl, "\\[(.*?)\\]", params);
    }

    public static String replaceRegexs(String srcUrl, String regex, Map<String, String> params) {
        if (params == null) {
            return srcUrl;
        }
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(srcUrl);
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = params.get(name);
            if (StringUtils.isNotEmpty(value)) {
                matcher.appendReplacement(sb, value);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static Map<String, String> match(String url, String regex) {
        String regexSrc = StringUtils.replace(regex, "?", "\\?");
        String regex1 = "\\{(.*?)\\}";
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile(regex1);
        Matcher matcher = pattern.matcher(regexSrc);
        List<String> names = new ArrayList<>();
        //每一个匹配都是键值对key:value或者只有key
        while (matcher.find()) {
            String name = matcher.group(1);
            //使用:分割,只能分成两个组
            String[] splits = name.split("\\s*:\\s*");
            names.add(splits[0]);
            //如果有自定义的正则表达式规则，使用自定义的正则表达式规则。类似Jersey的@Path语法
            String regex2 = "([^/]*)";
            if (splits.length > 1) {
                regex2 = "(" + splits[1] + ")";
            }
            matcher.appendReplacement(sb, regex2);
        }
        if (names.size() > 0) {
            matcher.appendTail(sb);
            String regex2 = sb.toString();
            if (log.isDebugEnabled()) {
                log.debug(regex2);
            }
            regex2 = "^" + regex2;
            Pattern pattern2 = Pattern.compile(regex2);
            Matcher matcher2 = pattern2.matcher(url);
            if (matcher2.matches()) {
                Map<String, String> params = new HashMap<>(names.size());
                for (int i = 1; i <= matcher2.groupCount(); i++) {
                    String value = matcher2.group(i);
                    try {
                        value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        if (log.isDebugEnabled()) {
                            log.error(e.getMessage(), e);
                        } else {
                            log.error(e.getMessage());
                        }
                    }
                    params.put(names.get(i - 1), value);
                }
                return params;
            }
        } else {
            //如果没有变量，返回空map
            if (url.equals(regex)) {
                return Collections.emptyMap();
            }
        }
        //适配失败返回null
        return null;
    }

    public static void main(String[] args) {
        //http://temai.tuniu.com/{catalog}/{srcId}
        //http://temai.tuniu.com/weihuo/{catalog}/s4-p{currPage}/
        //http://temai.tuniu.com/weihuo/tours/s4-p1/
        //http://temai.tuniu.com/tours/212055673
        //String regex = "http://temai.tuniu.com/weihuo/{catalog}/s4-p{currPage}/";
        //String url = "http://temai.tuniu.com/weihuo/tours/s4-p1/";
        //String regex = "http://temai.tuniu.com/{catalog}/{srcId}";
        //String url = "http://temai.tuniu.com/tours/212055673";
        //System.out.println(match(url, regex));
        String url = "http://www.ly.com/HotelInfo-597101.html#id_nameAndSliderInfo&is=1&searchId=undefined&ab=0";
        String regex = "http://www.ly.com/HotelInfo-{code}.html#{hash}";
        String url1 = "http://xxx.com/index.html";
        String url2 = "http://xxx.com/123456.html";
        String regex2 = "http://xxx.com/{code:[0-9]+}.html";
        String regex3 = "http://xxx.com/{code}.html";
        System.out.println(match(url2, regex2));
        System.out.println(match(url2, regex3));
    }
}
