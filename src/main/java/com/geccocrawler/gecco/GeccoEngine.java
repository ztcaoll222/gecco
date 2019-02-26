package com.geccocrawler.gecco;

import com.alibaba.fastjson.JSON;
import com.geccocrawler.gecco.common.GlobalThreadFactory;
import com.geccocrawler.gecco.config.GlobalConfig;
import com.geccocrawler.gecco.downloader.DownloaderAOPFactoryBuilder;
import com.geccocrawler.gecco.downloader.DownloaderFactoryBuilder;
import com.geccocrawler.gecco.downloader.proxy.FileProxys;
import com.geccocrawler.gecco.downloader.proxy.Proxys;
import com.geccocrawler.gecco.dynamic.DynamicGecco;
import com.geccocrawler.gecco.dynamic.GeccoClassLoader;
import com.geccocrawler.gecco.listener.EventListener;
import com.geccocrawler.gecco.monitor.GeccoJmx;
import com.geccocrawler.gecco.monitor.GeccoMonitor;
import com.geccocrawler.gecco.pipeline.PipelineFactory;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.request.StartRequestList;
import com.geccocrawler.gecco.scheduler.NoLoopStartScheduler;
import com.geccocrawler.gecco.scheduler.Scheduler;
import com.geccocrawler.gecco.scheduler.StartScheduler;
import com.geccocrawler.gecco.spider.Spider;
import com.geccocrawler.gecco.spider.SpiderBeanFactory;
import com.geccocrawler.gecco.spider.render.CustomFieldRenderFactory;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * 爬虫引擎，每个爬虫引擎最好独立进程，在分布式爬虫场景下，可以单独分配一台爬虫服务器。引擎包括Scheduler、Downloader、Spider、 SpiderBeanFactory4个主要模块
 *
 * @author huchengyi
 */
@CommonsLog
public class GeccoEngine<V> extends Thread implements Callable<V> {
    private Date startTime;

    private List<HttpRequest> startRequests = new ArrayList<HttpRequest>();

    private Scheduler scheduler;

    private SpiderBeanFactory spiderBeanFactory;

    private PipelineFactory pipelineFactory;

    private DownloaderFactoryBuilder downloaderFactoryBuilder;

    private CustomFieldRenderFactory customFieldRenderFactory;

    private List<Spider> spiders;

    private String classpath;

    private int threadCount;

    private CountDownLatch cdl;

    private int interval;

    private Proxys proxysLoader;

    private boolean proxy = true;

    private boolean loop;

    private boolean mobile;

    private boolean debug;

    private boolean withStartsJson = true;

    private String startsJson = GlobalConfig.DEFAULT_STARTS_JSON;

    private boolean monitor = true;

    private int retry;

    private EventListener eventListener;

    private String jmxPrefix;

    private V ret;//callable 返回值

    private DownloaderAOPFactoryBuilder downloaderAOPFactoryBuilder;

    public V getRet() {
        return ret;
    }

    public void setRet(V ret) {
        this.ret = ret;
    }

    private GeccoEngine() {
        this.retry = 3;
    }

    /**
     * 动态配置规则不能使用该方法构造GeccoEngine
     *
     * @return GeccoEngine
     */
    public static GeccoEngine create() {
        GeccoEngine geccoEngine = new GeccoEngine();
        geccoEngine.setName("GeccoEngine");
        return geccoEngine;
    }

    public static GeccoEngine create(String classpath) {
        return create(classpath, null, null, null, null);
    }

    public static GeccoEngine create(String classpath, PipelineFactory pipelineFactory, CustomFieldRenderFactory customFieldRenderFactory, DownloaderFactoryBuilder downloaderFactoryBuilder, DownloaderAOPFactoryBuilder downloaderAOPFactoryBuilder) {
        if (StringUtils.isEmpty(classpath)) {
            // classpath不为空
            throw new IllegalArgumentException("classpath cannot be empty");
        }
        GeccoEngine ge = create();
        ge.spiderBeanFactory = new SpiderBeanFactory(classpath, pipelineFactory, customFieldRenderFactory, downloaderFactoryBuilder, downloaderAOPFactoryBuilder);
        return ge;
    }

    public GeccoEngine start(String url) {
        return start(new HttpGetRequest(url));
    }

    public GeccoEngine start(String... urls) {
        for (String url : urls) {
            start(url);
        }
        return this;
    }

