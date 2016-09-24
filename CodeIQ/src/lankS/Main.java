package lankS;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Main {

	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String strStdIn = in.readLine();
		String[] mapSize = strStdIn.split(" ");
		char[][] map = new char[Integer.valueOf(mapSize[0])][Integer.valueOf(mapSize[1])];
		
		strStdIn = in.readLine();
		int row = 0;
		while(strStdIn!=null){
			for(int column = 0; column < strStdIn.length(); column++) {
				map[row][column] = strStdIn.charAt(column);
			}
			row++;
			strStdIn = in.readLine();
		}
		
		/*
		char[][] map = new char[4][4];
		map[0][0] = '.';map[0][1] = '#';map[0][2] = '.';map[0][3] = '.';
		map[1][0] = '.';map[1][1] = '.';map[1][2] = '.';map[1][3] = '#';
		map[2][0] = '.';map[2][1] = '#';map[2][2] = '.';map[2][3] = '.';
		map[3][0] = '.';map[3][1] = '#';map[3][2] = '#';map[3][3] = '.';
		*/
		
		System.out.println(AStarWalker.calc(new Field(map)));
	}
	
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
	 *
	 */
	public static class Field {
		/** マップデータ（YX） */
		private final char[][] mapData;
		
		/**
		 * 指定したファイルからマップを作成します。
		 * @param filename マップファイル名
		 */
		public Field(char[][] mapData) {
			this.mapData = mapData;
		}
		
		/** マップの横幅を返します */
		public int width() {return mapData[0].length;}
		/** マップの縦幅を返します */
		public int height() {return mapData.length;}
		
		/**
		 * 引数で指定した位置から移動出来る位置リストを返します。
		 * @param p 起点となる位置
		 * @return 起点pから移動可能な位置リスト
		 */
		public List<Position> movableList(Position p) {
			List<Position> result = new ArrayList<>();
			Position tmp;
			
			//右
			if (p.getX() < width() - 1) {
				tmp = new Position(p.getX() + 1, p.getY());
				if (isMovable(p, tmp)) result.add(tmp);
			}
			//下
			if (p.getY() < height() - 1) {
				tmp = new Position(p.getX(), p.getY() + 1);
				if (isMovable(p, tmp)) result.add(tmp);
			}
			//左
			if (p.getX() > 0) {
				tmp = new Position(p.getX() - 1, p.getY());
				if (isMovable(p, tmp)) result.add(tmp);
			}
			//上
			if (p.getY() > 0) {
				tmp = new Position(p.getX(), p.getY() - 1);
				if (isMovable(p, tmp)) result.add(tmp);
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
		 * @return 移動可能ならtrue
		 */
		public boolean isMovable(Position current, Position next) {
			char nextData = mapData(next);
			if (nextData == '.')
				return true;
			
			return false;
		}
	}
	
	/**
	 * 探索クラス
	 *
	 */
	public static class AStarWalker {
		/**
		 * スコア付きのPositionクラス
		 * 優先順位制御のためComparableを実装
		 *
		 */
		private static class ScorePosition implements Comparable<ScorePosition>{
			private static final int TOP = 0x1;
			private static final int RIGHT = 0x2;
			private static final int BOTTOM = 0x4;
			private static final int LEFT = 0x8;
			
			/* default */final Position pos;
			/* default */int rotate;
			/* default */int before;
			
			public ScorePosition(Position pos) {
				this.pos = pos;
				before = 0;
				rotate = 0;
			}
			
			public ScorePosition(Position pos, ScorePosition prev) {
				this.pos = pos;
				
				if (prev.pos.x < pos.x) {
					before = LEFT;
				}
				else if (prev.pos.x > pos.x) {
					before = RIGHT;
				}
				else if (prev.pos.y < pos.y) {
					before = BOTTOM;
				}
				else if (prev.pos.y > pos.y) {
					before = TOP;
				} else {
					before = 0;
				}
				
				int isRotate = 0;
				if (prev.before != 0 && (prev.before & before) == 0) {
					isRotate = 1;
				}
				this.rotate = prev.rotate + isRotate;
			}
			
			public boolean writeIfBetter(ScorePosition o) {
				if (this.rotate < o.rotate) {
					return false;
				}
				
				if (this.rotate == o.rotate) {
					if ((before & o.before) > 0) {
						return false;
					}
					this.before |= o.before;
				} else {
					this.before = o.before;
				}
				this.rotate = o.rotate;
				return true;
			}
			@Override
			public int compareTo(ScorePosition o) {
				return this.rotate - o.rotate;
			}
			
			@Override
			public String toString() {
				return "x:" + pos.x + ",y:" + pos.y + ",rotate:" + rotate + ",before:" + before;
			}
		}
		
		/**
		 * マップの左上から右下まで移動可能か計算します。
		 * @param field マップ情報
		 * @return 移動出来る場合true
		 */
		public static int calc(final Field field) {
			final Position start = new Position(0, 0);
			final Position goal = new Position(field.width()-1, field.height()-1);
			
			return search(field, start, goal);
		}
		
		/**
		 * 開始位置から目的地まで移動可能か計算します。
		 * @param field マップ情報
		 * @param start 開始位置
		 * @param goal 目的地
		 * @return goalまでのrotate数
		 */
		public static int search(Field field, Position start, Position goal){
			ScorePosition result = null;
			
			//探索済み(Close)リスト
			Map<Position, ScorePosition> moved1 = new HashMap<>();
			//優先順位付き(Open)リスト
			Queue<ScorePosition> queue1 = new PriorityQueue<>();
			queue1.add(new ScorePosition(start));

			moved1.put(start, queue1.peek());
			while(true) {
				ScorePosition current = queue1.poll();
				
				//到達不可能
				if (current == null) break;
				
				//最後にゴールの値を参照したいので覚えておく
				if (current.pos.equals(goal)) {
					result = current;
				}
				
				for(Position p : field.movableList(current.pos)) {
					ScorePosition next = moved1.get(p);
					if (next == null) {
						next = new ScorePosition(p, current);
						moved1.put(p, next);
						queue1.add(next);
					} else {
						if (next.writeIfBetter(new ScorePosition(p, current)))
							queue1.add(next);
					}
				}
			}
			
			return result.rotate;
		}
	}

}
