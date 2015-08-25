package com.example.nfcp2pdemo;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.primitives.Bytes;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity{
	private NfcAdapter mNfcAdapter=null;
	TextView ReceivetextView2;
	protected IntentFilter[] filters;
	PendingIntent pendingIntent;
	private AlertDialog alertDialog=null;
	private Context mContext=this;
	
	Button BNMbtn;
	Button RBMbtn;
	EditText mEditText;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcCheck();     
        ReceivetextView2=(TextView)this.findViewById(R.id.ReceiveTextView);   
        mEditText=(EditText)this.findViewById(R.id.editText1);
        pendingIntent =PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters = new IntentFilter[] { ndefDetected, tagDetected, techDetected };     
        BNMbtn=(Button)this.findViewById(R.id.button1);
        RBMbtn=(Button)this.findViewById(R.id.button2);
        BNMbtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				byte identifierCode =0x01;
				
				String inputText=mEditText.getText().toString();
				
				NdefMessage message=BobNdefMessage.getNdefMsg_from_RTD_URI(inputText, identifierCode, false);
				
				
				mNfcAdapter.setNdefPushMessage(message,MainActivity.this);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Sending ths message");
				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {			
					@Override
					public void onCancel(DialogInterface dialog) {
						
					}
				});
				alertDialog =builder.create();
				alertDialog.setCanceledOnTouchOutside(false);
				alertDialog.show();
					
			}
        	
        });
        
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

	@Override
	protected void onNewIntent(Intent intent) {
		alertDialog.dismiss();
		setIntent(intent);
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
    		for(NdefRecord record:records)
    		{
    			short tnf = record.getTnf();
    	    	if(tnf==NdefRecord.TNF_WELL_KNOWN)
    	    	{
    	    		if(Arrays.equals(record.getType(), NdefRecord.RTD_URI)){
    	    			parseWellKnownUriRecord(record);
    	    		}else if(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT))
    	    		{
    	    			
    	    				
    	    		    
    	    		}
    	    		else{
    	    			
    	    		}
    	    	}    			 			
    		}
    	}
	
	}
	private void parseWellKnownUriRecord(NdefRecord record){
    	Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_URI));
    	byte[] payload=record.getPayload();
    	String prefix=URI_PREFIX_MAP.get(payload[0]);
    	byte[] fullUri= Bytes.concat(prefix.getBytes(Charset.forName("UTF-8")), Arrays.copyOfRange(payload, 1,
                payload.length));
    	Uri uri=Uri.parse(new String(fullUri,Charset.forName("UTF-8")));  	
    	OpenBrowser(uri);
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

	private void OpenBrowser(final Uri uri){	
		ReceivetextView2.setText("收到:"+uri);		
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(uri);
		startActivity(i);
		
		
	
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
