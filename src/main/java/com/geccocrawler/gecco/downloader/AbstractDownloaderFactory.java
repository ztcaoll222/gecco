package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.config.GlobalConfig;
import lombok.extern.apachecommons.CommonsLog;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

/**
 * 下载器工厂类
 *
 * @author huchengyi
 */
@CommonsLog
public abstract class AbstractDownloaderFactory {
    private Map<String, Downloader> downloaders;

    public AbstractDownloaderFactory(Reflections reflections) {
        this.downloaders = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);

        reflections.getTypesAnnotatedWith(com.geccocrawler.gecco.annotation.Downloader.class).forEach(downloaderClass -> {
            com.geccocrawler.gecco.annotation.Downloader downloader = downloaderClass.getAnnotation(com.geccocrawler.gecco.annotation.Downloader.class);
            try {
                Object o = createDownloader(downloaderClass);
                if (o instanceof Downloader) {
                    Downloader downloaderInstance = (Downloader) o;
                    String name = downloader.value();
                    downloaders.put(name, downloaderInstance);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    public Downloader getDownloader(String name) {
        Downloader downloader = downloaders.get(name);
        if (downloader == null) {
            return defaultDownloader();
        }
        return downloader;
    }

    public Downloader defaultDownloader() {
        return downloaders.get(GlobalConfig.DEFAULT_DOWNLOADER);
    }

    protected abstract Object createDownloader(Class<?> downloaderClass) throws Exception;

    public void closeAll() {
        downloaders.values().forEach(Downloader::shutdown);
    }
}
