package com.geccocrawler.gecco.spider.render;

import com.geccocrawler.gecco.annotation.RequestParameter;
import com.geccocrawler.gecco.config.GlobalConfig;
import com.geccocrawler.gecco.exception.FieldRenderException;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.spider.conversion.Conversion;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestParameterFieldRender implements FieldRender {

    @Override
    @SuppressWarnings({"unchecked"})
    public void render(HttpRequest request, HttpResponse response, BeanMap beanMap, SpiderBean bean) {
        Map<String, Object> fieldMap = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
        Set<Field> requestParameterFields = ReflectionUtils.getAllFields(bean.getClass(), ReflectionUtils.withAnnotation(RequestParameter.class));
        for (Field field : requestParameterFields) {
            RequestParameter requestParameter = field.getAnnotation(RequestParameter.class);
            String key = requestParameter.value();
            if (StringUtils.isEmpty(key)) {
                key = field.getName();
            }
            String src = request.getParameter(key);
            try {
                Object value = Conversion.getValue(field.getType(), src);
                fieldMap.put(field.getName(), value);
            } catch (Exception ex) {
                FieldRenderException.log(field, src, ex);
            }
        }
        beanMap.putAll(fieldMap);
    }
}
