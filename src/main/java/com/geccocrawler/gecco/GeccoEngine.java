package com.geccocrawler.gecco;

import com.alibaba.fastjson.JSON;
import com.geccocrawler.gecco.common.GlobalThreadFactory;
import com.geccocrawler.gecco.config.GlobalConfig;
import com.geccocrawler.gecco.downloader.DefaultUserAgent;
import com.geccocrawler.gecco.downloader.DownloaderAOPFactoryBuilder;
import com.geccocrawler.gecco.downloader.DownloaderFactoryBuilder;
import com.geccocrawler.gecco.downloader.UserAgent;
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
import com.geccocrawler.gecco.spider.*;
import com.geccocrawler.gecco.spider.render.CustomFieldRenderFactory;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * 爬虫引擎，每个爬虫引擎最好独立进程，在分布式爬虫场景下，可以单独分配一台爬虫服务器。引擎包括Scheduler、Downloader、Spider、 SpiderBeanFactory4个主要模块
 *
 * @author huchengyi
 */
@CommonsLog
@Getter
public class GeccoEngine<V> extends Thread implements Callable<V> {
    //----------------------------- 定义变量 start ---------------------------

    private Date startTime;

    private List<HttpRequest> startRequests = new ArrayList<>();

    private Scheduler scheduler;

    private SpiderBeanFactory spiderBeanFactory;

    private PipelineFactory pipelineFactory;

    private DownloaderFactoryBuilder downloaderFactoryBuilder;

    private CustomFieldRenderFactory customFieldRenderFactory;

    private AbstractSpiderFactory spiderFactory;

    private String classpath;

    private int threadCount = GlobalConfig.DEFAULT_THREAD_COUNT;

    private CountDownLatch cdl;

    private int interval;

    private Proxys proxysLoader;

    private boolean proxy = true;

    private boolean loop;

    private boolean mobile = false;

    private boolean debug = false;

    private boolean withStartsJson = true;

    private String startsJson = GlobalConfig.DEFAULT_STARTS_JSON;

    private boolean monitor = true;

    private int retry;

    private EventListener eventListener;

    private String jmxPrefix;

    /**
     * callable 返回值
     */
    private V ret;

    private DownloaderAOPFactoryBuilder downloaderAOPFactoryBuilder;

    private SpiderFactoryBuilder spiderFactoryBuilder;

    private UserAgent userAgent = new DefaultUserAgent();

    //----------------------------- 定义变量 end ---------------------------

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

