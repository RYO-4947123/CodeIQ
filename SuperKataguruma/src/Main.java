import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {

	// ==================================================
	// ○クラス定義
	// ※1ファイルで完結させる関係上インナークラス化しています
	// ==================================================
	/**
	 * 肩車をする人です
	 *
	 */
	public static class Human {

		/**
		 * 身長の比較方法を定義したComparatorです。
		 * ※身長が同じ場合は、体重の降順とします
		 *
		 */
		public static class HeightComparator implements Comparator<Human> {
			@Override
			public int compare(Human o1, Human o2) {
				if (o1.height == o2.height)
					return o2.weight - o1.weight;
				return o1.height - o2.height;
			}
		}

		/**
		 * 体重の比較方法を定義したComparatorです。
		 * ※体重が同じ場合は、身長の降順とします
		 *
		 */
		public static class WeightComparator implements Comparator<Human> {
			@Override
			public int compare(Human o1, Human o2) {
				if (o1.weight == o2.weight)
					return o2.height - o1.height;
				return o1.weight - o2.weight;
			}
		}

		/** 個体番号 */
		private final int id;
		/** 身長 */
		private final int height;
		/** 体重 */
		private final int weight;

		public Human(int id, int height, int weight) {
			this.id = id;
			this.height = height;
			this.weight = weight;
		}

		public int id() {return id;}
		public int height() {return height;}
		public int weight() {return weight;}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + height;
			result = prime * result + weight;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Human other = (Human) obj;
			if (height != other.height)
				return false;
			if (weight != other.weight)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "(" + height + ", " + weight + ")";
		}

	}

	/**
	 * ファイルから人々を作成するファクトリークラスです
	 *
	 */
	public static final class HumanFactory {

		private int lastId = 0;

		/* シングルトン */
		private static final HumanFactory instance = new HumanFactory();
		private HumanFactory(){}
		public static HumanFactory getInstance() {return instance;}

		/**
		 * 指定したファイルからHumanクラス配列を作成します。
		 * @param filename
		 * @return Humanのリスト（エラーが発生した場合は空のリスト）
		 */
		public List<Human> createHumansForFile(String filename) {
			List<Human> result = new ArrayList<Main.Human>();

			FileReader fReader = null;
			BufferedReader br = null;
			try {
				fReader = new FileReader(filename);
				br = new BufferedReader(fReader);
				String line;

				while ((line = br.readLine()) != null) {
					//XXX:今回は読み込むテキストが決まっているのでエラー処理していない事に注意
					String[] args = line.split(" ");
					result.add(new Human(++lastId, Integer.parseInt(args[0]), Integer.parseInt(args[1])));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
					fReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return result;
		}
	}

	// ==================================================
	// クラス定義終わり
	// ==================================================

	/**
	 * エントリーポイント
	 * @param args
	 */
	public static void main(String[] args) {

		//ここに読み込むファイル名を指定して下さい。
		List<Human> humans = new ArrayList<Main.Human>();
			//HumaFactory.getInstance().createHumansForFile("");

		humans.add(new Human(1, 166, 71));
		humans.add(new Human(2, 166, 71));
		humans.add(new Human(3, 178, 84));
		humans.add(new Human(4, 174, 85));
		humans.add(new Human(5, 174, 85));
		humans.add(new Human(6, 174, 65));

		//身長の昇順でソート
		Collections.sort(humans, new Human.HeightComparator());

		//体重の昇順ソードリストを複製する。
		List<Human> humans2 = new ArrayList<Human>(humans);
		Collections.sort(humans2, new Human.WeightComparator());

		//処理スタート！
		List<Human> result = kataguruma(humans, humans2);

		//結果出力
		for(Human h : result) {
			System.out.println(h.toString());
		}
	}

	/**
	 * 身長で昇順ソートされたリストと体重で昇順ソートされたリストから、
	 * IDが同じ順で並んでいるものをピックアップします。
	 * その中で最も長く並んでいるものを返します。
	 * <br/>
	 * ※身長又は体重が同じ場合は肩車出来ないようにするには、
	 * 一方を降順にすることで意図的に矛盾を発生させて、選ばれないようにすることができます。
	 * @param hcList 身長の昇順ソートされたリスト（同じ場合は体重の降順）
	 * @param wcList 体重の昇順ソートされたリスト（同じ場合は身長の降順）
	 * @return 引数で渡された2つのリストに矛盾しない最長のIDの並びのリスト
	 */
	public static List<Human> kataguruma(List<Human> hcList, List<Human> wcList) {
		//操作済みのオブジェクトを入れるためのリスト
		List<Human> sumiList = new ArrayList<Human>();
		return kataguruma(hcList, wcList, sumiList);
	}

	public static List<Human> kataguruma(List<Human> hcList, List<Human> wcList, List<Human> sumiList) {

		List<Human> result = new ArrayList<Human>();
		List<Human> tmp = new ArrayList<Human>();
		int i = 0;

		//身長の低い人から順に走査
		for (Human h1 : hcList) {
			tmp.clear();
			i++;

			//既に走査済みの場合は処理を飛ばす
			if (sumiList.contains(h1)) {
				continue;
			}
			sumiList.add(h1);

			//体重リストに走査中の人がいない場合は肩車不可
			if (!wcList.contains(h1)) {
				continue;
			}
			tmp.add(h1);

			//走査中の人が体重リストのどの位置にいるかチェック
			int j = 0;
			for (Human h2 : wcList) {
				j++;
				if (!h1.equals(h2)) {
					continue;
				}
				break;
			}

			//捜査中の人よりも身長も体重も大きい人がいるか再起的にチェック
			if (i < hcList.size() && j < wcList.size()) {
				tmp.addAll(kataguruma(hcList.subList(i, hcList.size()), wcList.subList(j, wcList.size()), sumiList));
			}

			//今まで一番長いリストを保存
			if (result.size() < tmp.size()) {
				result.clear();
				result.addAll(tmp);
			}
		}

		return result;
	}

}
