package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.config.GlobalConfig;
import com.geccocrawler.gecco.downloader.proxy.Proxys;
import com.geccocrawler.gecco.downloader.proxy.ProxysContext;
import com.geccocrawler.gecco.exception.DownloadException;
import com.geccocrawler.gecco.exception.DownloadServerException;
import com.geccocrawler.gecco.exception.DownloadTimeoutException;
import com.geccocrawler.gecco.exception.MethodException;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.SpiderThreadLocal;
import com.geccocrawler.gecco.utils.UrlUtils;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.CharArrayBuffer;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 利用httpclient下载
 *
 * @author huchengyi
 */
@com.geccocrawler.gecco.annotation.Downloader(GlobalConfig.DEFAULT_DOWNLOADER)
@CommonsLog
public class HttpClientDownloader extends AbstractDownloader {
    private CloseableHttpClient httpClient;

    private HttpClientContext cookieContext;

    public HttpClientDownloader() {

        cookieContext = HttpClientContext.create();
        cookieContext.setCookieStore(new BasicCookieStore());

        Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
        try {
            //构造一个信任所有ssl证书的httpclient
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)
                    .build();
        } catch (Exception ex) {
            socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", SSLConnectionSocketFactory.getSocketFactory())
                    .build();
        }
        RequestConfig clientConfig = RequestConfig.custom().setRedirectsEnabled(false).build();
        PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        syncConnectionManager.setMaxTotal(1000);
        syncConnectionManager.setDefaultMaxPerRoute(50);

        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(clientConfig)
                .setConnectionManager(syncConnectionManager)
                .setRetryHandler((exception, executionCount, context) -> {
                    int retryCount = SpiderThreadLocal.get().getEngine().getRetry();
                    boolean retry = (executionCount <= retryCount);
                    if (log.isDebugEnabled() && retry) {
                        log.debug("retry : " + executionCount);
                    }
                    return retry;
                }).build();
    }

    @Override
    public HttpResponse download(HttpRequest request, int timeout) throws DownloadException {
        if (log.isDebugEnabled()) {
            log.debug("downloading..." + request.getUrl());
        }

        HttpRequestBase reqObj = null;

        switch (request.getMethod()) {
            case GET:
                reqObj = new HttpGet(request.getUrl());
                break;
            case POST:
                reqObj = new HttpPost(request.getUrl());
                //post fields
                List<NameValuePair> fields = request.getParameters().entrySet()
                        .stream()
                        .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
                HttpEntity entity = new UrlEncodedFormEntity(fields, GlobalConfig.DEFAULT_CHARSET);
                ((HttpEntityEnclosingRequestBase) reqObj).setEntity(entity);
                break;
            default:
                throw new MethodException("request method: " + request.getMethod() + " is not support");
        }

        // header
        boolean isMobile = SpiderThreadLocal.get().getEngine().isMobile();
        UserAgent userAgent = SpiderThreadLocal.get().getEngine().getUserAgent();
        reqObj.addHeader("User-Agent", userAgent.getUserAgent(isMobile));

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            reqObj.setHeader(entry.getKey(), entry.getValue());
        }

        // request config
        RequestConfig.Builder builder = RequestConfig.custom()
                //从连接池获取连接的超时时间
                .setConnectionRequestTimeout(1000)
                //获取内容的超时时间
                .setSocketTimeout(timeout)
                //建立socket连接的超时时间
                .setConnectTimeout(timeout)
                .setRedirectsEnabled(false);

        //proxy
        HttpHost proxy = null;
        Proxys proxys = ProxysContext.get();
        boolean isProxy = ProxysContext.isEnableProxy();
        if (proxys != null && isProxy) {
            proxy = proxys.getProxy();
            if (proxy != null) {
                log.debug("proxy:" + proxy.getHostName() + ":" + proxy.getPort());
                builder.setProxy(proxy);
                //如果走代理，连接超时时间固定为1s
                builder.setConnectTimeout(1000);
            }
        }

        reqObj.setConfig(builder.build());

        //request and response
        try {
            for (Map.Entry<String, String> entry : request.getCookies().entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
                cookie.setPath("/");
                cookie.setDomain(reqObj.getURI().getHost());
                cookieContext.getCookieStore().addCookie(cookie);
            }

            org.apache.http.HttpResponse response = httpClient.execute(reqObj, cookieContext);

            int status = response.getStatusLine().getStatusCode();
            HttpResponse resp = new HttpResponse();
            resp.setStatus(status);
            if (status == HttpStatus.SC_MOVED_TEMPORARILY || status == HttpStatus.SC_SEE_OTHER) {
                String redirectUrl = response.getFirstHeader("Location").getValue();
                resp.setContent(UrlUtils.relative2Absolute(request.getUrl(), redirectUrl));
            } else if (status == HttpStatus.SC_OK) {
                HttpEntity responseEntity = response.getEntity();
                ByteArrayInputStream raw = toByteInputStream(responseEntity.getContent());
                resp.setRaw(raw);
                String contentType = null;
                Header contentTypeHeader = responseEntity.getContentType();
                if (contentTypeHeader != null) {
                    contentType = contentTypeHeader.getValue();
                }
                resp.setContentType(contentType);
                if (!isImage(contentType)) {
                    String charset = request.isForceUseCharset() ? request.getCharset() : getCharset(request.getCharset(), contentType);
                    resp.setCharset(charset);
                    String content = getContent(raw, responseEntity.getContentLength(), charset);
                    resp.setContent(content);
                }
            } else {
                //404，500等
                if (proxy != null) {
                    proxys.failure(proxy.getHostName(), proxy.getPort());
                }
                throw new DownloadServerException(" " + status);
            }

            if (proxy != null) {
                proxys.success(proxy.getHostName(), proxy.getPort());
            }
            return resp;
        } catch (ConnectTimeoutException | SocketTimeoutException e) {
            if (proxy != null) {
                proxys.failure(proxy.getHostName(), proxy.getPort());
            }
            throw new DownloadTimeoutException(e);
        } catch (IOException e) {
            if (proxy != null) {
                proxys.failure(proxy.getHostName(), proxy.getPort());
            }
            throw new DownloadException(e);
        } finally {
            reqObj.releaseConnection();
        }
    }

    @Override
    public void shutdown() {
        try {
            httpClient.close();
        } catch (IOException e) {
            httpClient = null;
        }
    }

    public String getContent(InputStream instream, long contentLength, String charset) throws IOException {
        if (instream == null) {
            return null;
        }
        try {
            int i = (int) contentLength;
            if (i < 0) {
                i = 4096;
            }
            Reader reader = new InputStreamReader(instream, charset);
            CharArrayBuffer buffer = new CharArrayBuffer(i);
            char[] tmp = new char[1024];
            int l;
            while ((l = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
            return buffer.toString();
        } finally {
            instream.reset();
        }
    }

    private boolean isImage(String contentType) {
        return contentType != null && contentType.toLowerCase().startsWith("image");
    }
}
