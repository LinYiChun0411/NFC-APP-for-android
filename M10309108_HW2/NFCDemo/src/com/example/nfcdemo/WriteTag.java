package com.example.nfcdemo;

import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WriteTag extends Activity {
	NfcAdapter mNfcAdapter=null;
	protected IntentFilter[] filters;
	PendingIntent pendingIntent;
	TextView stateTextView;
	private String[][] mTechLists=null;
	private Context mContext;
	private AlertDialog alertDialog =null;
	private NdefMessage NDEFMsg2Write=null;
	EditText editText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.writetag);	
		
		editText=(EditText)this.findViewById(R.id.editText1);
		
		mContext=this;
		Button btn=(Button)this.findViewById(R.id.button2);
	    stateTextView=(TextView)this.findViewById(R.id.textView1);
        nfcCheck();
        initNFC();
		btn.setOnClickListener(new OnClickListener(){
		@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(WriteTag.this,MainActivity.class); //參數一:目前的Activity 參數二:下一頁的Activity 
				startActivity(intent);//換頁
				WriteTag.this.finish();//結束目前Activity 
			}
		});
		Button writeTagbtn=(Button)this.findViewById(R.id.button1);
		writeTagbtn.setOnClickListener(new OnClickListener(){
			@Override
				public void onClick(View v) {
					
							enableForegroundDispatch();
							AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
							builder.setTitle("touch to write").setOnCancelListener(new DialogInterface.OnCancelListener() {	
								@Override
								public void onCancel(DialogInterface dialog) {
									disableForegroundDispatch();
								}
							});
							alertDialog=builder.create();
							alertDialog.setCanceledOnTouchOutside(true);
							alertDialog.show();				
				}
			
			});
	}//onCreate finish

	private void enableForegroundDispatch(){
		if(mNfcAdapter!=null){
			mNfcAdapter.enableForegroundDispatch(this, pendingIntent, filters,mTechLists);
		}	
	}
	private void disableForegroundDispatch(){
		if(mNfcAdapter!=null){
			mNfcAdapter.disableForegroundDispatch(this);
		}	
	}
	
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Toast.makeText(this, "偵測到Tag", Toast.LENGTH_SHORT).show();	
		Tag detecTag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
		if(detecTag!=null){
			if(supportedTechs(detecTag.getTechList()))
			{
				Toast.makeText(this, "Tag支援Ndef", Toast.LENGTH_SHORT).show();
				byte[] payloadStr=editText.getText().toString().getBytes();
				//NDEFMsg2Write = BobNdefMessage.getNdefMsg_from_RTD_URI(payloadStr,(byte)0x05, false);
				//****************************************************************加密
				byte[] cipherText = null;
				//初始化密鑰
				try {
					byte[] key= AESCoder.initKey();					
				    ((GlobalVariable)this.getApplication()).setKey(key);
					//Toast.makeText(this,"密鑰:"+key.toString(), Toast.LENGTH_SHORT).show();				
					try {
						cipherText=AESCoder.encrypt(payloadStr, key);//資料加密
						Base64.encodeToString(cipherText, Base64.DEFAULT);
						Toast.makeText(this,"加密後:"+Base64.encodeToString(cipherText, Base64.DEFAULT), Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//*****************************************************************
	
				
				NDEFMsg2Write =BobNdefMessage.getNdefMsg_from_RTD_TEXT(Base64.encodeToString(cipherText, Base64.DEFAULT), true, false);
				new WriteTask(this,NDEFMsg2Write,detecTag).execute();
				Toast.makeText(this, "寫入成功", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(this, "Tag不支援", Toast.LENGTH_SHORT).show();
			}
		}
		else{
			Toast.makeText(this, "Tag為NULL", Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(alertDialog != null){
			alertDialog.cancel();			
		}
		disableForegroundDispatch();
	}
	private void nfcCheck(){
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(mNfcAdapter==null)
		{		
			Toast.makeText(this, "沒有NFC裝置", Toast.LENGTH_SHORT).show();
			return;
		}
		else{
			if(!mNfcAdapter.isEnabled())
			{
				
				Toast.makeText(this, "NFC裝置沒開", Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(this, "NFC裝置已啟動", Toast.LENGTH_SHORT).show();
		}
	}
	public void initNFC()
	{
		pendingIntent =PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters = new IntentFilter[] { ndefDetected, tagDetected, techDetected };
        mTechLists =new String[][]{new String[]{Ndef.class.getName()},new String[]{NdefFormatable.class.getName()}};
	}
	public static boolean supportedTechs(String[] techs){
		boolean ultralight =false;
		boolean nfcA =false;
		boolean ndef =false;
		for(String tech:techs){
			if(tech.equals("android.nfc.tech.MifareUltralight"))
			{
				ultralight=true;
			}
			else if(tech.equals("android.nfc.tech.NfcA"))
			{
				nfcA=true;
			}
			else if(tech.equals("android.nfc.tech.Ndef"))
			{
				ndef=true;
			}
			else{
				
			}
		}
		if(ndef)
		{
			return true;
		}
		else{
			return false;
		}			
	}
	
	
	static class WriteTask extends AsyncTask<Void, Void, Void> {
		Activity host=null;
		NdefMessage msg=null;
		Tag tag=null;
		String text=null;

		WriteTask(Activity host,NdefMessage msg,Tag tag){
			this.host=host;
			this.msg=msg;
			this.tag=tag;
		}
		@Override
		protected Void doInBackground(Void... params) {
			int size =msg.toByteArray().length;
			try{
				Ndef ndef= Ndef.get(tag);
				if(ndef==null){
					NdefFormatable formatable =NdefFormatable.get(tag);
					if(formatable!=null){
						try {
							formatable.connect();
							try{
								formatable.format(msg);
							}
							catch(Exception e){
								text="fail to format tag";		
							}
						}
						catch (Exception e) {
							text="fail to connect tag";
						}finally{
							formatable.close();
						}
					}
					else{
						text="NDEF is not supported in this Tag";
					}
				}
				else{
					ndef.connect();
					try{
						if(!ndef.isWritable())
						{
							text="tag is read-only";
						}else if(ndef.getMaxSize()<size)
						{
							text="data is too big can not write to tag";
						}
						else{
							ndef.writeNdefMessage(msg);
							text="Message is written tag,Message="+msg;
						}		
					}catch(Exception e){
						text="tag refused to connect";
					}finally{
						ndef.close();
					}				
				}
			}catch(Exception e){
				text="write operation is failed";
			}
			return (null);
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(text!=null)
			{
				Toast.makeText(host, text, Toast.LENGTH_SHORT).show();
			}
			//host.finish();
		}

	}
}





