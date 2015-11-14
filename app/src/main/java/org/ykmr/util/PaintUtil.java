package org.ykmr.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.util.Log;

public class PaintUtil {
	public static final String TAG = "PaintUtil";
	public static int fitTextSize(Paint p, String text, int w, int h){
		int res = h;
		while(true){
			Rect rect = new Rect();
			p.setTextSize(res);
			p.getTextBounds(text, 0, text.length(), rect);
			if(rect.width() < w && rect.height() < h) break;
			res--;
		}
		return res;
	}
	
	//指定の幅に収まる文字数を計算する.
	public static int getFittingLength(Paint p, String text, int w){
		int cnt = 1;
		for(;cnt<text.length();cnt++){
			Rect rect = new Rect();
			p.getTextBounds(text, 0, cnt, rect);
			if(rect.width()>w)
				return cnt-1;
		}
		return cnt;
	}
	
	//戻り値：size
	//引数resultに[1行目の文字数, 2行目の文字数, ...]をセットする
	public static int fitTextSizeMultiLine(Paint p, String text, int w, int h, int lines, int space, int[] result){
		int lineH = (h-space*(lines-1))/lines;
		//まずは縦に目一杯で収まる前提で計算
		int size = fitTextSize(p, text.substring(0, 1), w, lineH);
		for(;size > 0; size--){
			p.setTextSize(size);
			int current = 0;
			for(int i=0; i<lines && current < text.length(); i++){
				int l = getFittingLength(p, text.substring(current), w);
				current += l;
				result[i] = l;
//				Log.d(TAG, "fitTextSizeMultiLine : current = " + current);
			}
			if(current == text.length())
				break;
		}
		return size;
	}

	public static void drawText(Canvas c, Paint p, int x, int y, int w, int h, String text, boolean fit){
		drawText(c, p, x, y, w, h, text, fit, 0.5f);
	}

	public static void drawText(Canvas c, Paint p, int x, int y, int w, int h, String text, boolean fit, float align){
		Rect bounds = new Rect();
		if(fit){
			int size = fitTextSize(p, text, w, h);
			p.setTextSize(size);
		}else{
			p.setTextSize(h-1);
		}
		FontMetrics metrics = p.getFontMetrics();
		p.getTextBounds(text, 0, text.length(), bounds);
		int texty = y+h/2+(int)(metrics.bottom-metrics.ascent)/2-(int)metrics.bottom;
		c.drawText(text, x+(w-bounds.width())*align, texty, p);
	}
	
	public static void drawText(Canvas c, Paint p, int x, int y, int w, int h, String text, boolean fit, float align, int padding){
		drawText(c,p,x+padding, y+padding, w-padding*2, h-padding*2, text, fit, align);
	}
	
	public static void drawMultiLineText(Canvas c, Paint p, int x, int y, int w, int h, String text, boolean fit, float align, int lines, int space){
		int[] result = new int[lines];
		int size = fitTextSizeMultiLine(p, text, w, h, lines, space, result);
		p.setTextSize(size);
//		Log.d(TAG, "fit result : " + size);

		int lineH = (h-space*(lines-1))/lines;
		FontMetrics metrics = p.getFontMetrics();
		Rect bounds = new Rect();
		int pointer = 0;
		for(int i=0; i<lines && pointer<text.length(); i++){
			int lineY = y+lineH*i;
			String str = text.substring(pointer, pointer + result[i]);
			p.getTextBounds(str, 0, str.length(), bounds);
			int texty = lineY+lineH/2+(int)(metrics.bottom-metrics.ascent)/2-(int)metrics.bottom;
			c.drawText(str, x+(w-bounds.width())*align, texty, p);

			pointer += result[i];
		}
	}

	public static void drawMultiLineText(Canvas c, Paint p, int x, int y, int w, int h, String text, boolean fit, float align, int padding, int lines, int space){
		drawMultiLineText(c,p,x+padding, y+padding, w-padding*2, h-padding*2, text, fit, align, lines, space);
	}

	public static void drawTextWithShadow(Canvas c, Paint p, int x, int y, int w, int h, String text, boolean fit, float align, int padding, int shadowMove, int shadowColor){
		int orgColor = p.getColor();
		p.setColor(shadowColor);
		drawText(c,p,x+padding+shadowMove, y+padding+shadowMove,
				w-padding*2-shadowMove, h-padding*2-shadowMove,
				text, fit, align);
		p.setColor(orgColor);
		drawText(c,p,x+padding, y+padding,
				w-padding*2-shadowMove, h-padding*2-shadowMove, text, fit, align);
	}
}
