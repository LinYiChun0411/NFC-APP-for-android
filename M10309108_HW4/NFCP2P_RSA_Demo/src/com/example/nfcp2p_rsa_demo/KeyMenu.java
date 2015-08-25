package com.example.nfcp2p_rsa_demo;



import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class KeyMenu extends Activity {
	
    Button ShowPublicKeyBtn;
    Button ShowPrivateKeyBtn;
    Button SecretKeyGenerateBtn;
    TextView secretKeyTextView;
    Button SaveBtn;
    String publicKey;
    String privateKey;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.keymenu);
		publicKey=((GlobalVariable)getApplication()).getPublicKey();
		privateKey=((GlobalVariable)getApplication()).getPrivateKey();
		
		
		secretKeyTextView=(TextView)this.findViewById(R.id.SecretKeyTestView);	
		String SecretKey=((GlobalVariable)getApplication()).getKey();
		if(SecretKey!=null){
			secretKeyTextView.setText(SecretKey);
		}
		
		
		ShowPublicKeyBtn=(Button)this.findViewById(R.id.button1);
		ShowPublicKeyBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Builder MyAlertDialog = new AlertDialog.Builder(KeyMenu.this);	
				MyAlertDialog.setTitle("PublicKey");	
				MyAlertDialog.setMessage(publicKey);
				MyAlertDialog.show();
			}
			
		});
		ShowPrivateKeyBtn=(Button)this.findViewById(R.id.button2);
		ShowPrivateKeyBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Builder MyAlertDialog = new AlertDialog.Builder(KeyMenu.this);	
				MyAlertDialog.setTitle("PrivateKey");	
				MyAlertDialog.setMessage(privateKey);
				MyAlertDialog.show();
			}
			
		});
		
		SecretKeyGenerateBtn=(Button)this.findViewById(R.id.button3);
		SecretKeyGenerateBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				byte[] key;
				try {
					key = AESCoder.initKey();
					String base64stringKey=Base64.encodeToString(key, Base64.DEFAULT);
					((GlobalVariable)getApplication()).setKey(base64stringKey);
					secretKeyTextView.setText(Base64.encodeToString(key, Base64.DEFAULT));
				}
				catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}	
			}
		});
		SaveBtn=(Button)this.findViewById(R.id.button4);
		SaveBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Toast.makeText(KeyMenu.this, "save the secretKey",Toast.LENGTH_SHORT).show();
				finish();
			}
			
		});
				
	}
	

}
