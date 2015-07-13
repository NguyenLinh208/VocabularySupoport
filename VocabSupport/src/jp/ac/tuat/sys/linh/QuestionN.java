package jp.ac.tuat.sys.linh;

import java.util.Arrays;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import jp.ac.tuat.sys.kaneko.R;

public abstract class QuestionN extends QuestionBase implements OnClickListener {

	// 意味のみの射影
	protected static final String[] PROJECTION = new String[] {
		WordDbHelper.WCOL_MEANING,
	};
	// ボタンIDの配列
	private static final int[] BUTTON_ID = {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4};

	private WordEntity mNextWord;
	private String[] mCurrentMeanings;
	private String[] mNextMeanings;
	private Thread mBackgroundThread;

	private TextView mQuestionWord;
	private Button[] mAnswerButtons;

	// 抽象メソッド：選択肢の数
	protected abstract int getChoicesNum();

	@Override
	protected void initOnCreate() {
		// ビューの取得
        mQuestionWord = (TextView)findViewById(R.id.question_word);
        // スキップボタンのリスナー登録
        findViewById(R.id.skip_button).setOnClickListener(this);

        // 各種リストの初期化
        mCurrentMeanings = new String[getChoicesNum()];
        mNextMeanings    = new String[getChoicesNum()];

        // ボタンの取得およびリスナー登録
        mAnswerButtons = new Button[getChoicesNum()];
        for(int i=0; i<mAnswerButtons.length; i++) {
        	mAnswerButtons[i] = (Button)findViewById(BUTTON_ID[i]);
        	mAnswerButtons[i].setOnClickListener(this);
        }

 		// 最初の単語をセット
 		setupFirstQuestion();
	}

	private void setupFirstQuestion() {
		prepareNextWord();
		setupNextQuestion();
	}

	protected void setupNextQuestion() {
		// 次の単語がない場合
		if(mNextWord == null) {
			// 終了処理
			fin();
			return;
		}

		// 同期
		try {
			mBackgroundThread.join();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 現在の単語を更新
		mCurrentWord = mNextWord;
		// 現在の意味を更新
		System.arraycopy(mNextMeanings, 0, mCurrentMeanings, 0, mNextMeanings.length);
		// 学習単語のスペルをセット
		mQuestionWord.setText(mCurrentWord.getSpell());
		// 意味を各種ビューにセット
		for(int i=0; i<mAnswerButtons.length; i++) {
			mAnswerButtons[i].setText(mCurrentMeanings[i]);
		}

		// 次の単語の用意を開始
		prepareNextWord();
	}

	private void prepareNextWord() {
		if(mWordList.isEmpty()) {
			// 次の語がない場合，null
			mNextWord = null;
		} else {
			// 次の語をセット
			mNextWord = mWordList.remove(0);
			// ダミーの意味の検索開始
			searchNextMeanings();
		}
	}

	private void searchNextMeanings() {
		// 別スレッドで表示する選択肢を読み込む
    	mBackgroundThread = new Thread() {
			@Override
			public void run() {
				// 空文字で初期化
				Arrays.fill(mNextMeanings, 0, mNextMeanings.length, "");
				// DBから適当な意味をとってくる
				WordDao dao = new WordDao();
				// 検索条件設定
				String where = WordDbHelper.PCOL_NAME    + " = ? and not "
						     + WordDbHelper.WCOL_MEANING + " = ?";
				String param1 = mNextWord.getPart();
				String param2 = mNextWord.getMeaning();
				String order = "random()";
				String limit = String.valueOf(getChoicesNum()-1);
				// 検索実行
				int index = 0;
				for(WordEntity w : dao.searchWord(PROJECTION, where, new String[]{param1, param2}, order, limit)) {
					mNextMeanings[index++] = w.getMeaning();
				}
				mNextMeanings[index] = mNextWord.getMeaning();
				// ランダムな順番に並び替える
				ArrayUtil.shuffle(mNextMeanings);
			}
    	};
    	mBackgroundThread.start();
	}

	public void onClick(View v) {
		int answerNum = ArrayUtil.linearSearch(BUTTON_ID, v.getId());
		if(answerNum >= 0 && mCurrentWord.getMeaning().equals(mCurrentMeanings[answerNum])) {
			// 正解時処理
			performInRight();
		} else {
			// 不正解時処理
			performInWrong();
		}
	}

}
