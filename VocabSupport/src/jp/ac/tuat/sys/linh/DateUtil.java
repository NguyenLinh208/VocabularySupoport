package jp.ac.tuat.sys.linh;

public class DateUtil {
	private static final long MILLIS_TO_DAY = 1000 * 60 * 60 * 24;	// ミリ秒→日数の変換に使う定数

	private static int mPassedDays = 0;

	private DateUtil() {}

	public static int getToday() {
		return (int)(System.currentTimeMillis() / MILLIS_TO_DAY) + mPassedDays;
	}

	public static int getPassedDays() {
		return mPassedDays;
	}
	public static void setPassedDays(int passedDays) {
		mPassedDays = passedDays;
	}
	public static int passDays(int days) {
		return mPassedDays += days;
	}

}
