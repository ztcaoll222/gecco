package com.geccocrawler.gecco.downloader;

import org.reflections.Reflections;

/**
 * 下载器工厂类生成器
 *
 * @author ztcaoll222
 * Create time: 2019/1/30 14:25
 */
public interface DownloaderFactoryBuilder {
    DownloaderFactory builder(Reflections reflections);
}
