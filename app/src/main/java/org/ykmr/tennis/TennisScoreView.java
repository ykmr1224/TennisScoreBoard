package org.ykmr.tennis;

import org.ykmr.util.PaintUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TennisScoreView extends View {
	private static final int FOREGROUND_COLOR = 0xffffffff;
	TennisScoreModel mModel;
	int leftPlayer = 0;
	Bitmap ball, racket, bg, star;

	public TennisScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Resources r = getResources();
		ball = BitmapFactory.decodeResource(r, R.drawable.ball);
		racket = BitmapFactory.decodeResource(r, R.drawable.racket);
		bg = BitmapFactory.decodeResource(r, R.drawable.bg);
		star = BitmapFactory.decodeResource(r, R.drawable.star);
		this.setBackgroundDrawable(r.getDrawable(R.drawable.bg));
	}
	
	public void setModel(TennisScoreModel model){
		mModel = model;
	}
	
	public void changeCourt(){
		leftPlayer = 1-leftPlayer;
	}

	int W = 960;
	int H = 480;
	int H6 = H/6;
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		W = w;
		H = h;
		H6 = H/6;
	}
	
/*	//position = left duce:0, left adv:1, right duce:2, right adv:3
	protected int getPlayerAt(int position){
		int l = leftPlayer;
		int server = mModel.getServer();
		int side = mModel.getServiceSide();
		if(server%2 == leftPlayer){
			switch(position){
			case 0: return side==0?server:(server+2)%4;
			case 1: return side==1?server:(server+2)%4;
			case 2: return 1-l;
			case 3: return 1-l+2;
			}
		}else{
			switch(position){
			case 0: return l;
			case 1: return l+2;
			case 2: return side==0?server:(server+2)%4;
			case 3: return side==1?server:(server+2)%4;
			}
		}
		return 0;
	}*/

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(0xff007700);
//		canvas.drawRect(0, 0, W, H, p);

		p.setColor(FOREGROUND_COLOR);
		p.setStrokeWidth(3);
		canvas.drawLine(0, H6, W, H6, p);
		canvas.drawLine(0, H-H6, W, H-H6, p);
		canvas.drawLine(W/2-H6, 0, W/2-H6, H-H6, p);
		canvas.drawLine(W/2+H6, 0, W/2+H6, H-H6, p);
		p.setStrokeWidth(1);
		canvas.drawLine(0, H-H6/2, W-H6, H-H6/2, p);
		canvas.drawLine(H6*2, H-H6, H6*2, H, p);
		canvas.drawLine(W-H6, H-H6, W-H6, H, p);
		
		p.setColor(0x44ffffff);
		PaintUtil.drawText(canvas, p, W/2-H6, 0, H6*2, H6, "SET", true, 0.5f, 0);
		PaintUtil.drawText(canvas, p, W/2-H6, H6, H6*2, H6, "GAME", true, 0.5f, 0);
		
		if(mModel != null){
			p.setColor(FOREGROUND_COLOR);
			int l = leftPlayer;
			int r = 1-l;
			//name
			if(mModel.isDoubles()){
				PaintUtil.drawText(canvas, p, H6/2, 0, W/2-H6-H6/2, H6, mModel.getPlayerName(l), true, 0.5f, H6/10);
				PaintUtil.drawText(canvas, p, W/2+H6, 0, W/2-H6-H6/2, H6, mModel.getPlayerName(r), true, 0.5f, H6/10);
				PaintUtil.drawText(canvas, p, H6/2, H6, W/2-H6-H6/2, H6, mModel.getPlayerName((l+2)%4), true, 0.5f, H6/10);
				PaintUtil.drawText(canvas, p, W/2+H6, H6, W/2-H6-H6/2, H6, mModel.getPlayerName((r+2)%4), true, 0.5f, H6/10);
				p.setStrokeWidth(3);
				canvas.drawLine(0, H6*2, W/2-H6, H6*2, p);
				canvas.drawLine(W/2+H6, H6*2, W, H6*2, p);
			}else{
				PaintUtil.drawText(canvas, p, H6/2, 0, W/2-H6-H6/2, H6, mModel.getPlayerName(l), true, 0.5f, H6/10);
				PaintUtil.drawText(canvas, p, W/2+H6, 0, W/2-H6-H6/2, H6, mModel.getPlayerName(r), true, 0.5f, H6/10);
			}
			//current score
			if(mModel.isDoubles()){
				PaintUtil.drawText(canvas, p, 0, H6*2, W/2-H6, H-H6*3, mModel.getCurrentScore(l), true, 0.5f, H6/3);
				PaintUtil.drawText(canvas, p, W/2+H6, H6*2, W/2-H6, H-H6*3, mModel.getCurrentScore(r), true, 0.5f, H6/3);
			}else{
				PaintUtil.drawText(canvas, p, 0, H6, W/2-H6, H-H6*2, mModel.getCurrentScore(l), true, 0.5f, H6/3);
				PaintUtil.drawText(canvas, p, W/2+H6, H6, W/2-H6, H-H6*2, mModel.getCurrentScore(r), true, 0.5f, H6/3);
			}
			//sets
			PaintUtil.drawText(canvas, p, W/2-H6, 0, H6*2, H6, mModel.getSets(l)+"-"+mModel.getSets(r), true, 0.5f, H6/10);
			//games
			int h = H*2/15;
			for(int i=0; i<mModel.getNSet(); i++){
				String games = mModel.getGames(i, l) + "-" + mModel.getGames(i, r);
				PaintUtil.drawText(canvas, p, W/2-H6, H6+h*i, H6*2, h, games, true, 0.5f, H6/10);
			}
			//mini name
			PaintUtil.drawText(canvas, p, 0, H-H6, H6*2, H6/2, mModel.getPlayerName(0), true, 0.5f, H6/10);
			PaintUtil.drawText(canvas, p, 0, H-H6/2, H6*2, H6/2, mModel.getPlayerName(1), true, 0.5f, H6/10);
			//balls
			TennisScoreModel.Game g = mModel.getCurrentGame();
			int w = H6/2;
			int start = Math.max(0, g.score.size()-(W-H6*3)/w);
			for(int j=start; j<g.score.size(); j++){
				int left = H6*2+(j-start)*w;
				int top = H-H6+g.getPointAt(j)*w;
				canvas.drawBitmap(ball, null, new Rect(left, top, left+w, top+w), p);
//				canvas.drawCircle(H6*2+j*H6/2+rad, H-H6+H6*g.getPoint(j)/2+rad, rad*9/10, p);
			}
			//star
			canvas.drawBitmap(star, null, new Rect(W-H6, H-H6, W, H), p);
			//server
			int racketW = H6*9/16;
			int racketH = H6*3/4;
			int server = mModel.getServer();
			if(mModel.isDoubles()){
				if(server%2 == leftPlayer){
					canvas.drawBitmap(racket, null, new Rect(0, (server/2)*H6, racketW, (server/2)*H6+racketH), p);
				}else{
					canvas.drawBitmap(racket, null, new Rect(W-racketW, (server/2)*H6, W, (server/2)*H6+racketH), p);
				}
			}else{
				if(server%2 == leftPlayer){
					canvas.drawBitmap(racket, null, new Rect(0, 0, racketW, racketH), p);
				}else{
					canvas.drawBitmap(racket, null, new Rect(W-racketW, 0, W, racketH), p);
				}
			}
			if(!mModel.isFinished()){
				int matchPoints = mModel.getMatchPoint();
				int setPoints = mModel.getSetPoint();
				int breakPoints = mModel.getBreakPoint();
				if(matchPoints > 0){//match points
					drawPoints(canvas, "Match Point", matchPoints);
				}else if(setPoints > 0){//set points
					drawPoints(canvas, "Set Point", setPoints);
				}else if(breakPoints > 0){//break points
					drawPoints(canvas, "Break Point", breakPoints);
				}
			}
		}
	}
	
	public void drawPoints(Canvas canvas, String title, int count){
		Paint p = new Paint();
		p.setColor(0xcc004400);
		p.setAntiAlias(true);
		int h = H6/2;
		int w = H6*2+h*count+h;
		int left = W/2-w/2;
		int top = H*2/3;
		int right = W/2+w/2;
		int bottom = H*2/3+h;
		canvas.drawRoundRect(new RectF(left, top, right, bottom), H6/4, H6/4, p);
		p.setColor(FOREGROUND_COLOR);
		PaintUtil.drawText(canvas, p, left+h/2, top, H6*2, bottom-top, title, true, 0.5f, H6/20);
		for(int i=0; i<count; i++)
			canvas.drawBitmap(ball, null, new Rect(left+h/2+H6*2+i*h, top, left+h/2+H6*2+(i+1)*h, top+h), p);
	}
	
	public static final int OTHER = 0;
	public static final int PLAYER_LEFT = 1;
	public static final int PLAYER_RIGHT = 2;
	public static final int SETS = 3;
	public static final int SCORE_LEFT = 4;
	public static final int SCORE_RIGHT = 5;
	public static final int GAMES = 6;
	public static final int PLAYER_LEFT2 = 7;
	public static final int PLAYER_RIGHT2 = 8;
	public static final int STAR = 9;
	
	public int getRegion(int x, int y){
		if(mModel.isDoubles()){
			if(y < H6){
				if(x < W/2-H6) return PLAYER_LEFT;
				else if(x < W/2+H6) return SETS;
				else return PLAYER_RIGHT;
			}else if(y<H6*2){
				if(x < W/2-H6) return PLAYER_LEFT2;
				else if(x < W/2+H6) return GAMES;
				else return PLAYER_RIGHT2;
			}else if(y<H-H6){
				if(x < W/2-H6) return SCORE_LEFT;
				else if(x < W/2+H6) return GAMES;
				else return SCORE_RIGHT;
			}else if(x > W-H6){
				return STAR;
			}
			
		}else{
			if(y < H6){
				if(x < W/2-H6) return PLAYER_LEFT;
				else if(x < W/2+H6) return SETS;
				else return PLAYER_RIGHT;
			}else if(y<H-H6){
				if(x < W/2-H6) return SCORE_LEFT;
				else if(x < W/2+H6) return GAMES;
				else return SCORE_RIGHT;
			}else if(x > W-H6){
				return STAR;
			}
		}
		return OTHER;
	}
}
