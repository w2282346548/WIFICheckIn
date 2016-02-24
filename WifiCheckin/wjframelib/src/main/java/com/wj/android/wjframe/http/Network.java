
package com.wj.android.wjframe.http;



import com.wj.android.wjframe.utils.WJLoger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求执行器，将传入的Request使用HttpStack客户端发起网络请求，并返回一个NetworkRespond结果
 *
 *
 */
public class Network {
    protected static final boolean DEBUG = HttpConfig.DEBUG;
    protected final HttpStack mHttpStack;

    public Network(HttpStack httpStack) {
        mHttpStack = httpStack;
    }

    /**
     * 实际执行一个请求的方法
     *
     * @param request 一个请求任务
     * @return 一个不会为null的响应
     * @throws WJHttpException
     */
    public NetworkResponse performRequest(Request<?> request)
            throws WJHttpException {
        while (true) {
            WJHttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, String> responseHeaders = new HashMap<String, String>();
            try {
                // 标记Http响应头在Cache中的tag
                Map<String, String> headers = new HashMap<String, String>();
                addCacheHeaders(headers, request.getCacheEntry());
                httpResponse = mHttpStack.performRequest(request, headers);

                int statusCode = httpResponse.getResponseCode();
                responseHeaders = httpResponse.getHeaders();
                if (statusCode == HttpStatus.SC_NOT_MODIFIED) { // 304
                    return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED,
                            request.getCacheEntry() == null ? null : request
                                    .getCacheEntry().data,
                            responseHeaders, true);
                }

                if (httpResponse.getContentStream() != null) {
                    if (request instanceof FileRequest) {
                        responseContents = ((FileRequest) request)
                                .handleResponse(httpResponse);
                    } else {
                        responseContents = entityToBytes(httpResponse);
                    }
                } else {
                    responseContents = new byte[0];
                }

                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false);
            } catch (SocketTimeoutException e) {
                throw new WJHttpException(new SocketTimeoutException("socket timeout"));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                NetworkResponse networkResponse = null;
                if (httpResponse != null) {
                    statusCode = httpResponse.getResponseCode();
                } else {
                    throw new WJHttpException("NoConnection error", e);
                }
                WJLoger.debug("Unexpected response code %d for %s", statusCode, request.getUrl());
                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode,
                            responseContents, responseHeaders, false);
                    if (statusCode == HttpStatus.SC_UNAUTHORIZED
                            || statusCode == HttpStatus.SC_FORBIDDEN) {
                        throw new WJHttpException("auth error");
                    } else {
                        throw new WJHttpException(
                                "server error, Only throw ServerError for 5xx status codes.",
                                networkResponse);
                    }
                } else {
                    throw new WJHttpException();
                }
            }
        }
    }

    /**
     * 标记Respondeader响应头在Cache中的tag
     *
     * @param headers
     * @param entry
     */
    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry) {
        if (entry == null) {
            return;
        }
        if (entry.etag != null) {
            headers.put("If-None-Match", entry.etag);
        }
        if (entry.serverDate > 0) {
            Date refTime = new Date(entry.serverDate);
            DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
            headers.put("If-Modified-Since", sdf.format(refTime));

        }
    }

    /**
     * 把HttpEntry转换为byte[]
     *
     * @throws IOException
     * @throws WJHttpException
     */
    private byte[] entityToBytes(WJHttpResponse WJHttpResponse) throws IOException,
            WJHttpException {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(
                ByteArrayPool.get(), (int) WJHttpResponse.getContentLength());
        byte[] buffer = null;
        try {
            InputStream in = WJHttpResponse.getContentStream();
            if (in == null) {
                throw new WJHttpException("server error");
            }
            buffer = ByteArrayPool.get().getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
//                entity.consumeContent();
                WJHttpResponse.getContentStream().close();
            } catch (IOException e) {
                WJLoger.debug("Error occured when calling consumingContent");
            }
            ByteArrayPool.get().returnBuf(buffer);
            bytes.close();
        }
    }
}
