package jp.nmp;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 暗号化クラス
 * 
 * データを暗号化・復号化する。
 */
final class Crypto {
	/**
	 * HMAC生成用の秘密鍵
	 */
	private static final String APPCODE = "securemind";
	
	/**
	 * 暗号化アルゴリズム
	 */
	private static final String CRYPT_ALGORITHM = "BlowFish";
	
	/**
	 * メッセージダイジェストアルゴリズム
	 */
	private static final String MAC_ALGORITHM = "HmacSHA1";
	
	/**
	 * メッセージダイジェストの長さ (固定長)
	 */
	private static final int MAC_LENGTH = 20;
	
	/**
	 * データを暗号化する。
	 * 
	 * @param key パスワード
	 * @param data データ
	 * @return 暗号化データ
	 */
	public byte [] encrypt(byte [] key, byte [] data) {
		try {

			/* 引数が不正 */
			if (key == null || key.length == 0 || data == null || data.length == 0) {
				return (null);
			}

			/* 認証コード生成 */
			SecretKeySpec hmacSK = new SecretKeySpec(APPCODE.getBytes(), MAC_ALGORITHM);
			Mac mac = Mac.getInstance(hmacSK.getAlgorithm());
			mac.init(hmacSK);
			byte [] hmac = mac.doFinal(data);
			
			/* データ暗号化 */
			SecretKeySpec cryptSK = new SecretKeySpec(key, CRYPT_ALGORITHM);
			Cipher cipher  = Cipher.getInstance(CRYPT_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, cryptSK);
			byte[] enc = cipher.doFinal(data);
			
			/* 認証コードと連結 */
			return (joinArrays(hmac, enc));

		} catch (GeneralSecurityException e) {
			/* 暗号化失敗 */
			return (null);
		}
	}
	
	/**
	 * データを復号化する。
	 * 
	 * @param key パスワード
	 * @param data 暗号化データ
	 * @return 復号化データ
	 */
	public byte [] decrypt(byte [] key, byte [] data) {
		
		try {
			
			/* 引数が不正 */
			if (key == null || key.length == 0 || data == null || data.length == 0) {
				return (null);
			}
			
			/* データ分割 */
			byte [][] b = splitArrays(data);

			/* データ復号化 */
			SecretKeySpec cryptSK = new SecretKeySpec(key, CRYPT_ALGORITHM);
			Cipher cipher  = Cipher.getInstance(CRYPT_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, cryptSK);
			byte[] dec = cipher.doFinal(b[1]);
			
			/* 認証コードのチェック */
			SecretKeySpec hmacSK = new SecretKeySpec(APPCODE.getBytes(), MAC_ALGORITHM);
			Mac mac = Mac.getInstance(hmacSK.getAlgorithm());
			mac.init(hmacSK);
			byte [] hmac = mac.doFinal(dec);

			/* HMACが不一致 */
			if (!Arrays.equals(b[0], hmac)) {
				return (null);
			}
			
			return dec;
		} catch (GeneralSecurityException e) {
			return (null);
		}
	}
	
	/**
	 * HMACと暗号化データを結合する。
	 * 
	 * @param hmac HMAC
	 * @param data 暗号化データ
	 * @return 結合したデータ
	 */
	private byte[] joinArrays(byte [] hmac, byte [] data) {
		int len = hmac.length + data.length;
		byte[] buf = new byte[len]; 
		
		System.arraycopy(hmac, 0, buf, 0, hmac.length);
		System.arraycopy(data, 0, buf, hmac.length, data.length);
		return (buf);
	}

	/**
	 * HMACと暗号化データを分離する。
	 * 
	 * @param data 元データ
	 * @return インデックス0にHMAC、インデックス1に暗号化データが入った配列。
	 */
	private byte[][] splitArrays(byte [] data) {
		byte [] hmac = new byte[MAC_LENGTH];
		byte [] enc = new byte[data.length - MAC_LENGTH];
		
		System.arraycopy(data, 0, hmac, 0, MAC_LENGTH);
		System.arraycopy(data, MAC_LENGTH, enc, 0, data.length - MAC_LENGTH);
		return (new byte [][] {hmac, enc});
	}
}
