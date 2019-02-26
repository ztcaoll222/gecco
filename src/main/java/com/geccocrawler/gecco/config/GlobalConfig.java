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

    /**
     * pc端的默认ua
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36";

    /**
     * 手机端的默认ua
     */
    public static final String DEFAULT_MOBILE_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A403 Safari/8536.25";

    /**
     * 默认的线程数量
     */
    public static final int DEFAULT_THREAD_COUNT = 1;
}
