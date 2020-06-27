package com.arrownock.internal.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TokenHelper {
private static TokenHelper helper = new TokenHelper();
	
	/**
	 * Seed array which is used to construct characters array through bytes array which contains array index information.
	 */
	private final char[] sourceData = {'0','1','2','3','4','5','6','7','8','9',
								       'A','B','C','D','E','F','G','H','I','J','K','L','M',
								       'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	public static TokenHelper getInstance() {
		return helper;
	}
	
	public String getToken(String id, String appKey, long odd, String prefix) {
		String newClientId = id;
		if (id == null || id.trim().equals("")) {
			return null;
		}
		if(odd > 0) {
			newClientId = id + odd;
		}
		String encryptDevicedID = encrypt(EncryptionType.SHA1, newClientId + appKey);
		return prefix + encryptDevicedID;
//		return encryptDevicedID.substring(9);
	}
	
	private String encrypt(EncryptionType encryptType, String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(encryptType.name());
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught!");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		final byte[] digest = messageDigest.digest();
		return bytesToCharacters(digest);
		//return bytesToHex(digest);
	}
	
	@SuppressWarnings("unused")
	private String bytesToHex(byte[] b) {
      char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                         '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
      StringBuffer buf = new StringBuffer();
      for (int j=0; j<b.length; j++) {
         buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
         buf.append(hexDigit[b[j] & 0x0f]);
      }
      return buf.toString();
	}
	
	private enum EncryptionType {
		MD5,
		SHA1
	};
	
	/**
	 * Interpret the bytes array to a String array which are from sourceData char array.
	 * @param resultBytes
	 * @return the converted characters array, length is 20
	 */
	private String bytesToCharacters( byte[] resultBytes ){
	    char result;
	    StringBuffer sb = new StringBuffer();
	    int index = 0;
	    int offset = 0;
	    for(int i = 0; i < resultBytes.length; i++){
	    	index = resultBytes[i];
	    	if( index < 0 ) 
	    		index = Math.abs(index);
		    offset = index / 36;

		    if( offset < 1 )
			    result = sourceData[index];
		    else 
			    //result = const2[ val - position * 36 + position - 2];
		    	result = sourceData[ (index + offset) % 36 ];
		    sb.append(result);
	    }
	    String str =  sb.toString();
	    return str;
	}
}
