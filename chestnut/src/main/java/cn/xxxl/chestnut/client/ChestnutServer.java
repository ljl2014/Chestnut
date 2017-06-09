package cn.xxxl.chestnut.client;

import java.util.Map;

import cn.xxxl.chestnut.cache.CacheManager;
import cn.xxxl.chestnut.upload.UploadRequestBody;
import io.reactivex.Observable;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * @author Leon
 * @since 1.0.0
 */
public interface ChestnutServer {

    @HEAD
    Observable<Headers> head(@Url String url,
                             @HeaderMap Map<String, String> headers);

    @GET
    Observable<ResponseBody> get(@Url String url,
                                 @HeaderMap Map<String, String> headers,
                                 @QueryMap Map<String, Object> parameters);

    @GET
    Observable<ResponseBody> getWithCache(@Url String url,
                                          @Header(CacheManager.CACHEINFO) String cacheInfo,
                                          @HeaderMap Map<String, String> headers,
                                          @QueryMap Map<String, Object> parameters);

    @POST
    @FormUrlEncoded
    Observable<ResponseBody> post(@Url String url,
                                  @HeaderMap Map<String, String> headers,
                                  @FieldMap Map<String, Object> parameters);

    @POST
    @FormUrlEncoded
    Observable<ResponseBody> postWithCache(@Url String url,
                                           @Header(CacheManager.CACHEINFO) String cacheInfo,
                                           @HeaderMap Map<String, String> headers,
                                           @FieldMap Map<String, Object> parameters);

    @POST
    Observable<ResponseBody> body(@Url String url,
                                  @HeaderMap Map<String, String> headers,
                                  @Body Object parameter);

    @POST
    Observable<ResponseBody> bodyWithCache(@Url String url,
                                           @Header(CacheManager.CACHEINFO) String cacheInfo,
                                           @HeaderMap Map<String, String> headers,
                                           @Body Object parameter);

    @POST
    Observable<ResponseBody> upload(@Url String url,
                                    @HeaderMap Map<String, String> headers,
                                    @Body UploadRequestBody parameter);

}
