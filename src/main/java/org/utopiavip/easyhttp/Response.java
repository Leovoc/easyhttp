package org.utopiavip.easyhttp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class Response {

    private CloseableHttpResponse httpResponse;
    private HttpEntity httpEntity;
    private Header[] headers;

    private byte[] contentBuffer;
    private String content;

    private static final String UTF8 = "UTF-8";

    public Response(CloseableHttpResponse httpResponse) {
        this.httpResponse = httpResponse;
        this.httpEntity = httpResponse.getEntity();
        this.headers = httpResponse.getAllHeaders();

        try {
            flushInputStream2Buffer(); // Flush content to buffer
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void flushInputStream2Buffer() throws IOException {
        InputStream contentInputStream = this.httpEntity.getContent();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = -1;
        while ((len = contentInputStream.read(buf)) != -1) {
            outSteam.write(buf, 0, len);
        }
        this.contentBuffer = outSteam.toByteArray();
        outSteam.close();
    }

    public JSONObject json() {
        return JSON.parseObject(getContent());
    }

    public String text(){
        return getContent();
    }

    public Header[] getAllHeaders() {
        return this.headers;
    }

    public Header[] getHeaders(String name) {
        return this.httpResponse.getHeaders(name);
    }

    public String getContentType() {
        return getHeader(HTTP.CONTENT_TYPE);
    }

    public String getHeader(String name) {
        for (Header header : this.headers) {
            if (header.getName().equals(name)) {
                return header.getValue();
            }
        }
        return null;
    }

    public String getContent() {
        if (this.content == null) {
            if (this.contentBuffer != null) {
                this.content = new String(this.contentBuffer);
            }
        }
        return this.content;
    }

    public InputStream getInputStream() {
        if (this.contentBuffer != null) {
            return new ByteArrayInputStream(this.contentBuffer);
        }
        return null;
    }

    public Locale getLocale() {
        return this.httpResponse.getLocale();
    }

    public ProtocolVersion getProtocolVersion() {
        return this.httpResponse.getProtocolVersion();
    }

    public StatusLine getStatusLine() {
        return this.httpResponse.getStatusLine();
    }

    public int getStatusCode() {
        return getStatusLine().getStatusCode();
    }

}
