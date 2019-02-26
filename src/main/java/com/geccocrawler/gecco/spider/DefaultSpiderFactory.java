package com.geccocrawler.gecco.spider;

import com.geccocrawler.gecco.GeccoEngine;

/**
 * @author ztcaoll222
 * Create time: 2019/2/26 16:37
 */
public class DefaultSpiderFactory extends AbstractSpiderFactory {
    public DefaultSpiderFactory(int threadCount, GeccoEngine engine) {
        super(threadCount, engine);
    }

    @Override
    protected Spider createSpider(GeccoEngine engine) {
        return new Spider(engine);
    }
}
