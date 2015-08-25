package com.example.nfcdemo;

import android.app.Application;

public class GlobalVariable extends Application {
	
	byte[] key;

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}
	
}
