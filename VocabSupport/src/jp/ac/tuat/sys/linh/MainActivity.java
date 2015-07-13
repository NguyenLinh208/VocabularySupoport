package jp.ac.tuat.sys.linh;

import java.io.File;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import jp.ac.tuat.sys.kaneko.R;

public class MainActivity extends Activity implements OnClickListener {

	private static final int REQ_CODE_TTS = 1;

	private static File mFileDir;

	public static File getApplicationDir() {
		return mFileDir;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ディレクトリの取得
        // 他のクラスから一括で利用される
        // SDカードが利用可能ならSDカード，そうでなければ通常のディレクトリ
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        	mFileDir = getExternalFilesDir(null);
        } else {
        	mFileDir = getFilesDir();
        }

        // 各種ビューの取得
        Button learningButton = (Button)findViewById(R.id.learning_intent_button);
        Button reviseButton   = (Button)findViewById(R.id.revise_intent_button);
        Button registerButton = (Button)findViewById(R.id.register_intent_button);
        Button wordListButton = (Button)findViewById(R.id.check_intent_button);
        Button MemoButton = (Button)findViewById(R.id.notebook_intent_button);
        Button MenuButton = (Button)findViewById(R.id.menu_intent_button);
        
        // リスナーの登録
        learningButton.setOnClickListener(this);
        reviseButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        wordListButton.setOnClickListener(this);
        MenuButton.setOnClickListener(this);
        MemoButton.setOnClickListener(this);

        // 設定情報の初期値読み込み
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref, false);

        // TTSがインストールされているかを確認
        checkTTSinstalled();

//      // デモ用ボタンの設定
//      Button nextDayButton = (Button)findViewById(R.id.next_day_button);
//      Button threeDaysButton = (Button)findViewById(R.id.three_days_pass_button);
//      Button tenDaysButton = (Button)findViewById(R.id.ten_days_pass_button);
//      nextDayButton.setOnClickListener(this);
//      threeDaysButton.setOnClickListener(this);
//      tenDaysButton.setOnClickListener(this);
    }

	/**
	 * TTSのインストール状態確認
	 */
	private void checkTTSinstalled() {
		Intent intent = new Intent();
		intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(intent, REQ_CODE_TTS);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQ_CODE_TTS) {
			if(resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				showTTSinstallationDialog();
			}
		}
	}

	private void showTTSinstallationDialog() {
		// 確認は初回のみ
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(!pref.getBoolean(Config.TTS_CHECKED, false)) {
			// 確認ダイアログを出す
			String msg = getResources().getString(R.string.confirm_tts_install);
			new YesNoDialog(
					MainActivity.this,
					// "はい"選択時の処理
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent install = new Intent();
							install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
							startActivity(install);
						}
					},
					null,
					msg
			).show();
			// チェック済み情報を保存する
			pref.edit().putBoolean(Config.TTS_CHECKED, true).commit();
		}
	}

	public void onClick(View v) {
		Intent intent;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		switch(v.getId()) {
		case R.id.learning_intent_button:	// 学習モード
			pref.edit().putString(Config.DIALOG_MODE, "0").commit();
			intent = new Intent(MainActivity.this, LearningListActivity.class);
			startActivity(intent);
			break;
		case R.id.revise_intent_button:		// 復習モード
			pref.edit().putString(Config.DIALOG_MODE, "0").commit();
			intent = new Intent(MainActivity.this, TestSelectActivity.class);
			startActivity(intent);
			break;
		case R.id.register_intent_button:	// 単語登録
			intent = new Intent(MainActivity.this, RegisterActivity.class);
			startActivity(intent);
			break;
		case R.id.check_intent_button:		// 単語確認
			pref.edit().putString(Config.DIALOG_MODE, "1").commit();
			intent = new Intent(MainActivity.this, WordListSelectActivity.class);
			startActivity(intent);
			break;
		case R.id.notebook_intent_button:		// 設定
			intent = new Intent(MainActivity.this, NotebookActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_intent_button:		// 設定
			intent = new Intent(MainActivity.this, SettingActivity.class);
			startActivity(intent);
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

	@Override
	protected void onResume() {
		super.onResume();

		// データベース読み込み
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = pref.getString(Config.DB_USING, WordDbHelper.DEF_DB_NAME);
        WordDbHelper.initDataBase(getApplicationContext(), dbName);

		// 経過日数読み込み
		DateUtil.setPassedDays(pref.getInt(Config.PASSED_DAYS, 0));
	}

	@Override
	protected void onPause() {
		super.onPause();

		// 経過日数保存
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		pref.edit().putInt(Config.PASSED_DAYS, DateUtil.getPassedDays()).commit();
	}

	/**
	 * メニューボタンを押したときの処理
	 */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(MainActivity.this, SettingActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
