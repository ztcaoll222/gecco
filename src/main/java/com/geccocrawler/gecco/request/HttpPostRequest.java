package com.geccocrawler.gecco.request;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.config.GlobalConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class HttpPostRequest extends AbstractHttpRequest {

	private static final long serialVersionUID = -4451221207994730839L;

	private Map<String, String> fields;
	
	public HttpPostRequest() {
		super();
		fields = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
	}

	public HttpPostRequest(String url) {
		super(url);
		fields = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
	}

	public HttpPostRequest(String url, Map<String, String> params) {
		super(url);
		fields = params;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public HttpPostRequest setFields(Map<String, String> fields) {
		this.fields = fields;
		return this;
	}

	public HttpPostRequest addField(String name, String field) {
		fields.put(name, field);
		return this;
	}
	
	public String getField(String name) {
		return fields.get(name);
	}
	
	public static HttpPostRequest fromJson(JSONObject request) {
		return JSON.toJavaObject(request, HttpPostRequest.class);
	}

	@Override
	public void clearParams() {
		super.clearParams();
		Iterator<Map.Entry<String, String>> it = this.fields.entrySet().iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
	}
}
