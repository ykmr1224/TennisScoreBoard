package org.ykmr.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class MyRectButton implements MyButton{
	public static String TAG = "MyRectButton";
	Rect mRect;
	int mStatus = STATUS_UP;
	int mId;

	public MyRectButton(int id, int x, int y, int w, int h) {
		mId = id;
		mRect = new Rect(x, y, x+w, y+h);
	}
	public void setRect(int x, int y, int w, int h){
		mRect.set(x, y, x+w, y+h);
	}
	public boolean contains(int x, int y) {
		return mRect.contains(x, y);
	}
	public void onTap() {
		Log.d(TAG, "onTap : " + mRect.toString());
		if(mListener != null)
			mListener.onTap(mId);
	}
	public void setStatus(int status) {
		mStatus = status;
	}
	public void onDraw(Canvas canvas){
		Paint p = new Paint();
		p.setColor(mStatus==STATUS_UP?0xffffffff:0xffff7777);
		canvas.drawRect(mRect, p);
	}
	ButtonTapListener mListener;
	public void setListener(ButtonTapListener l) {
		mListener = l;
	}
}
