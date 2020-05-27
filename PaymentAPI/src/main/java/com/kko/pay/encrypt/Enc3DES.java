package com.kko.pay.encrypt;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class Enc3DES {

	private static Key key = null;

	static {
		if (key == null) {
			// Key 초기화
			KeyGenerator keyGenerator;
			try {
				keyGenerator = KeyGenerator.getInstance("TripleDES");
				keyGenerator.init(168);
				key = keyGenerator.generateKey();
				System.out.println("key :: " +key.getFormat().toString());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}

	public static String encode(String inStr) {
		StringBuffer sb = null;
		try {
			Cipher cipher = Cipher.getInstance("TripleDES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] plaintext = inStr.getBytes("UTF8");
			byte[] ciphertext = cipher.doFinal(plaintext);

			sb = new StringBuffer(ciphertext.length * 2);
			for (int i = 0; i < ciphertext.length; i++) {
				String hex = "0" + Integer.toHexString(0xff & ciphertext[i]);
				sb.append(hex.substring(hex.length() - 2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static String decode(String inStr) {
		String text = null;
		try {
			byte[] b = new byte[inStr.length() / 2];
			Cipher cipher = Cipher.getInstance("TripleDES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			for (int i = 0; i < b.length; i++) {
				b[i] = (byte) Integer.parseInt(inStr.substring(2 * i, 2 * i + 2), 16);
			}
			byte[] decryptedText = cipher.doFinal(b);
			text = new String(decryptedText, "UTF8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

}
