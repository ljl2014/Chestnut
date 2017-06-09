package cn.xxxl.chestnut.converter.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;

import java.io.IOException;

import cn.xxxl.chestnut.utils.CMediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * @author Leon
 * @since 1.0.0
 */
public final class JsonRequestBodyConverter<T> implements Converter<T, RequestBody> {

    private final SerializeConfig serializeConfig;

    public JsonRequestBodyConverter(SerializeConfig serializeConfig) {
        this.serializeConfig = serializeConfig;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
        return RequestBody.create(CMediaType.JSON, JSON.toJSONBytes(value, serializeConfig));
    }
}