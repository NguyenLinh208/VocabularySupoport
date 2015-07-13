package jp.ac.tuat.sys.linh;

public class ArrayUtil {

	private ArrayUtil() {}

	public static <T> void shuffle(T ary[]) {
		for(int i=1; i<ary.length; i++) {
			int j = (int)(Math.random() * (i+1));
			swap(ary, i, j);
		}
	}

	public static <T> void swap(T ary[], int i, int j) {
		if(i != j) {
			T tmp = ary[i];
			ary[i] = ary[j];
			ary[j] = tmp;
		}
	}

	public static <T> int linearSearch(T ary[], T target) {
		for(int i=0; i<ary.length; i++) {
			if(ary[i].equals(target)) {
				return i;
			}
		}
		return -1;
	}
	public static int linearSearch(int ary[], int target) {
		for(int i=0; i<ary.length; i++) {
			if(ary[i] == target) {
				return i;
			}
		}
		return -1;
	}

}
