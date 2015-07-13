package jp.ac.tuat.sys.linh;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.ac.tuat.sys.kaneko.R;

public class SpellHideQuestion extends QuestionBase implements OnClickListener {

	private TextView mSpellView;
	private TextView mMeaningView;
	private LinearLayout mFirstShow;
	private LinearLayout mSecondShow;

	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_meaning_hide_question);
	}

	@Override
	protected void initOnCreate() {
		// 各種ビューの取得
		mSpellView = (TextView)findViewById(R.id.question_word);
		mMeaningView = (TextView)findViewById(R.id.answer);
		mFirstShow = (LinearLayout)findViewById(R.id.first_show_layout);
		mSecondShow = (LinearLayout)findViewById(R.id.second_show_layout);
		Button openButton = (Button)findViewById(R.id.answer_open_button);
		Button rightButton = (Button)findViewById(R.id.right_button);
		Button wrongButton = (Button)findViewById(R.id.wrong_button);

		// リスナーの登録
		openButton.setOnClickListener(this);
		rightButton.setOnClickListener(this);
		wrongButton.setOnClickListener(this);

		setupNextQuestion();
	}

	@Override
	protected void setupNextQuestion() {
		// 次の語が無ければ終了
		if(mWordList.isEmpty()) {
			fin();
		} else {
			mCurrentWord = mWordList.remove(0);
			// スペルの表示
			//mSpellView.setText(mCurrentWord.getExampleEn().replaceAll(mCurrentWord.getSpell(), "_________") + "\n" + "["+ mCurrentWord.getExampleJa() +"]");
			mSpellView.setText(toBlank(mCurrentWord.getExampleEn(),mCurrentWord.getSpell()) + "\n" + "["+ mCurrentWord.getExampleJa() +"]");
			// 意味を隠す
			mMeaningView.setText("");
			// 可視状態の変更
			mFirstShow.setVisibility(View.VISIBLE);
			mSecondShow.setVisibility(View.INVISIBLE);
		}
	}

	private void showAnswer() {
		// 意味の表示
		//mMeaningView.setText(mCurrentWord.getSpell());
		mMeaningView.setText(whatBlank(mCurrentWord.getExampleEn()));
		// 可視状態の変更
		mFirstShow.setVisibility(View.INVISIBLE);
		mSecondShow.setVisibility(View.VISIBLE);
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.answer_open_button:
			showAnswer();
			break;
		case R.id.right_button:
			performInRight();
			break;
		case R.id.wrong_button:
			performInWrong();
			break;
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
