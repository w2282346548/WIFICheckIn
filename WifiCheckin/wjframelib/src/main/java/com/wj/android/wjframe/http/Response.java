
package com.wj.android.wjframe.http;

import java.util.Map;

/**
 * Http响应封装类，包含了本次响应的全部信息
 *
 */
public class Response<T> {
    /**
     * Http响应的类型
     */
    public final T result;

    /**
     * 本次响应的缓存对象，如果失败则为null
     */
    public final Cache.Entry cacheEntry;

    public final WJHttpException error;

    public final Map<String, String> headers;

    public boolean isSuccess() {
        return error == null;
    }

    private Response(T result, Map<String, String> headers,
                     Cache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.error = null;
        this.headers = headers;
    }

    private Response(WJHttpException error) {
        this.result = null;
        this.cacheEntry = null;
        this.headers = null;
        this.error = error;
    }

    /**
     * 返回一个成功的HttpRespond
     *
     * @param result     Http响应的类型
     * @param cacheEntry 缓存对象
     */
    public static <T> Response<T> success(T result,
                                          Map<String, String> headers, Cache.Entry cacheEntry) {
        return new Response<T>(result, headers, cacheEntry);
    }

    /**
     * 返回一个失败的HttpRespond
     *
     * @param error 失败原因
     */
    public static <T> Response<T> error(WJHttpException error) {
        return new Response<T>(error);
    }
}
