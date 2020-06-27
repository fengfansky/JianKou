package com.arrownock.internal.push;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogUtil {
	public enum LogLevel {
		DEBUG, INFO, WARN, ERROR
	};

	public final static String LOG_TAG = LogUtil.class.getName();
	private final static LogLevel DEFAULT_LOG_LEVEL = LogLevel.ERROR;
	protected static LogUtil instance;
	private boolean logToSDCard = false;
	
	protected LogUtil() {
		if(logToSDCard) {
			File sdcard = Environment.getExternalStorageDirectory();
			File logDir = new File(sdcard, "arrownock/log/");
			if (!logDir.exists()) {
				logDir.mkdirs();
				// do not allow media scan
				try {
					new File(logDir, ".nomedia").createNewFile();
				} catch (IOException ex) {
					Log.e(LOG_TAG, "Error on writing to SD card. Exception:" + ex.toString(), ex);
				}
			}
			logDirStr = logDir.getAbsolutePath();
		}
	}

	public static LogUtil getInstance() {
		if (instance == null) {
			instance = new LogUtil();
			instance.logLevel = DEFAULT_LOG_LEVEL;
		}
		return instance;
	}

	private String logDirStr;
	private LogLevel logLevel;

	public void println(String logFile, String message) {
		if(logToSDCard) {
			Writer mWriter = null;
			try {
				if (logFile == null || logFile.equals("")) {
					logFile = "noname";
				}
				String logFileStr = logDirStr + "/" + logFile + "-" + getTodayString() + ".log";
				mWriter = new BufferedWriter(new FileWriter(logFileStr, true), 2048);

				mWriter.write(TIMESTAMP_FMT.format(new Date()));
				mWriter.write(message);
				mWriter.write('\n');
				mWriter.flush();
			} catch (IOException ex) {
				Log.e(LOG_TAG, "Error on writing to SD card. Exception:" + ex.toString());
			} finally {
				try {
					if (mWriter != null)
						mWriter.close();
				} catch (IOException ex) {
					Log.e(LOG_TAG, "Error on writing to SD card. Exception:" + ex.toString());
				}
			}
		}
	}

	public void setLogLevel(LogLevel logLevel) {
//		this.logLevel = logLevel;
	}

	public void debug(String logFile, String message) {
		debug(null, logFile, message);
	}

	public void debug(String logTag, String logFile, String message) {
		if (logTag != null) {
			Log.d(logTag, message);
		}
		if (LogLevel.DEBUG.ordinal() >= logLevel.ordinal()) {
			println(logFile, "[D] " + message);
		}
	}

	public void info(String logFile, String message) {
		info(null, logFile, message);
	}

	public void info(String logTag, String logFile, String message) {
		if (logTag != null) {
			Log.i(logTag, message);
		}
		if (LogLevel.INFO.ordinal() >= logLevel.ordinal()) {
			println(logFile, "[I] " + message);
		}
	}

	public void warn(String logFile, String message) {
		warn(null, logFile, message);
	}

	public void warn(String logTag, String logFile, String message) {
		if (logTag != null) {
			Log.w(logTag, message);
		}
		if (LogLevel.WARN.ordinal() >= logLevel.ordinal()) {
			println(logFile, "[W] " + message);
		}
	}

	public void error(String logFile, String message) {
		error(null, logFile, message);
	}

	public void error(String logTag, String logFile, String message) {
		error(logTag, logFile, message, null);
	}

	public void error(String logTag, String logFile, String message, Throwable ex) {
		if (logTag != null) {
			if (ex != null) {
				Log.e(logTag, message, ex);
			} else {
				Log.e(logTag, message);
			}
		}
		if (LogLevel.ERROR.ordinal() >= logLevel.ordinal()) {
			println(logFile, "[E] " + message + (ex == null ? "" : " | Exception:" + ex.toString()));
		}
	}

	private static final SimpleDateFormat TIMESTAMP_FMT = new SimpleDateFormat("[HH:mm:ss] ");

	private static String getTodayString() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		// SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-hhmmss");
		return df.format(new Date());
	}
}
