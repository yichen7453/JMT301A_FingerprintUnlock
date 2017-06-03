package com.eshare.fplock;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jmt.fps.Jmt101;
import com.jmt.fps.jmt_module_callback;
import com.jmt.fps.jmtcallback;
import com.eshare.fplock.R;

public class EnrollFP extends Activity implements jmtcallback, jmt_module_callback {

	private static final String TAG = "EnrollActivity";

	private static Jmt101 mSensor;

	private boolean user_cancel;

	private int iFpImageWidth;
	private int iFpImageHeight;

	private byte[] pCurrMinu;

	private int[] iCurrMinuSize;

	private byte[] mRGBFinger;

	private static BitmapPool mBitmap_Finger;

	public static EnrollFP instance = null;

	private Thread tid_enroll;

	private Handler uiUpdateHandler;

	private LinearLayout backgroundLayout;
	private TextView enroll_tip;
	private TextView enroll_state;
	private TextView enroll_fail;

	private String code;
	
	File f_saveFile = new File(Environment.getDataDirectory() + "/data/com.eshare.fplock/Record");
	
	private int FileNumber = 1;
	private int recordNumber = 1;
    
    private ImageView finger_center;
    
    private TextView tvTitle;
    private TextView tvMessage;
    
    private Button btnYes;
    private Button btnNo;
    
    private Dialog dialogError;
    private Dialog dialogExit;
    private Dialog dialogConfirm;
    
    private TextView tvConfirm;
    
    private int swipeCount = 16;
    
    private List<File> file = new ArrayList<File>();

	static final int BITMAP_POOL_SIZE 			= 5;

	static final int MSG_NATIVE_CALLBACK 		= 1;
	static final int MSG_THREAD_ENROLL_FIG 		= 4;
	static final int MSG_THREAD_ENROLL_OK 		= 5;
	static final int MSG_THREAD_Reader_Error 	= 8;
	static final int MSG_THREAD_ENROLL_NOMATCH 	= 9;
	static final int MSG_THREAD_OUTOFMEMORY 	= 10;
	static final int MSG_NO_SENSOR 				= 11;
	static final int MSG_POOR_IMG 				= 13;

	public int callback(int event) {
		int wh = 1;
		
		if (event == 0) {
			wh = 1000;
		} else if (event == 1) {
			wh = 1001;
		} else if (event == 2) {
			wh = 1002;
		} else if (event == 3) {
			wh = 1003;
		} else if (event == 4) {
			wh = 1004;
		}
		
		if (uiUpdateHandler != null) {
			Message m = uiUpdateHandler.obtainMessage(wh, 0, 0);
			uiUpdateHandler.sendMessage(m);
		}
		
		if (user_cancel) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public boolean module_callback(byte[] pfpimage, int ifpwidth, int ifpheight, int iadvice) {
		int RawIdx;
		int RgbIdx;
		int Pixel;
		byte[] pRGBFinger;

		iFpImageWidth = ifpwidth;
		iFpImageHeight = ifpheight;

		pRGBFinger = mRGBFinger;
		RgbIdx = 0;
		for (RawIdx = 0; RawIdx < ifpwidth * ifpheight; RawIdx++) {
			Pixel = (pfpimage[RawIdx] & 0xFF);
			pRGBFinger[RgbIdx++] = (byte) Pixel;
			pRGBFinger[RgbIdx++] = (byte) Pixel;
			pRGBFinger[RgbIdx++] = (byte) Pixel;
			pRGBFinger[RgbIdx++] = (byte) 0xFF;
		}

		if (uiUpdateHandler != null) {
			Message m = uiUpdateHandler.obtainMessage(MSG_NATIVE_CALLBACK, iadvice, 0);
			uiUpdateHandler.sendMessage(m);
		}
		
		return (!(user_cancel));
	}

	@SuppressLint("HandlerLeak")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enroll_activity);
		Log.e(TAG, "EnrollActivity OnCreate");
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		uiUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bitmap bitmap;

