package jp.ac.tuat.sys.linh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import jp.ac.tuat.sys.kaneko.R;

public class LearningListActivity extends WordListBase<Integer> {

	private static final int REQ_CODE_LEARNING = 2;

	private List<WordEntity> mNewWords;
	private List<WordEntity> mReviseWords;
	private Thread mBackgroundThread;

	private Spinner mCategorySpinner;

	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_learning_list);
	}
	@Override
	protected void initOnCreate() {
		// ビューの取得
        mCategorySpinner = (Spinner)findViewById(R.id.category_spinner);

        // スピナー用のアダプタの作成とセット
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getCategoryList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);

        // スピナーの選択が変更された際の処理
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				// 選択されたカテゴリの単語を探す
				search(position);
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

        // 学習開始ボタン押下時の処理
        Button startButton = (Button)findViewById(R.id.start_button);
        startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 同期
				try {
					mBackgroundThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 学習開始
				startLearning();
			}
        });

        // 終了ボタン押下時の処理
  		Button finishButton = (Button)findViewById(R.id.finish_button);
  		finishButton.setOnClickListener(new OnClickListener() {
  			public void onClick(View v) {
  				// アクティビティ終了
  				finish();
  			}
  		});

        // 復習優先度の高い語を検索
        searchReviseWords();
	}

	private void startLearning() {
		// 設定された学習画面を取得
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		int modeNum = Integer.parseInt(pref.getString(Config.LEARNING_MODE, Config.DEF_LEARNING_MODE));
		// インテント設定
		// 付加情報として，学習語リストを渡す
		Intent intent = null;
		ArrayList<WordEntity> learningList = makeLearningList();
		switch(modeNum) {
		case Config.MODE_QUESTION_3:	// 三択問題
			intent = new Intent(LearningListActivity.this, Question3Activity.class);
			intent.putExtra(VSappConst.LEARNING_WORDS, learningList);
			break;
		case Config.MODE_QUESTION_4:	// 四択問題
			intent = new Intent(LearningListActivity.this, Question4Activity.class);
			intent.putExtra(VSappConst.LEARNING_WORDS, learningList);
			break;
		case Config.MODE_MEANING_HIDE:	// 想起
			intent = new Intent(LearningListActivity.this, MeaningHideQuestion.class);
			intent.putExtra(VSappConst.LEARNING_WORDS, learningList);
			break;
		}
		// 設定された学習画面に遷移
		if(intent != null) {
			startActivityForResult(intent, REQ_CODE_LEARNING);
		}
	}
	private ArrayList<WordEntity> makeLearningList() {
		// 学習語リストを作成
		ArrayList<WordEntity> learningList = new ArrayList<WordEntity>();
		learningList.addAll(mNewWords);
		learningList.addAll(mReviseWords);
		// 適当に並び替える
		Collections.shuffle(learningList);
		return learningList;
	}

	/**
	 * カテゴリに"全て"を加えた配列を作成
	 * @return
	 */
	private String[] getCategoryList() {
		WordDao dao = new WordDao();
		String[] catTemp = dao.getCategories();
		String[] catList = new String[catTemp.length+1];
		catList[0] = getResources().getString(R.string.all_category);
		System.arraycopy(catTemp, 0, catList, 1, catTemp.length);
		return catList;
	}

	@Override
	protected List<WordEntity> doInAsyncBackground(Integer... args) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		WordDao dao = new WordDao();
		// 共通検索条件設定
		String order = "random()";
		String limit = pref.getString(Config.LEARNING_NUM, Config.DEF_LEARNING_NUM);

		// 引数==0：全てのカテゴリ
		// 引数!=0：カテゴリ指定
		if(args[0] == 0) {
			String where = WordDbHelper.WCOL_LEVEL + " = ?";
			String[] params = {"0"};
			// 検索実行
			mNewWords = dao.searchWord(PROJECTION, where, params, order, limit);
		} else {
			String where = WordDbHelper.WCOL_LEVEL         + " = ? and "
						 + WordDbHelper.WCOL_CATEGORY_FULL + " = ?";
			String[] params = {"0", String.valueOf(args[0]-1)};
			// 検索実行
			mNewWords = dao.searchWord(PROJECTION, where, params, order, limit);
		}

		return mNewWords;
	}

	private void searchReviseWords() {
    	// 別スレッドで復習すべき単語を読み込む
    	mBackgroundThread = new Thread() {
			@Override
			public void run() {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				WordDao dao = new WordDao();
				// 検索条件設定
				String where    = WordDbHelper.WCOL_NEXT_LEARNING + " <= ? and "
							    + WordDbHelper.WCOL_LEVEL + " > ? and "
							    + WordDbHelper.WCOL_LEVEL + " < ?";
				String[] params = {String.valueOf(DateUtil.getToday()), "0", "3"};
				String order    = WordDbHelper.WCOL_NEXT_LEARNING + " asc";	// 学習予定が早い順
				String limit    = String.valueOf(pref.getString(Config.REVISE_MAX, Config.DEF_REVISE_MAX));
    			// 検索実行
				mReviseWords = dao.searchWord(PROJECTION, where, params, order, limit);
			}
    	};
    	mBackgroundThread.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQ_CODE_LEARNING) {
			if(resultCode == Activity.RESULT_OK) {
				finish();
			}
		}
	}
}
