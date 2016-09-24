package kakko;

public class Main {

	// ==================================================
	// ○クラス定義
	// ※1ファイルで完結させる関係上インナークラス化しています
	// ==================================================

	/**
	 * オペレータを表す列挙型<br/>
	 * 「*, /, %, +, -, <<, >>, &, ^, |」を表す事が出来ます。
	 *
	 */
	public static enum Operator {
		NULL("", 0),
		ASTERISK("*", 4),
		SLASH("/", 4),
		PER("%", 4),
		PLUS("+", 5),
		MINUS("-", 5),
		LSHIFT("<<", 6),
		RSHIFT(">>", 6),
		AND("&", 9),
		HAT("^", 10),
		PIPE("|", 11);

		private String str;
		private int priority;

		Operator(String s, int i) {
			str = s;
			priority = i;
		}

		public int getPriority() { return priority;}

		/**
		 * 与えられた文字列から適切なオペレータを返します。
		 * @param s　「*, /, %, +, -, <<, >>, &, ^, |」いずれか
		 * @return 文字列に対応したオペレータ
		 */
		public static Operator getOperatorForString(String s) {
			if (s.equals("*")) return ASTERISK;
			if (s.equals("/")) return SLASH;
			if (s.equals("%")) return PER;
			if (s.equals("+")) return PLUS;
			if (s.equals("-")) return MINUS;
			if (s.equals("<<")) return LSHIFT;
			if (s.equals(">>")) return RSHIFT;
			if (s.equals("&")) return AND;
			if (s.equals("^")) return HAT;
			if (s.equals("|")) return PIPE;
			return NULL;
		}

		@Override
		public String toString() {
			return " " + str + " ";
		}

	}

	/**
	 * 便宜上の二項式を表すクラスです。
	 * <br/>
	 * 二項式は左オペランド、右オペランド、オペレータからなります。<br/>
	 * 便宜上とは、多項式の場合に左多項式と右多項式に分けているためです。<br/>
	 * さらに括弧（）の有無を保持する事が出来ます。
	 *
	 */
	public static class Binomial {
		/** 左オペランド */
		private Binomial left;
		/** 右オペランド */
		private Binomial right;
		/** オペレータ */
		private Operator operator = Operator.NULL;
		/** 括弧付きかどうか */
		private boolean isKakko;
		/** ２分木のルートかどうか */
		private boolean isRoot;
		/** ２分木のリーフまでたどり着いた場合のオペランド */
		private String operand = null;

		/**
		 * コンストラクタ<br/>
		 * 与えられた式を２分木に変換します。
		 * このコンストラクタで作成されたインスタンスはルートとなります。
		 * @param str オペランド又は二項式又は多項式
		 */
		public Binomial(String str) {
			init(str, true);
		}

		/**
		 * コンストラクタ<br/>
		 * 通常ルート以外の接点のために使用します。
		 * @param str オペランド又は二項式又は多項式
		 * @param top ルートかどうか
		 */
		private Binomial(String str, boolean top) {
			init(str, top);
		}

		/**
		 * 与えられた式を２分木に変換します。
		 * <br/>
		 * XXX:オペレータとオペランドの間には必ず半角スペース1文字を設定しないと正しく動作しません。
		 * @param str オペランド又は二項式又は多項式
		 * @param top ルートかどうか
		 */
		private void init(String str, boolean top) {
			isRoot = top;
			str = clearKakko(str);

			String leftStr = getLeftString(str);
			String rightStr = getRightString(str);

			//対象文字列strが二項式である場合
			if (rightStr != null) {
				operator = Operator.getOperatorForString(rightStr.split(" ")[0]);
				right = new Binomial(rightStr.substring(rightStr.indexOf(' ')+1), false);

				if (leftStr != null) {
					left = new Binomial(leftStr, false);
				}
			} else {
				operand = leftStr;
			}
		}

		/**
		 * この二項式の開始と終了が括弧で包まれているかチェックします。
		 * 該当した場合はフラグ{@code isKakko}をtrueに設定します。
		 * @param str チェック対象文字列
		 * @return 括弧で包まれている場合は、括弧を除いた文字列。
		 */
		private String clearKakko(String str) {
			if (str.startsWith("(") && str.endsWith(")")) {
				if (getPosOfClose(str) == str.length()) {
					isKakko = true;
					//再起的に読んで連続した無駄な括弧を削除する
					return clearKakko(str.substring(1, str.length()-1));
				}
			}
			return str;
		}

		/**
		 * 与えられた文字列によって、以下の文字列を返します。<br/>
		 * ・オペランド<br/>
		 * 　そのまま返します。（スペースは取り除く）<br/>
		 * ・二項式<br/>
		 * 　左オペランド<br/>
		 * ・多項式<br/>
		 * 　多項式の左多項式
		 * @param str オペランド又は二項式又は多項式
		 * @return 左オペランド又は、２分木のリーフまでたどり着いた場合のオペランド
		 */
		private String getLeftString(String str) {
			if (str.startsWith("(")) {
				return str.substring(0, getPosOfClose(str));
			}
			return str.split(" ")[0];
		}

		/**
		 * 二項式の右オペランドを出力します。
		 * 文字列がオペランドの場合は、nullを返します。
		 * @param str オペランド又は二項式又は多項式
		 * @return 右オペランド又は、２分木のリーフまでたどり着いた場合はnull
		 */
		private String getRightString(String str) {
			if (str.indexOf(' ') == -1)
				return null;
			else
				return str.substring(getPosOfClose(str)+1).trim();
		}

		/**
		 * 指定した文字列の先頭括弧に対応する閉じ括弧の位置を出力します。
		 * <br/>
		 * ※必ず先頭に括弧があり、対応する閉じ括弧が存在する事
		 * @param str 先頭に括弧がある文字列
		 * @return 先頭括弧に対応する閉じ括弧の位置
		 */
		private int getPosOfClose(String str) {
			int start = 0;
			int close = 0;
			int end = 0;
			for(char c : str.toCharArray()) {
				if (c == '(') start++;
				if (c == ')') close++;
				if (start == close)
					break;
				end++;
			}
			return end+1;
		}

		@Override
		public String toString() {
			//リーフの場合はそのまま出力
			if (operand != null)
				return operand;

			//左オペランドのオペレータが自オペレータよりも優先度が高い場合は、括弧は不要
			if (operator.getPriority() >= left.operator.getPriority()) {
				left.isKakko = false;
			}

			//右オペランドのオペレータが自オペレータよりも優先度が高い場合は、括弧は不要
			if (operator.getPriority() >= right.operator.getPriority()) {
				right.isKakko = false;
			}

			//ルートは他に比較するオペランドがないため常に括弧が不要
			if (isRoot) {
				return left.toString() + operator + right.toString();
			}

			//上記全ての条件を満たさない場合は、元々の文字列の表現に従う
			if (isKakko) {
				return "(" + left.toString() + operator + right.toString() + ")";
			}
			return left.toString() + operator + right.toString();
		}
	}

	// ==================================================
	// クラス定義終わり
	// ==================================================

	/** エントリポイント */
	public static void main(String[] args) {
		Binomial b = new Binomial("12 | (16 >> ((2 + (4 % 3))))");
		System.out.println(b.toString());

		Binomial b2 = new Binomial("((7 & 13) + 4 / (3 + ((5 << 2) | 3)))");
		System.out.println(b2.toString());

	}

}
