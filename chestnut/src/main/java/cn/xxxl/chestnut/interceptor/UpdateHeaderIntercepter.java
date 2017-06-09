package cn.xxxl.chestnut.interceptor;

import android.support.annotation.Nullable;

import java.io.IOException;

import cn.xxxl.chestnut.entity.HeaderEntity;
import cn.xxxl.chestnut.utils.CUCheck;
import cn.xxxl.chestnut.utils.DataStorage;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Leon
 * @since 1.0.0
 */
public class UpdateHeaderIntercepter implements Interceptor {

    private final String key;
    private final String anotherkey;
    private String host;
    private String value;

    public UpdateHeaderIntercepter(String key) {
        this(null, key);
    }

    public UpdateHeaderIntercepter(String host, String key) {
        this(host, key, key);
    }

    public UpdateHeaderIntercepter(@Nullable String host, String responsekey, String requestkey) {
        this.host = host;
        this.key = responsekey;
        this.anotherkey = requestkey;
        this.value = getValue();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response;

        if (CUCheck.cString(host)) {
            if (request.url().host().equals(host)) {
                response = chain.proceed(getRequest(request));
                setResponse(response);
            } else {
                response = chain.proceed(request);
            }
        } else {
            response = chain.proceed(getRequest(request));
            setResponse(response);
        }
        return response;
    }

    private Request getRequest(Request request) {
        if (CUCheck.cString(value))
            request = request.newBuilder().addHeader(anotherkey, value).build();
        return request;
    }

    private String getValue() {
        String value = "";
        if (DataStorage.contains(HeaderEntity.class, key))
            value = DataStorage.load(HeaderEntity.class, key).getValue();
        return CUCheck.cString(value) ? value : "";
    }

    private void setResponse(Response response) {
        if (response.header(key) != null)
            setValue(response.header(key));
    }

    private void setValue(String value) {
        this.value = value;
        DataStorage.storeOrUpdate(new HeaderEntity(key, value));
    }
}
