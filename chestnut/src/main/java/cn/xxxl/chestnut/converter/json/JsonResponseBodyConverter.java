package cn.xxxl.chestnut.converter.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * @author Leon
 * @since 1.0.0
 */
public final class JsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final Type type;

    public JsonResponseBodyConverter(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        return JSON.parseObject(value.string(), type, Feature.AutoCloseSource);
    }
}