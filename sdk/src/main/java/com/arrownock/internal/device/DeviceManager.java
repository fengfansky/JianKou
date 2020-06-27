package com.arrownock.internal.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.arrownock.internal.device.DeviceLocator.LocationResult;
import com.arrownock.internal.util.Constants;
import com.arrownock.internal.util.DefaultHostnameVerifier;
import com.arrownock.push.PahoSocketFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class DeviceManager {
    private static final String LOG_TAG = DeviceManager.class.getName();
    private static final String DEVICE_ID = "com.arrownock.internal.device.DEVICE_ID";
    private static final String IS_REPORTED = "com.arrownock.internal.device.IS_REPORTED";
    private static final String SDK_VERSION = "com.arrownock.internal.device.SDK_VERSION";
    private static final String LAST_LOCATION_REPORT = "com.arrownock.internal.device.LAST_LOCATION_REPORT";
    private static final boolean DM_SECURE = Constants.DM_SECURE;
    private static final boolean DM_SELF_SIGN = Constants.DM_SELF_SIGN;

    private static DeviceManager reporter = null;
    private Context context = null;
    private String appKey = null;

    public static DeviceManager getInstance(Context context, String appKey) {
        if (reporter == null) {
            reporter = new DeviceManager(context);
        }
        reporter.setAppKey(appKey);
        return reporter;
    }

    private DeviceManager(Context context) {
        this.context = context;
        getDeviceId();
    }

    public String getDeviceId() {
        String deviceId = getFromLocalStorage(DEVICE_ID);
        if (deviceId.length() == 0) {
            deviceId = generateDeviceId();
            if (!"".equals(deviceId)) {
                saveToLocalStorage(DEVICE_ID, deviceId);
            }
        }
        return deviceId;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void reportDeviceData() {
        reportDeviceMetadata();
        reportLocation();
    }

    private void reportDeviceMetadata() {
        if (this.appKey != null) {
        	try {
	            final SharedPreferences pref = context.getSharedPreferences(DeviceManager.LOG_TAG, Context.MODE_PRIVATE);
	            Boolean isReported = pref.getBoolean(IS_REPORTED, false);
	            try {
	                if (isReported) {
	                    float sdk = Float.parseFloat(pref.getString(SDK_VERSION, "0"));
	                    float current = Float.parseFloat(Constants.SDK_VERSION);
	                    if (current > sdk) {
	                        isReported = false;
	                    }
	                }
	            } catch (Exception e) {
	                isReported = false;
	            }
	
	            if (!isReported) {
	                Runnable reportMetadataThread = new Runnable() {
	                    public void run() {
	                        List<NameValuePair> nameValuePairs = getDeviceMetadata();
	                        if (nameValuePairs == null) {
	                            return;
	                        }
	                        String deviceId = getDeviceId();
	                        if ("".equals(deviceId)) {
	                            return;
	                        }
	                        nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
	                        nameValuePairs.add(new BasicNameValuePair("device_type", "android"));
	                        nameValuePairs.add(new BasicNameValuePair("an_sdk", Constants.SDK_VERSION));
	                        int statusCode = sendReport(getMetadataURL() + "?key=" + appKey, "POST", nameValuePairs);
	                        if (statusCode == 200) {
	                            Editor e = pref.edit();
	                            e.putBoolean(IS_REPORTED, true);
	                            e.putString(SDK_VERSION, Constants.SDK_VERSION);
	                            e.commit();
	                        }
	                    }
	                };
	                Thread thread = new Thread(reportMetadataThread);
	                thread.start();
	            }
        	} catch (Exception e) {
				Log.w("DeviceManager", e.getMessage());
			}
        }
    }

    private List<NameValuePair> getDeviceMetadata() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        try {
            // settings
            nameValuePairs.add(new BasicNameValuePair("android_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)));

            // telephony
            String imei = null;
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
            	imei = tm.getDeviceId();
            } catch(Exception e) {
				// android 6 might throw exception here, mute
			}
            nameValuePairs.add(new BasicNameValuePair("network_operator", tm.getNetworkOperator()));
            nameValuePairs.add(new BasicNameValuePair("phone_type", String.valueOf(tm.getPhoneType())));
            if(imei != null) {
            	nameValuePairs.add(new BasicNameValuePair("phone_id", imei));
            }
            nameValuePairs.add(new BasicNameValuePair("sim_operator", tm.getSimOperator()));
            nameValuePairs.add(new BasicNameValuePair("network_type", String.valueOf(tm.getNetworkType())));
            nameValuePairs.add(new BasicNameValuePair("line1_number", tm.getLine1Number()));
            
            // system
            nameValuePairs.add(new BasicNameValuePair("board", Build.BOARD));
            nameValuePairs.add(new BasicNameValuePair("bootloader", Build.BOOTLOADER));
            nameValuePairs.add(new BasicNameValuePair("brand", Build.BRAND));
            nameValuePairs.add(new BasicNameValuePair("cpu_abi", Build.CPU_ABI));
            nameValuePairs.add(new BasicNameValuePair("cpu_abi2", Build.CPU_ABI2));
            nameValuePairs.add(new BasicNameValuePair("device", Build.DEVICE));
            nameValuePairs.add(new BasicNameValuePair("display", Build.DISPLAY));
            nameValuePairs.add(new BasicNameValuePair("fingerprint", Build.FINGERPRINT));
            nameValuePairs.add(new BasicNameValuePair("hardware", Build.HARDWARE));
            nameValuePairs.add(new BasicNameValuePair("build_id", Build.ID));
            nameValuePairs.add(new BasicNameValuePair("manufacturer", Build.MANUFACTURER));
            nameValuePairs.add(new BasicNameValuePair("model", Build.MODEL));
            nameValuePairs.add(new BasicNameValuePair("product", Build.PRODUCT));
            nameValuePairs.add(new BasicNameValuePair("tags", Build.TAGS));
            nameValuePairs.add(new BasicNameValuePair("type", Build.TYPE));
            nameValuePairs.add(new BasicNameValuePair("sdk_version", String.valueOf(Build.VERSION.SDK_INT)));
            nameValuePairs.add(new BasicNameValuePair("release_version", Build.VERSION.RELEASE));

            // display
            DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
            nameValuePairs.add(new BasicNameValuePair("screen_width", String.valueOf(dm.widthPixels)));
            nameValuePairs.add(new BasicNameValuePair("screen_height", String.valueOf(dm.heightPixels)));

            // processor
            String cpu = this.getCpuInfo();
            if (cpu != null) {
                String[] lines = cpu.split("\n");
                int cores = 0;
                for (String line : lines) {
                    if (line != null && line.indexOf(":") > 0) {
                        String label = line.substring(0, line.indexOf(":")).trim();
                        if ("Processor".equals(label)) {
                            String processor = line.substring(line.indexOf(":") + 1);
                            if (processor != null) {
                                nameValuePairs.add(new BasicNameValuePair("cpu_name", processor.trim()));
                            }
                            continue;
                        }
                        if ("processor".equals(label)) {
                            cores++;
                        }
                    }
                }
                if (cores > 0) {
                    nameValuePairs.add(new BasicNameValuePair("cpu_cores", String.valueOf(cores)));
                }
            }

            // memory
            String mem = this.getMemoryInfo();
            if (mem != null) {
                String[] lines = mem.split("\n");
                for (String line : lines) {
                    if (line != null && line.indexOf(":") > 0) {
                        String label = line.substring(0, line.indexOf(":")).trim();
                        if ("MemTotal".equals(label)) {
                            String memstr = line.substring(line.indexOf(":") + 1);
                            if (memstr != null) {
                                if (memstr.indexOf("kB") > 0) {
                                    memstr = memstr.substring(0, memstr.indexOf("kB"));
                                }
                                nameValuePairs.add(new BasicNameValuePair("mem_total", memstr.trim()));
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return nameValuePairs;
    }

    public void reportLocation() {
        if (this.appKey != null) {
        	SharedPreferences pref = context.getSharedPreferences(DeviceManager.LOG_TAG, Context.MODE_PRIVATE);
        	Long lastLocationReport = pref.getLong(LAST_LOCATION_REPORT, -1);
        	int interval = 1000 * 60 * 60 * Constants.DM_LOCATION_INTERVAL;
        	Long now = Calendar.getInstance().getTimeInMillis();
        	if(lastLocationReport < 0 || (now - lastLocationReport >= interval)) {
        		// report location every DM_LOCATION_INTERVAL hours
        		try {
    	            DeviceLocator locator = new DeviceLocator();
    	            locator.getLocation(context, new LocationResult() {
    	                @Override
    	                public void gotLocation(final Location location) {
    	                    if (location != null) {
    	                        sendLocation(location);
    	                    }
    	                }
    	            });
            	} catch (Exception e) {
    				Log.w("DeviceManager", e.getMessage());
    			}
        	}
        }
    }

    private void sendLocation(final Location location) {
        Runnable reportLocationThread = new Runnable() {
            public void run() {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                String deviceId = getDeviceId();
                if ("".equals(deviceId)) {
                    return;
                }
                nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
                nameValuePairs.add(new BasicNameValuePair("device_type", "android"));
                nameValuePairs.add(new BasicNameValuePair("an_sdk", Constants.SDK_VERSION));
                nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(location.getLatitude())));
                nameValuePairs.add(new BasicNameValuePair("lng", String.valueOf(location.getLongitude())));
                nameValuePairs.add(new BasicNameValuePair("acc", String.valueOf(location.getAccuracy())));
                nameValuePairs.add(new BasicNameValuePair("provider", String.valueOf(location.getProvider())));
                nameValuePairs.add(new BasicNameValuePair("alt", String.valueOf(location.getAltitude())));
                int statusCode = sendReport(getLocationURL() + "?key=" + appKey, "POST", nameValuePairs);
                
                // update latest location report time
                Editor editor = context.getSharedPreferences(DeviceManager.LOG_TAG, Context.MODE_PRIVATE).edit();
                editor.putLong(LAST_LOCATION_REPORT, Calendar.getInstance().getTimeInMillis());
	            editor.commit();
            }
        };
        Thread thread = new Thread(reportLocationThread);
        thread.start();
    }

    private int sendReport(String requestURL, String method,
            List<NameValuePair> params) {
        int statusCode = 0;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(requestURL);
            urlConnection = (HttpURLConnection) url.openConnection();

            if (DM_SECURE && DM_SELF_SIGN) {
                ((HttpsURLConnection) urlConnection).setHostnameVerifier(new DefaultHostnameVerifier());
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(PahoSocketFactory.getSocketFactoryFromBase64(
                                        Constants.SSL_SERVER_CERT,
                                        Constants.SSL_CLIENT_CERT,
                                        Constants.SSL_CLIENT_KEY, "", "BKS"));
            }

            urlConnection.setRequestMethod(method);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            try {
                OutputStream out = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(getQuery(params));
                writer.close();
                out.close();
                statusCode = urlConnection.getResponseCode();
            } catch (IOException e) {
                Log.e("DeviceManager", "Send device metadata failed: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e("DeviceManager", "Send device metadata failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return statusCode;
    }

    private String generateDeviceId() {
        String id = null;
        try {
        	String deviceId = null;
        	try {
        		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            	deviceId = tm.getDeviceId();
        	} catch(Exception e) {
				// android 6 might throw exception here, mute
			}
            String serial = Build.SERIAL;
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            if (deviceId == null || deviceId.equals("000000000000000") || "".equals(deviceId.trim())) {
                deviceId = "";
            } else {
                deviceId = "D" + deviceId;
            }

            if (serial == null || serial.trim().equals("unknown")) {
                serial = "";
            } else {
                serial = "S" + serial;
            }

            if (androidId == null || "".equals(androidId.trim())) {
                androidId = "";
            } else {
                androidId = "A" + androidId;
            }

            if ("".equals(deviceId) && "".equals(serial) && "".equals(androidId)) {
                // return null;
            } else {
                id = deviceId + serial + androidId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(id.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            id = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // no md5
            // id = UUID.nameUUIDFromBytes(id.getBytes()).toString();
            return "";
        }
        id = "2" + id;
        return id;
    }

    private String getFromLocalStorage(final String key) {
        SharedPreferences pref = context.getSharedPreferences(DeviceManager.LOG_TAG, Context.MODE_PRIVATE);
        return pref.getString(key, "");
    }

    private void saveToLocalStorage(final String key, final String value) {
        Editor editor = context.getSharedPreferences(DeviceManager.LOG_TAG, Context.MODE_PRIVATE).edit();
        editor.putString(key, value).commit();
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (pair.getValue() != null) {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }
        }
        return result.toString();
    }

    private String getCpuInfo() {
        try {
            Process proc = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            InputStream is = proc.getInputStream();
            String cpu = getStringFromInputStream(is);
            return cpu;
        } catch (IOException e) {
        }
        return null;
    }

    private String getMemoryInfo() {
        try {
            Process proc = Runtime.getRuntime().exec("cat /proc/meminfo");
            InputStream is = proc.getInputStream();
            String mem = getStringFromInputStream(is);
            return mem;
        } catch (IOException e) {
        }
        return null;
    }

    private String getStringFromInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }
    
    private String getMetadataURL() {
    	return (DM_SECURE ? Constants.HTTPS : Constants.HTTP) + Constants.DM_HOST + "/devices/metadata";
    }
    
    private String getLocationURL() {
    	return (DM_SECURE ? Constants.HTTPS : Constants.HTTP) + Constants.DM_HOST + "/devices/location";
    }
}
