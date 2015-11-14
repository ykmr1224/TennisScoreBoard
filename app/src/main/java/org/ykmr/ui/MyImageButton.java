package org.ykmr.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class MyImageButton extends MyRectButton{
	Bitmap mBmp;
	int mPadding;
	public MyImageButton(int id, Bitmap bmp, int x, int y, int w, int h, int padding) {
		super(id, x, y, w, h);
		mBmp = bmp;
		mPadding = padding;
	}
	public void onDraw(Canvas canvas){
		Paint p = new Paint();
		Rect imageRect = new Rect(mRect.left+mPadding, mRect.top+mPadding,
				mRect.right-mPadding, mRect.bottom-mPadding);
		canvas.drawBitmap(mBmp, null, imageRect, p);
		if(mStatus == STATUS_DOWN){
			p.setColor(0x44000000);
			canvas.drawRoundRect(new RectF(mRect), mRect.width()/5, mRect.height()/5, p);
		}
	}
	public void setPadding(int padding){
		mPadding = padding;
	}
}
