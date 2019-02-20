package com.geccocrawler.gecco.downloader;

import org.reflections.Reflections;

/**
 * @author ztcaoll222
 * Create time: 2019/2/20 14:31
 */
public interface DownloaderAOPFactoryBuilder {
    DownloaderAOPFactory build(Reflections reflections);
}
