package com.geccocrawler.gecco.downloader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.geccocrawler.gecco.config.GlobalConfig;
import org.reflections.Reflections;

import com.geccocrawler.gecco.annotation.GeccoClass;

public class DefaultDownloaderAOPFactory implements DownloaderAOPFactory {
	private Map<String, BeforeDownload> beforeDownloads;
	
	private Map<String, AfterDownload> afterDownloads;
	
	public DefaultDownloaderAOPFactory(Reflections reflections) {
		this.beforeDownloads = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
		this.afterDownloads = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(GeccoClass.class);
		for(Class<?> aopClass : classes) {
			GeccoClass geccoClass = aopClass.getAnnotation(GeccoClass.class);
			try {
				Class<?>[] geccoClasses = geccoClass.value();
				for(Class<?> c : geccoClasses) {
					String name = c.getName();
					Object o = aopClass.newInstance();
					if(o instanceof BeforeDownload) {
						beforeDownloads.put(name, (BeforeDownload)o);
					} else if(o instanceof AfterDownload) {
						afterDownloads.put(name, (AfterDownload)o);
					}
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public BeforeDownload getBefore(String spiderName) {
		return beforeDownloads.get(spiderName);
	}

	@Override
	public AfterDownload getAfter(String spiderName) {
		return afterDownloads.get(spiderName);
	}

}
