package com.example.nfcp2p_rsa_demo;



import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BeamActivity extends ActionBarActivity {
	EditText PlainTextEditText;
	Button KeyMenuBtn;
	Button ReceivePageBtn;
	Button EncriptPlainTextBtn;
	Button EncriptSecretKeyBtn;
	Button BNMbtn;
	TextView CipherTextView;
	TextView EncryptedSecretKeyTextView;
	static String CipherText=null;
    String Secretkey=null;
	static String encodedSecretkey = null;
	static String PlainText=null;
	Context mContext=this;
	private AlertDialog alertDialog=null;
	private NfcAdapter mNfcAdapter=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.beam);
    	String publicKey;
        String privateKey;
        publicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmfvRGEQq767+BukNpebZm407YCz0RuuQhjLNuIdtO8sC/hDsBAYpmULibaubD/epm5qx0LvMtkQozVi9L+H2ycuHf9I/ToKVC3A+6OsNk6q5Yxg8SayAkBavEYtqeuCw8o2yyVwI6pPi+uTFi1ldQ5VmJVItNARShhvoZkzog2wIDAQAB";
		privateKey="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKZ+9EYRCrvrv4G6Q2l5tmbjTtgLPRG65CGMs24h207ywL+EOwEBimZQuJtq5sP96mbmrHQu8y2RCjNWL0v4fbJy4d/0j9OgpULcD7o6w2TqrljGDxJrICQFq8Ri2p64LDyjbLJXAjqk+L65MWLWV1DlWYlUi00BFKGG+hmTOiDbAgMBAAECgYAgPSDrmE0tyh5Q7lIzUEADeCxCJRr0He4imO39e0zN4q9z5hTOww+bdDHq1i5M8RO8TRpDfPvVbOo+uod5/GnKHeIl3rRk53wKO9ZHRfNlSsitq+mpaquiizJZwhG+4SaIyBK3vymd3XLT9lkyygzjf0YAQxkjvJXozo4iaFYfUQJBANrO6qO3JH9aNdhT6DpQGw3/8cxXJ4FZziMN3gCZWzG0nkldAKFBZpPuJpZ5/7Ov6MeMP3cH7PYld4PMc5DGuJ0CQQDCy8BSxxnPKyRrDSMNwFNhV4bMDzN9gNV8MuGS9qS+KFavCqeLhebEFhCL/3bCT/wGNY3JeRHlijqV5n9tuNnXAkA149JcYgXmK8SpM/k1K3eOWiQmbKy5KfIgJhEwWpTgSBjX/sTh8maeNoBgfUmh6shNJViYK2aMNSwbVOZ9mMPtAkAomIHaPngYfqs2TCSUBxLZZH5JJJnce+8B1TDBiflHT+zQye2k3CD9mIOWSYUcdcLWIG8OH9ck+SvAdar02OM9AkEAjIwjEHzWsU1FZRgcIznYldCOCNzQohhB63aWQurKMv/Q4r0BlXK11wh/fD/V07Vigh+Ibi8zpjRqQWjsU97ChA==";	
		((GlobalVariable)getApplication()).setPublicKey(publicKey);
		((GlobalVariable)getApplication()).setPrivateKey(privateKey);
        
        
        KeyMenuBtn=(Button)this.findViewById(R.id.KeyMenuBtn);
        KeyMenuBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent MyIntent=new Intent();
				MyIntent.setClass(BeamActivity.this, KeyMenu.class);
				startActivity(MyIntent);//換頁
		}});
        
        
        PlainTextEditText=(EditText)this.findViewById(R.id.editText1);  
        if(PlainText!=null)
        {
        	
        	PlainTextEditText.setText(PlainText);
        }
        EncryptedSecretKeyTextView=(TextView)this.findViewById(R.id.EncryptedSecretKeyTextView);
           if(encodedSecretkey!=null){
        	   EncryptedSecretKeyTextView.setText(encodedSecretkey);
	      }
        CipherTextView=(TextView)this.findViewById(R.id.CipherTextView);
	      if(CipherText!=null){
	    	  CipherTextView.setText(CipherText);
	      }
        
        ReceivePageBtn=(Button)this.findViewById(R.id.button1);
        ReceivePageBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent MyIntent=new Intent();
				MyIntent.setClass(BeamActivity.this, ReceiveActivity.class);
				startActivity(MyIntent);//換頁
				//BeamActivity.this.finish();//結束目前Activity 
		}});
        
        
        nfcCheck();     
        //Beaming Message
        BNMbtn=(Button)this.findViewById(R.id.button3);
        BNMbtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(CipherText!=null && encodedSecretkey!=null){
					
					NdefMessage message;
					NdefRecord[] MyNdefRecord=new NdefRecord[2];		
					MyNdefRecord[0] =BobNdefMessage.getNdefRecord_from_RTD_TEXT(CipherText, true);
					MyNdefRecord[1] =BobNdefMessage.getNdefRecord_from_RTD_TEXT(encodedSecretkey, true);
					message=new NdefMessage(MyNdefRecord);
				
					mNfcAdapter.setNdefPushMessage(message,BeamActivity.this);
					
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
				}else{
					Toast.makeText(BeamActivity.this, "請先準備CipherText及EncodedSecretkey", Toast.LENGTH_SHORT).show();
				}
			}
        	
        });
   
          EncriptPlainTextBtn=(Button)this.findViewById(R.id.button4);      
          //Encript PlainText with Secret key
          EncriptPlainTextBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 String Secretkey=((GlobalVariable)getApplication()).getKey();//get secret key
				 if(Secretkey!=null){
					 //Toast.makeText(BeamActivity.this, "secretKey:"+Secretkey, Toast.LENGTH_SHORT).show();				 
					 try {
						 PlainText=PlainTextEditText.getText().toString();
						 byte[]temp=AESCoder.encrypt(PlainText.getBytes(), Base64.decode(Secretkey, Base64.DEFAULT));//用密鑰將資料加密成密文
						 CipherText=Base64.encodeToString(temp,Base64.DEFAULT);						 
					 } catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 CipherTextView.setText(CipherText);//TextView 顯示密文
				 }else{
					 
					 
				 }
			}
        	  
          });
          //Encript SecretKey with Publickey
          EncriptSecretKeyBtn=(Button)this.findViewById(R.id.button5);
          EncriptSecretKeyBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)  {
				 String Secretkey=((GlobalVariable)getApplication()).getKey();//get secret key
				 String Publickey=((GlobalVariable)getApplication()).getPublicKey();//get Public key
				 if(Secretkey!=null)
				 {
					 //Toast.makeText(BeamActivity.this, "SecretKey:"+Secretkey, Toast.LENGTH_SHORT).show();
					 //Toast.makeText(BeamActivity.this, "Publickey:"+Publickey, Toast.LENGTH_SHORT).show();				
						 try {//RSA 公鑰加密					 
							 byte[] temp = RSA.encryptByPublicKey(Base64.decode(Secretkey,Base64.DEFAULT),Base64.decode(Publickey,Base64.DEFAULT));						
							encodedSecretkey=Base64.encodeToString(temp, Base64.DEFAULT);
							EncryptedSecretKeyTextView.setText(encodedSecretkey);
						} catch (Exception e) {
							EncryptedSecretKeyTextView.setText(e.getMessage().toString());
							e.printStackTrace();
						}
					 
				 }else{
					 
				 }
				 
			}
        	  
          });
        
        
        
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
}
