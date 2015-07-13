package jp.ac.tuat.sys.linh;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import jp.ac.tuat.sys.kaneko.R;

public class WordListSelectActivity extends Activity implements OnClickListener {

	private static File mFileDir;

	public static File getApplicationDir() {
		return mFileDir;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list_select);
        
        // 各種ビューの取得
        Button categoryButton = (Button)findViewById(R.id.category_intent_button);
        Button keywordButton = (Button)findViewById(R.id.keyword_intent_button);
        Button bookmarkwordButton = (Button)findViewById(R.id.bookmark_intent_button);
        
        // リスナーの登録
        categoryButton.setOnClickListener(this);

        keywordButton.setOnClickListener(this);
        
        bookmarkwordButton.setOnClickListener(this);



//      // デモ用ボタンの設定
//      Button nextDayButton = (Button)findViewById(R.id.next_day_button);
//      Button threeDaysButton = (Button)findViewById(R.id.three_days_pass_button);
//      Button tenDaysButton = (Button)findViewById(R.id.ten_days_pass_button);
//      nextDayButton.setOnClickListener(this);
//      threeDaysButton.setOnClickListener(this);
//      tenDaysButton.setOnClickListener(this);
    }

	public void onClick(View v) {
		Intent intent;

		switch(v.getId()) {
		case R.id.category_intent_button:	// 学習モード
			intent = new Intent(WordListSelectActivity.this, CategoryListActivity.class);
			startActivity(intent);
			break;
		case R.id.keyword_intent_button:		// 単語確認
			intent = new Intent(WordListSelectActivity.this, WordListActivity.class);
			startActivity(intent);
			break;
		case R.id.bookmark_intent_button:		// 単語確認
			intent = new Intent(WordListSelectActivity.this, BookmarkListActivity.class);
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