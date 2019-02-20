package com.geccocrawler.gecco.scheduler;

import com.geccocrawler.gecco.request.HttpRequest;
import lombok.extern.apachecommons.CommonsLog;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程安全的非阻塞FIFO队列
 *
 * @author huchengyi
 */
@Deprecated
@CommonsLog
public class SpiderScheduler implements Scheduler {
    private ConcurrentLinkedQueue<HttpRequest> queue;

    public SpiderScheduler() {
        queue = new ConcurrentLinkedQueue<HttpRequest>();
    }

    @Override
    public HttpRequest out() {
        HttpRequest request = queue.poll();
        if (request != null) {
            if (log.isDebugEnabled()) {
                log.debug("OUT:" + request.getUrl() + "(Referer:" + request.getHeaders().get("Referer") + ")");
            }
        }
        return request;
    }

    @Override
    public void into(HttpRequest request) {
        queue.offer(request);
        if (log.isDebugEnabled()) {
            log.debug("INTO:" + request.getUrl() + "(Referer:" + request.getHeaders().get("Referer") + ")");
        }
    }
}
