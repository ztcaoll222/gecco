package com.geccocrawler.gecco.interfaces;

/**
 * @author ztcaoll222
 * Create time: 2019/3/4 9:04
 */
public interface Factory<T> {
    T get(String name);
}
