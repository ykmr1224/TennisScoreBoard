package org.ykmr.anim;

import java.util.LinkedList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.view.View;

public class AnimationManager extends Handler{
	static final int WAIT = 30;
	boolean mAnimating = false;
	List<AnimationItem> items = new LinkedList<AnimationItem>();
	List<AnimationItem> adding = new LinkedList<AnimationItem>();
	View mView;
	long mTime0;
	
	public static interface AnimationItem{
		void init(long globalTime);
		//return true if animation finished
		boolean reflesh(long globalTime, long delta);
		void finish();
	}

	//should invoked in UI Thread
	public AnimationManager(View view){
		this.mView = view;
		mTime0 = System.currentTimeMillis();
	}
	
	public void onStop(){
		mAnimating = false;
	}
	
	public void onResume(){
		startAnimating();
	}
	
	public boolean isAnimating(){
		return mAnimating;
	}
	
	public synchronized void addItem(AnimationItem item){
		adding.add(item);
		startAnimating();
	}
	
	private synchronized void startAnimating(){
		if(!mAnimating){
			mAnimating = true;
			sendMessage(obtainMessage(0));
		}
	}
	
	private synchronized void updateItems(){
		long time = System.currentTimeMillis();
		long delta = time - mTime0;
		mTime0 = time;

		//reflesh items;
		for(int i=0; i<items.size();){
			boolean finish = items.get(i).reflesh(time, delta);
			if(finish){
				AnimationItem rm = items.remove(i);
				if(mOnFinish != null)
					mOnFinish.onFinish(rm);
			}
			else i++;
		}
		//initialize added items
		for(AnimationItem item : adding){
			item.init(time);
			items.add(item);
		}
		adding.clear();

		if(items.size() == 0) mAnimating = false;
	}
	
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		updateItems();
		mView.invalidate();
		if(mAnimating)
			sleep(WAIT);
	}

	public void sleep(long delay){
		removeMessages(0);
		sendMessageDelayed(obtainMessage(0), delay);
	}

	public interface OnFinishListener{
		public void onFinish(AnimationItem item);
	}

	OnFinishListener mOnFinish = null;
	public void setOnFinish(OnFinishListener l){
		mOnFinish = l;
	}
}
