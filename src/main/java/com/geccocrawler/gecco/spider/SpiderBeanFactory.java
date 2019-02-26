package com.geccocrawler.gecco.spider;

import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.config.GlobalConfig;
import com.geccocrawler.gecco.downloader.*;
import com.geccocrawler.gecco.dynamic.GeccoClassLoader;
import com.geccocrawler.gecco.dynamic.GeccoJavaReflectionAdapter;
import com.geccocrawler.gecco.pipeline.DefaultPipelineFactory;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.pipeline.PipelineFactory;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.render.CustomFieldRenderFactory;
import com.geccocrawler.gecco.spider.render.MonitorRenderFactory;
import com.geccocrawler.gecco.spider.render.RenderFactory;
import com.geccocrawler.gecco.spider.render.RenderType;
import com.geccocrawler.gecco.utils.ReflectUtils;
import com.geccocrawler.gecco.utils.UrlMatcher;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpiderBean是爬虫渲染的JavaBean的统一接口类，所有Bean均继承该接口。SpiderBeanFactroy会根据请求的url地址，
 * 匹配相应的SpiderBean，同时生成该SpiderBean的上下文SpiderBeanContext. SpiderBeanContext包括需要改SpiderBean的渲染类
 * （目前支持HTML、JSON两种Bean的渲染方式）、下载前处理类、下载后处理类以及渲染完成后对SpiderBean的后续处理Pipeline。
 *
 * @author huchengyi
 */
@Getter
@CommonsLog
public class SpiderBeanFactory {
    /**
     * 匹配的SpriderBean matchUrl:SpiderBean
     */
    private Map<String, Class<? extends SpiderBean>> spiderBeans;

    /**
     * 匹配的SpiderBean上下文 SpiderBeanClassName:SpiderBeanClass
     */
    private Map<String, SpiderBeanContext> spiderBeanContexts;

    private AbstractDownloaderFactory downloaderFactory;

    private DownloaderAOPFactory downloaderAOPFactory;

    private RenderFactory renderFactory;

    private PipelineFactory pipelineFactory;

    protected Reflections reflections;

    public SpiderBeanFactory(String classPath) {
        this(classPath, null, null, null, null);
    }

    public SpiderBeanFactory(String classPath, PipelineFactory pipelineFactory, CustomFieldRenderFactory customFieldRenderFactory,
                             DownloaderFactoryBuilder downloaderFactoryBuilder, DownloaderAOPFactoryBuilder downloaderAOPFactoryBuilder) {
        if (StringUtils.isNotEmpty(classPath)) {
            reflections = new Reflections(ConfigurationBuilder.build(GlobalConfig.PACKAGE_NAME, classPath, GeccoClassLoader.get())
                    .setMetadataAdapter(new GeccoJavaReflectionAdapter())
                    .setExpandSuperTypes(false));
        } else {
            reflections = new Reflections(ConfigurationBuilder.build(GlobalConfig.PACKAGE_NAME, GeccoClassLoader.get())
                    .setMetadataAdapter(new GeccoJavaReflectionAdapter())
                    .setExpandSuperTypes(false));
        }

        dynamic();

        this.downloaderFactory = (downloaderFactoryBuilder == null ? new MonitorDownloaderFactory(reflections) : downloaderFactoryBuilder.build(reflections));
        this.downloaderAOPFactory = (downloaderAOPFactoryBuilder == null ? new DefaultDownloaderAOPFactory(reflections) : downloaderAOPFactoryBuilder.build(reflections));
        this.renderFactory = new MonitorRenderFactory(reflections, customFieldRenderFactory);
        if (pipelineFactory != null) {
            this.pipelineFactory = pipelineFactory;
        } else {
            this.pipelineFactory = new DefaultPipelineFactory(reflections);
        }
        this.spiderBeans = new ConcurrentHashMap<>();
        this.spiderBeanContexts = new ConcurrentHashMap<>();

        loadSpiderBean(reflections);
    }

    /**
     * 动态增加的spiderBean
     */
    private void dynamic() {
        GeccoClassLoader.get().getClasses().keySet()
                .forEach(className -> reflections.getStore()
                        .get(TypeAnnotationsScanner.class.getSimpleName())
                        .put(Gecco.class.getName(), className));
    }

    private void loadSpiderBean(Reflections reflections) {
        reflections.getTypesAnnotatedWith(Gecco.class).forEach(this::addSpiderBean);
    }

