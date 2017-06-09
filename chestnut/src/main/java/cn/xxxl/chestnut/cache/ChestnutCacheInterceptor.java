package cn.xxxl.chestnut.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

import java.io.IOException;

import cn.xxxl.chestnut.utils.CUCheck;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Leon
 * @since 1.0.0
 */
public class ChestnutCacheInterceptor implements Interceptor {

    private final boolean isCache;

    public ChestnutCacheInterceptor(boolean isCache) {
        this.isCache = isCache;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String info = request.header(CacheManager.CACHEINFO);
        if (CUCheck.cString(info)) {
            CacheInfo cacheInfo = JSON.parseObject(info, CacheInfo.class, Feature.AutoCloseSource);
            switch (cacheInfo.getMode()) {
                case DEFAULT:
                    if (request.method().equals("GET"))
                        return CacheManager.getDefaultResponse(chain);
                    else
                        return CacheManager.getNoCacheResponse(chain);
                case REQUESTFIRST:
                    return CacheManager.getRequestFirstResponse(chain, cacheInfo);
                case CACHEFIRST:
                    return CacheManager.getCacheFirstResponse(chain, cacheInfo);
                case CACHEANDREQUEST:
                    return CacheManager.getCacheAndRequestResponse(chain, cacheInfo);
                default:
                    return CacheManager.getNoCacheResponse(chain);
            }
        } else if (isCache && request.method().equals("GET"))
            return CacheManager.getDefaultResponse(chain);
        else
            return CacheManager.getNoCacheResponse(chain);
    }
}
