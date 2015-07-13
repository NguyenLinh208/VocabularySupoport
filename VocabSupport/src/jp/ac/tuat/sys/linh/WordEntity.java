package jp.ac.tuat.sys.linh;

import java.io.Serializable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

@SuppressWarnings("serial")
public class WordEntity implements Serializable {

	private int mId           = 0;
	private String mSpell     = "";
	private String mMeaning   = "";
	private int mPartId       = 0;
	private String mPart      = "";
	private String mExampleEn = "";
	private String mExampleJa = "";
	private int mCategoryId   = 0;
	private String mCategory  = "";
	private int mLevel        = 0;
	private int mBookmark     = 0;
	private int mPriority     = 0;
	private int mPrevDate     = 0;	// System.currentTimeMillis()を利用して，1970年1月1日からの経過日数で表現
	private int mNextDate     = 0;	// 同上

	public String toString() {
		return getSpell() + "\n" + "[" + getPart() + "] " + "[" + getCategory() + "] " + getMeaning() + "\n" + getExampleEn() + "\n" + getExampleJa();
	}

	public int getId() {
		return mId;
	}
	public void setId(int id) {
		mId = id;
	}
	public String getSpell() {
		return mSpell;
	}
	public void setSpell(String spell) {
		mSpell = spell;
	}
	public String getMeaning() {
		return mMeaning;
	}
	public void setMeaning(String meaning) {
		mMeaning = meaning;
	}
	public int getPartId() {
		return mPartId;
	}
	public void setPartId(int id) {
		mPartId = id;
	}
	public String getPart() {
		return mPart;
	}
	public void setPart(String part) {
		mPart = part;
	}
	public String getExampleEn() {
		return mExampleEn;
	}
	public void setExampleEn(String example) {
		mExampleEn = example;
	}
	public String getExampleJa() {
		return mExampleJa;
	}
	public void setExampleJa(String example) {
		mExampleJa = example;
	}
	public int getCategoryId() {
		return mCategoryId;
	}
	public void setCategoryId(int id) {
		mCategoryId = id;
	}
	public String getCategory() {
		return mCategory;
	}
	public void setCategory(String category) {
		mCategory = category;
	}
	public int getLearningLevel() {
		return mLevel;
	}
	public void setLearningLevel(int level) {
		mLevel = level;
	}
	public int getBookmark() {
		return mBookmark;
	}
	public void setBookmark(int bookmark) {
		mBookmark = bookmark;
	}
	// 復習優先度
	public int getPriority() {
		return mPriority;
	}
//	public void setPriority(int priority) {
//		mPriority = priority;
//	}
	public int getPrevLearning() {
		return mPrevDate;
	}
	public void setPrevLearning(int date) {
		mPrevDate = date;
	}
	public int getNextLearning() {
		return mNextDate;
	}
	public void setNextLearning(int date) {
		mNextDate = date;
	}

	private static final double UP_POINT = 0.75;			// 復習間隔のどのくらいの時点で急上昇を始めるか（%）
	private static final double UP_POINT_VALUE = 0.30;		// 急上昇を始める地点での優先度（%）

	private static final int LONG_MEMORY_THRESHOLD = 28;	// 何日後の復習で覚えていれば長期記憶と見なすか
	private static final int INTERVAL_MAX = 60;				// 復習間隔の最大値

	public static final int PRIORITY_MAX = (int)(Math.pow(UP_POINT_VALUE, 1.0 / (UP_POINT-1)));

	public void calcPriority() {
		double passedDayPer = Math.min(1.0, (double)(DateUtil.getToday()-mPrevDate) / (mNextDate-mPrevDate));
		double diff=0.0;

		diff=Math.min(1.0, 1.0 - 1.0 / Math.exp((double)mBookmark));

		switch(mLevel) {
		case 1:	// 初期復習
		case 2:	// 短期記憶
			// 優先度の計算式：一次関数
			//mPriority = Math.max(1, (int)(PRIORITY_MAX * passedDayPer));
			mPriority = Math.max(1, (int)(PRIORITY_MAX * passedDayPer + 0.01 * diff));
			break;
		case 3:	// 長期記憶
			// 優先度の計算式：指数関数
			// (優先度の最高値)^(経過日数/復習予定間隔)
			mPriority = (int)(Math.pow(PRIORITY_MAX, passedDayPer) + 0 * diff);
			break;
		default:
			mPriority = 1;
			break;
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
				nextInterval = Math.min(INTERVAL_MAX, plannedDay + Math.min(plannedDay, passedDay));
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
		mPriority = 1;
	}
	
	public boolean isPriorityMax() {
		return mPriority >= PRIORITY_MAX;
	}
	
	public void updateBookmark(boolean remember) {
		
		if(remember) {
			WordDbHelper mDbHelper = WordDbHelper.getInstance();
			// データベース取得 
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			
			//取得
			Cursor c = db.query(WordDbHelper.WORD_TABLE,
					new String[] { "bookmark" },
					WordDbHelper.KEY + " == ?",
					new String[]{String.valueOf(mId)}, null, null, null);
			c.moveToFirst();
			mBookmark = Integer.parseInt(c.getString(0)); 
			db.close();
		}
	}

}