    public GeccoEngine get(String url) {
        return start(url);
    }

    public GeccoEngine get(String... urls) {
        return start(urls);
    }

    public GeccoEngine post(String url) {
        return start(new HttpPostRequest(url));
    }

    public GeccoEngine post(String url, Map<String, String> params) {
        return start(new HttpPostRequest(url, params));
    }

    public GeccoEngine start(HttpRequest request) {
        this.startRequests.add(request);
        return this;
    }

    public GeccoEngine start(List<HttpRequest> requests) {
        for (HttpRequest request : requests) {
            start(request);
        }
        return this;
    }

    public GeccoEngine scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public GeccoEngine thread(int count) {
        this.threadCount = count;
        return this;
    }

    public GeccoEngine interval(int interval) {
        this.interval = interval;
        return this;
    }

    public GeccoEngine retry(int retry) {
        this.retry = retry;
        return this;
    }

    public GeccoEngine loop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public GeccoEngine proxysLoader(Proxys proxysLoader) {
        this.proxysLoader = proxysLoader;
        return this;
    }

    public GeccoEngine proxy(boolean proxy) {
        this.proxy = proxy;
        return this;
    }

    public GeccoEngine mobile(boolean mobile) {
        this.mobile = mobile;
        return this;
    }

    public GeccoEngine debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public GeccoEngine monitor(boolean monitor) {
        this.monitor = monitor;
        return this;
    }

    public GeccoEngine classpath(String classpath) {
        this.classpath = classpath;
        return this;
    }

    public GeccoEngine jmxPrefix(String jmxPrefix) {
        this.jmxPrefix = jmxPrefix;
        return this;
    }

    public GeccoEngine pipelineFactory(PipelineFactory pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
        return this;
    }

    public GeccoEngine downloaderFactoryBuilder(DownloaderFactoryBuilder downloaderFactoryBuilder) {
        this.downloaderFactoryBuilder = downloaderFactoryBuilder;
        return this;
    }

    public GeccoEngine customFieldRenderFactory(CustomFieldRenderFactory customFieldRenderFactory) {
        this.customFieldRenderFactory = customFieldRenderFactory;
        return this;
    }

    public GeccoEngine spiderBeanFactory(SpiderBeanFactory spiderBeanFactory) {
        this.spiderBeanFactory = spiderBeanFactory;
        return this;
    }

    public boolean isWithStartsJson() {
        return withStartsJson;
    }

    public GeccoEngine withStartsJson(boolean withStartsJson) {
        this.withStartsJson = withStartsJson;
        return this;
    }

    public String getStartsJson() {
        return startsJson;
    }

    public GeccoEngine startsJson(String startsJson) {
        this.startsJson = startsJson;
        return this;
    }

    public void register(Class<?> spiderBeanClass) {
        getSpiderBeanFactory().addSpiderBean(spiderBeanClass);
    }

    public void unregister(Class<?> spiderBeanClass) {
        getSpiderBeanFactory().removeSpiderBean(spiderBeanClass);
        DynamicGecco.unregister(spiderBeanClass);
    }

    public DownloaderAOPFactoryBuilder getDownloaderAOPFactoryBuilder() {
        return downloaderAOPFactoryBuilder;
    }

    public GeccoEngine downloaderAOPFactoryBuilder(DownloaderAOPFactoryBuilder downloaderAOPFactoryBuilder) {
        this.downloaderAOPFactoryBuilder = downloaderAOPFactoryBuilder;
        return this;
    }

