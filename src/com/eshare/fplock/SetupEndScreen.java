package com.eshare.fplock;

import com.eshare.fplock.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SetupEndScreen extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup_end);
		
		Intent intent = new Intent(SetupEndScreen.this, SetupIntro.class);
		startActivity(intent);
		finish();
	}
}