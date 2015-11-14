package org.ykmr.anim;

import org.ykmr.anim.AnimationManager.AnimationItem;

public class BlinkAnimation implements AnimationItem{
	int mOnDuration, mOffDuration, mTotalDuration;
	boolean mOn;
	long mStartTime;
	boolean mFinished;
	public BlinkAnimation(int onDuration, int offDuration, int totalDuration){
		mOnDuration = onDuration;
		mOffDuration = offDuration;
		mTotalDuration = totalDuration;
	}
	public boolean isOn(){
		return mOn;
	}
	public void init(long globalTime) {
		mStartTime = globalTime;
		mFinished = false;
	}
	public boolean reflesh(long globalTime, long delta) {
		if(mFinished){
			mOn = true;
			return true;
		}else{
			int time = (int)(globalTime-mStartTime);
			int rest = time%(mOnDuration+mOffDuration);
			mOn = rest < mOnDuration;
			if(time > mTotalDuration){
				mOn = true;
				return true;
			}else{
				return false;
			}
		}
	}
	@Override
	public void finish() {
		mFinished = true;
	}
}
