package jp.ac.tuat.sys.linh;

import jp.ac.tuat.sys.kaneko.R;

public class QuestionSentence3Activity extends QuestionSentenceN {

	@Override
	protected int getChoicesNum() {
		return 3;
	}
	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_question3);
	}

}
