package com.geccocrawler.gecco.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.emuns.MethodEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class HttpPostRequest extends AbstractHttpRequest {

    private static final long serialVersionUID = -4451221207994730839L;

    public HttpPostRequest() {
        super(MethodEnum.POST);
    }

    public HttpPostRequest(String url) {
        super(url, MethodEnum.POST);
    }

    public HttpPostRequest(String url, Map<String, String> params) {
        super(url, MethodEnum.POST);
        this.parameters.putAll(params);
    }

    public static HttpPostRequest fromJson(JSONObject request) {
        return JSON.toJavaObject(request, HttpPostRequest.class);
    }
}
