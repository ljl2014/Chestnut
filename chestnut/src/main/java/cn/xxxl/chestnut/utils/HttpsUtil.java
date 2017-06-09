package cn.xxxl.chestnut.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author Leon
 * @since 1.0.0
 */
public class HttpsUtil {
    public static class SSLParams {
        public SSLSocketFactory sSLSocketFactory;
        public X509TrustManager trustManager;
    }

    public static SSLParams getSslSocketFactory(Context context,
                                                String bksFileName, String password,
                                                String... certificates) {
        SSLParams sslParams = new SSLParams();
        try {
            KeyManager[] keyManagers = prepareKeyManager(getInputStream(context, bksFileName),
                    password);
            TrustManager[] trustManagers = prepareTrustManager(getInputStreams(context,
                    certificates));
            X509TrustManager manager;
            if (trustManagers != null && trustManagers.length > 0)
                manager = chooseTrustManager(trustManagers);
            else
                manager = DefaultTrustManager;

            // 创建TLS类型的SSLContext对象
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // 用上面得到的trustManagers初始化SSLContext，这样sslContext就会信任keyStore中的证书
            // 第一个参数是授权的密钥管理器，用来授权验证，比如授权自签名的证书验证
            // 第二个参数是被授权的证书管理器，用来验证服务器端的证书
            sslContext.init(keyManagers, new TrustManager[]{manager}, null);
            // 通过sslContext获取SSLSocketFactory对象
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = manager;
            return sslParams;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (KeyManagementException e) {
            throw new AssertionError(e);
        }
    }

    public static InputStream getInputStream(Context context, String fileName) {
        if (context == null || fileName == null)
            return null;
        try {
            return context.getAssets().open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream[] getInputStreams(Context context, String... fileNames) {
        if (context == null || fileNames == null || fileNames.length <= 0)
            return null;
        try {
            InputStream[] inputStreams = new InputStream[fileNames.length];
            for (int i = 0; i < fileNames.length; i++) {
                inputStreams[i] = context.getAssets().open(fileNames[i]);
            }
            return inputStreams;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static KeyManager[] prepareKeyManager(InputStream bksStream, String password) {
        try {
            if (bksStream == null || password == null)
                return null;
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksStream, password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
            kmf.init(clientKeyStore, password.toCharArray());
            return kmf.getKeyManagers();
        } catch (Exception e) {
            CUL.e(e);
        }
        return null;
    }

    private static TrustManager[] prepareTrustManager(InputStream[] certificates) {
        if (certificates == null || certificates.length <= 0)
            return null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            // 创建默认类型的KeyStore存储信任证书
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certStream : certificates) {
                String certificateAlias = Integer.toString(index++);
                // 证书工厂根据证书文件的流生成证书 cert
                Certificate cert = certificateFactory.generateCertificate(certStream);
                // 将 cert 作为可信证书放入keyStore
                keyStore.setCertificateEntry(certificateAlias, cert);
                try {
                    if (certStream != null)
                        certStream.close();
                } catch (IOException e) {
                    CUL.e(e);
                }
            }
            //创建一个默认类型的TrustManagerFactory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
            //用keyStore实例初始化TrustManagerFactory，使tmf信任keyStore中的证书
            tmf.init(keyStore);
            //通过tmf获取TrustManager数组，TrustManager也会信任keyStore中的证书
            return tmf.getTrustManagers();
        } catch (Exception e) {
            CUL.e(e);
        }
        return null;
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return DefaultTrustManager;
    }

    /**
     * 默认：客户端不对证书做任何检验
     * 注意：该方式存在严重安全隐患
     */
    public static X509TrustManager DefaultTrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws
                CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws
                CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    };

    /**
     * 默认：接受所有主机名
     * <p>
     * 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。
     * 策略可以是基于证书的或依赖于其他验证方案。
     * 当验证 URL 主机名使用的默认规则失败时使用这些回调。
     * 如果主机名是可接受的，则返回 true。
     */
    public static HostnameVerifier DefaultHostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
