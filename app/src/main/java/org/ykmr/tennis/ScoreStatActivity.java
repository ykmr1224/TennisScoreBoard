package org.ykmr.tennis;

import java.util.ArrayList;
import java.util.List;

import org.ykmr.tennis.TennisScoreModel.Game;
import org.ykmr.tennis.TennisScoreModel.Set;
import org.ykmr.util.PaintUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

public class ScoreStatActivity extends Activity {
	TennisScoreModel mModel;
	StatisticsView mView;
	
	public static final String EXTRA_MODEL = "EXTRA_MODEL";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent = getIntent();

        mModel = new TennisScoreModel(intent.getExtras().getString(EXTRA_MODEL));
        mView = new StatisticsView(this, null);
        calcStatistics();
        
        setContentView(mView);
    }
	
	public void calcStatistics(){
		mView.items.add(new StatItem(getResources().getString(R.string.stat_name),
				""+mModel.getPlayerName(0), ""+mModel.getPlayerName(1)));
		mView.items.add(new StatItem(getResources().getString(R.string.stat_sets),
				""+mModel.getSets(0), ""+mModel.getSets(1)));
		mView.items.add(new TotalGames(mModel));
		mView.items.add(new TotalPoints(mModel));
		mView.items.add(new ServiceGameKeep(mModel));
		mView.items.add(new ServicePointKeep(mModel));
	}
	
	class StatItem{
		String title;
		String[] val;
		public StatItem(){
			title = "";
			val = new String[]{"",""};
		}
		public StatItem(String title, String v0, String v1){
			this.title = title;
			this.val = new String[]{v0, v1};
		}
		public String getTitle(){
			return title;
		}
		public String get(int i){
			return val[i];
		}
	}
	
	class TotalGames extends StatItem{
		public TotalGames(TennisScoreModel model) {
			TennisScoreModel.Match m = model.mMatch;
			title = getResources().getString(R.string.stat_total_games);
			int[] v = new int[2];
			for(Set s : m.sets){
				v[0] += s.getGames(0);
				v[1] += s.getGames(1);
			}
			int sum = v[0]+v[1];
			if(sum == 0){
				val[0] = "-";
				val[1] = "-";
			}else{
				val[0] = ""+v[0]+"("+(v[0]*100/sum)+"%)";
				val[1] = ""+v[1]+"("+(v[1]*100/sum)+"%)";
			}
		}
	}
	
	class TotalPoints extends StatItem{
		public TotalPoints(TennisScoreModel model) {
			TennisScoreModel.Match m = model.mMatch;
			title = getResources().getString(R.string.stat_total_points);
			int[] v = new int[2];
			for(Set s : m.sets){
				for(Game g : s.games){
					for(int i=0; i<g.size(); i++){
						v[g.score.get(i)] ++;
					}
				}
			}
			int sum = v[0]+v[1];
			if(sum == 0){
				val[0] = "-";
				val[1] = "-";
			}else{
				val[0] = ""+v[0]+"("+(v[0]*100/sum)+"%)";
				val[1] = ""+v[1]+"("+(v[1]*100/sum)+"%)";
			}
		}
	}
	
	class ServiceGameKeep extends StatItem{
		public ServiceGameKeep(TennisScoreModel model) {
			TennisScoreModel.Match m = model.mMatch;
			title = getResources().getString(R.string.stat_service_game_keep);
			int[] v = new int[2];
			int[] total = new int[2];
			for(int s=0; s<m.sets.size(); s++){
				Set set = m.sets.get(s);
				for(int g=0; g<set.games.size()&&g<12; g++){
					Game game = set.getGame(g);
					int server = model.getServer(s, g, 0)%2;
					if(game.isFinished()){
						total[server]++;
						if(game.winner() == server)
							v[server]++;
					}
				}
			}
			
			if(total[0] == 0) val[0] = "-";
			else val[0] = ""+v[0]+"/"+total[0]+"("+(v[0]*100/total[0])+"%)";
			
			if(total[1] == 0) val[1] = "-";
			else val[1] = ""+v[1]+"/"+total[1]+"("+(v[1]*100/total[1])+"%)";
		}
	}
	class ServicePointKeep extends StatItem{
		public ServicePointKeep(TennisScoreModel model) {
			TennisScoreModel.Match m = model.mMatch;
			title = getResources().getString(R.string.stat_service_point_keep);
			int[] v = new int[2];
			int[] total = new int[2];
			for(int s=0; s<m.sets.size(); s++){
				Set set = m.sets.get(s);
				for(int g=0; g<set.games.size()&&g<12; g++){
					Game game = set.getGame(g);
					for(int p=0; p<game.size(); p++){
						int server = model.getServer(s, g, p)%2;
						total[server]++;
						if(game.getPointAt(p) == server)
							v[server]++;
					}
				}
			}
			if(total[0] == 0) val[0] = "-";
			else val[0] = ""+v[0]+"/"+total[0]+"("+(v[0]*100/total[0])+"%)";
			
			if(total[1] == 0) val[1] = "-";
			else val[1] = ""+v[1]+"/"+total[1]+"("+(v[1]*100/total[1])+"%)";
		}
	}
		
	public class StatisticsView extends View{
		List<StatItem> items;
		public StatisticsView(Context context, AttributeSet attrs) {
			super(context, attrs);
			Resources r = getResources();
			this.setBackgroundDrawable(r.getDrawable(R.drawable.bg));
			items = new ArrayList<StatItem>();
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

		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			Paint p = new Paint();
			p.setAntiAlias(true);
			p.setColor(0xff007700);
//			canvas.drawRect(0, 0, W, H, p);

			p.setColor(0xffffffff);
			p.setStrokeWidth(3);
			canvas.drawLine(0, H6, W, H6, p);
			canvas.drawLine(W/2-H6*2, 0, W/2-H6*2, H, p);
			canvas.drawLine(W/2+H6*2, 0, W/2+H6*2, H, p);
			
			if(mModel != null){
				p.setColor(0xffffffff);
				int l = 0;
				int r = 1-l;
				PaintUtil.drawText(canvas, p, W/2-H6*2, 0, H6*4, H6, items.get(0).getTitle(), true, 0.5f, H6/5);
				PaintUtil.drawText(canvas, p, 0, 0, W/2-H6*2, H6, items.get(0).get(0), true, 1.0f, H6/5);
				PaintUtil.drawText(canvas, p, W/2+H6*2, 0, W/2-H6*2, H6, items.get(0).get(1), true, 0.0f, H6/5);
				int H8 = H/6;
				for(int i=1; i<items.size(); i++){
					StatItem item = items.get(i);
					int y = H6+(i-1)*H8;
					PaintUtil.drawText(canvas, p, W/2-H6*2, y, H6*4, H8, item.getTitle(), true, 0.5f, H8/5);
					PaintUtil.drawText(canvas, p, 0, y, W/2-H6*2, H8, item.get(0), true, 1.0f, H8/5);
					PaintUtil.drawText(canvas, p, W/2+H6*2, y, W/2-H6*2, H8, item.get(1), true, 0.0f, H8/5);
				}
			}
		}
		
		public static final int OTHER = 0;
		public static final int PLAYER_LEFT = 1;
		public static final int PLAYER_RIGHT = 2;
		public static final int SETS = 3;
		public static final int SCORE_LEFT = 4;
		public static final int SCORE_RIGHT = 5;
		public static final int GAMES = 6;

	}
}
