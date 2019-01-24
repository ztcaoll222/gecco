package com.geccocrawler.gecco.spider.render;

import org.reflections.Reflections;

import com.geccocrawler.gecco.spider.render.html.HtmlRender;
import com.geccocrawler.gecco.spider.render.json.JsonRender;

public class DefaultRenderFactory extends RenderFactory {
	
	public DefaultRenderFactory(Reflections reflections, CustomFieldRenderFactory customFieldRenderFactory) {
		super(reflections, customFieldRenderFactory);
	}

	@Override
	public HtmlRender createHtmlRender() {
		return new HtmlRender();
	}

	@Override
	public JsonRender createJsonRender() {
		return new JsonRender();
	}
	
}
