package com.geccocrawler.gecco.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.emuns.MethodEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class HttpGetRequest extends AbstractHttpRequest {

    private static final long serialVersionUID = 6105458424891960971L;

    public HttpGetRequest() {
        super(MethodEnum.GET);
    }

    public HttpGetRequest(String url) {
        super(url, MethodEnum.GET);
    }

    public static HttpGetRequest fromJson(JSONObject request) {
        return JSON.toJavaObject(request, HttpGetRequest.class);
    }
}
