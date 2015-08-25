package com.example.nfcdemo;

import java.nio.charset.Charset;

import android.annotation.SuppressLint;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

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

}
