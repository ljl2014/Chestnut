package cn.xxxl.chestnut.cache;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.xxxl.chestnut.Chestnut;
import cn.xxxl.chestnut.utils.CUNet;
import cn.xxxl.chestnut.utils.DataStorage;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import xiaofei.library.datastorage.util.Condition;

/**
 * @author Leon
 * @since 1.0.0
 */
public class CacheManager {

    public static final String CACHEINFO = "Chestnut_Cache_Info";
    public static final int CACHE_ERROR = 900;

    public static Response getNoCacheResponse(Interceptor.Chain chain) throws IOException {
        return chain.proceed(restoreRequest(chain.request()));
    }

    public static Response getDefaultResponse(Interceptor.Chain chain) throws IOException {
        Request request = restoreRequest(chain.request());
        if (!CUNet.isNetworkAvailable(Chestnut.getContext())) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }
        Response response = chain.proceed(request);
        Response responseLatest;
        if (CUNet.isNetworkAvailable(Chestnut.getContext())) {
            int maxAge = 60; //有网失效一分钟
            responseLatest = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
        } else {
            int maxStale = 60 * 60 * 6; // 没网失效6小时
            responseLatest = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
        return responseLatest;
    }

    public static Response getRequestFirstResponse(Interceptor.Chain chain, CacheInfo cacheInfo)
            throws IOException {
        Request request = restoreRequest(chain.request());
        if (!CUNet.isNetworkAvailable(Chestnut.getContext())) {
            CacheEntity entity = getCache(cacheInfo);
            if (entity != null)
                return getCacheSucceedResponse(request, entity);
            else
                return getCacheFailedResponse(request);
        }
        Response response = chain.proceed(request);
        if (response.isSuccessful()) {
            response = saveCache(response, cacheInfo);
        } else {
            CacheEntity entity = getCache(cacheInfo);
            if (entity != null)
                response = getCacheSucceedResponse(request, entity);
            else
                response = getCacheFailedResponse(request);
        }
        return response;
    }

    public static Response getCacheFirstResponse(Interceptor.Chain chain, CacheInfo cacheInfo)
            throws IOException {
        Request request = restoreRequest(chain.request());
        Response response;
        CacheEntity entity = getCache(cacheInfo);
        if (entity != null)
            response = getCacheSucceedResponse(request, entity);
        else {
            response = saveCache(chain.proceed(request), cacheInfo);
        }
        return response;
    }

    public static Response getCacheAndRequestResponse(Interceptor.Chain chain, CacheInfo
            cacheInfo) throws IOException {
        Request request = restoreRequest(chain.request());
        Response response = chain.proceed(request);
        return saveCache(response, cacheInfo);
    }

    public static String getId(String url, String groupKey, String ownKey) {
        StringBuilder builder = new StringBuilder();
        builder.append(url).append("$c$c$c").append(groupKey == null ? "" : groupKey)
                .append("$c$c$c").append(ownKey == null ? "" : ownKey);
        return builder.toString();
    }

    public static String[] parseId(String id) {
        return id.split("\\$c\\$c\\$c", 3);
    }

    public static long getLocalExpire(long cacheTime, TimeUnit timeUnit) {
        return System.currentTimeMillis() + timeUnit.toMillis(cacheTime);
    }


    public static Response saveCache(Response response, CacheInfo cacheInfo) throws IOException {
        removeExpireCache();
        CacheEntity cacheEntity = new CacheEntity(cacheInfo.getId(),
                cacheInfo.getLocalExpire(), cacheInfo.isSafe());
        cacheEntity.parseResponse(response);
        DataStorage.storeOrUpdate(cacheEntity);
        return response.newBuilder().body(cacheEntity.createResponseBody()).build();
    }

    public static CacheEntity getCache(CacheInfo cacheInfo) {
        removeExpireCache();
        if (DataStorage.contains(CacheEntity.class, cacheInfo.getId())) {
            CacheEntity entity = DataStorage.load(CacheEntity.class, cacheInfo.getId());
            if (CUNet.isNetworkAvailable(Chestnut.getContext()) && entity.isExpire()) {
                removeCache(cacheInfo);
                return null;
            } else
                return entity;
        } else
            return null;
    }

    public static void removeCache(CacheInfo cacheInfo) {
        if (DataStorage.contains(CacheEntity.class, cacheInfo.getId()))
            DataStorage.delete(CacheEntity.class, cacheInfo.getId());
    }

    private static void removeExpireCache() {
        if (CUNet.isNetworkAvailable(Chestnut.getContext())) {
            DataStorage.delete(CacheEntity.class, new Condition<CacheEntity>() {
                @Override
                public boolean satisfy(CacheEntity o) {
                    return o.isExpire();
                }
            });
        }
    }

    private static Response getCacheSucceedResponse(Request request, CacheEntity cacheEntity) {
        return cacheEntity.createResponse(request);
    }

    private static Response getCacheFailedResponse(Request request) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(CACHE_ERROR)
                .build();
    }

    private static Request restoreRequest(Request request) {
        return request.newBuilder()
                .removeHeader(CACHEINFO)
                .build();
    }
}
