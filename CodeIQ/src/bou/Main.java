package bou;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

public class Main {

	static final int MAX=5002;
    static final int SIZE=MAX*10;
    static byte[] buf=new byte[SIZE];
    static int count=0;

    static int read_int(){
        int r=0;
        while(buf[count] >= '0' && buf[count] <= '9' ){
            r = r * 10 + buf[count++] - '0';
        }
        count++;
        return r;
    }

	public static void main(String[] args) throws IOException {

		System.in.read(buf,0,SIZE);
        int a[] = new int[MAX];
        for(int i=0;i<MAX;i++){
            a[i] = read_int();
        }
		
		int L = Integer.valueOf(a[0]);
		int N = Integer.valueOf(a[1]);
		
		Set<Integer> set = new TreeSet<Integer>();
		for(int i = 0; i < N; i++) {
			set.add(Integer.valueOf(a[i + 2]));
		}
		
		
		long st = System.currentTimeMillis();
		int result = calc(L, set); //1571200
		System.out.println(result);
		
		System.out.println(System.currentTimeMillis() -st);

	}
	
	private static int[] toInt(Object[] list) {
		int[] result = new int[list.length];
		for(int i = 0; i < list.length; i++) result[i] = (Integer)list[i];
		return result;
	}
	
	private static int calc(int L, Set<Integer> set) {
		int[] list = toInt(set.toArray());
		int result = 0;
		
		int max = list[list.length-1];
		int max2 = list[list.length-2];

		for (int idxX = 0; idxX < list.length - 2; idxX++) {
			int x = list[idxX];
			if( (x + max2 + max) < L) continue;
			if( (x*3 + 3) > L) return result;
			for(int idxY = idxX + 1; idxY < list.length - 1; idxY++){
				int y = list[idxY];
				if( (x + y + max) < L) continue;
				if( (x + y + y) >= L) break;
				
				int ans = L - x - y;
				if (set.contains(ans)){
					result++;
				}
			}
		}
		return result;
	}

}
