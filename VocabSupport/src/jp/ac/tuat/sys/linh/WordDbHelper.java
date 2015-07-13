package jp.ac.tuat.sys.linh;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class WordDbHelper extends SQLiteOpenHelper {

	// データベースのバージョン
	private static final int DB_VERSION = 1;

	// デフォルト設定
	public static final String DEF_DB_NAME = "WordData.db";
	// 品詞一覧
	private static final String[] DEF_PARTS = {"名詞", "動詞", "助動詞", "形容詞", "副詞", "前置詞", "接続詞", "熟語"};
	// カテゴリ
	private static final String[] DEF_CATEGORIES = {"Clothing","Direction","Nature","Culture","Daily Routines","Relationship","Weather","Office","Travel","TAT200"};

	// 単語テーブル
	public static final String WORD_TABLE = "wordTable";
	public static final String KEY = "_id";							// 主キー
	public static final String WCOL_SPELL = "spell";				// スペル
	public static final String WCOL_MEANING = "meaning";			// 意味
	public static final String WCOL_PART = "partId";				// 品詞：part-of-speech
	public static final String WCOL_EXAMPLE_EN = "exampleEn";		// 例文（英語）
	public static final String WCOL_EXAMPLE_JA = "exampleJa";		// 例文（日本語）
	public static final String WCOL_CATEGORY = "categoryId";		// カテゴリー
	public static final String WCOL_LEVEL = "level";				// 学習段階
	public static final String WCOL_BOOKMARK = "bookmark";		    // 学習段階
	public static final String WCOL_PREV_LEARNING = "prevLearning";	// 前回学習日時
	public static final String WCOL_NEXT_LEARNING = "nextLearning";	// 次回学習予定日時

	// 品詞テーブル：part-of-speech
	public static final String PART_TABLE = "partTable";
	public static final String PCOL_KEY   = WCOL_PART;				// 主キー
	public static final String PCOL_NAME  = "part";					// 品詞名

	// カテゴリテーブル
	public static final String CATEGORY_TABLE = "categoryTable";
	public static final String CCOL_KEY       = WCOL_CATEGORY;		// 主キー
	public static final String CCOL_NAME      = "category";			// 分類名（経済など）

	// 単語テーブルの全ての列
	public static final String[] WCOL_ALL = new String[] {
		KEY,
		WCOL_SPELL,
		WCOL_MEANING,
		WCOL_PART,
		WCOL_EXAMPLE_EN,
		WCOL_EXAMPLE_JA,
		WCOL_CATEGORY,
		WCOL_LEVEL,
		WCOL_BOOKMARK,
		WCOL_PREV_LEARNING,
		WCOL_NEXT_LEARNING
	};

	// 単語情報の射影
	public static final String WCOL_PART_FULL     = WORD_TABLE+"."+WCOL_PART;
	public static final String WCOL_CATEGORY_FULL = WORD_TABLE+"."+WCOL_CATEGORY;
	public static final String[] WORD_ALL_INFO = new String[] {
		KEY,
		WCOL_SPELL,
		WCOL_MEANING,
		WCOL_PART_FULL,
		WCOL_EXAMPLE_EN,
		WCOL_EXAMPLE_JA,
		WCOL_CATEGORY_FULL,
		WCOL_LEVEL,
		WCOL_BOOKMARK,
		WCOL_PREV_LEARNING,
		WCOL_NEXT_LEARNING,
		PCOL_NAME,
		CCOL_NAME
	};
	// 全表の結合条件
	public static final String WHERE_PART_JOIN =
			WCOL_PART_FULL     + " = " + PART_TABLE+"."+PCOL_KEY + " and " +
			WCOL_CATEGORY_FULL + " = " + CATEGORY_TABLE+"."+CCOL_KEY;

	// Singleton Pattern
	private static WordDbHelper mDbHelper = null;
	public static WordDbHelper getInstance() {
		return mDbHelper;
	}

	// DB初期化関数
	public static void initDataBase(Context context, String dbName) {
		// すでに開かれているDBは閉じておく
		if(mDbHelper != null) {
			mDbHelper.close();
		}
		// DB初期化
		mDbHelper = new WordDbHelper(context, dbName);
		// DAOをリセットしておく
		WordDao.reset();
	}

	private WordDbHelper(Context context, String dbName) {
		//super(context, context.getExternalFilesDir(null) + "/" + dbName, null, DB_VERSION);
		super(context, MainActivity.getApplicationDir() + "/" + dbName, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 単語テーブル作成
		db.execSQL("create table " + WORD_TABLE + " ("
				+ KEY                + " integer primary key autoincrement, "
				+ WCOL_SPELL         + " text, "
				+ WCOL_MEANING       + " text, "
				+ WCOL_PART          + " integer, "
				+ WCOL_EXAMPLE_EN    + " text, "
				+ WCOL_EXAMPLE_JA    + " text, "
				+ WCOL_CATEGORY      + " integer, "
				+ WCOL_LEVEL         + " integer default 0, "
				+ WCOL_BOOKMARK      + " integer default 0, "
				+ WCOL_PREV_LEARNING + " integer, "
				+ WCOL_NEXT_LEARNING + " integer"
				+ ");"
		);
		// 品詞テーブル作成
		db.execSQL("create table " + PART_TABLE + " ("
				+ PCOL_KEY  + " integer primary key, "
				+ PCOL_NAME + " text"
				+ ");"
		);
		// カテゴリテーブル作成
		db.execSQL("create table " + CATEGORY_TABLE + " ("
				+ CCOL_KEY  + " integer primary key, "
				+ CCOL_NAME + " text"
				+ ");"
		);

		// 品詞テーブル初期化
		initPartTable(db, DEF_PARTS);
		// カテゴリテーブル初期化
		initCategoryTable(db, DEF_CATEGORIES);
	}

	public void initPartTable(SQLiteDatabase db, String[] data) {
		// 現在のテーブルを削除する
		db.execSQL("delete from " + PART_TABLE);
		// トランザクション開始
		db.beginTransaction();
		try {
			SQLiteStatement stmt = db.compileStatement("insert into " + PART_TABLE + " values (?,?);");
			for(int i=0; i<data.length; i++) {
				stmt.bindLong(1, i);			// キー
				stmt.bindString(2, data[i]);	// 対応する品詞名
				stmt.executeInsert();
			}
			// 最後まで完了したらコミット
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void initCategoryTable(SQLiteDatabase db, String[] data) {
		// 現在のテーブルを削除する
		db.execSQL("delete from " + CATEGORY_TABLE);
		// トランザクション開始
		db.beginTransaction();
		try {
			SQLiteStatement stmt = db.compileStatement("insert into " + CATEGORY_TABLE + " values (?,?);");
			for(int i=0; i<data.length; i++) {
				stmt.bindLong(1, i);			// キー
				stmt.bindString(2, data[i]);	// 対応するカテゴリ名
				stmt.executeInsert();
			}
			// 最後まで完了したらコミット
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// バージョン変更時の処理
//		db.execSQL("drop table if exists " + WORD_TABLE);
//		db.execSQL("drop table if exists " + PART_TABLE);
//		db.execSQL("drop table if exists " + CATEGORY_TABLE);
//		onCreate(db);
//
//		debugInsertWord(db);
	}



	//////////////////////////////////////////////////////////////////////
	// debug用関数
	//
	public void debugInsertWord(SQLiteDatabase db) {
		db.beginTransaction();
		// 単語データベースに登録
		try {
			for(int i=0; i<5000; i++) {
				// WordEntity -> ContentValuesに変換
				WordEntity word = new WordEntity();
				word.setSpell("test" + i);
				word.setMeaning(String.valueOf((char)('a'+i%26)));
				word.setPartId((int)(Math.random()*DEF_PARTS.length));
				ContentValues value = createContentValues(word);

				db.insert(WordDbHelper.WORD_TABLE, null, value);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	private ContentValues createContentValues(WordEntity word) {
		// 登録データの作成
		ContentValues cv = new ContentValues();
		cv.put(WordDbHelper.WCOL_SPELL, word.getSpell());
		cv.put(WordDbHelper.WCOL_MEANING, word.getMeaning());
		cv.put(WordDbHelper.WCOL_PART, word.getPartId());
		cv.put(WordDbHelper.WCOL_EXAMPLE_EN, word.getExampleEn());
		cv.put(WordDbHelper.WCOL_EXAMPLE_JA, word.getExampleJa());
		cv.put(WordDbHelper.WCOL_LEVEL, word.getLearningLevel());
		cv.put(WordDbHelper.WCOL_BOOKMARK, word.getBookmark());
//		cv.put(WordDbHelper.WCOL_PRIORITY, word.getPriority());
		cv.put(WordDbHelper.WCOL_PREV_LEARNING, word.getPrevLearning());
		cv.put(WordDbHelper.WCOL_NEXT_LEARNING, word.getNextLearning());
		cv.put(WordDbHelper.CCOL_KEY, word.getCategoryId());

		return cv;
	}
	//
	//////////////////////////////////////////////////////////////////////
}
