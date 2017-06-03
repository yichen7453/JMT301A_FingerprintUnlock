package com.eshare.fplock;

import java.io.File;

import com.eshare.fplock.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditFP extends Activity {
	
	private String fpName;
	
	private EditTextWithDel mEditTextWithDel;
	
	private Button btnBack;
	private Button btnRename;
	private Button btnDelete;
	
    private TextView tvTitle;
    private TextView tvMessage;
    
    private Button btnYes;
    private Button btnNo;
 
    private Dialog dialogCorfirm;
    private Dialog dialogRename;
    private Dialog dialogDelete;
    private Dialog dialogExit;
    
    private boolean sameFile;
    
    private int swipeCount = 16;
  
	File f_saveFile = new File(Environment.getDataDirectory() + "/data/com.eshare.fplock/Record");
	
	private File[] file = new File[swipeCount + 1];
	private String[] str = new String[swipeCount + 1];
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fp_edit);
		
		btnBack = (Button) findViewById(R.id.edit_back_btn);
		btnBack.setOnClickListener(btnBack_OnClick);
		
		btnRename = (Button) findViewById(R.id.fp_rename);
		btnRename.setOnClickListener(btnRename_OnClick);
		
		btnDelete = (Button) findViewById(R.id.fp_delete);
		btnDelete.setOnClickListener(btnDelete_OnClcik);
		
		mEditTextWithDel = (EditTextWithDel) findViewById(R.id.fp_edit_rename);	
		fpName = getIntent().getStringExtra("FP_Name");
		mEditTextWithDel.setText(fpName);
		mEditTextWithDel.setSelection(fpName.length());
		
		sameFile = false;
	}
	
	private Button.OnClickListener btnBack_OnClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {			
			Intent intent = new Intent(EditFP.this, SetupIntro.class);
			startActivity(intent);
			finish();			
		}		
	};
	
	private Button.OnClickListener btnRename_OnClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {			
			File[] temFiles = f_saveFile.listFiles();
			
			for (int i = 1; i < swipeCount + 1; i++) {
				file[i] = new File(Environment.getDataDirectory() + "/data/com.eshare.fplock/Record/" + fpName + "_" + i + ".bin");
				str[i] = new String(Environment.getDataDirectory() + "/data/com.eshare.fplock/Record" + "/" + mEditTextWithDel.getText().toString() + "_" + i + ".bin");
			
				for (File mCurrentFile : temFiles) {			
					if (str[i].equals(mCurrentFile.getPath())) {
						sameFile = true;
					}
				}
			}	
			
			if (mEditTextWithDel.getText().toString().equals("")) {
				dialogCorfirm = new Dialog(EditFP.this, R.style.MyDialogStyle);
				dialogCorfirm.setContentView(R.layout.mydialog2);
			
				tvTitle = (TextView) dialogCorfirm.findViewById(R.id.title);
				tvTitle.setText("Attention");
				
				tvMessage = (TextView) dialogCorfirm.findViewById(R.id.message);
				tvMessage.setText("Fingerprint name\n can't be null");
				
				btnYes = (Button) dialogCorfirm.findViewById(R.id.positiveButton);
				btnYes.setText("OK");
				btnYes.setOnClickListener(new OnClickListener() {				
					@Override
					public void onClick(View v) {
						dialogCorfirm.dismiss();
					}
				});
							
				dialogCorfirm.show();
				
			} else if (sameFile) {
				dialogRename = new Dialog(EditFP.this, R.style.MyDialogStyle);
				dialogRename.setContentView(R.layout.mydialog);
			
				tvTitle = (TextView) dialogRename.findViewById(R.id.title);
				tvTitle.setText("Attention");
				
				tvMessage = (TextView) dialogRename.findViewById(R.id.message);
				tvMessage.setText("File name already exist,\n do you sure overwrite?");
				
				btnYes = (Button) dialogRename.findViewById(R.id.positiveButton);
				btnYes.setText("Yes");
				btnYes.setOnClickListener(new OnClickListener() {				
					@Override
					public void onClick(View v) {						
						dialogRename.dismiss();
						
						for (int i = 1; i < swipeCount + 1; i++) {
							file[i].renameTo(new File(str[i]));
						}						
						
						Intent intent = new Intent(EditFP.this, SetupIntro.class);
						startActivity(intent);
						finish();
					}
				});
				
				btnNo = (Button) dialogRename.findViewById(R.id.negativeButton);
				btnNo.setText("No");
				btnNo.setOnClickListener(new OnClickListener() {				
					@Override
					public void onClick(View v) {
						sameFile = false;
						dialogRename.dismiss();
					}
				});
				
				dialogRename.show();
						
			} else {
				
				for (int i = 1; i < swipeCount + 1; i++) {
					file[i].renameTo(new File(str[i]));
				}
				
				Intent intent = new Intent(EditFP.this, SetupIntro.class);
				startActivity(intent);
				finish();
			}			
		}		
	};
	
	private Button.OnClickListener btnDelete_OnClcik = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {			
			dialogDelete = new Dialog(EditFP.this, R.style.MyDialogStyle);
			dialogDelete.setContentView(R.layout.mydialog);
		
			tvTitle = (TextView) dialogDelete.findViewById(R.id.title);
			tvTitle.setText("Attention");
			
			tvMessage = (TextView) dialogDelete.findViewById(R.id.message);
			tvMessage.setText("Are you sure delete fingerprint?");
			
			btnYes = (Button) dialogDelete.findViewById(R.id.positiveButton);
			btnYes.setText("Yes");
			btnYes.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					dialogDelete.dismiss();
					for (int i = 1; i < swipeCount + 1; i++) {
						File file = new File(Environment.getDataDirectory() + "/data/com.eshare.fplock/Record/" + fpName + "_" + i + ".bin");
						if (file.exists()) {
			    			file.delete();
			    		}
					}
					
					Intent intent = new Intent(EditFP.this, SetupIntro.class);
					startActivity(intent);
					finish();
				}
			});
			
			btnNo = (Button) dialogDelete.findViewById(R.id.negativeButton);
			btnNo.setText("No");
			btnNo.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					dialogDelete.dismiss();
				}
			});
			
			dialogDelete.show();			
		}		
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
	
			dialogExit = new Dialog(this, R.style.MyDialogStyle);
			dialogExit.setContentView(R.layout.mydialog);
			
			tvTitle = (TextView) dialogExit.findViewById(R.id.title);
			tvTitle.setText("Exit");
				
			tvMessage = (TextView) dialogExit.findViewById(R.id.message);
			tvMessage.setText("Are you sure quit \n fingerprint edit ?");
				
			btnYes = (Button) dialogExit.findViewById(R.id.positiveButton);
			btnYes.setText("Yes");
			btnYes.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					dialogExit.dismiss();
					Intent intent = new Intent(EditFP.this, SetupIntro.class);
					startActivity(intent);
					finish();
				}
			});
				
			btnNo = (Button) dialogExit.findViewById(R.id.negativeButton);
			btnNo.setText("No");
			btnNo.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					dialogExit.dismiss();
				}
			});
				
			dialogExit.show();
		}
		return false;		
	}
	
	protected void onDestroy() {
		super.onDestroy();
		
		if (dialogRename != null) {
			dialogRename.cancel();
		}
		
		if (dialogDelete != null) {
			dialogDelete.cancel();
		}
		
		if (dialogExit != null) {
			dialogExit.cancel();
		}
	}
	
	/**
	 * Use android event distribution mechanism, the user click anywhere except EditText hide the keyboard
	 * @param event
	 * @return
	 */
	public boolean dispatchTouchEvent(MotionEvent event){
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			View view = getCurrentFocus();
			if (isShouldHideInput(view, event)) {
				mEditTextWithDel.clearFocus();				
				hideSoftInput(view.getWindowToken());
			}
		}
		return super.dispatchTouchEvent(event);
	}
	
	private boolean isShouldHideInput(View view, MotionEvent event) {
		if (view != null && (view instanceof EditText)) {
			int[] l = {0,0};
			view.getLocationInWindow(l);
			int left = l[0], top = l[1], right = left + view.getWidth(), bottom = top + view.getHeight();
			if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
				return false;
			} else {
				return true;
			}			
		}
		return false;
	}
	
	private void hideSoftInput(IBinder token) {
		if (token != null) {
			InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}