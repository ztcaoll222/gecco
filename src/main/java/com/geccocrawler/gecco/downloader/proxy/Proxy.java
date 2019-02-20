package com.geccocrawler.gecco.downloader.proxy;

import lombok.Data;
import org.apache.http.HttpHost;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class Proxy {
    private static final long serialVersionUID = -5943929366628713038L;

    private HttpHost httpHost;

    private AtomicLong successCount;

    private AtomicLong failureCount;

    private String src;//来源

    public Proxy(String host, int port) {
        this.httpHost = new HttpHost(host, port);
        this.src = "custom";
        this.successCount = new AtomicLong(0);
        this.failureCount = new AtomicLong(0);
    }

    public String getIP() {
        return this.getHttpHost().getHostName();
    }

    public int getPort() {
        return this.getHttpHost().getPort();
    }

    public String toHostString() {
        return httpHost.toHostString();
    }
}
