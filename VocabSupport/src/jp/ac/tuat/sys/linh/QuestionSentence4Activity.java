package jp.ac.tuat.sys.linh;

import jp.ac.tuat.sys.kaneko.R;

public class QuestionSentence4Activity extends QuestionSentenceN {

	@Override
	protected int getChoicesNum() {
		return 4;
	}
	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_question4);
	}

}