        spiderFactory = spiderFactoryBuilder == null ?
                new DefaultSpiderFactory(threadCount, this) :
                spiderFactoryBuilder.build(threadCount, this);
        spiderFactory.getSpiders().forEach(GlobalThreadFactory.INSTANCE::execute);

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
            if (monitor) {
                GeccoJmx.unexport();
            }
            log.info("close gecco!");
        }

        if (eventListener != null) {
            eventListener.onStop(this);
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (spiderFactory != null) {
            spiderFactory.getSpiders().forEach(Spider::pause);
        }
        if (eventListener != null) {
            eventListener.onPause(this);
        }
    }

    /**
     * 重新开始抓取
     */
    public void restart() {
        if (spiderFactory != null) {
            spiderFactory.getSpiders().forEach(Spider::restart);
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
        if (spiderFactory != null) {
            spiderFactory.getSpiders().forEach(Spider::stop);
        }
        if (eventListener != null) {
            eventListener.onStop(this);
        }
    }

    @Override
    public V call() throws Exception {
        run();
        return ret;
    }

    // ------------------------- setter start -------------------------------

    private GeccoEngine<V> initStartsJson(String filename) {
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

    /**
     * 启动引擎，并返回GeccoEngine对象
     *
     * @return GeccoEngine
     */
    public GeccoEngine<V> engineStart() {
        start();
        return this;
    }

    public GeccoEngine<V> start(String url) {
        return start(new HttpGetRequest(url));
    }

    public GeccoEngine<V> start(String... urls) {
        Arrays.stream(urls).forEach(this::start);
        return this;
    }

    public GeccoEngine<V> get(String url) {
        return start(url);
    }

    public GeccoEngine<V> get(String... urls) {
        return start(urls);
    }

    public GeccoEngine<V> post(String url) {
        return start(new HttpPostRequest(url));
    }

    public GeccoEngine<V> post(String url, Map<String, String> params) {
        return start(new HttpPostRequest(url, params));
    }

    public GeccoEngine<V> start(HttpRequest request) {
        this.startRequests.add(request);
        return this;
    }

    public GeccoEngine<V> start(List<HttpRequest> requests) {
        requests.forEach(this::start);
        return this;
    }

    public GeccoEngine<V> scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public GeccoEngine<V> thread(int count) {
        if (count <= 0) {
            log.error("thread count must above than 0!");
        } else {
            this.threadCount = count;
        }
        return this;
    }

    public GeccoEngine<V> interval(int interval) {
        this.interval = interval;
        return this;
    }

    public GeccoEngine<V> retry(int retry) {
        this.retry = retry;
        return this;
    }

    public GeccoEngine<V> loop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public GeccoEngine<V> proxysLoader(Proxys proxysLoader) {
        this.proxysLoader = proxysLoader;
        return this;
    }

    public GeccoEngine<V> proxy(boolean proxy) {
        this.proxy = proxy;
        return this;
    }

    public GeccoEngine<V> mobile(boolean mobile) {
        this.mobile = mobile;
        return this;
    }

    public GeccoEngine<V> debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public GeccoEngine<V> monitor(boolean monitor) {
        this.monitor = monitor;
        return this;
    }

    public GeccoEngine<V> classpath(String classpath) {
        this.classpath = classpath;
        return this;
    }

    public GeccoEngine<V> jmxPrefix(String jmxPrefix) {
        this.jmxPrefix = jmxPrefix;
        return this;
    }

    public GeccoEngine<V> pipelineFactory(PipelineFactory pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
        return this;
    }

    public GeccoEngine<V> downloaderFactoryBuilder(DownloaderFactoryBuilder downloaderFactoryBuilder) {
        this.downloaderFactoryBuilder = downloaderFactoryBuilder;
        return this;
    }

    public GeccoEngine<V> customFieldRenderFactory(CustomFieldRenderFactory customFieldRenderFactory) {
        this.customFieldRenderFactory = customFieldRenderFactory;
        return this;
    }

    public GeccoEngine<V> spiderBeanFactory(SpiderBeanFactory spiderBeanFactory) {
        this.spiderBeanFactory = spiderBeanFactory;
        return this;
    }

    public GeccoEngine<V> withStartsJson(boolean withStartsJson) {
        this.withStartsJson = withStartsJson;
        return this;
    }

    public GeccoEngine<V> startsJson(String startsJson) {
        this.startsJson = startsJson;
        return this;
    }

    public GeccoEngine<V> register(Class<?> spiderBeanClass) {
        getSpiderBeanFactory().addSpiderBean(spiderBeanClass);
        return this;
    }

    public GeccoEngine<V> unregister(Class<?> spiderBeanClass) {
        getSpiderBeanFactory().removeSpiderBean(spiderBeanClass);
        DynamicGecco.unregister(spiderBeanClass);
        return this;
    }

    public GeccoEngine<V> downloaderAOPFactoryBuilder(DownloaderAOPFactoryBuilder downloaderAOPFactoryBuilder) {
        this.downloaderAOPFactoryBuilder = downloaderAOPFactoryBuilder;
        return this;
    }

    public GeccoEngine<V> spiderFactoryBuilder(SpiderFactoryBuilder spiderFactoryBuilder) {
        this.spiderFactoryBuilder = spiderFactoryBuilder;
        return this;
    }

    public GeccoEngine<V> ret(V ret) {
        this.ret = ret;
        return this;
    }

    public GeccoEngine<V> eventListener(EventListener eventListener) {
        this.eventListener = eventListener;
        return this;
    }

    public GeccoEngine<V> userAgent(UserAgent userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    // ------------------------- setter end -------------------------------
    // ------------------------- getter start -------------------------------

    public boolean isLoop() {
        return loop;
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

    public boolean isWithStartsJson() {
        return withStartsJson;
    }

    public List<Spider> getSpiders() {
        return spiderFactory.getSpiders();
    }

    // ------------------------- getter end -------------------------------
}
