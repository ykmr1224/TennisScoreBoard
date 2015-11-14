package org.ykmr.tennis;

import java.util.HashMap;
import java.util.Map;

import org.ykmr.tennis.TennisScoreModel.Game;
import org.ykmr.tennis.TennisScoreModel.Match;
import org.ykmr.tennis.TennisScoreModel.Set;
import org.ykmr.tennis.TennisScoreModel.TieBreak;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.Toast;

public class TennisScoreActivity extends Activity implements OnTouchListener{
	public static final int PREFERENCE_REQUEST = 1;
	TennisScoreModel mModel;
	TennisScoreView mView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        new Thread(new Runnable(){
	        	public void run() {
	            initSound();
	        	}
        }).start();

        mView = new TennisScoreView(this, null);
        if(savedInstanceState != null){
        		mModel = new TennisScoreModel(savedInstanceState.getString(KEY_SCORE));
            mView.setModel(mModel);
        }else{
        		newGame();
        }
        setContentView(mView);
        
        mView.setOnTouchListener(this);
    }
	
	public static final String KEY_SCORE = "KEY_SCORE";
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mModel != null)
			outState.putString(KEY_SCORE, mModel.serialize());
	}
	
	public static final int DIALOG_LEFT_NAME = 0x201;
	public static final int DIALOG_RIGHT_NAME = 0x202;
	public static final int DIALOG_LEFT2_NAME = 0x203;
	public static final int DIALOG_RIGHT2_NAME = 0x204;
	public static final int DIALOG_NEW_GAME_ALERT = 0x205;
	public static final int DIALOG_RATE = 0x206;

	private Dialog createNameDialog(final int player){
		final EditText edit = new EditText(this);
		final String text = mModel.getPlayerName(player);
		edit.setText(text);
		return new AlertDialog.Builder(this)
		.setTitle(R.string.change_name_title)
		.setView(edit)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mModel.setPlayerName(player, edit.getText().toString());
				mView.invalidate();
			}
		})
		.create();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		if(id == DIALOG_LEFT_NAME){
			dialog = createNameDialog(mView.leftPlayer);
		}else if(id == DIALOG_RIGHT_NAME){
			dialog = createNameDialog(1-mView.leftPlayer);
		}else if(id == DIALOG_LEFT2_NAME){
			dialog = createNameDialog(mView.leftPlayer+2);
		}else if(id == DIALOG_RIGHT2_NAME){
			dialog = createNameDialog(1-mView.leftPlayer+2);
		}else if(id == DIALOG_NEW_GAME_ALERT){
			dialog = new AlertDialog.Builder(this)
			.setTitle(R.string.new_game_title)
			.setMessage(R.string.new_game_content)
			.setNegativeButton("Cancel", null)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					newGame();
				}
			}).create();
		}else if(id == DIALOG_RATE){
			dialog = new AlertDialog.Builder(this)
			.setTitle(R.string.rate_title)
			.setMessage(R.string.rate_content)
			.setNegativeButton("Cancel", null)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.ykmr.tennis"));
					startActivity(intent);
				}
			}).create();
		}
		return dialog;
	}
	
	boolean mSound = true;
	
	protected void onResume() {
		super.onResume();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		mSound = pref.getBoolean(getResources().getString(R.string.sound_key), true);
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch(mView.getRegion((int)event.getX(), (int)event.getY())){
		case TennisScoreView.PLAYER_LEFT:
			showDialog(DIALOG_LEFT_NAME);
			break;
		case TennisScoreView.PLAYER_RIGHT:
			showDialog(DIALOG_RIGHT_NAME);
			break;
		case TennisScoreView.PLAYER_LEFT2:
			showDialog(DIALOG_LEFT2_NAME);
			break;
		case TennisScoreView.PLAYER_RIGHT2:
			showDialog(DIALOG_RIGHT2_NAME);
			break;
		case TennisScoreView.SETS:
			break;
		case TennisScoreView.SCORE_LEFT:
			if(!mModel.isFinished()){
				int mode = mModel.addPoint(mView.leftPlayer);
				if(mSound)playScore(mode);
			}
			break;
		case TennisScoreView.SCORE_RIGHT:
			if(!mModel.isFinished()){
				int mode = mModel.addPoint(1-mView.leftPlayer);
				if(mSound)playScore(mode);
			}
			break;
		case TennisScoreView.GAMES:
			break;
		case TennisScoreView.STAR:
		{
			showDialog(DIALOG_RATE);
			break;
		}
		case TennisScoreView.OTHER:
			break;
		}
		mView.invalidate();
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflator = getMenuInflater();
		inflator.inflate(R.menu.menu, menu);
		return true;
	}
	
	public void newGame(){
        Intent prefIntent = new Intent(this, GamePreferenceActivity.class);
		this.startActivityForResult(prefIntent, PREFERENCE_REQUEST);
//		mModel = new TennisScoreModel(2, 6);
//		mView.setModel(mModel);
//		mView.invalidate();
	}
	
	public String describeScore(){
		StringBuffer buff = new StringBuffer();
		int l = mView.leftPlayer;
		if(mModel.isDoubles()){
			buff.append(mModel.getPlayerName(l));
			buff.append("/");
			buff.append(mModel.getPlayerName(l+2));
			buff.append(" vs ");
			buff.append(mModel.getPlayerName(1-l));
			buff.append("/");
			buff.append(mModel.getPlayerName(1-l+2));
			buff.append('\n');
		}else{
			buff.append(mModel.getPlayerName(l));
			buff.append(" vs ");
			buff.append(mModel.getPlayerName(1-l));
			buff.append('\n');
		}
		Match m = mModel.mMatch;
		for(int i=0; i<mModel.getNSet(); i++){
			buff.append(mModel.getGames(i, l) + "-" + mModel.getGames(i, 1-l));
			Set s = m.getSet(i);
			if(s.hasTieBreak()){
				TieBreak t = s.getTieBreak();
				buff.append("("+t.getScoreInt(l)+"-"+t.getScoreInt(1-l)+")");
			}
			buff.append(" ");
		}
		buff.append('\n');
		
		String[] text = new String[]{"1st", "2nd", "3rd", "4th", "5th"};
		for(int i=0; i<mModel.getNSet(); i++){
			buff.append("*" +text[i]+" set ");
			buff.append(mModel.getGames(i, l) + "-" + mModel.getGames(i, 1-l) + "\n");
			Set s = m.getSet(i);
			int[] games = new int[2];
			for(int j=0; j<s.getNGames(); j++){
				Game g = s.getGame(j);
				if(g.isFinished()){
					games[g.winner()]++;
					buff.append(games[l]+"-"+games[1-l]+" ");
					if(!(g instanceof TieBreak)){
						buff.append(mModel.getServer(i, j, 0)==l?"S ": "R ");
					}
					buff.append("(");
					for(int k=0; k<g.getNPoint(); k++){
						buff.append(g.getPointAt(k)==l?"O":"X");
					}
					buff.append(")\n");
				}
			}
		}
		return buff.toString();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		case R.id.Undo :
			mModel.removeLastShot();
			mView.invalidate();
			break;
		case R.id.ChangeCourt :
			mView.changeCourt();
			mView.invalidate();
			break;
		case R.id.Stat :
		{
			Intent intent = new Intent(this, ScoreStatActivity.class);
			intent.putExtra(ScoreStatActivity.EXTRA_MODEL, mModel.serialize());
			startActivity(intent);
			break;
		}
		case R.id.NewGame :
			showDialog(DIALOG_NEW_GAME_ALERT);
			break;
		case R.id.Preferences :
		{
			Intent intent = new Intent(this, TennisScorePreferenceActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.Send :
		{
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, describeScore());
			this.startActivity(Intent.createChooser(intent, "Send this score via..."));
		}
		}
		return true;
	}
	
	MediaPlayer[] mMp;
	SoundPool mSp;
	int mPlaying = -1;

	Map<String, Sound> mSoundS = new HashMap<String, Sound>();
	Map<String, Sound> mSoundR = new HashMap<String, Sound>();
	Map<String, Sound> mSoundO = new HashMap<String, Sound>();
