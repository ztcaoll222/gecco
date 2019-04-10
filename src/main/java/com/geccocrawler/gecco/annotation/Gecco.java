package com.geccocrawler.gecco.annotation;

import com.geccocrawler.gecco.config.GlobalConfig;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Gecco {

	/**
	 * 摒弃正则表达式的匹配方式，采用更容易理解的{value}方式
	 * 如：https://github.com/{user}/{project}
	 * 
	 * @return url匹配规则
	 */
	String[] matchUrl() default "*";
	
	/**
	 * url下载器，默认为httpClientDownloader
	 * 
	 * @return 下载器
	 */
	String downloader() default "";
	
	/**
	 * 下载超时时间
	 * 
	 * @return 下载超时时间
	 */
	int timeout() default GlobalConfig.DEFAULT_TIMEOUT;
	
	/**
	 * bean渲染完成后，后续的管道过滤器
	 * 
	 * @return 管道过滤器
	 */
	String[] pipelines() default "";

	/**
	 * 预处理
	 */
	String beforeDownload() default "";

	/**
	 * 后处理
	 */
	String afterDownload() default "";
}
