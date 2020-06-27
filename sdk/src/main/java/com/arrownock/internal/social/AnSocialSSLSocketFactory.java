package com.arrownock.internal.social;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class AnSocialSSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLSv1");
    private static KeyManagerFactory kmf;
    private static TrustManagerFactory tmf;

    public AnSocialSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        // sslContext.init(null, new TrustManager[] { tm }, null);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
            UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }

    public static KeyStore createTrustStore(final String caCrtBase64, final String clientCrtBase64,
            final String clientKeyBase64, final String password, final String keyStoreType) throws IOException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException,
            KeyManagementException {

        InputStream caCrt = new ByteArrayInputStream(ANBase64.decode(caCrtBase64));
        InputStream clientCrt = new ByteArrayInputStream(ANBase64.decode(clientCrtBase64));
        InputStream clientKey = new ByteArrayInputStream(ANBase64.decode(clientKeyBase64));

        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMReader reader = new PEMReader(new InputStreamReader(caCrt));
        X509Certificate caCert = (X509Certificate) reader.readObject();
        reader.close();

        // load client certificate
        reader = new PEMReader(new InputStreamReader(clientCrt));
        X509Certificate cert = (X509Certificate) reader.readObject();
        reader.close();

        // load client private key
        reader = new PEMReader(new InputStreamReader(clientKey), new PasswordFinder() {
            public char[] getPassword() {
                return password.toCharArray();
            }
        });
        KeyPair key = (KeyPair) reader.readObject();
        reader.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(keyStoreType);
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                new java.security.cert.Certificate[] { cert });
        kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        return ks;

    }
}
