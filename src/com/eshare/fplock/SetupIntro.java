package com.eshare.fplock;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.eshare.fplock.R;
import com.jmt.fps.Jmt101;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SetupIntro extends Activity {

	private static Jmt101 mSensor;
	
	private ListView mListView;
	
	private ListViewAdapter mListViewAdapter;
	
	public CheckBox dontShowAgain;
	
	private Dialog dialogAttention;
	private Dialog dialogDeletAll;
	
	private TextView tvTitle;
	private TextView tvMessage;
	
	private Button btnYes;
	private Button btnNo;
	
	private Button btnBack;
	private Button btnOverflow;
	private Button btnRegister;
	
	private ToggleButton toggleBtn;
	
	private TextView tvAPP;
	private TextView tvSDK;

	private List<File> file = new ArrayList<File>();
	
	private int swipeCount = 16;

	File f_saveFile = new File(Environment.getDataDirectory() + "/data/com.eshare.fplock/Record");
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_finger);
		
		mSensor = new Jmt101();
		
		toggleBtn = (ToggleButton) findViewById(R.id.toggleBtn);
		
		if (!f_saveFile.exists()) {
			f_saveFile.mkdir();
		} else if (f_saveFile.exists() && f_saveFile.listFiles().length == 0) {
			toggleBtn.setChecked(false);
		} else {
			toggleBtn.setChecked(true);
		}
		
		toggleBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (f_saveFile.listFiles().length == 0) {
						dialogAttention = new Dialog(SetupIntro.this, R.style.MyDialogStyle);
						dialogAttention.setContentView(R.layout.mydialog2);
					
						tvTitle = (TextView) dialogAttention.findViewById(R.id.title);
						tvTitle.setText("Attention");
					
						tvMessage = (TextView) dialogAttention.findViewById(R.id.message);
						tvMessage.setText("No fingerprint,\n please register !");
					
						btnYes = (Button) dialogAttention.findViewById(R.id.positiveButton);
						btnYes.setText("OK");
						btnYes.setOnClickListener(new OnClickListener() {				
							@Override
							public void onClick(View v) {
								dialogAttention.dismiss();
								
								toggleBtn.setChecked(false);
							}
						});
						dialogAttention.show();
					} else {
						
						Intent intent = new Intent();
						intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
						intent.putExtra("lockscreen.fingerprint_es_fallback", true);
						startActivity(intent);
						finish();
						
					}
				} else {					
				}				
			}
		});
		
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setOnItemClickListener(listView);
				
		mListViewAdapter = new ListViewAdapter(this);
		
		btnBack = (Button) findViewById(R.id.login_reback_btn);
		btnBack.setOnClickListener(btnBack_OnClick);
		
		btnOverflow = (Button) findViewById(R.id.login_overflow_btn);
		btnOverflow.setOnClickListener(btnOverflow_OnClick);
		
		btnRegister = (Button) findViewById(R.id.register_finger_btn);
		btnRegister.setOnClickListener(btnRegister_OnClick);
		
		tvAPP = (TextView) findViewById(R.id.app_version);
		tvAPP.setText("APP_version : 1.0.0.1");
		
		tvSDK = (TextView) findViewById(R.id.sdk_version);
		tvSDK.setText("SDK_version : " + mSensor.GetSDKVersion());
		
		readData();
	}
	
	private AdapterView.OnItemClickListener listView = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent intent = new Intent(SetupIntro.this, EditFP.class);
			intent.putExtra("FP_Name", parent.getItemAtPosition(position).toString());
			startActivity(intent);
			finish();
		}
	}; 
	
	private Button.OnClickListener btnBack_OnClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			System.exit(0);			
		}		
	};
	
	private Button.OnClickListener btnOverflow_OnClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (f_saveFile.listFiles().length == 0) {
				dialogAttention = new Dialog(SetupIntro.this, R.style.MyDialogStyle);
				dialogAttention.setContentView(R.layout.mydialog2);
			
				tvTitle = (TextView) dialogAttention.findViewById(R.id.title);
				tvTitle.setText("Attention");
			
				tvMessage = (TextView) dialogAttention.findViewById(R.id.message);
				tvMessage.setText("No fingerprint,\n please register !");
			
				btnYes = (Button) dialogAttention.findViewById(R.id.positiveButton);
				btnYes.setText("OK");
				btnYes.setOnClickListener(new OnClickListener() {				
					@Override
					public void onClick(View v) {
						dialogAttention.dismiss();
					}
				});
				dialogAttention.show();
				
			} else {
				dialogDeletAll = new Dialog(SetupIntro.this, R.style.MyDialogStyle);
				dialogDeletAll.setContentView(R.layout.mydialog);
			
				tvTitle = (TextView) dialogDeletAll.findViewById(R.id.title);
				tvTitle.setText("Attention");
			
				tvMessage = (TextView) dialogDeletAll.findViewById(R.id.message);
				tvMessage.setText("Are you sure delete \n all fingerprint ?");
			
				btnYes = (Button) dialogDeletAll.findViewById(R.id.positiveButton);
				btnYes.setText("Yes");
				btnYes.setOnClickListener(new OnClickListener() {				
					@Override
					public void onClick(View v) {
						dialogDeletAll.dismiss();
						File[] temFiles = f_saveFile.listFiles();
					
						for(File mCurrentFile:temFiles) {							
							mCurrentFile.delete();
						}
					
						Intent intent = new Intent();			  
						intent = new Intent(SetupIntro.this, SetupIntro.class);
						startActivity(intent);
						finish();
					}
				});
			
				btnNo = (Button) dialogDeletAll.findViewById(R.id.negativeButton);
				btnNo.setText("No");
				btnNo.setOnClickListener(new OnClickListener() {				
					@Override
					public void onClick(View v) {
						dialogDeletAll.dismiss();
					}
				});
						
				dialogDeletAll.show();
			}
		}		
	};
	
	private Button.OnClickListener btnRegister_OnClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			if ((f_saveFile.list().length / swipeCount) < 5) {
				Intent intent = new Intent(SetupIntro.this, EnrollFP.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent.putExtra("code", "enroll");
				startActivity(intent);
				finish();
			} 			
		}		
	};
	
	private void readData() {
		
		if (!f_saveFile.exists() || (f_saveFile.list().length == 0)) {
			f_saveFile.mkdir();
			
			File[] temFiles = f_saveFile.listFiles();
			
			for(File mCurrentFile:temFiles) {							
				mListViewAdapter.addData(mCurrentFile.getName());
			}
			mListView.setAdapter(mListViewAdapter);
		} else if (f_saveFile.exists() && (f_saveFile.list().length > 0)) {
			
			File[] temFiles = f_saveFile.listFiles();
			
			Collections.addAll(file, temFiles);
			Collections.sort(file, new FileTimeComparator());
			
			for (File mCurrentFile : file) {	
				if (Integer.parseInt(mCurrentFile.getName().substring(1 + mCurrentFile.getName().indexOf("_"), mCurrentFile.getName().indexOf("."))) == swipeCount) {
					mListViewAdapter.addData(mCurrentFile.getName().substring(0, mCurrentFile.getName().lastIndexOf("_")));
				} 
			}
			
			mListView.setAdapter(mListViewAdapter);
		}	
		
		if ((f_saveFile.list().length / swipeCount) == 5 ) {
			btnRegister.setEnabled(false);
			btnRegister.setText("Already have 5 fingerprints !!");
		}		
	}
	
	
	
	protected void onDestroy() {
		super.onDestroy();
		
		if (dialogDeletAll != null) {
			dialogDeletAll.cancel();
		}
	}
	
	/*
	 * 按照文件修改時間比較
	 */
	private static class FileTimeComparator implements Comparator<File> {
		@Override
		public int compare(File lhs, File rhs) {
			long lastModified1 = lhs.lastModified();
			long lastModified2 = rhs.lastModified();
			return lastModified1 > lastModified2 ? 1 : (lastModified1 == lastModified2 ? 0 : -1); //遞增
			//return lastModified1 > lastModified2 ? -1 : (lastModified1 == lastModified2 ? 0 : 1); //遞減
		}
	}
}