package com.geccocrawler.gecco.scheduler;

import com.geccocrawler.gecco.request.HttpRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.NavigableSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 保证队列内容唯一，剔除重复抓取
 *
 * @author huchengyi
 */
public class UniqueSpiderScheduler implements Scheduler {

    private static Log log = LogFactory.getLog(UniqueSpiderScheduler.class);

    private NavigableSet<SortHttpRequest> set;

    public UniqueSpiderScheduler() {
        set = new ConcurrentSkipListSet<>((o1, o2) -> {
            if (o1.getHttpRequest().hashCode() == o2.getHttpRequest().hashCode()) {
                if (o1.getHttpRequest().equals(o2.getHttpRequest())) {
                    return 0;
                }
            }
            return (o1.getPriority() - o2.getPriority()) > 0 ? 1 : -1;
        });
    }

    @Override
    public HttpRequest out() {
        SortHttpRequest sortHttpRequest = set.pollFirst();
        if (sortHttpRequest == null) {
            return null;
        }
        long priority = sortHttpRequest.getPriority();
        HttpRequest request = sortHttpRequest.getHttpRequest();
        if (request != null && log.isDebugEnabled()) {
            log.debug("OUT(" + priority + "):" + request.getUrl() + "(Referer:" + request.getHeaders().get("Referer") + ")");
        }
        return request;
    }

    @Override
    public void into(HttpRequest request) {
        long priority = System.nanoTime();
        boolean success = set.add(new SortHttpRequest(priority, request));
        if (success && log.isDebugEnabled()) {
            log.debug("INTO(" + priority + "):" + request.getUrl() + "(Referer:" + request.getHeaders().get("Referer") + ")");
        }
        if (!success && log.isDebugEnabled()) {
            log.error("not unique request : " + request.getUrl());
        }
    }

    private class SortHttpRequest {

        private long priority;

        private HttpRequest httpRequest;

        public SortHttpRequest(long priority, HttpRequest httpRequest) {
            super();
            this.priority = priority;
            this.httpRequest = httpRequest;
        }

        public long getPriority() {
            return priority;
        }

        public HttpRequest getHttpRequest() {
            return httpRequest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SortHttpRequest)) {
                return false;
            }
            SortHttpRequest that = (SortHttpRequest) o;
            return priority == that.priority &&
                    Objects.equals(httpRequest, that.httpRequest);
        }

        @Override
        public int hashCode() {
            return Objects.hash(priority, httpRequest);
        }
    }

}
