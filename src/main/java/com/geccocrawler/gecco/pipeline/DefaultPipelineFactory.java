package com.geccocrawler.gecco.pipeline;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.config.GlobalConfig;
import com.geccocrawler.gecco.spider.SpiderBean;
import lombok.extern.apachecommons.CommonsLog;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

@CommonsLog
public class DefaultPipelineFactory implements PipelineFactory {

    private Map<String, Pipeline<? extends SpiderBean>> pipelines;

    @SuppressWarnings({"unchecked"})
    public DefaultPipelineFactory(Reflections reflections) {
        this.pipelines = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);

        reflections.getTypesAnnotatedWith(PipelineName.class).forEach(pipelineClass -> {
            PipelineName spiderFilter = pipelineClass.getAnnotation(PipelineName.class);
            try {
                pipelines.put(spiderFilter.value(), (Pipeline<? extends SpiderBean>) pipelineClass.newInstance());
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.error(ex.getMessage(), ex);
                } else {
                    log.error(ex.getMessage());
                }
            }
        });
    }

    @Override
    public Pipeline<? extends SpiderBean> get(String name) {
        return pipelines.get(name);
    }

}
