package com.eshare.fplock;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import com.android.internal.policy.IFingerprintLockCallback;
import com.android.internal.policy.IFingerprintLockInterface;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.jmt.fps.Jmt101;
import com.jmt.fps.jmt_module_callback;
import com.eshare.fplock.R;

public class FingerprintLockService extends Service implements jmt_module_callback {
	
	private static final String TAG = "FingerprintLockService";
	
	private static Jmt101 mSensor;
	
	private int iFpImageWidth;
	private int iFpImageHeight;
	
	private byte[] pUserMinu;
	private int[] iUserMinuSize;
	
	private byte[] mRGBFinger;	

	private static BitmapPool mBitmap_Finger; 
	
	private Thread unlock_match;
		
	private TextView unlock_tv;
	
	private ImageView screen_finger;
	
	private View mView;
	
	private PowerManager pm;
	
	private PowerManager.WakeLock wakeLock;
	
	private Vibrator vibrator;
	
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	
	private TranslateAnimation translateAnimation;

	File f_saveFile = new File(Environment.getDataDirectory() + "/data/com.eshare.fplock/Record");
	
	private Handler uiUpdateHandler;
	
	private int mFailedAttempts;
	
	private int mOpenReader;
	
	static final int MAX_FAILED_ATTEMPTS 		= 20;
	
	static final int BITMAP_POOL_SIZE 			= 5;
	
	static final int MSG_NATIVE_CALLBACK        = 1;
	static final int MSG_NO_SENSOR				= 2;
	static final int MSG_START_UI				= 3;
	static final int MSG_STOP_UI				= 4;
	static final int MSG_SUCCESSFUL				= 5;
	static final int MSG_FAILURE 				= 6;
	static final int MSG_NO_FILE 				= 7;
	static final int MSG_READY 					= 8;
	static final int MSG_POOR_IMG 				= 9;
	static final int MSG_OUTOFMEMORY			= 10;
	static final int MSG_THREAD_Reader_Error	= 11;
		
