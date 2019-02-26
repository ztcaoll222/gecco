package com.geccocrawler.gecco.spider;

import com.geccocrawler.gecco.GeccoEngine;

/**
 * @author ztcaoll222
 * Create time: 2019/2/26 17:02
 */
public interface SpiderFactoryBuilder {
    AbstractSpiderFactory build(int threadCount, GeccoEngine engine);
}
