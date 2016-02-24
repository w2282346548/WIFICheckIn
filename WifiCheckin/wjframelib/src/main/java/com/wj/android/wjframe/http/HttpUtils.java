
package com.wj.android.wjframe.http;

import android.text.TextUtils;

import com.wj.android.wjframe.utils.WJLoger;



import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Http请求工具类
 *
 *
 */
public class HttpUtils {

    public static byte[] responseToBytes(WJHttpResponse response)
            throws IOException, WJHttpException {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(
                ByteArrayPool.get(), (int) response.getContentLength());
        byte[] buffer = null;
        try {
            InputStream in = response.getContentStream();
            if (isGzipContent(response) && !(in instanceof GZIPInputStream)) {
                in = new GZIPInputStream(in);
            }

            if (in == null) {
                throw new WJHttpException("服务器连接异常");
            }

            buffer = ByteArrayPool.get().getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources by
                // "consuming the content".
//                entity.consumeContent();
                response.getContentStream().close();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the
                // entity in
                // an invalid state.
                WJLoger.debug("Error occured when calling consumingContent");
            }
            ByteArrayPool.get().returnBuf(buffer);
            bytes.close();
        }
    }

    /**
     * Returns the charset specified in the Content-Type of this header.
     */
    public static String getCharset(WJHttpResponse response) {
        Map<String, String> header = response.getHeaders();
        if (header != null) {
            String contentType = header.get("Content-Type");
            if (!TextUtils.isEmpty(contentType)) {
                String[] params = contentType.split(";");
                for (int i = 1; i < params.length; i++) {
                    String[] pair = params[i].trim().split("=");
                    if (pair.length == 2) {
                        if (pair[0].equals("charset")) {
                            return pair[1];
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String getHeader(WJHttpResponse response, String key) {
        return response.getHeaders().get(key);
    }

    public static boolean isSupportRange(WJHttpResponse response) {
        if (TextUtils.equals(getHeader(response, "Accept-Ranges"), "bytes")) {
            return true;
        }
        String value = getHeader(response, "Content-Range");
        return value != null && value.startsWith("bytes");
    }

    public static boolean isGzipContent(WJHttpResponse response) {
        return TextUtils
                .equals(getHeader(response, "Content-Encoding"), "gzip");
    }

}
