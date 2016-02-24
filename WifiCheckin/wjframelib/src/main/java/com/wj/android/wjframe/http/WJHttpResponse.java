
package com.wj.android.wjframe.http;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * 兼容6.0，用于替换掉org.apache.http.HttpResponse
 * NOTE:再次感谢Q群中的 @黑猫白猫抓到老鼠 提供的这个KJHttpResponse的思路以及代码实现
 *
 */
public class WJHttpResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, String> headers;

    private int responseCode;

    private String responseMessage;

    private InputStream contentStream;

    private String contentEncoding;

    private String contentType;

    private long contentLength;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
}
