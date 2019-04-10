package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.config.GlobalConfig;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @author ztcaoll222
 * Create time: 2019/4/10 10:24
 */
public class DefaultUserAgent implements UserAgent {
    private List<String> userAgents = null;

    private List<String> mobileUserAgents = null;

    public DefaultUserAgent() {
        try {
            URL url = Resources.getResource("userAgents");
            File file = new File(url.getPath());
            userAgents = Files.readLines(file, GlobalConfig.DEFAULT_CHARSET);
        } catch (Exception ignore) {
        }

        try {
            URL url = Resources.getResource("mobileUserAgents");
            File file = new File(url.getPath());
            mobileUserAgents = Files.readLines(file, GlobalConfig.DEFAULT_CHARSET);
        } catch (Exception ignore) {
        }
    }

    @Override
    public String getUserAgent(boolean isMobile) {
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
