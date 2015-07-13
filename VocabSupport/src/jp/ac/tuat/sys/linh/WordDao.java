package jp.ac.tuat.sys.linh;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class WordDao {

	private static WordDbHelper mDbHelper = null;

	public WordDao() {
		if(mDbHelper == null) {
			mDbHelper = WordDbHelper.getInstance();
		}
	}

	public static void reset() {
		mDbHelper = null;
	}

	private ContentValues createContentValues(WordEntity word) {
		// 登録データの作成
		return createContentValues(word, WordDbHelper.WCOL_ALL);
	}
	private ContentValues createContentValues(WordEntity word, String[] projection) {
		// 登録データの作成
		ContentValues cv = new ContentValues();
		// 指定した列のデータのみ設定
		for(String p : projection) {
			if(p.equals(WordDbHelper.WCOL_SPELL)) {
				cv.put(WordDbHelper.WCOL_SPELL, word.getSpell());
			} else if(p.equals(WordDbHelper.WCOL_MEANING)) {
				cv.put(WordDbHelper.WCOL_MEANING, word.getMeaning());
			} else if(p.equals(WordDbHelper.WCOL_PART)) {
				cv.put(WordDbHelper.WCOL_PART, word.getPartId());
			} else if(p.equals(WordDbHelper.WCOL_EXAMPLE_EN)) {
				cv.put(WordDbHelper.WCOL_EXAMPLE_EN, word.getExampleEn());
			} else if(p.equals(WordDbHelper.WCOL_EXAMPLE_JA)) {
				cv.put(WordDbHelper.WCOL_EXAMPLE_JA, word.getExampleJa());
			} else if(p.equals(WordDbHelper.WCOL_CATEGORY)) {
				cv.put(WordDbHelper.WCOL_CATEGORY, word.getCategoryId());
			} else if(p.equals(WordDbHelper.WCOL_LEVEL)) {
				cv.put(WordDbHelper.WCOL_LEVEL, word.getLearningLevel());
			} else if(p.equals(WordDbHelper.WCOL_PREV_LEARNING)) {
				cv.put(WordDbHelper.WCOL_PREV_LEARNING, word.getPrevLearning());
			} else if(p.equals(WordDbHelper.WCOL_NEXT_LEARNING)) {
				cv.put(WordDbHelper.WCOL_NEXT_LEARNING, word.getNextLearning());
			}
		}
		return cv;
	}

	/**
	 * 単語を登録する
	 * @param word 登録する単語のデータ
	 * @return 登録された単語の_id
	 */
	public long insertWord(WordEntity word) {
		long id;
		// データベース取得
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// WordEntity -> ContentValuesに変換
		ContentValues value = createContentValues(word);
		// 単語データベースに登録
		id = db.insert(WordDbHelper.WORD_TABLE, null, value);
		return id;
	}
	public void insertWords(List<WordEntity> words) {
		// データベース取得
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// トランザクション開始
		db.beginTransaction();
		try {
			SQLiteStatement stmt = db.compileStatement("insert into " + WordDbHelper.WORD_TABLE + " values (?,?,?,?,?,?,?,?,?,?,?);");
			for(WordEntity word : words) {
				stmt.bindNull(1);	// idは自動で設定
				stmt.bindString(2, word.getSpell());
				stmt.bindString(3, word.getMeaning());
				stmt.bindLong(4, word.getPartId());
				stmt.bindString(5, word.getExampleEn());
				stmt.bindString(6, word.getExampleJa());
				stmt.bindLong(7, word.getCategoryId());
				stmt.bindLong(8, word.getBookmark());
				stmt.bindLong(9, word.getLearningLevel());
				stmt.bindLong(10, word.getPrevLearning());
				stmt.bindLong(11, word.getNextLearning());
				stmt.executeInsert();
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
//	public void insertWords(List<WordEntity> words) {
//		// データベース取得
//		SQLiteDatabase db = mDbHelper.getWritableDatabase();
//		// トランザクション開始
//		db.beginTransaction();
//		try {
//			for(WordEntity word : words) {
//				// WordEntity -> ContentValuesに変換
//				ContentValues value = createContentValues(word);
//				// 単語データベースに登録
//				db.insert(WordDbHelper.WORD_TABLE, null, value);
//			}
//			db.setTransactionSuccessful();
//		} finally {
//			db.endTransaction();
//		}
//	}

	/**
	 * 単語を更新する
	 * @param id 更新する単語のID
	 * @param word 更新する内容
	 */
	public void updateWord(int id, WordEntity word, String[] projection) {
		// データベース取得
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// WordEntity -> ContentValuesに変換
		ContentValues value = createContentValues(word, projection);
		// 条件を設定
		String where = WordDbHelper.KEY + " = " + id;
		// 単語データベースを更新
		db.update(WordDbHelper.WORD_TABLE, value, where, null);
	}
	public void updateWords(List<WordEntity> words, String[] projection) {
		// データベース取得
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// トランザクション開始
		db.beginTransaction();
		try {
			for(WordEntity word : words) {
				// WordEntity -> ContentValuesに変換
				ContentValues value = createContentValues(word, projection);
				// 条件を設定
				String where = WordDbHelper.KEY + " = " + word.getId();
				// 単語データベースを更新
				db.update(WordDbHelper.WORD_TABLE, value, where, null);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * 単語を削除する
	 * @param wordId 削除する単語のID(_id)
	 * @return 削除に成功したらtrue
	 */
	public boolean deleteWord(int wordId) {
		long id;
		// データベース取得
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// 条件を設定
		String where = WordDbHelper.KEY + " = " + wordId;
		// 単語データベースから削除
		id = db.delete(WordDbHelper.WORD_TABLE, where, null);
		return id > 0;
	}

	/**
	 * 指定した条件で検索する
	 * @return 検索結果（ArrayList<WordEntity>）
	 */
	private String makeSearchSQL(String[] table, String[] projection, String where, String orderBy, String limit) {
		int i;
		String sqlSelect;
		String sqlFrom;
		String sqlWhere = "";
		String sqlOrder = "";
		String sqlLimit = "";

		// select
		sqlSelect = "select ";
		for(i=0; i<projection.length-1; i++) {
			sqlSelect += projection[i] + ", ";
		}
		sqlSelect += projection[i];

		// from
		sqlFrom = " from ";
		for(i=0; i<table.length-1; i++) {
			sqlFrom += table[i] + ", ";
		}
		sqlFrom += table[i];

		// where
		if(where != null) {
			sqlWhere = " where " + where;
		}

		// order by
		if(orderBy != null) {
			sqlOrder = " order by " + orderBy;
		}

		// limit
		if(limit != null) {
			sqlLimit = " limit " + limit;
		}

		return sqlSelect + sqlFrom + sqlWhere + sqlOrder + sqlLimit;
	}

	public List<WordEntity> searchWord(String[] projection, String where, String[] param, String orderBy) {
		return searchWord(projection, where, param, orderBy, null);
	}
	public List<WordEntity> searchWord(String[] projection, String where, String[] param, String orderBy, String limit) {
		// データベース取得
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		// 返却するリストの作成
		ArrayList<WordEntity> results = new ArrayList<WordEntity>();
		// カーソルを用意
		Cursor cursor = null;
		try {
			// SQL文発行
			String[] table = {WordDbHelper.WORD_TABLE, WordDbHelper.PART_TABLE, WordDbHelper.CATEGORY_TABLE};
			if(where != null) {
				where += " and ";
				where += WordDbHelper.WHERE_PART_JOIN;
			} else {
				where  = WordDbHelper.WHERE_PART_JOIN;
			}
			String sql = makeSearchSQL(table, projection, where, orderBy, limit);
			// クエリ
			cursor = db.rawQuery(sql, param);
			// 検索結果をリストに格納
			if(cursor.moveToFirst()) {
				do {
					// 単語の情報を表から取得
					WordEntity word = new WordEntity();
					// 必要な情報のみを設定
					for(String p : projection) {
						if(p.equals(WordDbHelper.KEY)) {						// 主キー
							word.setId(cursor.getInt(cursor.getColumnIndex(WordDbHelper.KEY)));
						} else if(p.equals(WordDbHelper.WCOL_SPELL)) {			// スペル
							word.setSpell(cursor.getString(cursor.getColumnIndex(WordDbHelper.WCOL_SPELL)));
						} else if(p.equals(WordDbHelper.WCOL_MEANING)) {		// 意味
							word.setMeaning(cursor.getString(cursor.getColumnIndex(WordDbHelper.WCOL_MEANING)));
						} else if(p.equals(WordDbHelper.WCOL_PART)) {			// 品詞ID
							word.setPartId(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_PART)));
						} else if(p.equals(WordDbHelper.WCOL_PART_FULL)) {		// 品詞ID
							word.setPartId(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_PART)));
						} else if(p.equals(WordDbHelper.WCOL_EXAMPLE_EN)) {		// 例文
							word.setExampleEn(cursor.getString(cursor.getColumnIndex(WordDbHelper.WCOL_EXAMPLE_EN)));
						} else if(p.equals(WordDbHelper.WCOL_EXAMPLE_JA)) {		// 和訳
							word.setExampleJa(cursor.getString(cursor.getColumnIndex(WordDbHelper.WCOL_EXAMPLE_JA)));
						} else if(p.equals(WordDbHelper.WCOL_CATEGORY)) {		// カテゴリID
							word.setCategoryId(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_CATEGORY)));
						} else if(p.equals(WordDbHelper.WCOL_CATEGORY_FULL)) {	// カテゴリID
							word.setCategoryId(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_CATEGORY)));
						} else if(p.equals(WordDbHelper.WCOL_LEVEL)) {			// 学習レベル
							word.setLearningLevel(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_LEVEL)));
						} else if(p.equals(WordDbHelper.WCOL_BOOKMARK)) {			// しおり
							word.setBookmark(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_BOOKMARK)));
						} else if(p.equals(WordDbHelper.WCOL_PREV_LEARNING)) {	// 前回学習日
							word.setPrevLearning(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_PREV_LEARNING)));
						} else if(p.equals(WordDbHelper.WCOL_NEXT_LEARNING)) {	// 学習予定日
							word.setNextLearning(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_NEXT_LEARNING)));
						} else if(p.equals(WordDbHelper.PCOL_NAME)) {			// 品詞名
							word.setPart(cursor.getString(cursor.getColumnIndex(WordDbHelper.PCOL_NAME)));
						} else if(p.equals(WordDbHelper.CCOL_NAME)) {			// カテゴリ名
							word.setCategory(cursor.getString(cursor.getColumnIndex(WordDbHelper.CCOL_NAME)));
						}
					}
					// リストに追加
					results.add(word);
				} while(cursor.moveToNext());
			}
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
		return results;
	}

	/**
	 * 品詞とカテゴリの取得・設定
	 */
	public String[] getParts() {
		return searchStrings(WordDbHelper.PART_TABLE, WordDbHelper.PCOL_NAME);
	}
	public String[] getCategories() {
		return searchStrings(WordDbHelper.CATEGORY_TABLE, WordDbHelper.CCOL_NAME);
	}
	private String[] searchStrings(String table, String collumn) {
		// データベース取得
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		// 返却するリストの作成
		ArrayList<String> results = new ArrayList<String>();
		// カーソルを用意
		Cursor cursor = null;
		try {
			// SQL文発行
			String sql = makeSearchSQL(new String[]{table}, new String[]{collumn}, null, null, null);
			// クエリ
			cursor = db.rawQuery(sql, null);
			// 検索結果をリストに格納
			if(cursor.moveToFirst()) {
				do {
					results.add(cursor.getString(0));
				} while(cursor.moveToNext());
			}
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
		return results.toArray(new String[0]);
	}
	public void remakeParts(String[] data) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		mDbHelper.initPartTable(db, data);
	}
	public void remakeCategories(String[] data) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		mDbHelper.initCategoryTable(db, data);
	}
	
	// 学習情報の初期化
	public void clearLearningData() {
		// データベース取得
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// 全単語の学習レベルを0にする
		db.execSQL("update " + WordDbHelper.WORD_TABLE + " set " + WordDbHelper.WCOL_LEVEL + "=0");
		db.execSQL("update " + WordDbHelper.WORD_TABLE + " set " + WordDbHelper.WCOL_BOOKMARK + "=0");
	}


