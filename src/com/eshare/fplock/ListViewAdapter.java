package com.eshare.fplock;

import java.util.ArrayList;
import java.util.List;

import com.eshare.fplock.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {
	
	private List<String> datas = new ArrayList<String>();
	
	private Context mContext;	

	public ListViewAdapter(Context context) {
		this.mContext = context;
	}	
	
	public void addData(String strData) {
		if (strData != null) {
			datas.add(strData);
		}
	}
	
	private void delData() {
		if (datas.size() > 0) {
			datas.remove(datas.size() - 1);
		}
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		
		if (datas.size() > 1) {
			if (position == 0) {
				view = View.inflate(mContext, R.layout.list_item_top, null);
			} else if (position == datas.size() - 1) {
				view = View.inflate(mContext, R.layout.list_item_bottom, null);
			} else {
				view = View.inflate(mContext, R.layout.list_item_middle, null);
			}
		} else {
			view = View.inflate(mContext, R.layout.list_item_single, null);
		}
		
		((TextView) view.findViewById(R.id.title)).setText(datas.get(position));
		
		return view;
	}	
}