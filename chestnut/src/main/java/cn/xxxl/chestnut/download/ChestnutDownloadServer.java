package cn.xxxl.chestnut.download;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-22.
 */
public interface ChestnutDownloadServer {

    @GET
    @Streaming
    Flowable<Response<ResponseBody>> download(@Url String url);

    @GET
    @Streaming
    Flowable<Response<ResponseBody>> download(@Url String url,
                                              @Header("Range") String range);

    @HEAD
    Observable<Response<Void>> check(@Url String url);

    @HEAD
    Observable<Response<Void>> checkRange(@Url String url,
                                          @Header("Range") String range);

    @HEAD
    Observable<Response<Void>> checkChange(@Url String url,
                                           @Header("If-Modified-Since") String lastModify);
}
