package com.geccocrawler.gecco.response;

import com.google.common.io.CharStreams;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

@Data
public class HttpResponse implements Serializable {
	private static final long serialVersionUID = -1808592190693059897L;

	private ByteArrayInputStream raw;

	private String content;

	private String contentType;
	
	private String charset;

	private int status;
	
	public static HttpResponse createSimple(String content) {
		HttpResponse response = new HttpResponse();
		response.setContent(content);
		return response;
	}
	
	public String getContent(String charset) {
		if(charset == null) {
			return content;
		}
		try {
			return CharStreams.toString(new InputStreamReader(raw, charset));
		} catch (Exception e) {
			e.printStackTrace();
			return content;
		}
	}

	public void close() {
		if(raw != null) {
			try{
				raw.close();
			} catch(Exception ex) {
				raw = null;
			}
		}
	}
}
