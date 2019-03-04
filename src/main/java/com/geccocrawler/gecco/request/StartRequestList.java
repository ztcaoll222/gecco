package com.geccocrawler.gecco.request;

import com.geccocrawler.gecco.config.GlobalConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class StartRequestList implements Serializable {
	private static final long serialVersionUID = -8852418057148517662L;

	private String url;

	private String charset;

	private final Map<String, String> cookies;

	private final Map<String, String> headers;

	private final Map<String, String> posts;
	
	public StartRequestList() {
		cookies = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
		headers = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
		posts = new HashMap<>(GlobalConfig.DEFAULT_COLLECTION_SIZE);
	}
	
	public HttpRequest toRequest() {
		if(posts.size() > 0) {
			HttpPostRequest post = new HttpPostRequest(this.getUrl());
			post.setCharset(charset);
			post.setFields(posts);
			post.setCookies(cookies);
			post.setHeaders(headers);
			return post;
		} else {
			HttpGetRequest get = new HttpGetRequest(this.getUrl());
			get.setCharset(charset);
			get.setCookies(cookies);
			get.setHeaders(headers);
			return get;
		}
	}

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addCookie(String name, String value) {
		cookies.put(name, value);
	}
	
	public void addPost(String name, String value) {
		posts.put(name, value);
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies.putAll(cookies);
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers.putAll(headers);
	}

	public void setPosts(Map<String, String> posts) {
		this.posts.putAll(posts);
	}
}
