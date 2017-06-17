package cn.xxxl.chestnut.client;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import cn.xxxl.chestnut.Chestnut;
import cn.xxxl.chestnut.cache.CacheEntity;
import cn.xxxl.chestnut.cache.CacheInfo;
import cn.xxxl.chestnut.cache.CacheManager;
import cn.xxxl.chestnut.cache.CacheMode;
import cn.xxxl.chestnut.cache.ChestnutCacheInterceptor;
import cn.xxxl.chestnut.converter.json.JsonConverterFactory;
import cn.xxxl.chestnut.interceptor.LoggingInterceptor;
import cn.xxxl.chestnut.upload.UploadFile;
import cn.xxxl.chestnut.upload.UploadProgressListener;
import cn.xxxl.chestnut.upload.UploadRequestBody;
import cn.xxxl.chestnut.utils.CUException;
import cn.xxxl.chestnut.utils.CUFormat;
import cn.xxxl.chestnut.utils.CUNet;
import cn.xxxl.chestnut.utils.DataStorage;
import cn.xxxl.chestnut.utils.HttpsUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * @author Leon
 * @since 1.0.0
 */
public class ChestnutClient {

    public static final String TAG = "ChestnutClient";

    private Context app;
    private String baseUrl;
    private Retrofit.Builder retrofitBuilder;
    private Retrofit retrofit;
    private OkHttpClient.Builder okhttpBuilder;
    private OkHttpClient okHttpClient;

    private long connectTimeout;
    private long readTimeout;
    private long writeTimeout;

    private CookieJar cookieJar;
    private int maxConnections;
    private long aliveDuration;

    private boolean isLog;
    private String logTag;
    private LoggingInterceptor.Mode logMode;
    private int logLevel;

    private boolean isCache;
    private File cacheFile;
    private long cacheSize;
    private CacheMode defaultCacheMode;
    private long defaultCacheTime;

    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;
    private HostnameVerifier hostnameVerifier;

    private final List<Interceptor> interceptors;
    private final List<Interceptor> networkinterceptors;

    private final List<Converter.Factory> converterFactories;
    private final List<CallAdapter.Factory> calladapterFactories;


    private ChestnutServer server;
    private Map<String, Object> noBody = new HashMap<>();
    private Map<String, String> noHeader = new HashMap<>();

    public ChestnutClient(Application app) {
        this(new Builder(app));
    }