	private IFingerprintLockCallback mCallback;
	private IFingerprintLockInterface.Stub mService = new IFingerprintLockInterface.Stub() {
		
		@Override
		public void unregisterCallback(IFingerprintLockCallback cb) {
			Log.e(TAG, "FingerprintLockService unregisterCallback !!");
			mCallback = null;			
		}
		
		@Override
		public void stopUi() {
			uiUpdateHandler.sendEmptyMessage(MSG_STOP_UI);			
		}
		
		@Override
		public void startUi(IBinder containingWindowToken, int x, int y, int width, int height, boolean useLiveliness) throws RemoteException {
			Log.i(TAG, "startUi reached in service ");
			
			if (containingWindowToken == null) {
				Log.e(TAG, "containingWindowToken is null");
				return;
			}

			DisplayMetrics mDisplayMetrics = new DisplayMetrics();
			mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
			
			mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			
			mLayoutParams = new WindowManager.LayoutParams(1000);
			mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
			mLayoutParams.token = containingWindowToken;
			mLayoutParams.format = PixelFormat.RGBA_8888;
			mLayoutParams.x = x;
			mLayoutParams.y = y;
			mLayoutParams.width = width;
			mLayoutParams.height = height;
			mLayoutParams.gravity = Gravity.START ;
			mLayoutParams.packageName = "JMTFingerprintUnlock";
			Message uiMessage = uiUpdateHandler.obtainMessage(MSG_START_UI, mLayoutParams);
			uiMessage.sendToTarget();

		}
		
		@Override
		public void registerCallback(IFingerprintLockCallback cb) {
			Log.i(TAG, "FingerprintLockService registerCallback ");
			
			mCallback = cb;
			
			if (!f_saveFile.exists() || (f_saveFile.list().length <= 0)){
				Log.i(TAG, "FingerprintLockService registerCallback Fail , no fingerprint exist !!");
				try {
					cb.cancel();
				} catch (RemoteException e) {					
					e.printStackTrace();
				}
			} 
		}
	};

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
		return true;
	}
	
	@SuppressLint("HandlerLeak")
	@SuppressWarnings("deprecation")
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "FingerprintUnlockService onCreate ");
		
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);	

		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FingerprintLockService"); 
	    wakeLock.acquire();
		
		uiUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bitmap bitmap;
				switch (msg.what) {
				case MSG_NATIVE_CALLBACK:
					if (iFpImageWidth > 0 && iFpImageHeight > 0) {
						bitmap = mBitmap_Finger.getfirstBitmap();
						if (bitmap == null) {
							bitmap = Bitmap.createBitmap(iFpImageWidth, iFpImageHeight, Bitmap.Config.ARGB_8888);
						}
						bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mRGBFinger));
						mBitmap_Finger.putlastBitmap(bitmap);
					}
					break;
					
				case MSG_NO_SENSOR:
					Drawable tip = getResources().getDrawable(R.drawable.screen_finger_tip);
					screen_finger.setImageDrawable(tip);
					unlock_tv.setText("Not detect fingerprint sensor !!");
					break;	
					
				case MSG_START_UI:
					if ((mView == null) || (mWindowManager == null) || (msg.obj == null)) {
						Log.e(TAG, "Unlock view could not open ");
						return;
					}
					mWindowManager.addView(mView, (WindowManager.LayoutParams)msg.obj);
					Log.i(TAG, "Add unlock view ");
					break;
					
				case MSG_STOP_UI:					
					mView.setVisibility(View.INVISIBLE);
					if (mWindowManager != null) {
						mWindowManager.removeView(mView);
						Log.i(TAG,  "Remove unlock view ");
					}
					break;
					
				case MSG_SUCCESSFUL:				
					Log.i(TAG, "handleMessge(): MSG_SUCCESSFUL.");
					
					unlock_tv.setText("Recognized successfully !!");
										
					vibrator.vibrate(new long[]{100, 700}, -1);
										
					try {
						mCallback.unlock();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					break;
					
				case MSG_FAILURE:	
					Log.i(TAG, "handleMessge(): MSG_FAILURE.");
					
					mFailedAttempts++;
					if (mFailedAttempts < MAX_FAILED_ATTEMPTS) {
						showReplacedFP();						
						MatchFinger();
					} else {
						try {
							mCallback.reportFailedAttempt();
							mCallback.cancel();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					
					break;
					
				case MSG_NO_FILE:
					try {
						mCallback.reportFailedAttempt();
						mCallback.cancel();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					break;			
					
				case MSG_OUTOFMEMORY:
					unlock_tv.setText("Out of memory");
					break;
				
				case MSG_POOR_IMG:
					unlock_tv.setText("Fingerprint quality poor, try again.");
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		mSensor = new Jmt101();
		mSensor.OpenLibrary("Don't care");

		mRGBFinger = new byte[Jmt101.FINGER_SIZE * 4];
		
		pUserMinu = new byte[Jmt101.TEMPLATE_SIZE];		
		iUserMinuSize = new int[1];
	    
	    mBitmap_Finger = new BitmapPool(BITMAP_POOL_SIZE);

	    mView = View.inflate(this, R.layout.match, null);
	    
	    screen_finger = (ImageView) mView.findViewById(R.id.screen_finger);
	    screen_finger.setScaleType (ImageView.ScaleType.FIT_CENTER);
			 
		unlock_tv = (TextView) mView.findViewById(R.id.unlock_tv);
	    unlock_tv.setText("Use fingerprint to unlock");
	    
	    mFailedAttempts = 0;
	    
	    mOpenReader = 0;
	    
	    if (f_saveFile.exists() && (f_saveFile.list().length > 0)){
			
			MatchFinger();			
		}
	}

	private void MatchFinger() {
		Message uiMessage;
		
		if (Jmt101.OpenReader() != Jmt101.E_OK) {
			Log.i(TAG, "mSensor.OpenReader Failure !!");
			mOpenReader++;
			
			if (mOpenReader < 20) {
				MatchFinger();
			} else {
				uiMessage = new Message();
				uiMessage.what = MSG_NO_SENSOR;
				uiUpdateHandler.sendMessage(uiMessage);
			}
			
			return;
		} else {
			Log.i(TAG, "mSensor.OpenReader OK !!");
			
			unlock_match = new Thread(new MatchFingerThread());
			unlock_match.setPriority(Thread.MAX_PRIORITY);			
			unlock_match.start();
		}
	}
	
	private class MatchFingerThread implements Runnable {
		@Override
		public void run() {
			Log.i(TAG, "MatchFingerThread Runnable!!");
			boolean keep_going = true;
			int Module_Result;
			Message uiMessage;
		
			while (keep_going) {
				if (Thread.interrupted()) {
					Log.i (TAG, "MatchFingerThread interrupted!"); 
					break;
				}
				
				Module_Result = mSensor.GetFinger(pUserMinu, iUserMinuSize);
				
				switch (Module_Result) {
				case Jmt101.E_OK:
	            	
					ArrayList<File> filePathList = FileOperate.getFileList(f_saveFile);
					byte[] fileContent = null;
	            	
					for (File list:filePathList) { 
						try {
							fileContent = FileOperate.getContentByLocalFile(list);
	            			
							Module_Result = mSensor.MatchFinger(pUserMinu, iUserMinuSize[0], fileContent, fileContent.length); 	            			
	            			
							if (Module_Result == Jmt101.E_OK) {
								Log.i(TAG, "FP Verify OK!!");	            		
	            				
								uiMessage = new Message();
								uiMessage.what = MSG_SUCCESSFUL;
								uiUpdateHandler.sendMessage(uiMessage);
	            				           				
								keep_going = false;
								break;
							} 
						} catch (IOException e) {
							e.printStackTrace();
						}            		
					}
	            	
					if (Module_Result != Jmt101.E_OK) {
						Log.i(TAG, "FP Verify Fail, Try again");
						uiMessage = new Message();
						uiMessage.what = MSG_FAILURE;
						uiMessage.arg1 = mFailedAttempts;
						uiUpdateHandler.sendMessage(uiMessage);
            			
						keep_going = false;
						break;
					}
	            
					break;

				case Jmt101.E_TIMEOUT:
					/* notify user to swipe finger */
					break;

				case Jmt101.E_CANCELLED:
					Log.i (TAG, "User Cancel");
					keep_going = false;
					break;

				case Jmt101.E_OUTOFMEMORY:
					Log.i (TAG, "Out of memory");
					uiMessage = new Message();
					uiMessage.what = MSG_OUTOFMEMORY;
					uiUpdateHandler.sendMessage(uiMessage);
					keep_going = false;
					break;

					case Jmt101.E_READER:
						Log.i (TAG, "Access FP Reader Error");
						uiMessage = new Message();
						uiMessage.what = MSG_THREAD_Reader_Error;
						uiUpdateHandler.sendMessage(uiMessage);  
						keep_going = false;
						break;
	               
					case Jmt101.E_POOR_IMG:
						Log.i (TAG, "Fingerprint quality poor, try again");
	            	
						uiMessage = new Message();
						uiMessage.what = MSG_POOR_IMG;
						uiUpdateHandler.sendMessage(uiMessage);
						break;

					default:
						//Log.i (TAG, "Something wrong here");
						//keep_going = false;
						//break;			
				}
			}
			
			Jmt101.CloseReader ();		
		} 
	}
	
	private void showReplacedFP() {
		
		Drawable transition = getResources().getDrawable(R.drawable.screen_finger_tip);
		
		screen_finger.setImageDrawable(transition);
		
		unlock_tv.setText("Verify failed , try again !");
		
		vibrator.vibrate(new long[] { 100, 300 }, -1);
		
		translateAnimation = new TranslateAnimation(10, -10, 0, 0);
        translateAnimation.setInterpolator(new OvershootInterpolator());
        translateAnimation.setDuration(50);     
        translateAnimation.setRepeatCount(3);
        translateAnimation.setRepeatMode(Animation.REVERSE);
			
		screen_finger.setAnimation(translateAnimation);
		screen_finger.startAnimation(translateAnimation);
		unlock_tv.setAnimation(translateAnimation);
		unlock_tv.startAnimation(translateAnimation);
		translateAnimation.start();
	}
	
	@Override
	public void onDestroy() {
		Log.e(TAG, "FingerprintLockService onDestory ");
		
		vibrator.cancel();	
		
		if (translateAnimation != null) {
			translateAnimation.cancel();
		}
		
		if (mSensor != null) {
			mSensor.SetAbort();
			mSensor.CloseLibrary();
		}
		
		if ((unlock_match != null) && (unlock_match.isAlive())) {
			unlock_match.interrupt();
		}

		if (wakeLock != null) {
			wakeLock.release();
		}
				
		super.onDestroy();
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return mService;
	}
}