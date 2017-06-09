package cn.xxxl.chestnut.cache;

/**
 * @author Leon
 * @since 1.0.0
 */
public enum CacheMode {
    /**
     * 不使用缓存
     */
    NOCACHE,

    /**
     * HTTP协议默认缓存规则(304)
     */
    DEFAULT,

    /**
     * 网络请求优先，网络请求失败，则读取本地缓存
     */
    REQUESTFIRST,

    /**
     * 本地缓存优先，缓存不存在／失效，则发起网络请求
     */
    CACHEFIRST,

    /**
     * 先读取本地缓存，后发起网络请求
     */
    CACHEANDREQUEST,
}
