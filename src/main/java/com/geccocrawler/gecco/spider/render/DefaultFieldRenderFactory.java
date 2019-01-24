package com.geccocrawler.gecco.spider.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.geccocrawler.gecco.annotation.FieldRenderName;

public class DefaultFieldRenderFactory implements CustomFieldRenderFactory {
	
	private Map<String, CustomFieldRender> map;

	public DefaultFieldRenderFactory(Reflections reflections) {
		this.map = new HashMap<>();
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(FieldRenderName.class);
		for(Class<?> clazz : classes) {
			FieldRenderName fieldRenderName = (FieldRenderName)clazz.getAnnotation(FieldRenderName.class);
			try {
				map.put(fieldRenderName.value(), (CustomFieldRender)clazz.newInstance());
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public CustomFieldRender getCustomFieldRender(String name) {
		return map.get(name);
	}

}
