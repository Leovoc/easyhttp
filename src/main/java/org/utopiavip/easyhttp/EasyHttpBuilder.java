package org.utopiavip.easyhttp;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.*;

/**
 * Http Builder, Used to send HTTP requests
 *
 * @author cyj
 */
public class EasyHttpBuilder {

    private Map<String, String> headers = new HashMap<String, String>();

    private String contentType = null;
    private RequestConfig.Builder requestConfigbuilder = RequestConfig.custom();

    private static final String UTF8 = "UTF-8";

    private EasyHttpBuilder() {
    }

    /**
     * Create a EasyHttpBuilder
     *
     * @return
     */
    public static EasyHttpBuilder build() {
        return new EasyHttpBuilder();
    }

    /**
     * Http GET
     * <p>Append parameters to url directly or Use urlVariables map to pass parameter</p>
     *
     * @param url
     * @param urlVariables
     * @return
     */
    public Response get(String url, Map<String, Object> urlVariables) {
        return doRequestInterval(url, HttpMethod.GET, urlVariables, null);
    }

    public Response get(String url) {
        return get(url, null);
    }

    /**
     * Http POST, use variables map to pass parameters, will auto parse it according by content-type
     * @param url
     * @param variables
     * @return
     */
    public Response post(String url, Map<String, Object> variables) {

        return doRequestInterval(url, HttpMethod.POST, variables, null);
    }

    /**
     * Http POST, pass http entity directly, will auto-parse it according to content-type
     * @param url
     * @param httpEntity The http entity,
     * @return
     */
    public Response post(String url, String httpEntity) {

        return doRequestInterval(url, HttpMethod.POST, null, httpEntity);
    }

    public Response post(String url) {

        return doRequestInterval(url, HttpMethod.POST, null, null);
    }

    public Response put(String url, Map<String, Object> variables) {

        return doRequestInterval(url, HttpMethod.PUT, variables, null);
    }

    public Response put(String url, String httpEntity) {

        return doRequestInterval(url, HttpMethod.PUT, null, httpEntity);
    }

    public Response delete(String url, Map<String, Object> urlVariables) {
        return doRequestInterval(url, HttpMethod.DELETE, urlVariables, null);
    }

    public Response delete(String url) {
        return delete(url, null);
    }

