package jp.ac.tuat.sys.linh;

/* 現在未使用 */

public class WordPriority {

	private static final double UP_POINT = 0.75;			// 復習間隔のどのくらいの時点で急上昇を始めるか（%）
	private static final double UP_POINT_VALUE = 0.30;		// 急上昇を始める地点での優先度（%）

	private static final int LONG_MEMORY_THRESHOLD = 28;	// 何日後の復習で覚えていれば長期記憶と見なすか
	private static final int INTERVAL_MAX = 60;				// 復習間隔の最大値

	public static final int PRIORITY_MAX = (int)(Math.pow(1.0 / UP_POINT_VALUE, 1.0 / (1-UP_POINT)));
	public static final int HIGH_PRIORITY_THRESHOLD = (int)(PRIORITY_MAX * 0.90);

	private int mLevel;
	private int mPriority;
	private int mPrevDate;
	private int mNextDate;

	public WordPriority(int level, int prev, int next, int priority) {
		mLevel = level;
		mPrevDate = prev;
		mNextDate = next;
		mPriority = priority;
	}

	public void calcPriority() {
		double passedDayPer = Math.min(1.0, (double)(DateUtil.getToday()-mPrevDate) / (mNextDate-mPrevDate));
		switch(mLevel) {
		case 1:	// 初期復習
		case 2:	// 短期記憶
			// 優先度の計算式：一次関数
			mPriority = Math.max(1, (int)(PRIORITY_MAX * passedDayPer));
			break;
		case 3:	// 長期記憶
			// 優先度の計算式：指数関数
			// (優先度の最高値)^(経過日数/復習予定間隔)
			mPriority = (int)(Math.pow(PRIORITY_MAX, passedDayPer));
			break;
		default:
			mPriority = 0;
		}
	}

	public void updatePriority(boolean remember) {
		int currentDate = DateUtil.getToday();
		int passedDay   = currentDate - mPrevDate;	// 前回の学習からの経過日数
		int plannedDay  = mNextDate   - mPrevDate;	// 復習予定間隔
		int nextInterval = 0;
		switch(mLevel) {
		case 0:	// 未学習
			mLevel ++;
			nextInterval = 1;	// 翌日
			break;
		case 1:	// 初期復習
			mLevel ++;
			nextInterval = 7;	// 1週間後
			break;
		case 2:	// 短期記憶
			if(remember) {
				// 一定日数以上経過して覚えていたら長期記憶に移行
				if(passedDay >= LONG_MEMORY_THRESHOLD) {
					mLevel ++;
				}
				// 今回の復習間隔を加算したものが次の復習間隔
				// ただし，2倍を超えない
				nextInterval = plannedDay + Math.min(plannedDay, passedDay);
			} else {
				// 今回の復習間隔のまま
				nextInterval = plannedDay;
				// 今回の復習間隔 / 2を加算したものが次の復習間隔
				//nextInterval = planedDay + Math.min(planedDay, passedDay / 2);
			}
			break;
		case 3:	// 長期記憶
			if(remember) {
				// 今回の復習間隔を加算したものが次の復習間隔
				// ただし，復習間隔の最大値を超えない
				nextInterval = Math.min(INTERVAL_MAX, plannedDay + passedDay);
			} else {
				// 復習間隔が短いにもかかわらず忘れていた場合，短期記憶に戻る
				if(plannedDay < LONG_MEMORY_THRESHOLD) {
					mLevel --;
				}
				// 今回の復習間隔 / 2が次の復習間隔
				nextInterval = plannedDay / 2;
			}
			break;
		}
		mPrevDate = currentDate;
		mNextDate = currentDate + nextInterval;
		mPriority = 0;
	}

	public boolean isHighPriority() {
		return mPriority >= HIGH_PRIORITY_THRESHOLD;
	}

	public int getLevel() {
		return mLevel;
	}
	public void setLevel(int level) {
		mLevel = level;
	}
	public int getPriority() {
		return mPriority;
	}
	public int getPrevDate() {
		return mPrevDate;
	}
	public void setPrevDate(int date) {
		mPrevDate = date;
	}
	public int getNextDate() {
		return mNextDate;
	}
	public void setNextDate(int date) {
		mNextDate = date;
	}

}
