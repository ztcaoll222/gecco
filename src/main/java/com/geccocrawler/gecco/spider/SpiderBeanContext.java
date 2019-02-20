package com.geccocrawler.gecco.spider;

import java.io.Serializable;
import java.util.List;

import com.geccocrawler.gecco.downloader.AfterDownload;
import com.geccocrawler.gecco.downloader.BeforeDownload;
import com.geccocrawler.gecco.downloader.Downloader;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.spider.render.Render;
import lombok.Data;

/**
 * 渲染bean的上下文对象。 包括下载前的自定义操作beforeDownload,下载后的自定义操作afterDownload。 使用的哪种渲染器渲染bean，目前支持html、json、xml。
 * 渲染完成后通过管道过滤器进行bean的进步一部清洗和整理。
 * 
 * @author huchengyi
 *
 */
@Data
public class SpiderBeanContext implements Serializable {
	private static final long serialVersionUID = -3223473055910432077L;

	private Render render;

	private Downloader downloader;

	private int timeout;

	private BeforeDownload beforeDownload;

	private AfterDownload afterDownload;

	@SuppressWarnings({ "rawtypes" })
	private List<Pipeline> pipelines;
}
