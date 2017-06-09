package cn.xxxl.chestnut.cookies;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @author Leon
 * @since 1.0.0
 */
public class ChestnutCookieJar implements CookieJar {

    protected CookieStore store;

    public ChestnutCookieJar() {
        this.store = new CookieStore();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        store.saveCookies(url, cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return store.loadCookies(url);
    }

    public CookieStore getStore() {
        return store;
    }
}
