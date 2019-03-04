package com.geccocrawler.gecco.interfaces;

import org.reflections.Reflections;

/**
 * @author ztcaoll222
 * Create time: 2019/3/4 9:14
 */
public interface FactoryBuilder<T> {
    T build(Reflections reflections);
}
