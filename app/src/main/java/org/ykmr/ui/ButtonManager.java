package org.ykmr.ui;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import android.view.MotionEvent;

public class ButtonManager {
	public static final String TAG = "ButtonManager";
	List<MyButton> mButtons = new LinkedList<MyButton>();
	MyButton mDown = null;
	public void addButton(MyButton btn){
		mButtons.add(btn);
	}
	public MyButton getButtonAt(int x, int y){
		for(MyButton b : mButtons)
			if(b.contains(x, y))
				return b;
		return null;
	}
	public boolean onTouch(MotionEvent e){
		int x = (int)e.getX();
		int y = (int)e.getY();
		MyButton b = getButtonAt(x, y);
		Log.d(TAG, e.getAction() + " : " + x + "," + y + " : " + b);
		if(b == null){
			if(mDown != null){
				mDown.setStatus(MyButton.STATUS_UP);
				return true;
			}
		}else{
			switch(e.getAction()){
			case MotionEvent.ACTION_DOWN:
				if(mDown != null)
					mDown.setStatus(MyButton.STATUS_UP);
				b.setStatus(MyButton.STATUS_DOWN);
				mDown = b;
				break;
			case MotionEvent.ACTION_UP:
				if(mDown == b)
					b.onTap();
				b.setStatus(MyButton.STATUS_UP);
				mDown = null;
				break;
			case MotionEvent.ACTION_MOVE:
				if(mDown != null && b != mDown){
					mDown.setStatus(MyButton.STATUS_UP);
					mDown = null;
				}
				break;
			}
			return true;
		}
		return false;
	}
}