    @SuppressWarnings({"unchecked"})
    public void addSpiderBean(Class<?> spiderBeanClass) {
        Gecco gecco = spiderBeanClass.getAnnotation(Gecco.class);
        Arrays.stream(gecco.matchUrl()).forEach(matchUrl -> {
            try {
                if (spiderBeans.containsKey(matchUrl)) {
                    log.warn("there are multil '" + matchUrl + "' ,first htmlBean will be Override。");
                }
                spiderBeans.put(matchUrl, (Class<? extends SpiderBean>) spiderBeanClass);
                SpiderBeanContext context = initContext(spiderBeanClass);
                spiderBeanContexts.put(spiderBeanClass.getName(), context);
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.error(ex.getMessage(), ex);
                } else {
                    log.error(ex.getMessage());
                }
            }
        });
    }

    public void removeSpiderBean(Class<?> spiderBeanClass) {
        Gecco gecco = spiderBeanClass.getAnnotation(Gecco.class);
        Arrays.stream(gecco.matchUrl()).forEach(matchUrl -> {
            try {
                spiderBeans.remove(matchUrl);
                spiderBeanContexts.remove(spiderBeanClass.getName());
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.error(ex.getMessage(), ex);
                } else {
                    log.error(ex.getMessage());
                }
            }
        });
    }

    public Class<? extends SpiderBean> matchSpider(HttpRequest request) {
        String url = request.getUrl();
        // 通用爬虫
        Class<? extends SpiderBean> commonSpider = null;
        for (Map.Entry<String, Class<? extends SpiderBean>> entrys : spiderBeans.entrySet()) {
            Class<? extends SpiderBean> spider = entrys.getValue();
            String urlPattern = entrys.getKey();
            Map<String, String> params = UrlMatcher.match(url, urlPattern);
            if (params != null) {
                request.setParameters(params);
                return spider;
            } else {
                if ("*".equals(urlPattern)) {
                    commonSpider = spider;
                }
            }
        }
        if (commonSpider != null) {
            // 如果包含通用爬虫，返回通用爬虫
            return commonSpider;
        }
        return null;
    }

    public SpiderBeanContext getContext(Class<? extends SpiderBean> spider) {
        return spiderBeanContexts.get(spider.getName());
    }

    private SpiderBeanContext initContext(Class<?> spiderBeanClass) {
        SpiderBeanContext context = new SpiderBeanContext();
        // 关联的after、before、downloader
        downloadContext(context, spiderBeanClass);
        // 关联的render
        renderContext(context, spiderBeanClass);
        // 关联的pipelines
        Gecco gecco = spiderBeanClass.getAnnotation(Gecco.class);
        String[] pipelineNames = gecco.pipelines();
        pipelineContext(context, pipelineNames);
        return context;
    }

    private void downloadContext(SpiderBeanContext context, Class<?> spiderBeanClass) {
        Gecco gecco = spiderBeanClass.getAnnotation(Gecco.class);

        String beforeDownloadName = gecco.beforeDownload();
        String afterDownload = gecco.afterDownload();
        String geccoName = spiderBeanClass.getName();
        context.setBeforeDownload(downloaderAOPFactory.getBefore(beforeDownloadName, geccoName));
        context.setAfterDownload(downloaderAOPFactory.getAfter(afterDownload, geccoName));

        String downloader = gecco.downloader();
        context.setDownloader(downloaderFactory.getDownloader(downloader));

        context.setTimeout(gecco.timeout());
    }

    private void renderContext(SpiderBeanContext context, Class<?> spiderBeanClass) {
        RenderType renderType = RenderType.HTML;
        if (ReflectUtils.haveSuperType(spiderBeanClass, JsonBean.class)) {
            renderType = RenderType.JSON;
        }
        context.setRender(renderFactory.getRender(renderType));
    }

    @SuppressWarnings({"rawtypes"})
    private void pipelineContext(SpiderBeanContext context, String[] pipelineNames) {
        if (pipelineNames != null && pipelineNames.length > 0) {
            List<Pipeline> pipelines = new ArrayList<>();
            for (String pipelineName : pipelineNames) {
                if (StringUtils.isEmpty(pipelineName)) {
                    continue;
                }
                Pipeline pipeline = pipelineFactory.getPipeline(pipelineName);
                if (pipeline != null) {
                    pipelines.add(pipeline);
                }
            }
            context.setPipelines(pipelines);
        }
    }
}
