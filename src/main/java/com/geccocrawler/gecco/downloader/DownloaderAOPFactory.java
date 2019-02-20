package com.geccocrawler.gecco.downloader;

/**
 * @author ztcaoll222
 * Create time: 2019/2/20 9:41
 */
public interface DownloaderAOPFactory {
    BeforeDownload getBefore(String beforeDownloadName, String spiderName);

    AfterDownload getAfter(String afterDownloadName, String spiderName);
}
