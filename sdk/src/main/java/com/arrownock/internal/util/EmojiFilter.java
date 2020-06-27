package com.arrownock.internal.util;

public class EmojiFilter {
	public static boolean containsEmoji(String source) {
		if (source == null || "".equals(source.trim())) {
			return false;
		}
		int len = source.length();
		for (int i = 0; i < len; i++) {
			char codePoint = source.charAt(i);
			if (!isNormalCharacter(codePoint)) {
				// has emoji
				return true;
			}
		}
		return false;
	}

	private static boolean isNormalCharacter(char codePoint) {
		return (codePoint == 0x0) 
				|| (codePoint == 0x9) 
				|| (codePoint == 0xA)
				|| (codePoint == 0xD)
				|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
				|| ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
				|| ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
	}

	public static String filterEmoji(String source) {
		if (!containsEmoji(source)) {
			return source;
		}
		StringBuilder buf = null;
		int len = source.length();
		for (int i = 0; i < len; i++) {
			char codePoint = source.charAt(i);
			if (isNormalCharacter(codePoint)) {
				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(codePoint);
			} else {
			}
		}

		if (buf == null) {
			return ""; // all emoji
		} else {
			if (buf.length() == len) {// 这里的意义在于尽可能少的toString，因为会重新生成字符串
				buf = null;
				return source;
			} else {
				return buf.toString();
			}
		}
	}
}
