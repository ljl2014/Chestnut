package cn.xxxl.chestnut.interceptor;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Map;

import cn.xxxl.chestnut.utils.CUCheck;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Leon
 * @since 1.0.0
 */
public class AddHeadersIntercepter implements Interceptor {

    private final Map<String, String> headers;
    private String host;

    public AddHeadersIntercepter(Map<String, String> headers) {
        this(null, headers);
    }

    public AddHeadersIntercepter(@Nullable String host, Map<String, String> headers) {
        this.host = host;
        this.headers = headers;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!CUCheck.cMap(headers))
            return chain.proceed(chain.request());


        Request request = chain.request();
        Response response;
        if (CUCheck.cString(host)) {
            if (request.url().host().equals(host)) {
                response = chain.proceed(getHeadersRequest(request));
            } else {
                response = chain.proceed(request);
            }
        } else {
            response = chain.proceed(getHeadersRequest(request));
        }
        return response;
    }

    private Request getHeadersRequest(Request request) {
        Request.Builder builder = request.newBuilder();
        for (String key : this.headers.keySet()) {
            if (!CUCheck.cString(request.header(key)))
                request.newBuilder().addHeader(key, this.headers.get(key));
        }
        return builder.build();
    }
}
