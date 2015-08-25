package com.example.nfcdemo;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Base64;
import android.widget.Toast;

public class BobNdefMessage {
	
	@SuppressLint("NewApi") public static NdefMessage getNdefMsg_from_RTD_URI(String uriFiledStr,byte identifierCode,boolean flagAddAAR){
		byte[] uriField = uriFiledStr.getBytes(Charset.forName("US-ASCII"));
		byte[] payLoad=new byte[uriField.length+1];
		payLoad[0] = identifierCode;
		System.arraycopy(uriField, 0, payLoad, 1, uriField.length);
		NdefRecord rtdUriRecord1=new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_URI,new byte[0],payLoad);	
		if(flagAddAAR){
			return new NdefMessage(new NdefRecord[]{rtdUriRecord1,NdefRecord.createApplicationRecord("com.example.nfcdemo")});
		}
		else{
			return new NdefMessage(new NdefRecord[]{rtdUriRecord1});
		}
		
	}
	
	@SuppressLint("NewApi") public static NdefMessage getNdefMsg_from_RTD_TEXT(String text,boolean encodeInUtf8,boolean flagAddAAR){
		Locale locale =new Locale("en","US");
		byte[] langBytes=locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
		Charset utfEncoding =encodeInUtf8 ? Charset.forName("UTF-8"):Charset.forName("UTF-16");
		int utfBit = encodeInUtf8?0:(1<<7);
		char status=(char) (utfBit+langBytes.length);
		byte[] textBytes= text.getBytes(utfEncoding);
		byte[] data =new byte[1+langBytes.length+textBytes.length];
		data[0]=(byte)status;
		System.arraycopy(langBytes, 0, data, 1, langBytes.length);
		System.arraycopy(textBytes, 0, data, 1+langBytes.length,textBytes.length);
		NdefRecord textRecord =new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],data);
		
		
		if(flagAddAAR){
			return new NdefMessage(new NdefRecord[]{textRecord,NdefRecord.createApplicationRecord("com.example.nfcdemo")});
		}
		else{
			return new NdefMessage(new NdefRecord[]{textRecord});
		}
		
	}

}