    private ChestnutClient(Builder builder) {
        this.app = builder.app;
        this.baseUrl = builder.baseUrl;
        this.retrofitBuilder = builder.retrofitBuilder;
        this.retrofit = builder.retrofit;
        this.okhttpBuilder = builder.okhttpBuilder;
        this.okHttpClient = builder.okHttpClient;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.cookieJar = builder.cookieJar;
        this.maxConnections = builder.maxConnections;
        this.aliveDuration = builder.aliveDuration;
        this.isLog = builder.isLog;
        this.logTag = builder.logTag;
        this.logMode = builder.logMode;
        this.logLevel = builder.logLevel;
        this.isCache = builder.isCache;
        this.cacheFile = builder.cacheFile;
        this.cacheSize = builder.cacheSize;
        this.defaultCacheMode = builder.defaultCacheMode;
        this.defaultCacheTime = builder.defaultCacheTime;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.trustManager = builder.trustManager;
        this.hostnameVerifier = builder.hostnameVerifier;
        this.interceptors = builder.interceptors;
        this.networkinterceptors = builder.networkinterceptors;
        this.converterFactories = builder.converterFactories;
        this.calladapterFactories = builder.calladapterFactories;
        this.server = this.retrofit.create(ChestnutServer.class);
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public Context getContext() {
        return app;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public boolean isLog() {
        return isLog;
    }

    public static final class Builder {

        private final int DEFAULT_TIMEOUT = 15;
        private final int DEFAULT_MAXCONNECTIONS = 15;
        private final long DEFAULT_ALIVEDURATION = 5;
        private final long DEFAULT_CACHESIZE = 10 * CUFormat.MB;
        private final long DEFAULT_CACHETIME = 60;

        private Context app;
        private String baseUrl;
        private Retrofit.Builder retrofitBuilder;
        private Retrofit retrofit;
        private OkHttpClient.Builder okhttpBuilder;
        private OkHttpClient okHttpClient;

        private long connectTimeout = DEFAULT_TIMEOUT;
        private long readTimeout = DEFAULT_TIMEOUT;
        private long writeTimeout = DEFAULT_TIMEOUT;

        private CookieJar cookieJar;
        private int maxConnections = DEFAULT_MAXCONNECTIONS;
        private long aliveDuration = DEFAULT_ALIVEDURATION;

        private boolean isLog = false;
        private String logTag = Chestnut.TAG;
        private LoggingInterceptor.Mode logMode = LoggingInterceptor.Mode.ALL;
        private int logLevel = Log.DEBUG;

        private boolean isCache = false;
        private File cacheFile;
        private long cacheSize = DEFAULT_CACHESIZE;
        private CacheMode defaultCacheMode = CacheMode.DEFAULT;
        private long defaultCacheTime = DEFAULT_CACHETIME;

        private SSLSocketFactory sslSocketFactory;
        private X509TrustManager trustManager;
        private HostnameVerifier hostnameVerifier;

        private final List<Interceptor> interceptors = new ArrayList<>();
        private final List<Interceptor> networkinterceptors = new ArrayList<>();

        private final List<Converter.Factory> converterFactories = new ArrayList<>();
        private final List<CallAdapter.Factory> calladapterFactories = new ArrayList<>();

        public Builder(Application app) {
            DataStorage.init(app);      //初始化数据库管理
            this.app = app;
            this.cacheFile = this.app.getExternalCacheDir();
        }

        Builder(ChestnutClient client) {
            this.app = client.app;
            this.baseUrl = client.baseUrl;
            this.retrofitBuilder = client.retrofitBuilder;
            this.retrofit = client.retrofit;
            this.okhttpBuilder = client.okhttpBuilder;
            this.okHttpClient = client.okHttpClient;
            this.connectTimeout = client.connectTimeout;
            this.readTimeout = client.readTimeout;
            this.writeTimeout = client.writeTimeout;
            this.cookieJar = client.cookieJar;
            this.maxConnections = client.maxConnections;
            this.aliveDuration = client.aliveDuration;
            this.isLog = client.isLog;
            this.logTag = client.logTag;
            this.logMode = client.logMode;
            this.logLevel = client.logLevel;
            this.isCache = client.isCache;
            this.cacheFile = client.cacheFile;
            this.cacheSize = client.cacheSize;
            this.defaultCacheMode = client.defaultCacheMode;
            this.defaultCacheTime = client.defaultCacheTime;
            this.sslSocketFactory = client.sslSocketFactory;
            this.trustManager = client.trustManager;
            this.hostnameVerifier = client.hostnameVerifier;
            this.interceptors.addAll(client.interceptors);
            this.networkinterceptors.addAll(client.networkinterceptors);
            this.converterFactories.addAll(client.converterFactories);
            this.calladapterFactories.addAll(client.calladapterFactories);
        }

        /**
         * 设置基础url
         *
         * @param baseUrl 基础url
         */
        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = CUException.cNull(baseUrl, "baseUrl == null");
            return this;
        }

        /**
         * 设置连接超时时间
         *
         * @param timeout 超时时间（默认：15,单位：秒）
         */
        public Builder setConnectTimeout(long timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        /**
         * 设置连接超时时间
         *
         * @param timeout  超时时间（默认：15,单位：秒）
         * @param timeUnit 时间单位
         */
        public Builder setConnectTimeout(long timeout, TimeUnit timeUnit) {
            this.connectTimeout = timeUnit.toSeconds(timeout);
            return this;
        }

        /**
         * 设置读取超时时间
         *
         * @param timeout 超时时间（默认：15,单位：秒）
         */
        public Builder setReadTimeout(long timeout) {
            this.readTimeout = timeout;
            return this;
        }

        /**
         * 设置读取超时时间
         *
         * @param timeout  超时时间（默认：15,单位：秒）
         * @param timeUnit 时间单位
         */
        public Builder setReadTimeout(long timeout, TimeUnit timeUnit) {
            this.readTimeout = timeUnit.toSeconds(timeout);
            return this;
        }

        /**
         * 设置写入超时时间
         *
         * @param timeout 超时时间（默认：15,单位：秒）
         */
        public Builder setWriteTimeout(long timeout) {
            this.writeTimeout = timeout;
            return this;
        }

        /**
         * 设置写入超时时间
         *
         * @param timeout  超时时间（默认：15,单位：秒）
         * @param timeUnit 时间单位
         */
        public Builder setWriteTimeout(long timeout, TimeUnit timeUnit) {
            this.writeTimeout = timeUnit.toSeconds(timeout);
            return this;
        }

        /**
         * 设置CookieJar<p>
         * - 直接使用　ChestnutCookieJar<p>
         * - 自定义　CookieJar
         *
         * @param cookieJar CookieJar
         */
        public Builder setCookieJar(CookieJar cookieJar) {
            this.cookieJar = CUException.cNull(cookieJar, "CookieJar == null");
            return this;
        }

        /**
         * 设置最大连接数
         *
         * @param maxConnections 　最大连接数（默认：15）
         */
        public Builder setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * 设置连接存活时长
         *
         * @param aliveDuration 　连接存活时长（默认：5，单位：分）
         */
        public Builder setAliveDuration(long aliveDuration) {
            this.aliveDuration = aliveDuration;
            return this;
        }

        /**
         * 设置连接存活时长
         *
         * @param aliveDuration 　连接存活时长（默认：5，单位：分）
         * @param TimeUnit      时间单位
         */
        public Builder setAliveDuration(long aliveDuration, TimeUnit TimeUnit) {
            this.aliveDuration = TimeUnit.toMinutes(aliveDuration);
            return this;
        }

        /**
         * 设置缓存文件夹
         *
         * @param cacheFile 　缓存文件夹（默认：getExternalCacheDir()）
         */
        public Builder setCacheFile(File cacheFile) {
            this.cacheFile = cacheFile;
            return this;
        }

        /**
         * 设置缓存空间大小
         *
         * @param cacheSize 　缓存空间大小（默认：10MB）
         */
        public Builder setCacheSize(long cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        /**
         * 设置是否缓存
         *
         * @param isCache 　是否缓存（默认：false）
         */
        public Builder isCache(boolean isCache) {
            this.isCache = isCache;
            return this;
        }

        /**
         * 设置默认缓存模式
         *
         * @param cacheMode 　缓存模式（默认：CacheMode.DEFAULT）
         */
        public Builder setDefaultCacheMode(CacheMode cacheMode) {
            this.defaultCacheMode = cacheMode;
            return this;
        }

        /**
         * 设置默认缓存时间
         *
         * @param cacheTime 　缓存时间（默认：60,单位：秒）
         */
        public Builder setDefaultCacheTime(long cacheTime) {
            this.defaultCacheTime = cacheTime;
            return this;
        }

        /**
         * 设置默认缓存时间
         *
         * @param cacheTime 　缓存时间（默认：60,单位：秒）
         * @param timeUnit  时间单位
         */
        public Builder setDefaultCacheTime(long cacheTime, TimeUnit timeUnit) {
            this.defaultCacheTime = timeUnit.toSeconds(cacheTime);
            return this;
        }

        /**
         * 是否打印log
         *
         * @param isLog 　是否打印（默认：false）
         */
        public Builder isLog(boolean isLog) {
            this.isLog = isLog;
            return this;
        }

        /**
         * 设置LogTag
         *
         * @param logTag LogTag（默认：Chestnut）
         */
        public Builder setLogTag(String logTag) {
            this.logTag = logTag;
            return this;
        }

        /**
         * 设置LogMode<p>
         * {NONE, ALL, HEADERS, BODY}
         *
         * @param logMode logMode（默认：ALL）
         */
        public Builder setLogMode(LoggingInterceptor.Mode logMode) {
            this.logMode = logMode;
            return this;
        }

        /**
         * 设置LogLevel<p>
         * {Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR}
         *
         * @param logLevel logLevel（默认：Log.DEBUG）
         */
        public Builder setLogLevel(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        /**
         * https不认证，即certificates留空<p>
         * 注意：不认证会信任所有证书,不安全！有风险！
         */
        public Builder setCertificates() {
            setSingleCertificates();
            return this;
        }

        /**
         * https单向认证（证书请置于Assets目录中）
         *
         * @param certificates 服务器公钥证书名
         */
        public Builder setSingleCertificates(String... certificates) {
            setDoubleCertificates(null, null, certificates);
            return this;
        }

        /**
         * https双向认证（证书请置于Assets目录中）
         *
         * @param bksFileName  bks证书文件名
         * @param password     bks证书密码
         * @param certificates 服务器公钥证书名
         */
        public Builder setDoubleCertificates(String bksFileName, String password, String...
                certificates) {
            HttpsUtil.SSLParams sslParams = HttpsUtil.getSslSocketFactory(app,
                    bksFileName, password, certificates);
            sslSocketFactory = sslParams.sSLSocketFactory;
            trustManager = sslParams.trustManager;
            return this;
        }

        /**
         * 设置https的域名匹配规则<p>
         * 注意：使用不当会导致https握手失败
         *
         * @param hostnameVerifier 　HostnameVerifier（默认：接受所有主机名HttpsUtil.DefaultHostnameVerifier）
         */
        public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * 添加Interceptor拦截器<p>
         * 默认添加以下Interceptor，无需另行添加<p>
         * - LoggingInterceptor<p>
         * - ChestnutCacheInterceptor
         *
         * @param interceptor Interceptor
         */
        public Builder addInterceptor(Interceptor interceptor) {
            this.interceptors.add(CUException.cNull(interceptor, "interceptor == null"));
            return this;
        }

        /**
         * 添加NetworkInterceptor网络拦截器
         *
         * @param interceptor NetworkInterceptor
         */
        public Builder addNetworkInterceptor(Interceptor interceptor) {
            this.networkinterceptors.add(CUException.cNull(interceptor, "networkinterceptor " +
                    "== null"));
            return this;
        }

        /**
         * 添加Converter转换器
         * 默认添加以下Converter，无需另行添加<p>
         * - JsonConverterFactory 默认使用FastJson解析<p>
         *
         * @param factory Converter.Factory
         */
        public Builder addConverterFactory(Converter.Factory factory) {
            this.converterFactories.add(CUException.cNull(factory, "converterfactory == " +
                    "null"));
            return this;
        }

        /**
         * 添加CallAdapter转换器
         * 默认添加以下CallAdapter，无需另行添加<p>
         * - RxJava2CallAdapterFactory 内部使用RxJava2实现<p>
         *
         * @param factory CallAdapter.Factory
         */
        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            this.calladapterFactories.add(CUException.cNull(factory, "calladapterfactory == " +
                    "null"));
            return this;
        }

        /**
         * 生成ChestnutClient客户端
         */
        public ChestnutClient build() {
            if (baseUrl == null)
                throw new IllegalStateException("BaseUrl is required");

            //OkHttp
            okhttpBuilder = new OkHttpClient.Builder();

            okhttpBuilder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
            okhttpBuilder.readTimeout(readTimeout, TimeUnit.SECONDS);
            okhttpBuilder.writeTimeout(writeTimeout, TimeUnit.SECONDS);

            if (cookieJar != null)
                okhttpBuilder.cookieJar(cookieJar);
            okhttpBuilder.connectionPool(new ConnectionPool(maxConnections, aliveDuration,
                    TimeUnit.MINUTES));
            okhttpBuilder.cache(new Cache(cacheFile, cacheSize));

            if (interceptors != null && interceptors.size() != 0) {
                for (Interceptor interceptor : interceptors) {
                    if (interceptor instanceof LoggingInterceptor
                            || interceptor instanceof ChestnutCacheInterceptor)
                        continue;
                    okhttpBuilder.addInterceptor(interceptor);
                }
            }

            //缓存管理
            okhttpBuilder.addInterceptor(new ChestnutCacheInterceptor(isCache));
            //打印Log
            okhttpBuilder.addInterceptor(new LoggingInterceptor(logTag, isLog, logMode, logLevel));

            if (networkinterceptors != null && networkinterceptors.size() != 0)
                for (Interceptor interceptor : networkinterceptors)
                    okhttpBuilder.addInterceptor(interceptor);

            //https
            if (trustManager != null)
                okhttpBuilder.sslSocketFactory(sslSocketFactory, trustManager);
            if (hostnameVerifier != null)
                okhttpBuilder.hostnameVerifier(hostnameVerifier);

            //Retrofit
            retrofitBuilder = new Retrofit.Builder();
            retrofitBuilder.baseUrl(baseUrl);

            if (converterFactories != null && converterFactories.size() != 0) {
                for (Converter.Factory factory : converterFactories) {
                    if (factory instanceof JsonConverterFactory)
                        continue;
                    retrofitBuilder.addConverterFactory(factory);
                }
            }

            //默认使用FastJson解析
            retrofitBuilder.addConverterFactory(JsonConverterFactory.create());

            if (calladapterFactories != null && calladapterFactories.size() != 0) {
                for (CallAdapter.Factory factory : calladapterFactories) {
                    if (factory instanceof RxJava2CallAdapterFactory)
                        continue;
                    retrofitBuilder.addCallAdapterFactory(factory);
                }
            }

            retrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());

            okHttpClient = okhttpBuilder.build();
            retrofitBuilder.client(okHttpClient);
            retrofit = retrofitBuilder.build();

            return new ChestnutClient(this);
        }
    }

    //#################### HEAD ####################
    public HeadProcess head(String url) {
        return new HeadProcess(url);
    }

    public class HeadProcess {
        private String url;
        private Map<String, String> headers = noHeader;

        private HeadProcess(String url) {
            this.url = url;
        }

        /**
         * 添加请求头参数
         *
         * @param key   　key
         * @param value 　请求头值
         */
        public HeadProcess addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加请求头参数
         *
         * @param headers 　请求头Map
         */
        public HeadProcess addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 执行网络请求
         */
        public Observable<Headers> request() {
            return server.head(url, headers)
                    .subscribeOn(Schedulers.io());
        }

        /**
         * 执行网络请求,observeOn(AndroidSchedulers.mainThread())
         */
        public Observable<Headers> requestMain() {
            return request()
                    .observeOn(AndroidSchedulers.mainThread());
        }

    }

    //#################### GET ####################
    public <T> GetProcess<T> get(String url, Class<T> tClass) {
        return new GetProcess<>(url, tClass);
    }

    public class GetProcess<T> {

        private final String url;
        private final Class<T> tClass;
        private Map<String, String> headers = noHeader;
        private Map<String, Object> parameters = noBody;

        private GetProcess(String url, Class<T> tClass) {
            this.url = url;
            this.tClass = tClass;
        }

        /**
         * 添加请求头参数
         *
         * @param key   　key
         * @param value 　请求头值
         */
        public GetProcess<T> addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加请求头参数
         *
         * @param headers 　请求头Map
         */
        public GetProcess<T> addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param key   　key
         * @param value 　请求参数
         */
        public GetProcess<T> addParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param parameters 　请求参数Map
         */
        public GetProcess<T> addParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * 执行网络请求
         */
        public Observable<T> request() {
            return server.get(url, headers, parameters)
                    .subscribeOn(Schedulers.io())
                    .compose(getJsonTransformer(tClass));
        }

        /**
         * 执行网络请求,observeOn(AndroidSchedulers.mainThread())
         */
        public Observable<T> requestMain() {
            return request()
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public <T> GetCacheProcess<T> getWithCache(String url, Class<T> tClass) {
        return new GetCacheProcess<>(url, tClass);
    }

    public class GetCacheProcess<T> {

        private final String url;
        private final Class<T> tClass;
        private Map<String, String> headers = noHeader;
        private Map<String, Object> parameters = noBody;
        private CacheMode mode = defaultCacheMode;
        private String groupKey = null;
        private String ownKey = null;
        private long cacheTime = defaultCacheTime;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private boolean isSafe = false;

        private GetCacheProcess(String url, Class<T> tClass) {
            this.url = url;
            this.tClass = tClass;
        }

        /**
         * 添加请求头参数
         *
         * @param key   　key
         * @param value 　请求头值
         */
        public GetCacheProcess<T> addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加请求头参数
         *
         * @param headers 　请求头Map
         */
        public GetCacheProcess<T> addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param key   　key
         * @param value 　请求参数
         */
        public GetCacheProcess<T> addParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param parameters 　请求参数Map
         */
        public GetCacheProcess<T> addParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * 设置缓存模式
         *
         * @param mode 缓存模式
         */
        public GetCacheProcess<T> setCacheMode(CacheMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * 设置缓存分组key
         *
         * @param groupKey 缓存分组key
         */
        public GetCacheProcess<T> setGroupKe(String groupKey) {
            this.groupKey = groupKey;
            return this;
        }

        /**
         * 设置缓存独立key
         *
         * @param ownKey 缓存独立key
         */
        public GetCacheProcess<T> setOwnKey(String ownKey) {
            this.ownKey = ownKey;
            return this;
        }

        /**
         * 设置缓存时间
         *
         * @param cacheTime 缓存时间（单位：秒）
         */
        public GetCacheProcess<T> setCacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        /**
         * 设置缓存时间
         *
         * @param cacheTime 缓存时间
         * @param timeUnit  时间单位
         */
        public GetCacheProcess<T> setCacheTime(long cacheTime, TimeUnit timeUnit) {
            this.cacheTime = timeUnit.toSeconds(cacheTime);
            return this;
        }

        /**
         * 设置缓存安全模式（永久保存）
         *
         * @param isSafe 缓存安全模式
         */
        public GetCacheProcess<T> isSafe(boolean isSafe) {
            this.isSafe = isSafe;
            return this;
        }

        /**
         * 执行网络请求
         */
        public Observable<T> request() {
            CacheInfo cacheInfo = getCacheInfo(url, mode, groupKey, ownKey, cacheTime, timeUnit,
                    isSafe);
            Observable<ResponseBody> observable = server.getWithCache(url,
                    cacheInfo.toString(), headers, parameters);
            return getCacheObservable(observable, cacheInfo)
                    .subscribeOn(Schedulers.io())
                    .compose(getJsonTransformer(tClass));
        }

        /**
         * 执行网络请求,observeOn(AndroidSchedulers.mainThread())
         */
        public Observable<T> requestMain() {
            return request()
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    //#################### POST ####################
    public <T> PostProcess<T> post(String url, Class<T> tClass) {
        return new PostProcess<>(url, tClass);
    }

    public class PostProcess<T> {

        private final String url;
        private final Class<T> tClass;
        private Map<String, String> headers = noHeader;
        private Map<String, Object> parameters = noBody;

        private PostProcess(String url, Class<T> tClass) {
            this.url = url;
            this.tClass = tClass;
        }

        /**
         * 添加请求头参数
         *
         * @param key   　key
         * @param value 　请求头值
         */
        public PostProcess<T> addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加请求头参数
         *
         * @param headers 　请求头Map
         */
        public PostProcess<T> addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param key   　key
         * @param value 　请求参数
         */
        public PostProcess<T> addParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param parameters 　请求参数Map
         */
        public PostProcess<T> addParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * 执行网络请求
         */
        public Observable<T> request() {
            return server.post(url, headers, parameters)
                    .subscribeOn(Schedulers.io())
                    .compose(getJsonTransformer(tClass));
        }

        /**
         * 执行网络请求,observeOn(AndroidSchedulers.mainThread())
         */
        public Observable<T> requestMain() {
            return request()
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public <T> PostCacheProcess<T> postWithCache(String url, Class<T> tClass) {
        return new PostCacheProcess<>(url, tClass);
    }

    public class PostCacheProcess<T> {

        private final String url;
        private final Class<T> tClass;
        private Map<String, String> headers = noHeader;
        private Map<String, Object> parameters = noBody;
        private CacheMode mode = defaultCacheMode;
        private String groupKey = null;
        private String ownKey = null;
        private long cacheTime = defaultCacheTime;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private boolean isSafe = false;

        private PostCacheProcess(String url, Class<T> tClass) {
            this.url = url;
            this.tClass = tClass;
        }

        /**
         * 添加请求头参数
         *
         * @param key   　key
         * @param value 　请求头值
         */
        public PostCacheProcess<T> addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加请求头参数
         *
         * @param headers 　请求头Map
         */
        public PostCacheProcess<T> addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param key   　key
         * @param value 　请求参数
         */
        public PostCacheProcess<T> addParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param parameters 　请求参数Map
         */
        public PostCacheProcess<T> addParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * 设置缓存模式
         *
         * @param mode 缓存模式
         */
        public PostCacheProcess<T> setCacheMode(CacheMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * 设置缓存分组key
         *
         * @param groupKey 缓存分组key
         */
        public PostCacheProcess<T> setGroupKe(String groupKey) {
            this.groupKey = groupKey;
            return this;
        }

        /**
         * 设置缓存独立key
         *
         * @param ownKey 缓存独立key
         */
        public PostCacheProcess<T> setOwnKey(String ownKey) {
            this.ownKey = ownKey;
            return this;
        }

        /**
         * 设置缓存时间
         *
         * @param cacheTime 缓存时间（单位：秒）
         */
        public PostCacheProcess<T> setCacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        /**
         * 设置缓存时间
         *
         * @param cacheTime 缓存时间
         * @param timeUnit  时间单位
         */
        public PostCacheProcess<T> setCacheTime(long cacheTime, TimeUnit timeUnit) {
            this.cacheTime = timeUnit.toSeconds(cacheTime);
            return this;
        }

        /**
         * 设置缓存安全模式（永久保存）
         *
         * @param isSafe 缓存安全模式
         */
        public PostCacheProcess<T> isSafe(boolean isSafe) {
            this.isSafe = isSafe;
            return this;
        }

        /**
         * 执行网络请求
         */
        public Observable<T> request() {
            CacheInfo cacheInfo = getCacheInfo(url, mode, groupKey, ownKey, cacheTime, timeUnit,
                    isSafe);
            Observable<ResponseBody> observable = server.postWithCache(url,
                    cacheInfo.toString(), headers, parameters);
            return getCacheObservable(observable, cacheInfo)
                    .subscribeOn(Schedulers.io())
                    .compose(getJsonTransformer(tClass));
        }

        /**
         * 执行网络请求,observeOn(AndroidSchedulers.mainThread())
         */
        public Observable<T> requestMain() {
            return request()
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    //#################### BODY ####################
    public <T> BodyProcess<T> body(String url, Class<T> tClass) {
        return new BodyProcess<>(url, tClass);
    }

    public class BodyProcess<T> {
        private final String url;
        private final Class<T> tClass;
        private Map<String, String> headers = noHeader;
        private Object parameter = null;

        private BodyProcess(String url, Class<T> tClass) {
            this.url = url;
            this.tClass = tClass;
        }

        /**
         * 添加请求头参数
         *
         * @param key   　key
         * @param value 　请求头值
         */
        public BodyProcess<T> addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加请求头参数
         *
         * @param headers 　请求头Map
         */
        public BodyProcess<T> addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 添加Body参数
         *
         * @param parameter 　Body参数
         */
        public BodyProcess<T> addBodyParameter(Object parameter) {
            this.parameter = parameter;
            return this;
        }

        /**
         * 执行网络请求
         */
        public Observable<T> request() {
            CUException.cNull(parameter, "Parameter == null");
            return server.body(url, headers, parameter)
                    .subscribeOn(Schedulers.io())
                    .compose(getJsonTransformer(tClass));
        }

        /**
         * 执行网络请求,observeOn(AndroidSchedulers.mainThread())
         */
        public Observable<T> requestMain() {
            return request()
                    .observeOn(AndroidSchedulers.mainThread());
        }

    }

    public <T> BodyCacheProcess<T> bodyWithCache(String url, Class<T> tClass) {
        return new BodyCacheProcess<>(url, tClass);
    }

    public class BodyCacheProcess<T> {

        private final String url;
        private final Class<T> tClass;
        private Map<String, String> headers = noHeader;
        private Object parameter = null;
        private CacheMode mode = defaultCacheMode;
        private String groupKey = null;
        private String ownKey = null;
        private long cacheTime = defaultCacheTime;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private boolean isSafe = false;

        private BodyCacheProcess(String url, Class<T> tClass) {
            this.url = url;
            this.tClass = tClass;
        }

        /**
         * 添加请求头参数
         *
         * @param key   　key
         * @param value 　请求头值
         */
        public BodyCacheProcess<T> addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加请求头参数
         *
         * @param headers 　请求头Map
         */
        public BodyCacheProcess<T> addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 添加Body参数
         *
         * @param parameter 　Body参数
         */
        public BodyCacheProcess<T> addBodyParameter(Object parameter) {
            this.parameter = parameter;
            return this;
        }

        /**
         * 设置缓存模式
         *
         * @param mode 缓存模式
         */
        public BodyCacheProcess<T> setCacheMode(CacheMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * 设置缓存分组key
         *
         * @param groupKey 缓存分组key
         */
        public BodyCacheProcess<T> setGroupKe(String groupKey) {
            this.groupKey = groupKey;
            return this;
        }

        /**
         * 设置缓存独立key
         *
         * @param ownKey 缓存独立key
         */
        public BodyCacheProcess<T> setOwnKey(String ownKey) {
            this.ownKey = ownKey;
            return this;
        }

        /**
         * 设置缓存时间
         *
         * @param cacheTime 缓存时间（单位：秒）
         */
        public BodyCacheProcess<T> setCacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        /**
         * 设置缓存时间
         *
         * @param cacheTime 缓存时间
         * @param timeUnit  时间单位
         */
        public BodyCacheProcess<T> setCacheTime(long cacheTime, TimeUnit timeUnit) {
            this.cacheTime = timeUnit.toSeconds(cacheTime);
            return this;
        }

        /**
         * 设置缓存安全模式（永久保存）
         *
         * @param isSafe 缓存安全模式
         */
        public BodyCacheProcess<T> isSafe(boolean isSafe) {
            this.isSafe = isSafe;
            return this;
        }

        /**
         * 执行网络请求
         */
        public Observable<T> request() {
            CUException.cNull(parameter, "Parameter == null");
            CacheInfo cacheInfo = getCacheInfo(url, mode, groupKey, ownKey, cacheTime, timeUnit,
                    isSafe);
            Observable<ResponseBody> observable = server.bodyWithCache(url,
                    cacheInfo.toString(), headers, parameter);
            return getCacheObservable(observable, cacheInfo)
                    .subscribeOn(Schedulers.io())
                    .compose(getJsonTransformer(tClass));
        }

        /**
         * 执行网络请求,observeOn(AndroidSchedulers.mainThread())
         */
        public Observable<T> requestMain() {
            return request()
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    //#################### UPLOAD ####################
    public <T> UploadProcess<T> upload(String url, Class<T> tClass) {
        return new UploadProcess<>(url, tClass);
    }

    public class UploadProcess<T> {
        private final String url;
        private final Class<T> tClass;
        private Map<String, String> headers = noHeader;
        private Map<String, Object> parameters = noBody;
        private UploadProgressListener listener = null;

        private UploadProcess(String url, Class<T> tClass) {
            this.url = url;
            this.tClass = tClass;
        }

        /**
         * 添加请求头参数
         *
         * @param key   　key
         * @param value 　请求头值
         */
        public UploadProcess<T> addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加请求头参数
         *
         * @param headers 　请求头Map
         */
        public UploadProcess<T> addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param key   　key
         * @param value 　请求参数
         */
        public UploadProcess<T> addParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        /**
         * 添加请求参数
         *
         * @param parameters 　请求参数Map
         */
        public UploadProcess<T> addParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * 添加参数（上传File）
         *
         * @param key  　key
         * @param name 　上传文件名
         * @param file 　上传文件
         * @param type 　上传文件类型 CMediaType
         */
        public UploadProcess<T> addFileParameter(String key, String name, File file, MediaType
                type) {
            this.parameters.put(key, new UploadFile(type, file, name));
            return this;
        }

        /**
         * 添加参数（上传File）
         *
         * @param key  　key
         * @param file 　上传文件
         * @param type 　上传文件类型 CMediaType
         */
        public UploadProcess<T> addFileParameter(String key, File file, MediaType type) {
            this.parameters.put(key, new UploadFile(type, file));
            return this;
        }

        /**
         * 设置上传进度监听
         *
         * @param listener 　上传进度监听
         */
        public UploadProcess<T> setUploadProgressListener(UploadProgressListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * 执行网络请求
         */
        public Observable<T> request() {
            return server.upload(url, headers, getUploadBody(parameters, listener))
                    .subscribeOn(Schedulers.io())
                    .compose(getJsonTransformer(tClass));
        }

        /**
         * 执行网络请求,observeOn(AndroidSchedulers.mainThread())
         */
        public Observable<T> requestMain() {
            return request()
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    //#################### CACHE ####################

    public <T> Observable<T> cache(Observable<T> originObservable, CacheInfo cacheInfo,
                                   final Class<T> tClass) {
        Observable<T> resultObservable = originObservable;
        if (cacheInfo.getMode() == CacheMode.CACHEANDREQUEST &&
                DataStorage.contains(CacheEntity.class, cacheInfo.getId())) {
            CacheEntity entity = CacheManager.getCache(cacheInfo);
            if (entity != null) {
                if (CUNet.isNetworkAvailable(Chestnut.getContext()) && entity.isExpire())
                    CacheManager.removeCache(cacheInfo);
                else
                    resultObservable = Observable.mergeDelayError(
                            Observable.just(entity.createResponseBody())
                                    .compose(getJsonTransformer(tClass)),
                            originObservable);
            }
        }
        return resultObservable;
    }

    public CacheInfoBuilder getCacheInfoBuilder(String url) {
        return new CacheInfoBuilder(url);
    }

    public class CacheInfoBuilder {

        private final String url;
        private CacheMode mode = defaultCacheMode;
        private String groupKey = null;
        private String ownKey = null;
        private long cacheTime = defaultCacheTime;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private boolean isSafe = false;

        public CacheInfoBuilder(String url) {
            this.url = url;
        }

        /**
         * 设置缓存模式
         *
         * @param mode 缓存模式
         */
        public CacheInfoBuilder setCacheMode(CacheMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * 设置缓存分组key
         *
         * @param groupKey 缓存分组key
         */
        public CacheInfoBuilder setGroupKe(String groupKey) {
            this.groupKey = groupKey;
            return this;
        }

        /**
         * 设置缓存独立key
         *
         * @param ownKey 缓存独立key
         */
        public CacheInfoBuilder setOwnKey(String ownKey) {
            this.ownKey = ownKey;
            return this;
        }

        /**
         * 设置缓存时间
         *
         * @param cacheTime 缓存时间（单位：秒）
         */
        public CacheInfoBuilder setCacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        /**
         * 设置缓存时间
         *
         * @param cacheTime 缓存时间
         * @param timeUnit  时间单位
         */
        public CacheInfoBuilder setCacheTime(long cacheTime, TimeUnit timeUnit) {
            this.cacheTime = timeUnit.toSeconds(cacheTime);
            return this;
        }

        /**
         * 设置缓存安全模式（永久保存）
         *
         * @param isSafe 缓存安全模式
         */
        public CacheInfoBuilder isSafe(boolean isSafe) {
            this.isSafe = isSafe;
            return this;
        }

        public CacheInfo build() {
            return getCacheInfo(url, mode, groupKey, ownKey, cacheTime, timeUnit, isSafe);
        }
    }

    //#################### TOOLS ####################
    private <T> ObservableTransformer<ResponseBody, T> getJsonTransformer(final Class<T> tClass) {
        return new ObservableTransformer<ResponseBody, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<ResponseBody> upstream) {
                return upstream.flatMap(new Function<ResponseBody, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull ResponseBody responseBody)
                            throws Exception {
                        return Observable.just(JSON.parseObject(responseBody.string(),
                                tClass, Feature.AutoCloseSource));
                    }
                })
                        .subscribeOn(Schedulers.computation());
            }
        };
    }

    private Observable<ResponseBody> getCacheObservable(Observable<ResponseBody> observable,
                                                        CacheInfo cacheInfo) {
        Observable<ResponseBody> resultObservable = observable;
        if (cacheInfo.getMode() == CacheMode.CACHEANDREQUEST &&
                DataStorage.contains(CacheEntity.class, cacheInfo.getId())) {
            CacheEntity entity = CacheManager.getCache(cacheInfo);
            if (entity != null) {
                if (CUNet.isNetworkAvailable(Chestnut.getContext()) && entity.isExpire())
                    CacheManager.removeCache(cacheInfo);
                else
                    resultObservable = Observable.mergeDelayError(
                            Observable.just(entity.createResponseBody()), observable);
            }
        }
        return resultObservable;
    }

    private CacheInfo getCacheInfo(@NonNull String url,
                                   CacheMode mode,
                                   String groupKey,
                                   String ownKey,
                                   long cacheTime,
                                   TimeUnit timeUnit,
                                   boolean isSafe) {
        return new CacheInfo(CacheManager.getId(url, groupKey, ownKey),
                CacheManager.getLocalExpire(cacheTime, timeUnit), isSafe, mode);
    }

    private UploadRequestBody getUploadBody(Map<String, Object> parameters,
                                            UploadProgressListener listener) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        Set<String> keySet = parameters.keySet();
        for (String key : keySet) {
            Object param = parameters.get(key);
            if (param instanceof UploadFile) {
                UploadFile uploadFile = (UploadFile) param;
                builder.addFormDataPart(key, uploadFile.getName(), RequestBody.create
                        (uploadFile.getType(), uploadFile.getFile()));
            } else if (param instanceof File) {
                File file = (File) param;
                builder.addFormDataPart(key, file.getName(), RequestBody.create
                        (MediaType.parse("*/*"), file));
            } else
                builder.addFormDataPart(key, param.toString());
        }

        RequestBody requestBody = builder.build();
        return new UploadRequestBody(requestBody, listener);
    }
}
