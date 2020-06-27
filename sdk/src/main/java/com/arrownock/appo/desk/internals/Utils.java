package com.arrownock.appo.desk.internals;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.arrownock.im.AnIM;
import com.arrownock.internal.util.Constants;
import com.arrownock.internal.util.DefaultHostnameVerifier;
import com.arrownock.push.PahoSocketFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

public class Utils {
    public final static String PREF_IM_API = "imAPI";
    private final static String API_BASE_URL = Constants.ARROWNOCK_API_URL;
    private final static HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();

    public static String sendRequest(Context context, String path, String method, boolean secureConnection,
            Map<String, String> params) {
        HttpURLConnection urlConnection = null;
        try {
            // parameters
            String queryString = getQuery(params);

            String reqPath = path;
            if ("GET".equals(method)) {
                reqPath += "?" + queryString;
            }

            if (secureConnection) {
                URL url = new URL(Constants.HTTPS + getAPIHost(context) + "/" + reqPath);
                urlConnection = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection) urlConnection).setHostnameVerifier(hostnameVerifier);
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(
                        getServerCert(), getClientCert(), getClientKey(), "", "BKS"));
            } else {
                URL url = new URL(Constants.HTTP + getAPIHost(context) + "/" + reqPath);
                urlConnection = (HttpURLConnection) url.openConnection();
            }

            if ("GET".equals(method)) {
                urlConnection.connect();
            } else {
                urlConnection.setRequestMethod(method);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                OutputStream out = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(queryString);
                writer.close();
                out.close();
            }

            try {
                int statusCode = urlConnection.getResponseCode();
                if (statusCode == 200) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String s = convertStreamToString(in);
                    return s;
                } else {
                    InputStream es = new BufferedInputStream(urlConnection.getErrorStream());
                    String s = convertStreamToString(es);
                    return s;
                }
            } catch (IOException e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static String getAPIHost(Context context) {
        String api = getFromLocalStorage(context, PREF_IM_API);
        return "".equals(api) ? API_BASE_URL : api;
    }

    private static String getFromLocalStorage(final Context androidContext, final String key) {
        SharedPreferences pref = androidContext.getSharedPreferences(AnIM.class.getName(), Context.MODE_PRIVATE);
        return pref.getString(key, "");
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static String getQuery(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                if (result.length() > 0) {
                    result.append('&');
                }
                result.append(URLEncoder.encode(e.getKey(), "UTF-8")).append('=')
                        .append(URLEncoder.encode(e.getValue(), "UTF-8"));
            }
        }
        return result.toString();
    }

    private static String getServerCert() {
        return Constants.SSL_SERVER_CERT;
    }

    private static String getClientCert() {
        return Constants.SSL_CLIENT_CERT;
    }

    private static String getClientKey() {
        return Constants.SSL_CLIENT_KEY;
    }

    public static byte[] compressImageDataByOriginalImage(byte[] data, float size) {
        size = size * 1024f;
        if (data.length <= size) {
            return data;
        }
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        final double MAX_SIZE = 500;
        double imgViewW = 0;
        double imgViewH;
        if (originalBitmap.getWidth() > originalBitmap.getHeight()) {
            imgViewW = MAX_SIZE;
            imgViewH = originalBitmap.getHeight() * (MAX_SIZE / originalBitmap.getWidth());
        } else {
            imgViewH = MAX_SIZE;
            imgViewW = originalBitmap.getWidth() * (MAX_SIZE / originalBitmap.getHeight());
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, (int) imgViewW, (int) imgViewH, true);
        originalBitmap.recycle();
        originalBitmap = null;

        byte[] compressedData = data;
        compressedData = compressImageData(resizedBitmap, size);
        resizedBitmap.recycle();
        resizedBitmap = null;
        return compressedData;
    }

    public static byte[] compressImageData(Bitmap bitMap, float size) {
        int quality = 90;
        int maxQuality = 50;
        byte[] btArray = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        while (out.toByteArray().length > size && quality >= maxQuality) {
            quality = quality - 20;
            out.reset();
            bitMap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }
        btArray = out.toByteArray();
        // bitMap.recycle();
        // bitMap = null;
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out = null;
        }
        return btArray;
    }
}