//	Map<Integer, Integer> mSoundIDs = new HashMap<Integer, Integer>();
	
	class Sound{
		SoundPool mSp;
		int mResID;
		int mSoundID;
		int mDuration;
		Sound(SoundPool sp, int resID, int duration){
			mSp = sp;
			mResID = resID;
			mDuration = duration;
			mSoundID = sp.load(TennisScoreActivity.this, resID, 1);
		}
		int play(){
			mSp.play(mSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
			return mDuration;
		}
	}
	
	public void initSound(){
		// サウンドデータの初期化
		mSp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		mSoundS.put("0", new Sound(mSp, R.raw.s0, 400));
		mSoundR.put("0", new Sound(mSp, R.raw.r0, 400));
		mSoundS.put("15", new Sound(mSp, R.raw.s15, 600));
		mSoundR.put("15", new Sound(mSp, R.raw.r15, 500));
		mSoundS.put("30", new Sound(mSp, R.raw.s30, 500));
		mSoundR.put("30", new Sound(mSp, R.raw.r30, 500));
		mSoundS.put("40", new Sound(mSp, R.raw.s40, 500));
		mSoundR.put("40", new Sound(mSp, R.raw.r40, 500));
		mSoundS.put("ZERO", new Sound(mSp, R.raw.szero, 500));
		mSoundR.put("ZERO", new Sound(mSp, R.raw.rzero, 500));
		mSoundS.put("1", new Sound(mSp, R.raw.s1, 400));
		mSoundR.put("1", new Sound(mSp, R.raw.r1, 400));
		mSoundS.put("2", new Sound(mSp, R.raw.s2, 400));
		mSoundR.put("2", new Sound(mSp, R.raw.r2, 400));
		mSoundS.put("3", new Sound(mSp, R.raw.s3, 400));
		mSoundR.put("3", new Sound(mSp, R.raw.r3, 400));
		mSoundS.put("4", new Sound(mSp, R.raw.s4, 400));
		mSoundR.put("4", new Sound(mSp, R.raw.r4, 400));
		mSoundS.put("5", new Sound(mSp, R.raw.s5, 400));
		mSoundR.put("5", new Sound(mSp, R.raw.r5, 400));
		mSoundS.put("6", new Sound(mSp, R.raw.s6, 600));
		mSoundR.put("6", new Sound(mSp, R.raw.r6, 600));
		mSoundO.put("SVR", new Sound(mSp, R.raw.svr, 500));
		mSoundO.put("RCV", new Sound(mSp, R.raw.rcv, 500));
		mSoundO.put("ADV", new Sound(mSp, R.raw.adv, 700));
		mSoundO.put("ALL", new Sound(mSp, R.raw.all, 300));
		mSoundO.put("DUCE", new Sound(mSp, R.raw.duce, 500));
		mSoundO.put("GAME", new Sound(mSp, R.raw.game, 500));
		mSoundO.put("GAME_AND_SET", new Sound(mSp, R.raw.gs, 800));
		mSoundO.put("GAME_SET_AND_MATCH", new Sound(mSp, R.raw.gsm, 1000));
	}

	public void playScore(int mode){
		int server = mModel.getServer()%2;
		String svr = mModel.getCurrentScore(server);
		String rcv = mModel.getCurrentScore(1-server);
		if(mode == TennisScoreModel.GAME_FINISHED){
			startPlaySound(mSoundO.get("GAME"));
		}else if(mode == TennisScoreModel.SET_FINISHED){
			startPlaySound(mSoundO.get("GAME_AND_SET"));
		}else if(mode == TennisScoreModel.MATCH_FINISHED){
			startPlaySound(mSoundO.get("GAME_SET_AND_MATCH"));
		}else{
			if(mModel.getCurrentGame() instanceof TieBreak){
				int s = Integer.parseInt(svr);
				int r = Integer.parseInt(rcv);
				if(s>6 || r>6){
					if(s == r) startPlaySound(mSoundO.get("DUCE"));
					else if(s > r) startPlaySound(mSoundO.get("ADV"), mSoundO.get("SVR"));
					else startPlaySound(mSoundO.get("ADV"), mSoundO.get("RCV"));
				}else if(svr.equals(rcv)){
					startPlaySound(mSoundS.get(svr), mSoundO.get("ALL"));
				}else{
					if(svr.equals("0")) svr = "ZERO";
					if(rcv.equals("0")) rcv = "ZERO";
					startPlaySound(mSoundS.get(svr), mSoundR.get(rcv));
				}
			}else if(svr == TennisScoreModel.SCORE_STR[TennisScoreModel.SCORE_ADV]){
				startPlaySound(mSoundO.get("ADV"), mSoundO.get("SVR"));
			}else if(rcv == TennisScoreModel.SCORE_STR[TennisScoreModel.SCORE_ADV]){
				startPlaySound(mSoundO.get("ADV"), mSoundO.get("RCV"));
			}else if(svr == TennisScoreModel.SCORE_STR[TennisScoreModel.SCORE_40] &&
					rcv == TennisScoreModel.SCORE_STR[TennisScoreModel.SCORE_40]){
				startPlaySound(mSoundO.get("DUCE"));
			}else if(svr == TennisScoreModel.SCORE_STR[TennisScoreModel.SCORE_0] &&
					rcv == TennisScoreModel.SCORE_STR[TennisScoreModel.SCORE_0]){
				startPlaySound(mSoundO.get("GAME"));
			}else {
				if(svr != rcv){
					startPlaySound(mSoundS.get(svr), mSoundR.get(rcv));
				}else{
					startPlaySound(mSoundS.get(svr), mSoundO.get("ALL"));
				}
			}
		}
	}
	
	public void startPlaySound(final Sound s) {
//		int stream_id = mSp.play(mSoundIDs.get(i), 1.0F, 1.0F, 0, 0, 1.0F);
		if(s!=null)
			s.play();
	}
	public void startPlaySound(final Sound s0, final Sound s1) {
//		Log.d("sound", "play " + i + "," + j);
		if(s0 != null && s1 != null)
			new Thread(new Runnable(){
				public void run() {
					int duration = s0.play();
	//				int stream_id = mSp.play(mSoundIDs.get(i), 1.0F, 1.0F, 0, 0, 1.0F);
					try {
						Thread.sleep(duration);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					s1.play();
	//				stream_id = mSp.play(mSoundIDs.get(j), 1.0F, 1.0F, 0, 0, 1.0F);
				}
			}).start();
	}
	
    protected void loadPreference(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		Resources res = getResources();
		String racetoSets = pref.getString(res.getString(R.string.raceto_set_key), "1");
		String racetoGames = pref.getString(res.getString(R.string.raceto_game_key), "3");
		String gameType = pref.getString(res.getString(R.string.game_type_key), "S");
		mModel = new TennisScoreModel(
				Integer.parseInt(racetoSets),
				Integer.parseInt(racetoGames), "D".equals(gameType));
        mView.setModel(mModel);
        mView.invalidate();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == PREFERENCE_REQUEST){
			loadPreference();
		}
	}

}
