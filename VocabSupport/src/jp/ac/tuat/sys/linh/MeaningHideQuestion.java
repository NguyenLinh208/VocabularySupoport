package jp.ac.tuat.sys.linh;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.ac.tuat.sys.kaneko.R;

public class MeaningHideQuestion extends QuestionBase implements OnClickListener {

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
			mSpellView.setText(mCurrentWord.getSpell());
			// 意味を隠す
			mMeaningView.setText("");
			// 可視状態の変更
			mFirstShow.setVisibility(View.VISIBLE);
			mSecondShow.setVisibility(View.INVISIBLE);
		}
	}

	private void showAnswer() {
		// 意味の表示
		mMeaningView.setText(mCurrentWord.getMeaning());
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

}
