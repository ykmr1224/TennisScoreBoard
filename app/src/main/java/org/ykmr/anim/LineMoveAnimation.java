package org.ykmr.anim;

import org.ykmr.anim.AnimationManager.AnimationItem;

import android.graphics.Rect;
import android.view.animation.Interpolator;

public class LineMoveAnimation implements AnimationItem{
	private Rect mStart, mEnd;
	private long mDuration;
	Interpolator mInterpolater;
	
	private Rect mCurrent;
	private long mStartTime;
	private boolean mFinished = false;

	public LineMoveAnimation(Rect start, Rect end, long duration, Interpolator i){
		mStart = new Rect(start);
		mEnd = new Rect(end);
		mCurrent = new Rect(start);
		mDuration = duration;
		mInterpolater = i;
	}
	public void init(long globalTime){
		mStartTime = globalTime;
		mFinished = false;
	}
	public boolean reflesh(long globalTime, long delta) {
		if(mFinished){
			mCurrent = mEnd;
			return true;
		}else{
			long time = Math.min(mStartTime+mDuration, globalTime);
			float timeRatio = (float)(time-mStartTime)/mDuration;
			float ratio = mInterpolater.getInterpolation(timeRatio);
			mCurrent.left = (int)(mStart.left + (mEnd.left-mStart.left)*ratio);
			mCurrent.top = (int)(mStart.top + (mEnd.top-mStart.top)*ratio);
			mCurrent.right = (int)(mStart.right + (mEnd.right-mStart.right)*ratio);
			mCurrent.bottom = (int)(mStart.bottom + (mEnd.bottom-mStart.bottom)*ratio);
			if(globalTime >= mStartTime + mDuration){
				return true;
			}else{
				return false;
			}
		}
	}
	public Rect getCurrent(){
		return mCurrent;
	}
	@Override
	public void finish() {
		mFinished = true;
	}
}
