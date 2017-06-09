package cn.xxxl.chestnut.converter.json;

import com.alibaba.fastjson.serializer.SerializeConfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import cn.xxxl.chestnut.utils.CUException;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @author Leon
 * @since 1.0.0
 */
public class JsonConverterFactory extends Converter.Factory {

    private final SerializeConfig serializeConfig;

    private JsonConverterFactory(SerializeConfig serializeConfig) {
        this.serializeConfig = CUException.cNull(serializeConfig, "serializeConfig == null");
    }

    public static JsonConverterFactory create() {
        return create(SerializeConfig.getGlobalInstance());
    }

    public static JsonConverterFactory create(SerializeConfig serializeConfig) {
        return new JsonConverterFactory(serializeConfig);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        return new JsonResponseBodyConverter<>(type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[]
            parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new JsonRequestBodyConverter<>(serializeConfig);
    }
}