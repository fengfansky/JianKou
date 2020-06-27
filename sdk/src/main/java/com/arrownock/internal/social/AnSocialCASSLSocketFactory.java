package com.arrownock.internal.social;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.net.SSLCertificateSocketFactory;
import android.os.Build;
import android.util.Log;

public class AnSocialCASSLSocketFactory extends SSLSocketFactory {
	final static String LOG_TAG = "CASSLSocketFactory";
	final static HostnameVerifier hostnameVerifier = SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
	final SSLContext sslContext = SSLContext.getInstance("TLS");

	public AnSocialCASSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		X509TrustManager tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sslContext.init(null, new TrustManager[] { tm }, null);
	}

	public static KeyStore getKeystoreOfCA(InputStream cert) {
		// Load CAs from an InputStream
		InputStream caInput = null;
		Certificate ca = null;
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			caInput = new BufferedInputStream(cert);
			ca = cf.generateCertificate(caInput);
		} catch (CertificateException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (caInput != null) {
					caInput.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Create a KeyStore containing our trusted CAs
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = null;
		try {
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyStore;
	}

	public static KeyStore getKeystore() {
		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return trustStore;
	}

	public static SSLSocketFactory getCASocketFactory() {
		SSLSocketFactory socketFactory;
		try {
			socketFactory = new AnSocialCASSLSocketFactory(getKeystore());
			socketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		} catch (Throwable t) {
			t.printStackTrace();
			socketFactory = SSLSocketFactory.getSocketFactory();
		}
		return socketFactory;
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
		if (autoClose) {
			// we don't need the plainSocket
			socket.close();
		}

		// create and connect SSL socket, but don't do hostname/certificate
		// verification yet
		SSLCertificateSocketFactory sslSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0);
		SSLSocket ssl = (SSLSocket) sslSocketFactory.createSocket(InetAddress.getByName(host), port);

		// enable TLSv1.1/1.2 if available
		// (see https://github.com/rfc2822/davdroid/issues/229)
		ssl.setEnabledProtocols(ssl.getSupportedProtocols());

		// set up SNI before the handshake
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			Log.d(LOG_TAG, "Setting SNI hostname");
			sslSocketFactory.setHostname(ssl, host);
		} else {
			Log.d(LOG_TAG, "No SNI support on Android < 4.2, trying reflection");
			try {
				java.lang.reflect.Method setHostnameMethod = ssl.getClass().getMethod("setHostname", String.class);
				setHostnameMethod.invoke(ssl, host);
			} catch (Exception e) {
				Log.w(LOG_TAG, "SNI not useable", e);
			}
		}

		// verify hostname and certificate
		SSLSession session = ssl.getSession();
		if (!hostnameVerifier.verify(host, session)) {
			throw new SSLPeerUnverifiedException("Cannot verify hostname: " + host);
		}
		//Log.d(LOG_TAG, "Protocol: " + session.getProtocol() + " ; host: " + session.getPeerHost() + " ; " + session.getCipherSuite());
		return ssl;
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}
}
