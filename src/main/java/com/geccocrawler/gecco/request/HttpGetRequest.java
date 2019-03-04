package com.geccocrawler.gecco.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class HttpGetRequest extends AbstractHttpRequest {

	private static final long serialVersionUID = 6105458424891960971L;

	public HttpGetRequest() {
		super();
	}

	public HttpGetRequest(String url) {
		super(url);
	}

	public static HttpGetRequest fromJson(JSONObject request) {
		return JSON.toJavaObject(request, HttpGetRequest.class);
	}

	public static void main(String[] args) {
	    HttpGetRequest a1 = new HttpGetRequest();
	    a1.setUrl("");
	    HttpGetRequest a2 = new HttpGetRequest();
	    a2.setUrl("");
		System.out.println(a1.equals(a2));
		System.out.println(a1.hashCode());
		System.out.println(a2.hashCode());
	}
}
