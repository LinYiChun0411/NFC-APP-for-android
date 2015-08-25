package com.example.nfcdemo;


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.primitives.Bytes;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	TextView TagTypeTextView,TagMaxSizeTextView;
	TextView cipherTextView,plainTextView;
	NfcAdapter mNfcAdapter=null;
	protected IntentFilter[] filters;
	PendingIntent pendingIntent;
	private static final String myTag ="zhaob/nfc";
	@Override
    protected void onCreate(Bundle savedInstanceState) {	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        initUI();
        nfcCheck();
        pendingIntent =PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters = new IntentFilter[] { ndefDetected, tagDetected, techDetected };
    }
	@Override
	protected void onResume() {
		super.onResume();
		mNfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);	
	}	
	@Override
	protected void onPause() {
		super.onPause();
	}
	public void onNewIntent(Intent intent) {
		
			//解析Tag
			Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Toast.makeText(this, "找到NDEFTag", Toast.LENGTH_SHORT).show();					
			Ndef ndefTag = Ndef.get(myTag);
			if(ndefTag!=null){
					System.out.println(ndefTag);
					int size = ndefTag.getMaxSize();         // tag size
					boolean writable = ndefTag.isWritable(); // is tag writable?
					String type = ndefTag.getType();         // tag type
					TagTypeTextView.setText("TagType:"+type);
					TagMaxSizeTextView.setText("TagMaxsize:"+size);
					Toast.makeText(this, "TagMaxsize:"+size+"type:"+type, Toast.LENGTH_SHORT).show();
					//解析messages
					NdefMessage[] messages=null;
					Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
					if(rawMsgs !=null)
					{
						messages=new NdefMessage[rawMsgs.length];
						for(int i=0;i< rawMsgs.length;i++){
							messages[i]=(NdefMessage)rawMsgs[i];					
						}
					}
					else{
						Toast.makeText(this,"沒有讀到EXTRA_NDEF_MESSAGES", Toast.LENGTH_SHORT).show();
					}			
				processNDEFMsg(messages);
			}
		
	}
	private void initUI(){
		 TagTypeTextView=(TextView)this.findViewById(R.id.TagTypetext);
	     TagMaxSizeTextView=(TextView)this.findViewById(R.id.TagMaxSizetext);
	     cipherTextView=(TextView)this.findViewById(R.id.cipherTextView);
	     plainTextView=(TextView)this.findViewById(R.id.plainTextView);
	     Button btn=(Button)this.findViewById(R.id.button1);
	     btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this,WriteTag.class); //參數一:目前的Activity 參數二:下一頁的Activity 
				startActivity(intent);//換頁
				MainActivity.this.finish();//結束目前Activity 
	
			}});
	}
	
	
    private void processNDEFMsg(NdefMessage[] messages){
    	String cipherText = null;
    	String plainText = null;
    	if(messages ==null || messages.length==0 )
    	{
    		Toast.makeText(this, "NdefMessage is null", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	for(int i=0;i<messages.length;i++)
    	{
    		int length = messages[i].getRecords().length;//幾個紀錄
    		NdefRecord[] records=messages[i].getRecords();
    		for(NdefRecord record:records)
    		{
    			short tnf = record.getTnf();
    	    	if(tnf==NdefRecord.TNF_WELL_KNOWN)
    	    	{
    	    		if(Arrays.equals(record.getType(), NdefRecord.RTD_URI)){
    	    			parseWellKnownUriRecord(record);
    	    		}else if(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT))
    	    		{
    	    			cipherText=parseRTD_TEXTRecord(record);
    	    			Toast.makeText(this,"讀出密文:"+cipherText, Toast.LENGTH_SHORT).show();
    	    			//解密
    	    	    	 byte[] key=((GlobalVariable) this.getApplication()).getKey();//用全域變數取得金鑰
    	    			    //Toast.makeText(this,"金鑰:"+key.toString(), Toast.LENGTH_SHORT).show();	    		  
    	    				try {				
    	    					//解密	
    	    					plainText= new String( AESCoder.decrypt(	Base64.decode(cipherText.getBytes(), Base64.DEFAULT),key) );
    	    					Toast.makeText(this,"解密後:"+plainText, Toast.LENGTH_SHORT).show();
    	    					plainTextView.setText(plainText);
    	    				} catch (Exception e) {
    	    					// TODO Auto-generated catch block	
    	    					Toast.makeText(this,"解密失敗"+e.toString(), Toast.LENGTH_SHORT).show();   	    					
    	    					e.printStackTrace();
    	    				}
    	    		    //解密
    	    		}
    	    		else{
    	    			
    	    		}
    	    	}    			 			
    		}
    	}
    	cipherTextView.setText("讀出密文:"+cipherText);
    	plainTextView.setText("讀出明文:"+plainText);
    	
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
    private void parseWellKnownUriRecord(NdefRecord record){
    	Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_URI));
    	byte[] payload=record.getPayload();
    	String prefix=URI_PREFIX_MAP.get(payload[0]);
    	byte[] fullUri= Bytes.concat(prefix.getBytes(Charset.forName("UTF-8")), Arrays.copyOfRange(payload, 1,
                payload.length));
    	Uri uri=Uri.parse(new String(fullUri,Charset.forName("UTF-8")));  	
    	//read_uri.setText("收到:"+uri);
    	if(prefix=="tel:"){
    		Intent dial = new Intent();
	    	dial.setAction("android.intent.action.CALL");
	    	dial.setData(uri);
	    	startActivity(dial);
    	}
    }
	private void nfcCheck(){
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(mNfcAdapter==null)
		{
			Log.w(myTag,"Your NFC not Support NFC");//沒有NFC裝置
			Toast.makeText(this, "沒有NFC裝置", Toast.LENGTH_SHORT).show();
			return;
		}
		else{
			if(!mNfcAdapter.isEnabled())
			{
				Log.w(myTag,"Your NFC not enable");//NFC裝置沒開
				Toast.makeText(this, "NFC裝置沒開", Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(this, "NFC裝置已啟動", Toast.LENGTH_SHORT).show();
		}
	}
	/**
     * NFC Forum "URI Record Type Definition"
     *
     * This is a mapping of "URI Identifier Codes" to URI string prefixes,
     * per section 3.2.2 of the NFC Forum URI Record Type Definition document.
     */
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
