package com.geccocrawler.gecco.request;

import com.geccocrawler.gecco.emuns.MethodEnum;

import java.util.Map;

public interface HttpRequest extends Cloneable {

	MethodEnum getMethod();
	
	String getUrl();
	
	void setUrl(String url);
	
	void addParameter(String name, String value);
	
	void setParameters(Map<String, String> parameters);
	
	String getParameter(String name);
	
	Map<String, String> getParameters();
	
	void addHeader(String name, String value);
	
	Map<String, String> getHeaders();
	
	void clearHeader();

	void refer(String refer);
	
	String getCharset();
	
	void setCharset(String charset);
	
	void setForceUseCharset(boolean forceUseCharset);
	
	boolean isForceUseCharset();
	
	HttpRequest subRequest(String url);
	
	Map<String, String> getCookies();

    void clearParams();

    void addCookie(String name, String value);

    void setCookies(Map<String, String> cookies);
	
	String getCookie(String name);
	
	void clearCookie();

	void setHeaders(Map<String, String> headers);

	long getPriority();
	
	void setPriority(long prio);
}