    /**
     * Set org.utopiavip.easyhttp request header attributes
     *
     * @param name  attribute name
     * @param value attribute value
     * @return
     */
    public EasyHttpBuilder head(String name, String value) {
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(value)) {
            headers.put(name, value);

            if (name.equals(HTTP.CONTENT_TYPE)) {
                contentType = value;
            }
        }
        return this;
    }

    /**
     * Set org.utopiavip.easyhttp request header[content-type]
     *
     * @param contentType
     * @return
     */
    public EasyHttpBuilder contentType(String contentType) {
        return head(HTTP.CONTENT_TYPE, contentType);
    }

    /**
     * Set org.utopiavip.easyhttp request header[Authorization]
     *
     * @param authorization
     * @return
     */
    public EasyHttpBuilder auth(String authorization) {
        return head("Authorization", authorization);
    }

    private Response doRequestInterval(String url, HttpMethod method, Map<String, Object> urlVariables, String body) {

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpRequestBase request = null;

            if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE)) {
                url = buildUrl(url, urlVariables);

                if (method.equals(HttpMethod.GET)) {
                    request = new HttpGet(url);
                } else {
                    request = new HttpDelete(url);
                }

            } else if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {

                HttpEntityEnclosingRequestBase entityEnclosingRequestBase = null;
                if (method.equals(HttpMethod.POST)) {
                    entityEnclosingRequestBase = new HttpPost(url);
                } else {
                    entityEnclosingRequestBase = new HttpPut(url);
                }

                AbstractHttpEntity httpEntity = buildHttpEntity(urlVariables, body);
                if (httpEntity != null) {
                    entityEnclosingRequestBase.setEntity(httpEntity);
                }

                request = entityEnclosingRequestBase;

            }

            addHeader(headers);
            request.setConfig(this.requestConfigbuilder.build());

            CloseableHttpResponse httpResponse = httpClient.execute(request);

            return new Response(httpResponse);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AbstractHttpEntity buildHttpEntity(Map<String, Object> urlVariables, String bodyEntity) throws UnsupportedEncodingException {

        AbstractHttpEntity httpEntity = null;

        if (!StringUtils.isEmpty(bodyEntity)) {
            httpEntity = new StringEntity(bodyEntity, UTF8);

        } else {
            if (urlVariables != null && urlVariables.size() > 0) {
                if (MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    for (Map.Entry<String, Object> entry : urlVariables.entrySet()) {
                        String paramName = entry.getKey() == null ? "" : entry.getKey();
                        String paramValue = entry.getValue() == null ? "" : entry.getValue().toString();
                        nameValuePairs.add(new BasicNameValuePair(paramName, paramValue));
                    }
                    httpEntity = new UrlEncodedFormEntity(nameValuePairs, UTF8);

                } else if (MediaType.APPLICATION_JSON.equals(contentType)) {
                    String body = JSON.toJSONString(urlVariables);
                    httpEntity = new StringEntity(body, UTF8);
                }
            }
        }

        return httpEntity;
    }

    private void addHeader(Object httpClient) {
        if (httpClient instanceof HttpRequestBase) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                ((HttpRequestBase) httpClient).addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private String buildUrl(String url, Map<String, Object> urlVariables) {

        if (url == null || "".equals(url)) {
            throw new IllegalArgumentException("url can't be null");
        }

        StringBuilder buf = new StringBuilder(url);
        if (urlVariables != null && urlVariables.size() > 0) {
            if (url.contains("?")) {
                buf.append("&");
            } else {
                buf.append("?");
            }

            for (Map.Entry<String, Object> entry : urlVariables.entrySet()) {
                buf.append(entry.getKey() + "=" + entry.getValue() + "&");
            }
            buf.deleteCharAt(buf.length() - 1); // delete the final char

        }
        return buf.toString();
    }

    /**
     * 以下所有方法拷贝于RequestConfig
     */

    public EasyHttpBuilder setExpectContinueEnabled(boolean expectContinueEnabled) {
        this.requestConfigbuilder.setExpectContinueEnabled(expectContinueEnabled);
        return this;
    }

    public EasyHttpBuilder setProxy(HttpHost proxy) {
        this.requestConfigbuilder.setProxy(proxy);
        return this;
    }

    public EasyHttpBuilder setLocalAddress(InetAddress localAddress) {
        this.requestConfigbuilder.setLocalAddress(localAddress);
        return this;
    }

    public EasyHttpBuilder setCookieSpec(String cookieSpec) {
        this.requestConfigbuilder.setCookieSpec(cookieSpec);
        return this;
    }

    public EasyHttpBuilder setRedirectsEnabled(boolean redirectsEnabled) {
        this.requestConfigbuilder.setRedirectsEnabled(redirectsEnabled);
        return this;
    }

    public EasyHttpBuilder setRelativeRedirectsAllowed(boolean relativeRedirectsAllowed) {
        this.requestConfigbuilder.setRelativeRedirectsAllowed(relativeRedirectsAllowed);
        return this;
    }

    public EasyHttpBuilder setCircularRedirectsAllowed(boolean circularRedirectsAllowed) {
        this.requestConfigbuilder.setCircularRedirectsAllowed(circularRedirectsAllowed);
        return this;
    }

    public EasyHttpBuilder setMaxRedirects(int maxRedirects) {
        this.requestConfigbuilder.setMaxRedirects(maxRedirects);
        return this;
    }

    public EasyHttpBuilder setAuthenticationEnabled(boolean authenticationEnabled) {
        this.requestConfigbuilder.setAuthenticationEnabled(authenticationEnabled);
        return this;
    }

    public EasyHttpBuilder setTargetPreferredAuthSchemes(Collection<String> targetPreferredAuthSchemes) {
        this.requestConfigbuilder.setTargetPreferredAuthSchemes(targetPreferredAuthSchemes);
        return this;
    }

    public EasyHttpBuilder setProxyPreferredAuthSchemes(Collection<String> proxyPreferredAuthSchemes) {
        this.requestConfigbuilder.setProxyPreferredAuthSchemes(proxyPreferredAuthSchemes);
        return this;
    }

    public EasyHttpBuilder setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.requestConfigbuilder.setConnectionRequestTimeout(connectionRequestTimeout);
        return this;
    }

    public EasyHttpBuilder setConnectTimeout(int connectTimeout) {
        this.requestConfigbuilder.setConnectTimeout(connectTimeout);
        return this;
    }

    public EasyHttpBuilder setSocketTimeout(int socketTimeout) {
        this.requestConfigbuilder.setSocketTimeout(socketTimeout);
        return this;
    }

    public EasyHttpBuilder setContentCompressionEnabled(boolean contentCompressionEnabled) {
        this.requestConfigbuilder.setContentCompressionEnabled(contentCompressionEnabled);
        return this;
    }
}
