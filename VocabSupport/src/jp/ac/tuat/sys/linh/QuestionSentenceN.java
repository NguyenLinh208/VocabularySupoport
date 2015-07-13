package jp.ac.tuat.sys.linh;

import java.util.Arrays;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import jp.ac.tuat.sys.kaneko.R;

public abstract class QuestionSentenceN extends QuestionBase implements OnClickListener {

	// 意味のみの射影
	protected static final String[] PROJECTION = new String[] {
		WordDbHelper.WCOL_SPELL,
	};
	// ボタンIDの配列
	private static final int[] BUTTON_ID = {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4};

	private WordEntity mNextWord;
	private String[] mCurrentSpells;
	private String[] mNextSpells;
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
        mCurrentSpells = new String[getChoicesNum()];
        mNextSpells    = new String[getChoicesNum()];

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
	
	public static boolean check=false;
	
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
		// 現在の例文を更新
		mCurrentWord = mNextWord;
		// 現在のスペルを更新
		System.arraycopy(mNextSpells, 0, mCurrentSpells, 0, mNextSpells.length);
		// 学習単語の例文をセット
		//mQuestionWord.setText(mCurrentWord.getExampleEn().replaceAll(mCurrentWord.getSpell(), "_________") + "\n" + "["+ mCurrentWord.getExampleJa() +"]");
		mQuestionWord.setText(toBlank(mCurrentWord.getExampleEn(),mCurrentWord.getSpell()) + "\n" + "["+ mCurrentWord.getExampleJa() +"]");
		// 意味を各種ビューにセット
		
		if(Character.isLowerCase(new Character(whatBlank(mCurrentWord.getExampleEn()).charAt(0))))
			check=true;
		
		for(int i=0; i<mAnswerButtons.length; i++) {
			if (mCurrentSpells[i].equals(mNextWord.getSpell()))
				mAnswerButtons[i].setText(whatBlank(mCurrentWord.getExampleEn()));
			else{
				if(check) mAnswerButtons[i].setText(mCurrentSpells[i]);
				else mAnswerButtons[i].setText(new StringBuilder(mCurrentSpells[i]).replace(0, 1, mCurrentSpells[i].substring(0,1).toUpperCase()).toString());				
			}
		}
		check=false;
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
				Arrays.fill(mNextSpells, 0, mNextSpells.length, "");
				// DBから適当なスペルをとってくる
				WordDao dao = new WordDao();
				// 検索条件設定
				String where = WordDbHelper.PCOL_NAME    + " = ? and not "
						     + WordDbHelper.WCOL_SPELL + " = ?";
				String param1 = mNextWord.getPart();
				String param2 = mNextWord.getSpell();
				String order = "random()";
				String limit = String.valueOf(getChoicesNum()-1);
				// 検索実行
				int index = 0;
				for(WordEntity w : dao.searchWord(PROJECTION, where, new String[]{param1, param2}, order, limit)) {
					mNextSpells[index++] = w.getSpell();
				}
				mNextSpells[index] = mNextWord.getSpell();
				// ランダムな順番に並び替える
				ArrayUtil.shuffle(mNextSpells);
			}
    	};
    	mBackgroundThread.start();
	}

	public void onClick(View v) {
		int answerNum = ArrayUtil.linearSearch(BUTTON_ID, v.getId());
		if(answerNum >= 0 && mCurrentWord.getSpell().equals(mCurrentSpells[answerNum])) {
			// 正解時処理
			performInRight();
		} else {
			// 不正解時処理
			performInWrong();
		}
	}
	
	private static String[] Change = new String[18];// 活用変化(規則変化)
	
	private static int index=0; //　最初に一致した場所
	private static int index2=0; // 最初に一致した活用変化
	
	//活用変化の作成(規則的な変化)
	protected static String[] makeChange(String word){
		String lowerword=word.toLowerCase();
		StringBuilder sblw=new StringBuilder(lowerword);
		
		for(int i=0; i<Change.length; i++) {
			switch(i){
				case(0): // 三単現 複数形(通常)
					Change[i]=lowerword + "s";
					break;
				case(1): // 三単現 複数形（通常）
					Change[i]=lowerword + "es";
					break;
				case(2): // 過去形 過去分詞(通常)
					Change[i]=lowerword + "d";
					break;
				case(3): // 過去形　過去分詞(通常)
					Change[i]=lowerword + "ed";
					break;
				case(4): //　最後を重ねた過去形 過去分詞(stopなど)
					Change[i]=lowerword + sblw.substring(lowerword.length()-1) + "ed";
					break;
				case(5): //　最後をiに変えた過去形　過去分詞 (studyなど)
					Change[i]=sblw.substring(0, lowerword.length()-1) + "ied";
					break;
				case(6): // 現在分詞 進行形 (通常)
					Change[i]=lowerword + "ing";
					break;
				case(7): // 最後を重ねた現在分詞 進行形 (stopなど)
					Change[i]=lowerword + sblw.substring(lowerword.length()-1) + "ing";
					break;
				case(8): //　最後を消した進行形 (takeなど)
					Change[i]=sblw.substring(0, lowerword.length()-1) + "ing";;
					break;
				case(9): // 比較級(通常)
					Change[i]=lowerword + "r";
					break;
				case(10): // 比較級(通常)
					Change[i]=lowerword + "er";
					break;
				case(11): //　最後をiに変えた比較級 (happyなど)
					Change[i]=sblw.substring(0, lowerword.length()-1) + "ier";
					break;
				case(12): // 最後を重ねた比較級 (bigなど)
					Change[i]=lowerword + sblw.substring(lowerword.length()-1) + "er";
					break;
				case(13): // 最上級(通常)
					Change[i]=lowerword + "st";
					break;
				case(14): // 最上級(通常)
					Change[i]=lowerword + "est";
					break;
				case(15): //　最後をiに変えた最上級 (happyなど)
					Change[i]=sblw.substring(0, lowerword.length()-1) + "iest";
					break;
				case(16): // 最後を重ねた最上級 (bigなど)
					Change[i]=lowerword + sblw.substring(lowerword.length()-1) + "est";
					break;
				default: //　原形 3単以外の現在形 (A-A-A型の不規則動詞) 原級 more-most型
					Change[i]=lowerword;
			}
		}
		
		return Change;	
	}
	
	//穴を開ける
	protected static String toBlank(String rawsentence, String word){
		StringBuilder sbrs = new StringBuilder(rawsentence);
		StringBuilder sbrslw = new StringBuilder(rawsentence.toLowerCase());
		
		Change = makeChange(word); //活用変化の作成
				
		for(int i=0; i<Change.length; i++) {
			if(sbrslw.indexOf(Change[i])!=-1){
				index=sbrslw.indexOf(Change[i]);
				sbrs.replace(sbrslw.indexOf(Change[i]), sbrslw.indexOf(Change[i])+Change[i].length(), "____");
				index2=i;
				return sbrs.toString();
			}
		}
		
		return rawsentence; //穴が開けられない場合は、そのまま返す。
	}
	
	//穴を開けたものの取得
	protected static String whatBlank(String rawsentence){
		StringBuilder sbrs = new StringBuilder(rawsentence);
		return sbrs.substring(index,index + Change[index2].length()).toString();		
	}

}