//	public List<WordEntity> search(String[] projection, String where, String[] param, String orderBy, String limit) {
//		// データベース取得
//		SQLiteDatabase db = mDbHelper.getReadableDatabase();
//		// 返却するリストの作成
//		ArrayList<WordEntity> results = new ArrayList<WordEntity>();
//		// 検索条件設定
//		if(where == null) {
//			param = null;
//		}
//		String groupBy = null;
//		String having  = null;
//		// カーソルを用意
//		Cursor cursor = null;
//		try {
//			// クエリ
//			cursor = db.query(
//					WordDbHelper.WORD_TABLE,
//					projection,
//					where,
//					param,
//					groupBy,
//					having,
//					orderBy,
//					limit);
//			// 検索結果をリストに格納
//			if(cursor.moveToFirst()) {
//				do {
//					// 単語の情報を表から取得
//					WordEntity word = new WordEntity();
//					// 必要な情報のみを設定
//					for(String p : projection) {
//						if(p.equals(WordDbHelper.KEY)) {
//							word.setId(cursor.getInt(cursor.getColumnIndex(WordDbHelper.KEY)));
//						} else if(p.equals(WordDbHelper.WCOL_SPELL)) {
//							word.setSpell(cursor.getString(cursor.getColumnIndex(WordDbHelper.WCOL_SPELL)));
//						} else if(p.equals(WordDbHelper.WCOL_MEANING)) {
//							word.setMeaning(cursor.getString(cursor.getColumnIndex(WordDbHelper.WCOL_MEANING)));
//						} else if(p.equals(WordDbHelper.WCOL_PART)) {
//							word.setPartId(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_PART)));
//						} else if(p.equals(WordDbHelper.WCOL_EXAMPLE_EN)) {
//							word.setExampleEn(cursor.getString(cursor.getColumnIndex(WordDbHelper.WCOL_EXAMPLE_EN)));
//						} else if(p.equals(WordDbHelper.WCOL_EXAMPLE_JA)) {
//							word.setExampleJa(cursor.getString(cursor.getColumnIndex(WordDbHelper.WCOL_EXAMPLE_JA)));
//						} else if(p.equals(WordDbHelper.WCOL_LEVEL)) {
//							word.setLearningLevel(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_LEVEL)));
//						} else if(p.equals(WordDbHelper.WCOL_PREV_LEARNING)) {
//							word.setPrevLearning(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_PREV_LEARNING)));
//						} else if(p.equals(WordDbHelper.WCOL_NEXT_LEARNING)) {
//							word.setNextLearning(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_NEXT_LEARNING)));
//						} else if(p.equals(WordDbHelper.WCOL_CATEGORY)) {
//							word.setCategoryId(cursor.getInt(cursor.getColumnIndex(WordDbHelper.WCOL_CATEGORY)));
//						}
//					}
//					// リストに追加
//					results.add(word);
//				} while(cursor.moveToNext());
//			}
//		} finally {
//			if(cursor != null) {
//				cursor.close();
//			}
//		}
//		return results;
//
//	}

}
