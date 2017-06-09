package cn.xxxl.chestnut.interceptor;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.xxxl.chestnut.utils.CUCheck;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;


/**
 * @author Leon
 * @since 1.0.0
 */
public class LoggingInterceptor implements Interceptor {

    private final String TAG;
    private final boolean isLog;
    private final Mode MODE;
    private final int LEVEL;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;

    private static final int JSON_INDENT = 4;
    private static final int MAX_LONG_SIZE = 148;
    private static final String N = "\n";
    private static final String T = "\t";

    private static final String BORDER_CHAR = "║ ";
    private static final String TOP_BORDER =
            "╔═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════";
    private static final String DIVIDER_BORDER =
            "║─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────";
    private static final String BOTTOM_BORDER =
            "╚═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════";

    public LoggingInterceptor() {
        this(null, true, null, 0);
    }

    /**
     * @param log isLog
     */
    public LoggingInterceptor(boolean log) {
        this(null, log, null, 0);
    }

    /**
     * @param log  isLog
     * @param mode {NONE, ALL, HEADERS, BODY}
     */
    public LoggingInterceptor(boolean log, @Nullable Mode mode) {
        this(null, log, mode, 0);
    }

    /**
     * @param tag   TAG
     * @param log   isLog
     * @param mode  {NONE, ALL, HEADERS, BODY}
     * @param level {Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR}
     */
    public LoggingInterceptor(@Nullable String tag, boolean log, @Nullable Mode mode, int level) {
        TAG = CUCheck.cString(tag) ? tag : "Chestnut";
        isLog = log;
        MODE = mode == null ? Mode.ALL : mode;
        LEVEL = level >= 2 && level <= 6 ? level : Log.DEBUG;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!isLog || MODE == Mode.NONE)
            return chain.proceed(request);

        logRequest(request);
        String URL = request.url().toString();

        long startTime = System.nanoTime();
        Response response = chain.proceed(request);

        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        return logResponse(URL, duration, response);
    }

    private void logRequest(Request request) {
        if (request == null)
            return;

        RequestBody requestBody = request.body();

        MediaType contentType = null;
        if (requestBody != null)
            contentType = requestBody.contentType();

        String subtype = null;
        if (contentType != null)
            subtype = contentType.subtype();

        if (subtype != null && (subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("plain")
                || subtype.contains("html")
                || subtype.contains("form")))
            printRequest(request, true);
        else
            printRequest(request, false);
    }

    private Response logResponse(String URL, long duration, Response response) {
        ResponseBody responseBody = response.body();

        MediaType contentType = null;
        if (responseBody != null)
            contentType = responseBody.contentType();

        String subtype = null;
        if (contentType != null)
            subtype = contentType.subtype();

        if (subtype != null && (subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("plain")
                || subtype.contains("html")
                || subtype.contains("form")))
            return printResponse(URL, duration, response, true);
        else
            return printResponse(URL, duration, response, false);
    }

    private void printRequest(Request request, boolean isJson) {
        String header = request.headers().toString();
        boolean isLogHeaders = MODE == Mode.HEADERS || MODE == Mode.ALL;
        boolean isLogBody = MODE == Mode.BODY || MODE == Mode.ALL;

        logContent("TYPE: Request" + DOUBLE_SEPARATOR +
                "URL: " + request.url() + DOUBLE_SEPARATOR +
                "METHOD: " + request.method() + LINE_SEPARATOR +
                (isEmpty(header) ? "" :
                        isLogHeaders ? LINE_SEPARATOR + "HEADERS:" + LINE_SEPARATOR + getHeaders
                                (header) : "") +
                (!isLogBody ? "" :
                        isJson ? LINE_SEPARATOR + "BODY:" + request.body().contentType().subtype
                                () + LINE_SEPARATOR + bodyToString(request) :
                                LINE_SEPARATOR + "BODY: RequestBody maybe none or a file!" +
                                        " Omitted!"));

    }

    private Response printResponse(String url, long duration, Response response, boolean isJson) {
        String header = response.headers().toString();
        boolean isLogHeaders = MODE == Mode.HEADERS || MODE == Mode.ALL;
        boolean isLogBody = MODE == Mode.BODY || MODE == Mode.ALL;

        String content = "";
        if (isJson)
            try {
                content = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

        logContent("TYPE: Response" + DOUBLE_SEPARATOR +
                "URL: " + url + DOUBLE_SEPARATOR +
                "STATUS: " + response.code() + LINE_SEPARATOR +
                (isEmpty(header) ? "" :
                        isLogHeaders ? LINE_SEPARATOR + "HEADERS:" + LINE_SEPARATOR + getHeaders
                                (header) : "") +
                (!isLogBody ? "" :
                        isJson ? LINE_SEPARATOR + "BODY:" + LINE_SEPARATOR + getBodyJson
                                (content) :
                                LINE_SEPARATOR + "BODY: ResponseBody maybe none or a file! " +
                                        "Omitted!") +
                DOUBLE_SEPARATOR + "DURATION: " + duration + "ms");

        return isJson ? response.newBuilder().body(ResponseBody.create(response.body()
                .contentType(), content)).build() : response;
    }

    private boolean isEmpty(String line) {
        return !CUCheck.cString(line) || N.equals(line) || T.equals(line) || !CUCheck.cString
                (line.trim());
    }

    private String getHeaders(String header) {
        String[] headers = header.split(LINE_SEPARATOR);
        StringBuilder builder = new StringBuilder();
        for (String item : headers) {
            builder.append("- ").append(item).append(LINE_SEPARATOR);
        }
        return builder.toString();
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            if (copy.body() == null)
                return "";
            copy.body().writeTo(buffer);
            return getBodyJson(buffer.readUtf8());
        } catch (IOException e) {
            return "Exception:" + e.getMessage();
        }
    }

    private String getBodyJson(String json) {
        String body;
        try {
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                body = jsonObject.toString(JSON_INDENT);
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                body = jsonArray.toString(JSON_INDENT);
            } else if (json.contains("&")) {
                body = json.replace("&", N);
            } else {
                body = json;
            }
        } catch (JSONException e) {
            body = "Parse JSON error." + N +
                    "JSON string:" + json + N +
                    "Exception:" + e.getMessage();
        }
        return body;
    }

    private void logContent(String content) {
        log(TOP_BORDER);
        logLines(content.split(LINE_SEPARATOR));
        log(BOTTOM_BORDER);
    }

    private void logLines(String[] lines) {
        for (String line : lines) {
            int lineLength = line.length();
            if (lineLength == 0)
                log(DIVIDER_BORDER);
            else
                for (int i = 0; i <= lineLength / MAX_LONG_SIZE; i++) {
                    int start = i * MAX_LONG_SIZE;
                    int end = (i + 1) * MAX_LONG_SIZE;
                    end = end > line.length() ? line.length() : end;
                    log(BORDER_CHAR + line.substring(start, end));
                }
        }
    }

    private void log(String msg) {
        switch (LEVEL) {
            case Log.DEBUG:
                Log.d(TAG, msg);
                break;
            case Log.INFO:
                Log.i(TAG, msg);
                break;
            case Log.WARN:
                Log.w(TAG, msg);
                break;
            case Log.ERROR:
                Log.e(TAG, msg);
                break;
            default:
                Log.v(TAG, msg);
                break;
        }
    }

    public enum Mode {
        /**
         * 不显示
         */
        NONE,
        /**
         * 全显示
         */
        ALL,
        /**
         * 只显示请求/响应头
         */
        HEADERS,
        /**
         * 只显示请求/响应体
         */
        BODY
    }
}
