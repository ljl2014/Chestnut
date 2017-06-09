package cn.xxxl.chestnut.cookies;

import java.util.ArrayList;
import java.util.List;

import cn.xxxl.chestnut.utils.CUCheck;
import okhttp3.Cookie;
import xiaofei.library.datastorage.annotation.ClassId;
import xiaofei.library.datastorage.annotation.ObjectId;

/**
 * @author Leon
 * @since 1.0.0
 */
@ClassId("$CCO")
public class CookiesEntity {

    @ObjectId
    private String host;
    private List<Cookie> cookies;

    public CookiesEntity(String host, List<Cookie> cookies) {
        this.host = host;
        this.cookies = cookies;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public void addCookie(Cookie cookie) {
        if (cookies == null)
            cookies = new ArrayList<>();
        cookies.add(cookie);
    }

    public void removeCookie(Cookie cookie) {
        if (!CUCheck.cCollection(cookies))
            return;
        cookies.remove(cookie);
    }

    public boolean isCookieExists(Cookie cookie) {
        if (!CUCheck.cCollection(cookies))
            return false;
        return cookies.contains(cookie);
    }
}

