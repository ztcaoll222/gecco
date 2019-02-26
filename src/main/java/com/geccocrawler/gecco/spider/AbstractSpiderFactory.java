package com.geccocrawler.gecco.spider;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.utils.RangeUtil;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ztcaoll222
 * Create time: 2019/2/26 16:26
 */
@Getter
@CommonsLog
public abstract class AbstractSpiderFactory {
    private List<Spider> spiders;

    public AbstractSpiderFactory(int threadCount, GeccoEngine engine) {
        spiders = new ArrayList<>(threadCount);
        RangeUtil.rangeInt(0, threadCount).forEach(it -> spiders.add(createSpider(engine)));
    }

    protected abstract Spider createSpider(GeccoEngine engine);
}
