package com.example.nfcp2p_rsa_demo;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiveActivity extends Activity {
	Button BeamingPageBtn;
	Button RBMbtn;
	private NfcAdapter mNfcAdapter=null;
	protected IntentFilter[] filters;
	PendingIntent pendingIntent;
	private AlertDialog alertDialog=null;
	Context mContext=this;
	TextView CypherTextTextView;
	TextView SecretKeyTexView;
	TextView EncryptedSecretKeyTextView;
	TextView PlainTextTextView;
	Button DecriptSecretKeyWithPrivateKeyBtn;
	Button DecriptCiphertextwithSecretkeyBtn;
	String CypherText=null;
	String PlainText=null;
	String EncryptedSecretKey=null;	
	String Secretkey=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receive);
		nfcCheck();     
		pendingIntent =PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters = new IntentFilter[] { ndefDetected, tagDetected, techDetected };    
		
		
        CypherTextTextView=(TextView)this.findViewById(R.id.CypherText);
        EncryptedSecretKeyTextView=(TextView)this.findViewById(R.id.EncryptedSecretKey);
        
        SecretKeyTexView=(TextView)this.findViewById(R.id.SecretKey);
        
		BeamingPageBtn=(Button)this.findViewById(R.id.button2);
        BeamingPageBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent MyIntent=new Intent();
				MyIntent.setClass(ReceiveActivity.this, BeamActivity.class);
				startActivity(MyIntent);//換頁
				ReceiveActivity.this.finish();//結束目前Activity 
		}});
        RBMbtn=(Button)this.findViewById(R.id.button3);
        RBMbtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				enableForegroundDispatch();
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Receiving ths message");
				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {			
					@Override
					public void onCancel(DialogInterface dialog) {
						disableForegroundDispatch();
					}
				});
				alertDialog =builder.create();
				alertDialog.setCanceledOnTouchOutside(false);
				alertDialog.show();
				
			}
        	
        });
        
        DecriptSecretKeyWithPrivateKeyBtn=(Button)this.findViewById(R.id.button4);
        DecriptSecretKeyWithPrivateKeyBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(EncryptedSecretKey!=null)
				{
					String PrivateKey=((GlobalVariable)getApplication()).getPrivateKey();//get secret key
					//Toast.makeText(ReceiveActivity.this, "用私鑰開始解密", Toast.LENGTH_SHORT).show();
					//Toast.makeText(ReceiveActivity.this, "PrivateKey:"+PrivateKey, Toast.LENGTH_SHORT).show();
					//Toast.makeText(ReceiveActivity.this, "EncryptedSecretKey:"+EncryptedSecretKey, Toast.LENGTH_SHORT).show();
					try {
						byte[] temp=RSA.decryptByPrivateKey(Base64.decode(EncryptedSecretKey, Base64.DEFAULT),Base64.decode(PrivateKey, Base64.DEFAULT));
						Secretkey=Base64.encodeToString(temp, Base64.DEFAULT);
						Toast.makeText(ReceiveActivity.this, "私鑰解密成功", Toast.LENGTH_SHORT).show();
						SecretKeyTexView.setText(Secretkey);					
					} catch (Exception e) {					
						e.printStackTrace();
						SecretKeyTexView.setText(e.getMessage());
						Toast.makeText(ReceiveActivity.this, "Decript SecretKey With PrivateKey error", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(ReceiveActivity.this, "EncryptedSecretKey is null", Toast.LENGTH_SHORT).show();
				}
			}
        	
        });
        PlainTextTextView=(TextView)this.findViewById(R.id.PlainTextTextView);
        DecriptCiphertextwithSecretkeyBtn=(Button)this.findViewById(R.id.button5);
        DecriptCiphertextwithSecretkeyBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(ReceiveActivity.this, "AES對稱式金鑰解密", Toast.LENGTH_SHORT).show();
				if(CypherText!=null)
				{
					try {
						byte[] temp=AESCoder.decrypt(Base64.decode(CypherText, Base64.DEFAULT), Base64.decode(Secretkey, Base64.DEFAULT));
						PlainText=new String(temp);
						PlainTextTextView.setText(PlainText);
						Toast.makeText(ReceiveActivity.this, "AES對稱式金鑰解密成功", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					Toast.makeText(ReceiveActivity.this, "CypherText is null", Toast.LENGTH_SHORT).show();
				}
				
			}
        	
        });
        
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();	
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
		{		
			//Toast.makeText(this, "ACTION_NDEF_DISCOVERED", Toast.LENGTH_SHORT).show();		
			resolveIntent(getIntent());
			return;
		}
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
	        //Toast.makeText(this, "ACTION_TAG_DISCOVERED", Toast.LENGTH_SHORT).show();
	        resolveIntent(getIntent());              
	        return;
	    }
	    if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
	        //Toast.makeText(this, "ACTION_TECH_DISCOVERED", Toast.LENGTH_SHORT).show();
	        resolveIntent(getIntent());              
	        return;
	    }
		
		
	}
	private void resolveIntent(Intent intent) {
		//String action=intent.getAction();	
		//if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){
			NdefMessage[] messages=null;
			Parcelable[] rawMsgs =intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if(rawMsgs != null){
				messages=new NdefMessage[rawMsgs.length];
				for(int i=0;i<rawMsgs.length;i++){
					messages[i]=(NdefMessage)rawMsgs[i];
							
				}
			}else{//Unkown tag type
				byte[] empty = new byte[]{};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,empty,empty,empty);
				NdefMessage msg=new NdefMessage(new NdefRecord[]{record});
				messages = new NdefMessage[]{msg};
						
			}
			setTitle("scanned_tag");
			processNDEFMsg(messages);
				
		//}
	}
