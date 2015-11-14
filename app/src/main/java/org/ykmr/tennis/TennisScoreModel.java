package org.ykmr.tennis;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class TennisScoreModel{
	public static final int SCORE_0 = 0;
	public static final int SCORE_15 = 1;
	public static final int SCORE_30 = 2;
	public static final int SCORE_40 = 3;
	public static final int SCORE_ADV = 4;
	public static final int SCORE_WIN = 5;
	
	public static final String[] SCORE_STR = new String[]{
		"0", "15", "30", "40", "Adv", "Win"
	};
	
	Match mMatch;
	String[] mPlayerName = new String[]{"Player A", "Player B", "Player A2", "Player B2"};
	boolean mDoubles;
	
	public TennisScoreModel(int raceToSets, int raceToGames, boolean doubles){
		mMatch = new Match(raceToSets, raceToGames);
		mDoubles = doubles;
	}
	
	public TennisScoreModel(String serialized){
		CSVReader r = new CSVReader(new StringReader(serialized));
		String[] vals;
		try {
			vals = r.readNext();
		} catch (IOException e) {
			e.printStackTrace();
			mMatch = new Match(1, 6);
			return;
		}
		if(vals.length < 7){
			mMatch = new Match(1, 6);
			return;
		}
		
		mDoubles = "D".equals(vals[0]);
		mPlayerName[0] = vals[1];
		mPlayerName[1] = vals[2];
		mPlayerName[2] = vals[3];
		mPlayerName[3] = vals[4];
		mMatch = new Match(Integer.parseInt(vals[5]), Integer.parseInt(vals[6]));
		for(int i=7; i<vals.length; i++){
			for(int j=0; j<vals[i].length(); j++){
				char c = vals[i].charAt(j);
				switch(c){
				case '0': addPoint(0); break;
				case '1': addPoint(1); break;
				case ' ': break;
				}
			}
		}
	}
	public String encode(String str){
		return "\""+str.replace("\"", "\"\"")+"\"";
	}
	public String serialize(){
		return (mDoubles?"D":"S")+","+
		encode(mPlayerName[0])+","+encode(mPlayerName[1])+","+
		encode(mPlayerName[2])+","+encode(mPlayerName[3])+","+
		mMatch.serialize();
	}
	
	public int getSets(int player){
		return mMatch.getSets(player);
	}
	
	public int getNSet(){
		return mMatch.sets.size();
	}
	
	public int getGames(int set, int player){
		return mMatch.getSet(set).getGames(player);
	}

	public int getCurrentSetGames(int player){
		return mMatch.getCurrentSet().getGames(player);
	}
	
	public String getCurrentScore(int player){
		return mMatch.getCurrentSet().getCurrentGame().getScoreString(player);
	}
	
	public Game getCurrentGame(){
		return mMatch.getCurrentSet().getCurrentGame();
	}
	
	public String getPlayerName(int player){
		return mPlayerName[player];
	}
	
	public void setPlayerName(int player, String name){
		mPlayerName[player] = name;
	}
	
	public int getServer(int set, int game, int point){
		if(game == mMatch.raceToGames*2)//tie break;
			return (set+(point+1)/2)%(mDoubles?4:2);
		else
			return (set+game)%(mDoubles?4:2);
	}

	public int getServer(){
		int set = mMatch.sets.size()-1;
		int game = mMatch.getCurrentSet().size()-1;
		return getServer(set, game, getCurrentGame().getNPoint());
	}
	
	/**
	 * 
	 * @return service side (0:duce side, 1:adv side)
	 */
	public int getServiceSide(){
		Game g = getCurrentGame();
		if(g instanceof TieBreak){
			return (g.size()+1)%2;
		}else{
			return g.size()%2;
		}
	}
	
	public boolean isDoubles(){
		return mDoubles;
	}
	
	public int getBreakPoint(){
		Game g = getCurrentGame();
		int sp = g.getScoreInt(getServer()%2);
		int rp = g.getScoreInt(1-getServer()%2);
		if(g instanceof TieBreak){
			return 0;
		}else{
			if(rp > sp && rp >= 3){
				return rp-sp;
			}
		}
		return 0;
	}
	
	private int getSetPoint(int player){
		Game g = getCurrentGame();
		int p0 = g.getScoreInt(player);
		int p1 = g.getScoreInt(1-player);
		if(g instanceof TieBreak){
			if(p0 >= 6 && p0-p1>=1)
				return p0-p1;
		}else{
			int g0 = getCurrentSetGames(player);
			int g1 = getCurrentSetGames(1-player);
			if(g0 >= mMatch.raceToGames-1 && g0-g1>=1){
				if(p0 >= 3 && p0-p1 >= 1)
					return p0-p1;
			}
		}
		return 0;
	}
	
	public int getSetPoint(){
		int a = getSetPoint(0);
		int b = getSetPoint(1);
		return Math.max(a, b);
	}

	private int getMatchPoint(int player){
		if(getSets(player) == mMatch.raceToSets-1){
			Game g = getCurrentGame();
			int p0 = g.getScoreInt(player);
			int p1 = g.getScoreInt(1-player);
			if(g instanceof TieBreak){
				if(p0 >= 6 && p0-p1>=1)
					return p0-p1;
			}else{
				int g0 = getCurrentSetGames(player);
				int g1 = getCurrentSetGames(1-player);
				if(g0 >= mMatch.raceToGames-1 && g0-g1>=1){
					if(p0 >= 3 && p0-p1 >= 1)
						return p0-p1;
				}
			}
		}
		return 0;
	}
	
	public int getMatchPoint(){
		int a = getMatchPoint(0);
		int b = getMatchPoint(1);
		return Math.max(a, b);
	}
	
	public boolean removeLastShot(){
		return mMatch.removeLastShot();
	}
	
	public boolean isFinished(){
		return mMatch.isFinished();
	}
	
	public static final int CONTINUE = 0;
	public static final int GAME_FINISHED = 1;
	public static final int SET_FINISHED = 2;
	public static final int MATCH_FINISHED = 3;
	
	public int addPoint(int player){
		Set s = mMatch.getCurrentSet();
		Game g = s.getCurrentGame();
		g.point(player);
		if(g.isFinished()){
			if(s.isFinished()){
				if(mMatch.isFinished()){
					return MATCH_FINISHED;
				}else{
					mMatch.nextSet();
					return SET_FINISHED;
				}
			}else{
				s.nextGame();
				return GAME_FINISHED;
			}
		}
		return CONTINUE;
	}
	
	class Game{
		List<Integer> score = new ArrayList<Integer>();
		public Game(){}
		public void point(int player){
			score.add(player);
		}
		public void pop(){
			score.remove(score.size()-1);
		}
		public int getNPoint(){
			return score.size();
		}
		public int getPointAt(int i){
			return score.get(i);
		}
		public int getScoreInt(int player){
			int count = 0;
			for(int p : score)
				if(p==player) count++;
			return count;
		}
		public String getScoreString(int player){
			int n = score.size();
			if(isFinished()&&winner()==player) return SCORE_STR[5];//Win
			if(n<=5){
				int count = 0;
				for(int p : score)
					if(p==player) count++;
				return SCORE_STR[count];
			}else if(n%2==0){//duce
				return SCORE_STR[3];
			}else{
				if(score.get(n-1) == player)
					return SCORE_STR[4];//Advantage
				else
					return SCORE_STR[3];//40
			}
		}
		public int winner(){
			return score.get(score.size()-1);
		}
		public boolean isFinished(){
			int[] sum = new int[2];
			for(int p: score) sum[p]++;
			if(sum[0] > sum[1]){
				return sum[0] >= 4 && sum[0]-sum[1]>=2;
			}else{
				return sum[1] >= 4 && sum[1]-sum[0]>=2;
			}
		}
		public int size(){
			return score.size();
		}
		public boolean removeLastShot(){
			if(score.size()>0){
				pop();
				return true;
			}else{
				return false;
			}
		}
		public String serialize(){
			StringBuffer res = new StringBuffer();
			for(int p : score) res.append(Integer.toString(p));
			return res.toString();
		}
	}
	
	class TieBreak extends Game{
		public TieBreak() {}
		@Override
		public String getScoreString(int player) {
			if(isFinished()&&winner()==player) return SCORE_STR[5];//Win
			int n = 0;
			for(int p:score) if(p==player) n++;
			return Integer.toString(n);
		}
		public boolean isFinished(){
			int[] sum = new int[2];
			for(int p: score) sum[p]++;
			if(sum[0] > sum[1]){
				return sum[0] >= 7 && sum[0]-sum[1]>=2;
			}else{
				return sum[1] >= 7 && sum[1]-sum[0]>=2;
			}
		}
	}
	
	class Set{
		List<Game> games = new ArrayList<Game>();
		int raceTo;
		public Set(int raceTo){
			this.raceTo = raceTo;
			nextGame();
		}
		public void nextGame(){
			if(games.size()==raceTo*2)
				games.add(new TieBreak());
			else
				games.add(new Game());
		}
		public Game getGame(int i){
			return games.get(i);
		}
		public int getNGames(){
			return games.size();
		}
		public Game getCurrentGame(){
			return games.get(games.size()-1);
		}
		public int getGames(int player){
			int n = 0;
			for(Game g : games)
				if(g.isFinished() && g.winner() == player) n++;
			return n;
		}
		public boolean hasTieBreak(){
			return games.get(games.size()-1) instanceof TieBreak;
		}
		public TieBreak getTieBreak(){
			if(hasTieBreak())
				return (TieBreak)games.get(games.size()-1);
			return null;
		}
		public int winner(){
			return games.get(games.size()-1).winner();
		}
		public boolean isFinished(){
			int[] sum = new int[2];
			for(Game g: games)
				if(g.isFinished())
					sum[g.winner()]++;
			if(getCurrentGame() instanceof TieBreak){
				return getCurrentGame().isFinished();
			}else if(sum[0] > sum[1]){
				return sum[0] >= raceTo && sum[0]-sum[1]>=2;
			}else{
				return sum[1] >= raceTo && sum[1]-sum[0]>=2;
			}
		}
		public int size(){
			return games.size();
		}
		public boolean removeLastShot(){
			for(int i=games.size()-1; i>=0; i--){
				if(games.get(i).removeLastShot())
					return true;
				else
					games.remove(i);
			}
			return false;
		}
		public String serialize(){
			StringBuffer res = new StringBuffer();
			for(Game g : games){
				res.append(g.serialize());
				res.append(" ");
			}
			return res.toString();
		}
	}

	public class Match{
		List<Set> sets = new ArrayList<Set>();
		int raceToSets;
		int raceToGames;
		public Match(int raceToSets, int raceToGames){
			this.raceToSets = raceToSets;
			this.raceToGames = raceToGames;
			nextSet();
		}
		public void nextSet(){
			sets.add(new Set(raceToGames));
		}
		public Set getSet(int i){
			return sets.get(i);
		}
		public Set getCurrentSet(){
			return sets.get(sets.size()-1);
		}
		public int getSets(int player){
			int n = 0;
			for(Set s : sets)
				if(s.isFinished() && s.winner() == player) n++;
			return n;
		}
		public int winner(){
			return sets.get(sets.size()-1).winner();
		}
		public boolean isFinished(){
			int[] sum = new int[2];
			for(Set s: sets)
				if(s.isFinished())
					sum[s.winner()]++;
			if(sum[0] > sum[1]){
				return sum[0] >= raceToSets;
			}else{
				return sum[1] >= raceToSets;
			}
		}
		public boolean removeLastShot(){
			for(int i=sets.size()-1; i>=0; i--){
				if(sets.get(i).removeLastShot())
					return true;
				else
					sets.remove(i);
			}
			nextSet();
			return false;
		}
		public String serialize(){
			StringBuffer res = new StringBuffer();
			res.append(this.raceToSets);
			res.append(",");
			res.append(this.raceToGames);
			res.append(",");
			for(Set s : sets){
				res.append(s.serialize());
				res.append(",");
			}
			return res.toString();
		}
	}
}
