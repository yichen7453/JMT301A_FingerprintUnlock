package com.eshare.fplock;

import com.eshare.fplock.R;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

public class EditTextWithDel extends EditText {
	
	private final static String TAG = "EditTextWithDel";
	
	private Drawable imgClear;
	
	private Context mContext;
	
	public EditTextWithDel(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public EditTextWithDel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}
	
	public EditTextWithDel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	private void init() {
		imgClear = mContext.getResources().getDrawable(R.drawable.delete_gray);
		
		addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			
			@Override
			public void afterTextChanged(Editable s) { 
				setDrawable();
			}
		});
		setDrawable();
	}
	
	private void setDrawable() {
		if (length() < 1) {
			setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		} else {
			setCompoundDrawablesWithIntrinsicBounds(null, null, imgClear, null);
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		if (imgClear != null && event.getAction() == MotionEvent.ACTION_UP) {
			int eventX = (int) event.getRawX();
			int eventY = (int) event.getRawY();
			Log.e(TAG, "eventX = " + eventX + "; eventY = " + eventY);
			Rect rect = new Rect();
			getGlobalVisibleRect(rect);
			rect.left = rect.right - 50;
			if (rect.contains(eventX, eventY)) {
				setText("");
			}
		}
		return super.onTouchEvent(event);	
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
	}
}