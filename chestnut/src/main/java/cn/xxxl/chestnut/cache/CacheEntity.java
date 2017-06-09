package cn.xxxl.chestnut.cache;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import xiaofei.library.comparatorgenerator.Criterion;
import xiaofei.library.comparatorgenerator.Order;
import xiaofei.library.datastorage.annotation.ClassId;
import xiaofei.library.datastorage.annotation.ObjectId;

/**
 * @author Leon
 * @since 1.0.0
 */
@ClassId("$CCA")
public class CacheEntity {

    @ObjectId
    private String id;

    private String url;
    private String groupKey;
    private String ownKey;

    @Criterion(priority = 1, order = Order.DESCENDING)
    private long localExpire;
    private boolean isSafe;

    private int code;
    private Protocol protocol;
    private Headers headers;
    private MediaType mediaType;
    private String content;

    public CacheEntity(String url, String groupKey, String ownKey,
                       long cacheTime, TimeUnit timeUnit, boolean isSafe) {
        this.id = CacheManager.getId(url, groupKey, ownKey);
        this.url = url;
        this.groupKey = groupKey;
        this.ownKey = ownKey;
        this.localExpire = CacheManager.getLocalExpire(cacheTime, timeUnit);
        this.isSafe = isSafe;
    }

    public CacheEntity(String id, long localExpire, boolean isSafe) {
        this.id = id;
        String[] ss = CacheManager.parseId(id);
        this.url = ss[0];
        this.groupKey = ss[1];
        this.ownKey = ss[2];
        this.localExpire = localExpire;
        this.isSafe = isSafe;
    }

    public void parseResponse(Response response) throws IOException {
        this.code = response.code();
        this.protocol = response.protocol();
        this.headers = response.headers();
        this.mediaType = response.body().contentType();
        this.content = response.body().string();
    }

    public Response createResponse(Request request) {
        return new Response.Builder()
                .request(request)
                .code(code)
                .protocol(protocol)
                .headers(headers)
                .body(createResponseBody())
                .build();
    }

    public ResponseBody createResponseBody(){
        return ResponseBody.create(mediaType, content);
    }

    public boolean isExpire() {
        return localExpire <= System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public String getOwnKey() {
        return ownKey;
    }

    public boolean isSafe() {
        return isSafe;
    }

    @Override
    public String toString() {
        return "CacheEntity{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", groupKey='" + groupKey + '\'' +
                ", ownKey='" + ownKey + '\'' +
                ", localExpire=" + localExpire +
                ", isSafe=" + isSafe +
                ", code=" + code +
                ", protocol=" + protocol +
                ", headers=" + headers +
                ", mediaType=" + mediaType +
                ", content='" + content + '\'' +
                '}';
    }
}
