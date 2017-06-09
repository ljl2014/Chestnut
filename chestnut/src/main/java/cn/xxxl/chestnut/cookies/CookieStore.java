package cn.xxxl.chestnut.cookies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cn.xxxl.chestnut.utils.CUCheck;
import cn.xxxl.chestnut.utils.CUTime;
import cn.xxxl.chestnut.utils.DataStorage;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * @author Leon
 * @since 1.0.0
 */
public class CookieStore {

    private final HashMap<String, ConcurrentHashMap<String, Cookie>> map = new HashMap<>();

    public CookieStore() {
        List<CookiesEntity> cookiesList = DataStorage.loadAll(CookiesEntity.class);
        for (CookiesEntity cookies : cookiesList)
            if (CUCheck.cString(cookies.getHost())) {
                map.put(cookies.getHost(), new ConcurrentHashMap<String, Cookie>());
                for (Cookie cookie : cookies.getCookies())
                    map.get(cookies.getHost()).put(cookie.name(), cookie);
            }
    }

    public void saveCookies(HttpUrl url, List<Cookie> cookies) {
        if (!map.containsKey(url.host()))
            map.put(url.host(), new ConcurrentHashMap<String, Cookie>());

        for (Cookie cookie : cookies) {
            if (isCookieExpired(cookie))
                removeCookie(url, cookie);
            else
                saveCookie(url, cookie);
        }
    }

    public void saveCookie(HttpUrl url, Cookie cookie) {
        map.get(url.host()).put(cookie.name(), cookie);

        CookiesEntity cookiesEntity;
        if (DataStorage.contains(CookiesEntity.class, url.host()))
            cookiesEntity = DataStorage.load(CookiesEntity.class, url.host());
        else
            cookiesEntity = new CookiesEntity(url.host(), null);
        cookiesEntity.addCookie(cookie);
        DataStorage.storeOrUpdate(cookiesEntity);
    }

    public List<Cookie> loadCookies(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        if (map.containsKey(url.host())) {
            Collection<Cookie> values = map.get(url.host()).values();
            for (Cookie cookie : values) {
                if (isCookieExpired(cookie))
                    removeCookie(url, cookie);
                else
                    cookies.add(cookie);
            }
        }
        return cookies;
    }

    public void removeCookie(HttpUrl url, Cookie cookie) {
        if (map.containsKey(url.host()) && map.get(url.host()).containsKey(cookie.name())) {
            map.get(url.host()).remove(cookie.name());
            if (map.get(url.host()).size() == 0) {
                map.remove(url.host());

                if (DataStorage.contains(CookiesEntity.class, url.host()))
                    DataStorage.delete(CookiesEntity.class, url.host());
            } else {
                if (DataStorage.contains(CookiesEntity.class, url.host())) {
                    CookiesEntity cookiesEntity = DataStorage.load(CookiesEntity.class, url
                            .host());
                    if (cookiesEntity.isCookieExists(cookie)) {
                        cookiesEntity.removeCookie(cookie);
                        DataStorage.storeOrUpdate(cookiesEntity);
                    }
                }
            }
        }
    }

    public void removeCookie(HttpUrl url) {
        if (map.containsKey(url.host())) {
            map.remove(url.host());

            if (DataStorage.contains(CookiesEntity.class, url.host()))
                DataStorage.delete(CookiesEntity.class, url.host());
        }
    }

    public void removeAllCookie() {
        map.clear();
        DataStorage.deleteAll(CookiesEntity.class);
    }

    public List<Cookie> getCookies(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        if (map.containsKey(url.host()))
            cookies.addAll(map.get(url.host()).values());
        return cookies;
    }

    public List<Cookie> getAllCookies() {
        List<Cookie> cookies = new ArrayList<>();
        for (String key : map.keySet())
            cookies.addAll(map.get(key).values());
        return cookies;
    }

    private boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < CUTime.getCurrentTimeLong();
    }
}
