package com.geccocrawler.gecco.spider.render;

import com.geccocrawler.gecco.annotation.FieldRenderName;
import com.geccocrawler.gecco.config.GlobalConfig;
import lombok.extern.apachecommons.CommonsLog;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

@CommonsLog
public class DefaultFieldRenderFactory implements CustomFieldRenderFactory {

    private Map<String, CustomFieldRender> map;

    public DefaultFieldRenderFactory(Reflections reflections) {
        this.map = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);

        reflections.getTypesAnnotatedWith(FieldRenderName.class).forEach(clazz -> {
            FieldRenderName fieldRenderName = clazz.getAnnotation(FieldRenderName.class);
            try {
                map.put(fieldRenderName.value(), (CustomFieldRender) clazz.newInstance());
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
    public CustomFieldRender get(String name) {
        return map.get(name);
    }

}