    @Override
    public void run() {
        if (debug) {
            Logger log = LogManager.getLogger("com.geccocrawler.gecco.spider.render");
            log.setLevel(Level.DEBUG);
        }
        if (proxysLoader == null) {
            //默认采用proxys文件代理集合
            proxysLoader = new FileProxys();
        }
        if (scheduler == null) {
            if (loop) {
                scheduler = new StartScheduler();
            } else {
                scheduler = new NoLoopStartScheduler();
            }
        }
        if (spiderBeanFactory == null) {
            if (StringUtils.isEmpty(classpath)) {
                // classpath不为空
                throw new IllegalArgumentException("classpath cannot be empty");
            }
            spiderBeanFactory = new SpiderBeanFactory(classpath, pipelineFactory, customFieldRenderFactory, downloaderFactoryBuilder, downloaderAOPFactoryBuilder);
        }
        if (threadCount <= 0) {
            threadCount = 1;
        }
        this.cdl = new CountDownLatch(threadCount);
        if (withStartsJson) {
            initStartsJson(startsJson);
        }
        if (startRequests.isEmpty()) {
            // startRequests不为空
            // throw new IllegalArgumentException("startRequests cannot be empty");
        }
        for (HttpRequest startRequest : startRequests) {
            scheduler.into(startRequest);
        }
        spiders = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Spider spider = new Spider(this);
            spiders.add(spider);
            GlobalThreadFactory.INSTANCE.execute(spider);
        }
        startTime = new Date();
        if (monitor) {
            // 监控爬虫基本信息
            GeccoMonitor.monitor(this);
            // 启动导出jmx信息
            GeccoJmx.export(jmxPrefix == null ? classpath : jmxPrefix);
        }
        // 非循环模式等待线程执行完毕后关闭
        closeUnitlComplete();
    }

    @Override
    public synchronized void start() {
        if (eventListener != null) {
            eventListener.onStart(this);
        }
        super.start();
    }

    private GeccoEngine initStartsJson(String filename) {
        try {
            URL url = Resources.getResource(filename);
            File file = new File(url.getPath());
            if (file.exists()) {
                String json = Files.asCharSource(file, StandardCharsets.UTF_8).read();
                List<StartRequestList> list = JSON.parseArray(json, StartRequestList.class);
                for (StartRequestList start : list) {
                    start(start.toRequest());
                }
            }
        } catch (IllegalArgumentException ex) {
            log.info("starts.json not found");
        } catch (IOException ioex) {
            log.error(ioex);
        }
        return this;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public SpiderBeanFactory getSpiderBeanFactory() {
        return spiderBeanFactory;
    }

    public int getInterval() {
        return interval;
    }

    public Date getStartTime() {
        return startTime;
    }

    public List<HttpRequest> getStartRequests() {
        return startRequests;
    }

    public List<Spider> getSpiders() {
        return spiders;
    }

    public int getRetry() {
        return retry;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public boolean isLoop() {
        return loop;
    }

    public Proxys getProxysLoader() {
        return proxysLoader;
    }

    public boolean isMobile() {
        return mobile;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isProxy() {
        return proxy;
    }

    public boolean isMonitor() {
        return monitor;
    }

    /**
     * spider线程告知engine执行结束
     */
    public void notifyComplete() {
        this.cdl.countDown();
    }

    /**
     * 非循环模式等待线程执行完毕后关闭
     */
    public void closeUnitlComplete() {
        if (!loop) {
            try {
                cdl.await();
            } catch (InterruptedException e) {
                log.error(e);
            }
            if (spiderBeanFactory != null) {
                spiderBeanFactory.getDownloaderFactory().closeAll();
            }
            GeccoJmx.unexport();
            log.info("close gecco!");
        }

        if (eventListener != null) {
            eventListener.onStop(this);
        }
    }

    /**
     * 启动引擎，并返回GeccoEngine对象
     *
     * @return GeccoEngine
     */
    public GeccoEngine engineStart() {
        start();
        return this;
    }

    /**
     * 暂停
     */
    public void pause() {
        if (spiders != null) {
            for (Spider spider : spiders) {
                spider.pause();
            }
        }
        if (eventListener != null) {
            eventListener.onPause(this);
        }
    }

    /**
     * 重新开始抓取
     */
    public void restart() {
        if (spiders != null) {
            for (Spider spider : spiders) {
                spider.restart();
            }
        }
        if (eventListener != null) {
            eventListener.onRestart(this);
        }
    }

    public void beginUpdateRule() {
        if (log.isDebugEnabled()) {
            log.debug("begin update rule");
        }
        // 修改规则前需要暂停引擎并且重新创建ClassLoader
        pause();
        GeccoClassLoader.create();
    }

    public void endUpdateRule() {
        // 修改完成后重启引擎
        restart();
        if (log.isDebugEnabled()) {
            log.debug("end update rule");
        }
    }

    public void engineStop() {
        if (spiders != null) {
            for (Spider spider : spiders) {
                spider.stop();
            }
        }
        if (eventListener != null) {
            eventListener.onStop(this);
        }
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public GeccoEngine setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
        return this;
    }


    @Override
    public V call() throws Exception {
        run();
        return ret;
    }
}
