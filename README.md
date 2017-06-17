# Chestnut

基于Okhttp+Retrofit封装的网络框架，完美结合RxJava
> 由于时间匆忙，部分功能未能完整测试<br>
> 故不建议用于实际生产

## 功能实现
- [x] 支持 HEAD / GET / POST / BODY 常规请求
- [x] 支持单一文件、多文件、多文件多参数混合上传及上传进度回调
- [x] 支持多线程下载、断点续传、后台下载及下载进度回调
- [x] 支持格式化Log
- [x] 支持多种缓存模式
- [x] 支持cookie自动管理
- [x] 支持HTTPS访问，单向/双向认证
- [x] 支持链式调用

## 下一步计划
- [ ] 持续优化精简代码，适当重构部分模块
- [ ] 支持webSocket
- [ ] 更改为使用Kotlin编写

## 使用方式
### 准备工作
添加依赖<br>
1.Add it in your root build.gradle at the end of repositories:
```groovy
    allprojects {
    	repositories {
    		...
    		maven { url 'https://jitpack.io' }
    	}
    }
```
2.Add the dependency [![Chestnut](https://jitpack.io/v/YTxx/Chestnut.svg)](https://jitpack.io/#YTxx/Chestnut)
```groovy
    dependencies {
    	        compile 'com.github.YTxx:Chestnut:1.1.1'
    	}
```

配置权限
```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
```
> 注意: **Android 6.0+** 使用**下载**功能前必须申请运行时权限

### 初始化
```java
// 请于Application的onCreate()中完成初始化
// 简易模式
Chestnut.init(new ChestnutClient.Builder(this)
                .setBaseUrl("http://rapapi.org/xxxxx/")
                .build());
                
// 自定义模式
Chestnut.init(new ChestnutClient.Builder(this)
                // 设置基础url
                .setBaseUrl("http://rapapi.org/xxxxx/")
                // 设置连接超时时间（默认：15s）
                .setConnectTimeout(15)
                // 设置读取超时时间（默认：15s）
                .setReadTimeout(15)
                // 设置写入超时时间（默认：15s）
                .setWriteTimeout(1, TimeUnit.MINUTES)
                // 设置CookieJar
                .setCookieJar(new ChestnutCookieJar())
                // 设置最大连接数（默认：15）
                .setMaxConnections(15)
                // 设置连接存活时长（默认：5min)
                .setAliveDuration(5)
                // 设置缓存文件夹（默认：getExternalCacheDir()）
                .setCacheFile(getExternalCacheDir())
                // 设置缓存空间大小（默认：10MB）
                .setCacheSize(10 * CUFormat.MB)
                // 设置是否缓存（默认：false）
                .isCache(false)
                // 设置默认缓存模式（默认：CacheMode.DEFAULT）
                .setDefaultCacheMode(CacheMode.DEFAULT)
                // 设置默认缓存时间（默认：60s)
                .setDefaultCacheTime(60)
                // 是否打印log（默认：false）
                .isLog(true)
                // 设置LogTag（默认：Chestnut）
                .setLogTag("Chestnut")
                // 设置LogMode（默认：ALL）
                .setLogMode(LoggingInterceptor.Mode.ALL)
                // 设置LogLevel（默认：Log.DEBUG）
                .setLogLevel(Log.DEBUG)
                //https不认证
                .setCertificates()
                // https单向认证（证书请置于Assets目录中）
                //      .setSingleCertificates("xxxxx.cer")
                // https双向认证（证书请置于Assets目录中）
                //      .setDoubleCertificates("xxxxx.bks", "password", "xxxxx.cer")
                // 设置https的域名匹配规则（注意：使用不当会导致https握手失败）
                .setHostnameVerifier(HttpsUtil.DefaultHostnameVerifier)
                // 添加Interceptor拦截器（注意：无需额外添加Log拦截器）
                //      .addInterceptor()
                // 添加公共请求头（可选）
                .addInterceptor(new AddHeadersIntercepter(getHeaders()))
                // 根据响应头返回请求头（如：token）（可选）
                .addInterceptor(new UpdateHeaderIntercepter("token"))
                // 添加NetworkInterceptor网络拦截器
                //      .addNetworkInterceptor()
                // 添加Converter转换器（注意：无需额外添加Json转换器）
                //      .addConverterFactory()
                // 添加CallAdapter转换器（注意：无需额外添加RxJava2CallAdapterFactory）
                //      .addCallAdapterFactory()
                .build(),
                //下载客户端设置（可选）
                new ChestnutDownloadClient.Builder(this)
                //设置最大连接数（默认：5）
                .setMaxConnectCount(5)
                //设置最大重试次数（默认：3）
                .setMaxRetryCount(3)
                //设置最大任务数（默认：3）
                .setMaxTaskCount(3)
                //设置默认下载路径（默认：getExternalFilesDir(Context.DOWNLOAD_SERVICE)）
                .setDefaultDownloadFile(getExternalFilesDir(Context.DOWNLOAD_SERVICE))
                .build());
```
### 常规请求
- HEAD
- GET
- POST
- BODY
- UPLOAD

通过获取与各请求方法相对应的`XXXProcess`进行详细请求参数设置<br>
![](https://github.com/YTxx/Chestnut/raw/master/pic/process.jpg)

e.g.
```java
//返回Disposable方便手动管理RxJava
Disposable disposable = Chestnut.get("url", Entity.class)
                .addParameter("phone", 110)
                .request()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Entity>() {
                    @Override
                    public void accept(@NonNull Entity entity) throws Exception {
                        //doSomething();
                    }
                });
                
Chestnut.post("url", Entity.class)
                .addHeaders(getHeaders())
                .addParameters(getMap())
                .requestMain()
                //支持RxLifecycle简化管理
                .compose(this.<Entity>bindToLifecycle())
                //支持自定义Observer，可以做一些返回结果全局判断
                //ChestnutObserver仅作为样例
                .subscribe(new ChestnutObserver<>(new ChestnutCallback<Entity>() {
                    @Override
                    public void succeed(Entity entity) {
                        //doSomething();
                    }

                    @Override
                    public void failed(int code, Entity entity) {
                        //doSomething();
                    }
                }));
```

### 缓存策略
本框架封装了**五**种缓存策略（**CacheMode**）：
- `NOCACHE`：不使用缓存
- `DEFAULT`：HTTP协议默认缓存规则(304)
- `REQUESTFIRST`：网络请求优先，网络请求失败，则读取本地缓存
- `CACHEFIRST`：本地缓存优先，缓存不存在／失效，则发起网络请求
- `CACHEANDREQUEST`：先读取本地缓存，后发起网络请求

缓存仅支持GET/POST/BODY三种请求方式
1. 可在初始化中设置默认缓存参数（模式、缓存时间）
2. 可为每个请求设置独立缓存条件（初始化不开启默认缓存下仍可使用）

通过获取与各请求方法相对应的`XXXCacheProcess`进行详细请求参数设置<br>
![](https://github.com/YTxx/Chestnut/raw/master/pic/cacheprocess.png)

e.g.
```java
Chestnut.getWithCache("url", Entity.class)
                .setCacheMode(CacheMode.CACHEFIRST)
                .setCacheTime(10)
                .request()
                .compose(this.<Entity>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Entity>() {
                    @Override
                    public void accept(@NonNull Entity entity) throws Exception {
                        //doSomething();
                    }
                });
```

### 下载
下载模块思路参考了[RxDownload](https://github.com/ssseasonnn/RxDownload)，感谢原作者所作贡献！

**DownloadProgress** 下载进度
1. 封装了部分常用表达
2. 包含下载状态

#### 常规下载
用于轻量下载，不具备后台下载能力
> Chestnut.download() 直接返回的是 Observable`<DownloadProgress>`

```java
Chestnut.download("url")
// Chestnut.download("url","Demo.apk")  （可对下载文件重命名）
                .compose(this.<DownloadProgress>bindToLifecycle())
                //.observeOn(AndroidSchedulers.mainThread());  （已预设，无需再设置）
                .subscribe(new Consumer<DownloadProgress>() {
                    @Override
                    public void accept(@NonNull DownloadProgress progress) throws Exception {
                        // doSomething();    
                        // e.g. show(progress.getFormatSizePercent());
                    }
                });
```

> 若需要终止下载，取消订阅即可
```java
    Disposable disposable = Chestnut.download("url").subscribe();
    CURx.dispose(this.disposable);
```
#### 下载服务
使用Service进行下载, 具备后台下载能力

> 下载与获取下载进度分离，可在任意需要的地方获取下载进度

开启下载
```java
    Chestnut.serviceDownload("url").subscribe();
```
暂停下载
```java
    Chestnut.pauseServiceDownload("url").subscribe();
```

删除下载
```java
    // boolean - 是否同时删除文件
    Chestnut.deleteServiceDownload("url", false).subscribe();
```

获取下载进度
```java
Chestnut.getDownloadProgress("url")
                .compose(this.<DownloadProgress>bindToLifecycle())
                .subscribe(new Consumer<DownloadProgress>() {
                    @Override
                    public void accept(@NonNull DownloadProgress progress) throws Exception {
                        //doSomething();
                    }
                });
```

### 自定义
#### 常规请求
```java
    //用法同Retrofit
    Test test = Chestnut.getRetrofit().create(Test.class);
```
#### 缓存请求
> 在需要使用缓存的请求参数中添加`@Header(CacheManager.CACHEINFO) String cacheInfo`

```java
    private interface Test {
        @GET
        Observable<TestEntity> test(@Url String url,
                                    @Header(CacheManager.CACHEINFO) String cacheInfo);
    }
```

> 设置缓存参数信息CacheInfo

```java
    CacheInfo cacheInfo = Chestnut.getCacheInfoBuilder("url")
            .setCacheMode(CacheMode.CACHEANDREQUEST)
            .setCacheTime(3, TimeUnit.MINUTES)
            .isSafe(true)
            .build();
```

> 发送请求

```java
    Test test = Chestnut.getRetrofit().create(Test.class);
    Chestnut.cache(test.test("url", cacheInfo.toString()), cacheInfo, Entity.class)
            .compose(this.<Entity>bindToLifecycle())
            .subscribe(new Consumer<Entity>() {
                @Override
                public void accept(@NonNull Entity entity) throws Exception {
                    //doSomething();
                }
            });
```
## Libraries
- [RxJava2](https://github.com/ReactiveX/RxJava) : RxJava – Reactive Extensions for the JVM – a library for composing asynchronous and event-based programs using observable sequences for the Java VM.
- [RxAndroid](https://github.com/ReactiveX/RxAndroid) : RxJava bindings for Android.
- [Okhttp3](https://github.com/square/okhttp) : An HTTP+HTTP/2 client for Android and Java applications.
- [Okio](https://github.com/square/okio) : A modern I/O API for Java.
- [Retrofit](https://github.com/square/retrofit) : Type-safe HTTP client for Android and Java by Square, Inc.
- [fastjson](https://github.com/alibaba/fastjson) :  A fast JSON parser/generator for Java.
- [AndroidDataStorage](https://github.com/Xiaofei-it/AndroidDataStorage) : An easy-to-use and high-performance library for storing data in the Android system.
- [ComparatorGenerator](https://github.com/Xiaofei-it/ComparatorGenerator) : An easy-to-use helper class for generating a comparator for a specified class. Useful when sorting the instances of the specified class.

## 参考项目
- [OkGo](https://github.com/jeasonlzy/okhttp-OkGo) : 一个基于okhttp的标准RESTful风格的网络框架
- [RxDownload](https://github.com/ssseasonnn/RxDownload) : Multi-thread download tool based on RxJava.
 

## License

    Copyright (C) 2016 wshk729@163.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
