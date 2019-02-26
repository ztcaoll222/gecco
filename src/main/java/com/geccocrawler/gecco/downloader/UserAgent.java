package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.config.GlobalConfig;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * 随机获取userAgent，通过在classpath根目录下放置userAgents文件，配置多个userAgent，随机选择，如果希望某个ua概率较高请配置多个
 *
 * @author huchengyi
 */
public class UserAgent {
    private static List<String> userAgents = null;

    static {
        try {
            URL url = Resources.getResource("userAgents");
            File file = new File(url.getPath());
            userAgents = Files.readLines(file, GlobalConfig.DEFAULT_CHARSET);
        } catch (Exception ex) {
        }
    }

    private static List<String> mobileUserAgents = null;

    static {
        try {
            URL url = Resources.getResource("mobileUserAgents");
            File file = new File(url.getPath());
            mobileUserAgents = Files.readLines(file, GlobalConfig.DEFAULT_CHARSET);
        } catch (Exception ex) {
        }
    }

    public static String getUserAgent(boolean isMobile) {
        if (isMobile) {
            if (mobileUserAgents == null || mobileUserAgents.size() == 0) {
                return GlobalConfig.DEFAULT_MOBILE_USER_AGENT;
            }
            Collections.shuffle(mobileUserAgents);
            return mobileUserAgents.get(0);
        } else {
            if (userAgents == null || userAgents.size() == 0) {
                return GlobalConfig.DEFAULT_USER_AGENT;
            }
            Collections.shuffle(userAgents);
            return userAgents.get(0);
        }
    }

}
