package jp.ac.tuat.sys.linh;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import jp.ac.tuat.sys.kaneko.R;

public class TestSelectActivity extends Activity implements OnClickListener {


	private static File mFileDir;

	public static File getApplicationDir() {
		return mFileDir;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_select);
        
        // 各種ビューの取得
        Button wordButton = (Button)findViewById(R.id.word_intent_button);
        Button sentenceButton = (Button)findViewById(R.id.sentence_intent_button);

        
        // リスナーの登録
        wordButton.setOnClickListener(this);

        sentenceButton.setOnClickListener(this);



//      // デモ用ボタンの設定
//      Button nextDayButton = (Button)findViewById(R.id.next_day_button);
//      Button threeDaysButton = (Button)findViewById(R.id.three_days_pass_button);
//      Button tenDaysButton = (Button)findViewById(R.id.ten_days_pass_button);
//      nextDayButton.setOnClickListener(this);
//      threeDaysButton.setOnClickListener(this);
//      tenDaysButton.setOnClickListener(this);
    }

	public void onClick(View v) {
		Intent intent=null;
		// 設定された学習画面を取得
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		int modeNum = Integer.parseInt(pref.getString(Config.REVISE_MODE, Config.DEF_REVISE_MODE));

		switch(v.getId()) {
		case R.id.word_intent_button:	// 学習モード
			if(modeNum==Config.NOT_ALL) intent = new Intent(TestSelectActivity.this, ReviseSelectActivity.class);
			if(modeNum==Config.ALL) intent = new Intent(TestSelectActivity.this, FullReviseSelectActivity.class);
			startActivity(intent);
			break;
		case R.id.sentence_intent_button:		// 例文モード
			if(modeNum==Config.NOT_ALL) intent = new Intent(TestSelectActivity.this, ReviseSentenceSelectActivity.class);
			if(modeNum==Config.ALL) intent = new Intent(TestSelectActivity.this, FullReviseSentenceSelectActivity.class);
			startActivity(intent);
			break;

//			break;
//		case R.id.next_day_button:			// 1日進める（デモ用)
//			DateUtil.passDays(1);
//			Toast.makeText(getApplicationContext(), "1日進めました", Toast.LENGTH_SHORT).show();
//			break;
//		case R.id.three_days_pass_button:	// 3日進める（デモ用)
//			DateUtil.passDays(3);
//			Toast.makeText(getApplicationContext(), "3日進めました", Toast.LENGTH_SHORT).show();
//			break;
//		case R.id.ten_days_pass_button:		// 10日進める（デモ用)
//			DateUtil.passDays(10);
//			Toast.makeText(getApplicationContext(), "10日進めました", Toast.LENGTH_SHORT).show();
//			break;
		}
	}

}