package com.geccocrawler.gecco.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author ztcaoll222
 * Create time: 2019/1/25 9:55
 */
public class GlobalConfig {
    private GlobalConfig() {
    }

    public static final int DEFAULT_COLLECTION_SIZE = 64;

    public static final String DEFAULT_STARTS_JSON = "starts.json";

    /**
     * 默认的下载器
     */
    public static final String DEFAULT_DOWNLOADER = "httpClientDownloader";

    /**
     * 默认的字符编码
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 包名
     */
    public static final String PACKAGE_NAME = "com.geccocrawler.gecco";
}