private void processNDEFMsg(NdefMessage[] messages){
    	
    	if(messages ==null || messages.length==0 )
    	{
    		Toast.makeText(this, "NdefMessage is null", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	for(int i=0;i<messages.length;i++)
    	{
    		int length = messages[i].getRecords().length;//幾個紀錄
    		NdefRecord[] records=messages[i].getRecords();
    		int j=0;
    		for(NdefRecord record:records)
    		{
    			
    			short tnf = record.getTnf();
    	    	if(tnf==NdefRecord.TNF_WELL_KNOWN)
    	    	{
    	    		if(Arrays.equals(record.getType(), NdefRecord.RTD_URI)){
    	    			//parseWellKnownUriRecord(record);
    	    		}else if(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT))
    	    		{
    	    			alertDialog.dismiss();
    	    			//Toast.makeText(this, parseRTD_TEXTRecord(record), Toast.LENGTH_SHORT).show();
    	    			if(j==0){
    	    				//Toast.makeText(this, parseRTD_TEXTRecord(record), Toast.LENGTH_SHORT).show();
    	    				
    	    				//CipherText=parseRTD_TEXTRecord(record).getBytes() ;	
    	    				CypherText=parseRTD_TEXTRecord(record);
    	    				CypherTextTextView.setText(CypherText); 
    	    			}
    	    			if(j==1){
    	    				//Toast.makeText(this, parseRTD_TEXTRecord(record), Toast.LENGTH_SHORT).show();
    	    				 EncryptedSecretKey=parseRTD_TEXTRecord(record);
    	    				
    	    				EncryptedSecretKeyTextView.setText(EncryptedSecretKey);
    	    			}
    	    			
    	    		}
    	    		else{
    	    			
    	    		}
    	    		
    	    	}    
    	    	j++;
    		}
    	}
	
	}
	@Override
	protected void onNewIntent(Intent intent) {
		alertDialog.dismiss();
		setIntent(intent);
	}
	private void enableForegroundDispatch(){
    	if (mNfcAdapter != null){
			mNfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
		}
    }
    
    
    private void disableForegroundDispatch(){
    	if (mNfcAdapter != null){
			mNfcAdapter.disableForegroundDispatch(this);
		}
    }
	 private void nfcCheck(){
			mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if(mNfcAdapter==null)
			{
				//沒有NFC裝置
				Toast.makeText(this, "沒有NFC裝置", Toast.LENGTH_SHORT).show();
				return;
			}
			else{
				if(!mNfcAdapter.isEnabled())
				{
					//NFC裝置沒開
					Toast.makeText(this, "NFC裝置沒開", Toast.LENGTH_SHORT).show();
					return;
				}
				
				else{
					Toast.makeText(this, "NFC裝置已啟動", Toast.LENGTH_SHORT).show();
					
				}
			}
		}

	  private String parseRTD_TEXTRecord(NdefRecord record){
	    	Preconditions.checkArgument(record.getTnf()== NdefRecord.TNF_WELL_KNOWN);
	    	Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT));
	    	String parloadStr="";
	    	byte[] payload = record.getPayload();   	
	    	Byte statusByte = record.getPayload()[0];
	    	String textEncoding = "";
	    	textEncoding =((statusByte & 0200)==0)?"UTF-8":"UTF-16";
	    	int languageCodeLength =0;
	    	languageCodeLength = statusByte & 0077;
	    	String languageCode="";
	    	languageCode = new String(payload,1,languageCodeLength,Charset.forName("UTF-8"));
	    	try {
				parloadStr=new String(payload,languageCodeLength+1,payload.length-languageCodeLength-1,textEncoding);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return parloadStr;	
	    }
	   private static final BiMap<Byte, String> URI_PREFIX_MAP = ImmutableBiMap.<Byte, String>builder()
	            .put((byte) 0x00, "")
	            .put((byte) 0x01, "http://www.")
	            .put((byte) 0x02, "https://www.")
	            .put((byte) 0x03, "http://")
	            .put((byte) 0x04, "https://")
	            .put((byte) 0x05, "tel:")
	            .put((byte) 0x06, "mailto:")
	            .put((byte) 0x07, "ftp://anonymous:anonymous@")
	            .put((byte) 0x08, "ftp://ftp.")
	            .put((byte) 0x09, "ftps://")
	            .put((byte) 0x0A, "sftp://")
	            .put((byte) 0x0B, "smb://")
	            .put((byte) 0x0C, "nfs://")
	            .put((byte) 0x0D, "ftp://")
	            .put((byte) 0x0E, "dav://")
	            .put((byte) 0x0F, "news:")
	            .put((byte) 0x10, "telnet://")
	            .put((byte) 0x11, "imap:")
	            .put((byte) 0x12, "rtsp://")
	            .put((byte) 0x13, "urn:")
	            .put((byte) 0x14, "pop:")
	            .put((byte) 0x15, "sip:")
	            .put((byte) 0x16, "sips:")
	            .put((byte) 0x17, "tftp:")
	            .put((byte) 0x18, "btspp://")
	            .put((byte) 0x19, "btl2cap://")
	            .put((byte) 0x1A, "btgoep://")
	            .put((byte) 0x1B, "tcpobex://")
	            .put((byte) 0x1C, "irdaobex://")
	            .put((byte) 0x1D, "file://")
	            .put((byte) 0x1E, "urn:epc:id:")
	            .put((byte) 0x1F, "urn:epc:tag:")
	            .put((byte) 0x20, "urn:epc:pat:")
	            .put((byte) 0x21, "urn:epc:raw:")
	            .put((byte) 0x22, "urn:epc:")
	            .put((byte) 0x23, "urn:nfc:")
	            .build();
}
