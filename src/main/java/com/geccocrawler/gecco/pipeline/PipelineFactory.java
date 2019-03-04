package com.geccocrawler.gecco.pipeline;

import com.geccocrawler.gecco.interfaces.Factory;
import com.geccocrawler.gecco.spider.SpiderBean;

public interface PipelineFactory extends Factory<Pipeline<? extends SpiderBean>> {
}
