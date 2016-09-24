package abc_maze;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Main {
	
	// ==================================================
	// ○クラス定義
	// ※1ファイルで完結させる関係上インナークラス化しています
	// ==================================================
	/**
	 * マップ内の位置を表すクラス
	 */
	public static class Position {
		private final int x;
		private final int y;
		
		public Position(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX(){ return x;}
		public int getY(){ return y;}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Position) {
				Position p = (Position) o;
				return p.x == x && p.y == y;
			}
			return false;
		}
		
	}
	
	/**
	 * マップ情報クラス<br/>
	 * 不変オブジェクト化しているためスレッドセーフ
	 *
	 */
	public static class Field {
		/** マップデータ（YX） */
		private final char[][] mapData;
		
		/**
		 * 指定したファイルからマップを作成します。
		 * @param filename マップファイル名
		 */
		public Field(String filename) {
			mapData = createMapForFile(filename);
		}
		
		/** ファイルからマップデータを取得します。 */
		private char[][] createMapForFile(String filename) {
			List<char[]> row = new ArrayList<>();

			//auto closeable
			try (FileReader fReader = new FileReader(filename);
				BufferedReader br = new BufferedReader(fReader)){
				String line;

				while ((line = br.readLine()) != null) {
					//XXX:今回は読み込むテキストが決まっているのでエラー処理していない事に注意
					row.add(line.toCharArray());
				}
				
				return row.toArray(new char[0][0]);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new char[0][0];
		}
		
		/** マップの横幅を返します */
		public int width() {return mapData[0].length;}
		/** マップの縦幅を返します */
		public int height() {return mapData.length;}
		
		/**
		 * 引数で指定した位置から移動出来る位置リストを返します。
		 * @param p 起点となる位置
		 * @param reverse 逆探索の場合true
		 * @return 起点pから移動可能な位置リスト
		 */
		public List<Position> movableList(Position p, boolean reverse) {
			List<Position> result = new ArrayList<>();
			Position tmp;
			
			//右
			if (p.getX() < width() - 1) {
				tmp = new Position(p.getX() + 1, p.getY());
				if (isMovable(p, tmp, reverse)) result.add(tmp);
			}
			//下
			if (p.getY() < height() - 1) {
				tmp = new Position(p.getX(), p.getY() + 1);
				if (isMovable(p, tmp, reverse)) result.add(tmp);
			}
			//左
			if (p.getX() > 0) {
				tmp = new Position(p.getX() - 1, p.getY());
				if (isMovable(p, tmp, reverse)) result.add(tmp);
			}
			//上
			if (p.getY() > 0) {
				tmp = new Position(p.getX(), p.getY() - 1);
				if (isMovable(p, tmp, reverse)) result.add(tmp);
			}
			
			return result;
		}
		
		/** 指定した位置のマップ情報を出力します。 */
		public char mapData(Position p) {
			return mapData[p.getY()][p.getX()];
		}
		
		/**
		 * 指定した起点から次の位置へ移動可能かどうか調べます。
		 * @param current 起点
		 * @param next 次の位置
		 * @param reverse 逆探索の場合true
		 * @return 移動可能ならtrue
		 */
		public boolean isMovable(Position current, Position next, boolean reverse) {
			char currData = mapData(current);
			char nextData = mapData(next);
			
			if (reverse) {
				if (
						(currData == 'A' && nextData == 'C') ||
						(currData == 'B' && nextData == 'A') ||
						(currData == 'C' && nextData == 'B')) {
					return true;
				}
			} else {
				if (
						(currData == 'A' && nextData == 'B') ||
						(currData == 'B' && nextData == 'C') ||
						(currData == 'C' && nextData == 'A')) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * A*アルゴリズムによる探索クラス
	 *
	 */
	public static class AStarWalker {
		/**
		 * スコア付きのPositionクラス
		 * 優先順位制御のためComparableを実装
		 *
		 */
		private static class ScorePosition implements Comparable<ScorePosition>{
			/* default */final Position pos;
			/* default */final int score;
			
			public ScorePosition(Position pos, int score) {
				this.pos = pos;
				this.score = score;
			}
			@Override
			public int compareTo(ScorePosition o) {
				return this.score - o.score;
			}
		}
		
		/**
		 * マップの左上から右下まで移動可能か計算します。
		 * @param field マップ情報
		 * @return 移動出来る場合true
		 */
		public static boolean calc(final Field field) {
			final Position start = new Position(0, 0);
			final Position goal = new Position(field.width()-1, field.height()-1);
			
			return search(field, start, goal);
		}
		
		/**
		 * 開始位置から目的地まで移動可能か計算します。
		 * @param field マップ情報
		 * @param start 開始位置
		 * @param goal 目的地
		 * @return 移動出来る場合true
		 */
		public static boolean search(Field field, Position start, Position goal){
			//探索済み(Close)リスト
			Set<Position> moved1 = new HashSet<>();
			Set<Position> moved2 = new HashSet<>();
			//優先順位付き(Open)リスト
			Queue<ScorePosition> queue1 = new PriorityQueue<>();
			Queue<ScorePosition> queue2 = new PriorityQueue<>();
			queue1.add(new ScorePosition(start, 0));
			queue2.add(new ScorePosition(goal, 0));

			moved1.add(queue1.peek().pos);
			moved2.add(queue2.peek().pos);
			while(true) {
				ScorePosition sp1 = queue1.poll();
				ScorePosition sp2 = queue2.poll();
				
				//いずれかが到達不可能
				if (sp1 == null || sp2 == null) break;
				
				//start方向からの探索
				{
					Position current = sp1.pos;
					if (moved2.contains(current)) return true;
				
					for(Position p : field.movableList(current, false)) {
						if (moved1.contains(p)) continue;
						moved1.add(p);
					
						queue1.add(new ScorePosition(p, score(start, current, p, goal)));
					}
				}
				
				//goal方向からの探索
				{
					Position current = sp2.pos;
					if (moved1.contains(current)) return true;
				
					for(Position p : field.movableList(current, true)) {
						if (moved2.contains(p)) continue;
						moved2.add(p);
					
						queue2.add(new ScorePosition(p, score(goal, current, p, start)));
					}
				}
			}
			
			return false;
		}
		
		/**
		 * 現在位置とゴールの距離からスコアを計算します。
		 * @param start スタート
		 * @param current 現在位置
		 * @param next 次に移動しようとしている位置
		 * @param goal ゴール
		 * @return スコア
		 */
		private static int score(Position start, Position current, Position next, Position goal) {
			return
					Math.abs(current.getX() - start.getX()) + Math.abs(current.getY() - start.getY()) +
					Math.abs(next.getX() - goal.getX()) + Math.abs(next.getY() - goal.getY());
		}
		
		//アルゴリズムを適用していない基本的な探索（検証用）
		//スタックオーバーフローのリスクもあるため非推奨
		/*
		public static boolean calc2(Field field) {
			Position start = new Position(0, 0);
			Position goal = new Position(field.width()-1, field.height()-1);
			Set<Position> moved = new HashSet<>();
			
			return search2(field, start, goal, moved);
		}
		
		private static boolean search2(Field field, Position current, Position goal, Set<Position> moved){
			if (current.equals(goal)) return true;
			moved.add(current);
			
			for(Position p : field.movableList(current, false)) {
				if (moved.contains(p)) continue;
				if (search2(field, p, goal, moved)) return true;
			}
			return false;
		}
		*/
	}
	
	// ==================================================
	// クラス定義終わり
	// ==================================================

	/**
	 * エントリーポイント
	 * @param args
	 */
	public static void main(String[] args) {
		//ここにファイル名を指定する
		String[] files = {
			"abc_maze/case1.in.txt",
			"abc_maze/case2.in.txt",
			"abc_maze/case3.in.txt",
			"abc_maze/case4.in.txt",
			"abc_maze/case5.in.txt",
			"abc_maze/case6.in.txt",
			"abc_maze/case7.in.txt"
		};
		
		for (String filename : files) {
			//long time = System.currentTimeMillis();
			System.out.println(AStarWalker.calc(new Field(filename)) ? "possible" : "impossible");
			//System.out.println(" - " + (System.currentTimeMillis() - time));
			
			//time = System.currentTimeMillis();
			//System.out.print(AStarWalker.calc2(new Field(filename)) ? "possible" : "impossible");
			//System.out.println(" - " + (System.currentTimeMillis() - time));
		}
	}
}
