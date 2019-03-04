package com.geccocrawler.gecco.request;

import com.alibaba.fastjson.JSON;
import com.geccocrawler.gecco.config.GlobalConfig;
import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@CommonsLog
@Data
public abstract class AbstractHttpRequest implements HttpRequest, Comparable<HttpRequest>, Serializable {

    private static final long serialVersionUID = -7284636094595149962L;

    private String url;

    private boolean forceUseCharset = false;

    private String charset;

    private final Map<String, String> parameters;

    private final Map<String, String> cookies;

    private final Map<String, String> headers;

    private long priority;

    public AbstractHttpRequest() {
        this.parameters = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
        this.headers = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
        this.cookies = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
    }

    public AbstractHttpRequest(String url) {
        this();
        this.setUrl(url);
    }

    @Override
    public void clearHeader() {
        Iterator<Map.Entry<String, String>> it = this.headers.entrySet().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    @Override
    public void clearCookie() {
        Iterator<Map.Entry<String, String>> it = this.cookies.entrySet().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    @Override
    public void clearParams() {
        Iterator<Map.Entry<String, String>> it = this.parameters.entrySet().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    @Override
    public void addCookie(String name, String value) {
        cookies.put(name, value);
    }

    @Override
    public String getCookie(String name) {
        return cookies.get(name);
    }

    @Override
    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public HttpRequest subRequest(String url) {
        try {
            HttpRequest request = (HttpRequest) clone();
            request.setUrl(url);
            request.refer(this.getUrl());
            return request;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.error(ex.getMessage(), ex);
            } else {
                log.error(ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void refer(String refer) {
        this.addHeader("Referer", refer);
    }

    @Override
    public boolean isForceUseCharset() {
        return forceUseCharset;
    }

    @Override
    public void setCookies(Map<String, String> cookies) {
        this.cookies.putAll(cookies);
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    public void setParameters(Map<String, String> parameters) {
        this.parameters.putAll(parameters);
    }

    @Override
    public void setUrl(String url) {
        this.url = StringUtils.substringBefore(url, "#");
    }

    /**
     * 数字小，优先级高
     */
    @Override
    public int compareTo(HttpRequest o) {
        return Long.compare(this.priority, o.getPriority());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        //通过json的序列号和反序列化实现对象的深度clone
        //序列化
        String text = JSON.toJSONString(this);
        //反序列化
        return JSON.parseObject(text, this.getClass());
    }
}
