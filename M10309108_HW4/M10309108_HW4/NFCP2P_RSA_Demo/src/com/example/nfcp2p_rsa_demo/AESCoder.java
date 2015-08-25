package com.example.nfcp2p_rsa_demo;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public abstract class AESCoder {
	public static final String KEY_ALGORITHM = "AES";
	public static final String CIPHER_ALGORITHM="AES/ECB/PKCS5Padding";
	
	//�ഫ�K�_
	private static Key toKey(byte[] key)throws Exception{
		SecretKey secretKey =new SecretKeySpec(key,KEY_ALGORITHM);
		return secretKey;
	}
	//�ѱK
	public static byte[] decrypt(byte[] data,byte[] key) throws Exception{
		Key k= toKey(key);
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE,k);//�]���ѱK�Ҧ�
		return cipher.doFinal(data);
		
	}

	
	
	//�[�K	
	public static byte[] encrypt(byte[] data,byte[] key) throws Exception{
		Key k= toKey(key);
		Cipher cipher =Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE,k);//�]���[�K�Ҧ�
		
		return cipher.doFinal(data);	
	}
	
	
	
	
	public static byte[] initKey() throws NoSuchAlgorithmException{
		KeyGenerator kg=KeyGenerator.getInstance(KEY_ALGORITHM);
		kg.init(128);//�ͦ����K�K�_
		SecretKey secretKey =kg.generateKey();
		return secretKey.getEncoded();
		
	}
	
	
	
	
}
