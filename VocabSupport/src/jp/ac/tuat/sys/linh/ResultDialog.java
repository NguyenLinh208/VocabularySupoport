package jp.ac.tuat.sys.linh;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import jp.ac.tuat.sys.kaneko.R;

public class ResultDialog extends Dialog {

	public static final int RIGHT = 0;
	public static final int WRONG = 1;

	private int mStringId;
	private int mWaitTime;

	public ResultDialog(Context context, int result, int wait) {
		super(context, R.style.ResultDialogTheme);

		if(result == RIGHT) {
			mStringId = R.string.right_answer;
		} else {
			mStringId = R.string.wrong_answer;
		}
		mWaitTime = wait;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_dialog);

		// テキストをセット
		TextView text = (TextView)findViewById(R.id.result_text);
		text.setText(mStringId);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// 指定した時間経過でdismiss()を呼ぶ
		new CountDownTimer(mWaitTime, mWaitTime) {

			@Override
			public void onFinish() {
				dismiss();
			}

			@Override
			public void onTick(long millisUntilFinished) {
			}

		}.start();
	}

}
