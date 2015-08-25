package com.example.nfcdemo;



import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.widget.Toast;


public class WriteTask extends AsyncTask<Void, Void, Void> {
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if(text!=null)
		{
			Toast.makeText(host, text, Toast.LENGTH_SHORT).show();
		}
		host.finish();
	}
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
		return null;
	}


}
