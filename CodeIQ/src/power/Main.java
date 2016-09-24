package power;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		
		for(;;){
			StringBuilder sb = new StringBuilder();
			int c;
			while ((c = System.in.read()) != 10) {
				sb.append((char)c);
			}
		
			String[] str = sb.toString().split(" ");
			long a = Long.parseLong(str[0]);
			long b = Long.parseLong(str[1]);
		
			if (a == 0 && b == 0) break;
		
			System.out.println(power(a,b) % 100000000);
			
			//System.out.println(BigDecimal.valueOf(a).pow((int) b));
		
			/*
			long x = a;
			for(int i = 0; i < (b - 1); i++) {
				x = (x * a) % 100000000L;
			}
			System.out.println(x);
		*/
		}
	}
	
	public static long power(long x, long y) {
		x = x  % 100000000L;
		if (y == 0) return 1;
		
		if (y % 2 == 0)
			return power(x*x, y / 2) % 100000000L;
		else
			return (x * power(x, y - 1)) % 100000000L;
	}
	
/*
テストケース１
2 123
3 456
109 1009
2019 30003
509 90006
317 165764
0 0

テストケース２
3863080011 2613515386
21321331 1234653876
521340345 1396720193
1843165372 2835135645
417934669 3961963772
7564929 1434531250
3713420107 616334320
57564748 243756997
2249407224 3483719867
444893257 3572472608
0 0
*/

}