				switch (msg.what) {
				case MSG_NO_SENSOR:
					showDialog2("Error", "Not detect fingerprint sensor !!", "OK");
					break;

				case MSG_NATIVE_CALLBACK:
					if (iFpImageWidth > 0 && iFpImageHeight > 0 && msg.arg1 != Jmt101.QUALITY_ADVICE_SWIPEFINGER) {
						bitmap = mBitmap_Finger.getfirstBitmap();
						if (bitmap == null) {
							bitmap = Bitmap.createBitmap(iFpImageWidth, iFpImageHeight, Bitmap.Config.ARGB_8888);
						}
						bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mRGBFinger));
						mBitmap_Finger.putlastBitmap(bitmap);
					}
					break;

				case MSG_THREAD_ENROLL_FIG:											
					break;

				case MSG_THREAD_ENROLL_OK:					
					finger_center.setImageLevel(0);
					backgroundLayout.setBackgroundColor(Color.parseColor("#00AA00"));
					enroll_state.setText("");
					enroll_tip.setText("Complete.");
					enroll_tip.setTextColor(Color.WHITE);
					
					try {
						Thread.sleep(700);
						
						if (msg.arg1 == swipeCount) {
							
							Intent intent = new Intent();
							intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
							intent.putExtra("lockscreen.fingerprint_es_fallback", true);
							startActivity(intent);					
							finish();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
					
					break;				

				case MSG_THREAD_Reader_Error:
					showDialog2("Error", "Not detect fingerprint sensor !!", "OK");
					break;

				case MSG_THREAD_ENROLL_NOMATCH:									
					enroll_tip.setText("Please swipe SAME finger again !! ");
					enroll_tip.setTextColor(Color.RED);					
					break;

				case MSG_THREAD_OUTOFMEMORY:
					showDialog2("Error", "Out of memory !!", "OK");
					break;

				case MSG_POOR_IMG:
					enroll_tip.setText("");
					enroll_fail.setText("Make sure that your finger covers the entire sensor");					
					break;
					
				case 1000:
					Log.i(TAG, "NO FINGER TOUCH");
					break;
					
				case 1001:
					Log.i(TAG, "FINGER TOUCH");
					break;
					
				case 1002:
					Log.i(TAG, "TOUCH SHORT");					
					break; 
					
				case 1003:
					Log.i(TAG, "PRESS OK");	
					break;
					
				case 1004:
					Log.i(TAG, "FINGER TOUCH TOO LONGER");
					
					finger_center.setImageLevel(recordNumber);
					
					enroll_state.setText(recordNumber * 6.25 + " %");
					
					enroll_tip.setText(R.string.enroll_activity_tip1);
					enroll_fail.setText("");
					
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		mSensor = new Jmt101(this);
		mSensor.OpenLibrary("Don't care");

		mSensor.SetModCallback(this);
		
		user_cancel = false;

		mRGBFinger = new byte[Jmt101.FINGER_SIZE * 4];

		pCurrMinu = new byte[Jmt101.TEMPLATE_SIZE];
		
		iCurrMinuSize = new int[1];

		mBitmap_Finger = new BitmapPool(BITMAP_POOL_SIZE);

		initEnrollFPN();
		
		dialogConfirm = new Dialog(EnrollFP.this, R.style.MyDialogStyle);
		dialogConfirm.setContentView(R.layout.fragment);
		dialogConfirm.setCancelable(false);
		tvConfirm = (TextView) dialogConfirm.findViewById(R.id.fragment_confirm);
		tvConfirm.setClickable(true);
		tvConfirm.setFocusable(true);
		tvConfirm.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				dialogConfirm.dismiss();				
				code = getIntent().getStringExtra("code");
				if (code.equals("enroll")) {
					EnrollFinger();
				}				
			}
		});
		dialogConfirm.show();
	}
	
	private void initEnrollFPN() {
		
		backgroundLayout = (LinearLayout) findViewById(R.id.enroll_activity);
		
		enroll_tip = (TextView) findViewById(R.id.enroll_tip);
		enroll_state = (TextView) findViewById(R.id.enroll_state);
		enroll_fail = (TextView) findViewById(R.id.enroll_fail);
		
		finger_center = (ImageView) findViewById(R.id.img1);
		finger_center.setImageLevel(0);
		
		ScreenListener();
	}
	
	public void ScreenListener() {
		IntentFilter filter = new IntentFilter();
    	filter.addAction(Intent.ACTION_SCREEN_ON);
    	filter.addAction(Intent.ACTION_SCREEN_OFF);   	
    	registerReceiver(ScreenStatusReceiver, filter);
	}
	
	public void EnrollFinger() {
		Log.i(TAG, "Begin EnrollFinger Thread!!");
		Message uiMessage;
		
		File[] temFiles = f_saveFile.listFiles();
		
		if (temFiles.length == 0) {
			FileNumber = 1;
		} else {			
			Collections.addAll(file, temFiles);
			Collections.sort(file, new FileTimeComparator());
			
			for (File mCurrentFile : file) {							
				if ((mCurrentFile.getName().length() == 19 || mCurrentFile.getName().length() == 20) && (mCurrentFile.getName().indexOf("Fingerprint ") == 0)) {
					FileNumber = Integer.parseInt(mCurrentFile.getName().substring(1 + mCurrentFile.getName().indexOf(" "), mCurrentFile.getName().indexOf("_"))) + 1;					
				} 
			}
		}		
		
		if (Jmt101.OpenReader() != Jmt101.E_OK) {
			Log.e(TAG, "Sensor OpenReader Failure !!");
			
			uiMessage = new Message();
			uiMessage.what = MSG_NO_SENSOR;
			uiUpdateHandler.sendMessage(uiMessage);

		} else {
			Log.e(TAG, "Sensor OpenReader OK !!");			
			
			recordNumber = 1;

			tid_enroll = new Thread(new EnrollPieceThread());
			tid_enroll.setPriority(Thread.MAX_PRIORITY);
			tid_enroll.start();
		}
	}
	
	private class EnrollPieceThread implements Runnable {
		@Override
		public void run() {
			Log.e(TAG, "EnrollPiece Thread Runnable!!");
			int Module_Result;
			boolean keep_going = true;
			Message uiMessage;
			
			while (keep_going) {
				
				if (Thread.interrupted()) {
					Log.i(TAG, "EnrollFingerThread interrupted!");
					break;
				}

				if (user_cancel) {
					Log.i(TAG, "User cancel!");
					break;
				}
			
				Module_Result = mSensor.EnrollPiece(2500, pCurrMinu, iCurrMinuSize);
		
				if (Module_Result == Jmt101.E_OK) {
				
					File createfile = new File(Environment.getDataDirectory().getPath() + "/data/com.eshare.fplock/Record/Fingerprint " + FileNumber + "_" + recordNumber + ".bin");
				
					try {
						Log.e(TAG, "FP Enroll OK");
						FileOperate.writeToFile(createfile, pCurrMinu);
					
						uiMessage = new Message();
						uiMessage.what = MSG_THREAD_ENROLL_FIG;
						uiMessage.arg1 = recordNumber;
						uiUpdateHandler.sendMessage(uiMessage);
											
					} catch (IOException e) {
						e.printStackTrace();
					}	
				
					if (recordNumber == swipeCount) {
			
						uiMessage = new Message();
						uiMessage.what = MSG_THREAD_ENROLL_OK;
						uiMessage.arg1 = recordNumber;
						uiUpdateHandler.sendMessage(uiMessage);	
						
						keep_going = false;
					}
				
					recordNumber++;											
				} else {
					Log.e(TAG, "Enroll Failed !!");
					uiMessage = new Message();
					uiMessage.what = MSG_POOR_IMG;
					uiUpdateHandler.sendMessage(uiMessage);
				}	
			}
			Jmt101.CloseReader();
		}		
	}
	
	private void showDialog2(String pTitle, String pMsg, String pButton) {
		dialogError = new Dialog(EnrollFP.this, R.style.MyDialogStyle);
		dialogError.setContentView(R.layout.mydialog2);
		
		tvTitle = (TextView) dialogError.findViewById(R.id.title);
		tvTitle.setText(pTitle);
		
		tvMessage = (TextView) dialogError.findViewById(R.id.message);
		tvMessage.setText(pMsg);
		
		btnYes = (Button) dialogError.findViewById(R.id.positiveButton);
		btnYes.setText(pButton);
		btnYes.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				dialogError.dismiss();
				Intent intent = new Intent(EnrollFP.this, SetupIntro.class);
				startActivity(intent);
				finish();				
			}
		});
		dialogError.show();
	}

	public void onBackPressed() {
		dialogExit = new Dialog(this, R.style.MyDialogStyle);
		dialogExit.setContentView(R.layout.mydialog);
	
		tvTitle = (TextView) dialogExit.findViewById(R.id.title);
		tvTitle.setText("Exit");
			
		tvMessage = (TextView) dialogExit.findViewById(R.id.message);
		tvMessage.setText("Are you sure quit register fingerprint ?");
			
		btnYes = (Button) dialogExit.findViewById(R.id.positiveButton);
		btnYes.setText("Yes");
		btnYes.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				dialogExit.dismiss();
				
				if (mSensor != null) {
					mSensor.SetAbort();
			    	mSensor.CloseLibrary();
			    }
				
				if (recordNumber != swipeCount + 1) {
					for (int i = 1; i < swipeCount + 1; i++) {
						File file = new File(Environment.getDataDirectory().getPath() + "/data/com.eshare.fplock/Record/Fingerprint " + FileNumber + "_" + i + ".bin");
						if (file.exists()) {
							file.delete();
						}
					}
				}
				
				Intent intent = new Intent(EnrollFP.this, SetupIntro.class);
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
	
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "EnrollFP onDestory");
		
		if (mSensor != null) {
			mSensor.SetAbort();
	    	mSensor.CloseLibrary();
	    }

	    if ((tid_enroll != null) && (tid_enroll.isAlive())) {
	      tid_enroll.interrupt();
	    }   
	    
	    if (dialogExit != null) {
	    	dialogExit.dismiss();
	    }
	    
	    if (dialogConfirm != null) {
	    	dialogConfirm.cancel();
	    }
	    
	    if (dialogError != null) {
	    	dialogError.cancel();
	    }
	    
	    if (!dialogConfirm.isShowing()) {	    	
			if (mSensor != null) {
				mSensor.SetAbort();
				mSensor.CloseLibrary();

				if (recordNumber != swipeCount + 1) {
					for (int i = 1; i < swipeCount + 1; i++) {
						File file = new File(Environment.getDataDirectory().getPath() + "/data/com.eshare.fplock/Record/Fingerprint " + FileNumber + "_" + i + ".bin");
						if (file.exists()) {
							file.delete();
						}
					}
				}			    	 
			}
		}
	    
	    unregisterReceiver(ScreenStatusReceiver);
	}
	
	private final BroadcastReceiver ScreenStatusReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {	
			if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
				Log.e(TAG, "SCREEN_ON");
			}
			
			if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
				Log.e(TAG, "SCREEN_OFF");
				finish();
				
				if (!dialogConfirm.isShowing()) {
					if (mSensor != null) {
						mSensor.SetAbort();
						mSensor.CloseLibrary();	

						if (recordNumber != swipeCount + 1) {
							for (int i = 1; i < swipeCount + 1; i++) {
								File file = new File(Environment.getDataDirectory().getPath() + "/data/com.eshare.fplock/Record/Fingerprint " + FileNumber + "_" + i + ".bin");
								if (file.exists()) {
									file.delete();
								}
							}
						}			    	 
						finish();
					}
				}
			}
		}
	};
	
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