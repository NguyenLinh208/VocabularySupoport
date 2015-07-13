package jp.ac.tuat.sys.linh;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;

import jp.ac.tuat.sys.kaneko.R;

public abstract class QuestionBase extends Activity {

	private static final int RIGHT_RESULT = 0;
	private static final int WRONG_RESULT = 1;

	protected ArrayList<WordEntity> mWordList;
	protected ArrayList<WordEntity> mRightList;
	protected ArrayList<WordEntity> mWrongList;
	protected WordEntity mCurrentWord;

	private int mPopTime;
	private boolean mSoundUse;
	private SoundPool mSoundPool;
	private int mRightSE;
	private int mWrongSE;

	// 抽象メソッド：setContentView(～)を行うのみ
	protected abstract void setContentView();
	// 抽象メソッド：onCreate()時の処理を記述
	protected abstract void initOnCreate();
	// 抽象メソッド：次の問題をセット
	protected abstract void setupNextQuestion();

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // サブクラスで定義する処理
        setContentView();

        // 各種リストの初期化
        mWordList  = new ArrayList<WordEntity>();
        mRightList = new ArrayList<WordEntity>();
        mWrongList = new ArrayList<WordEntity>();

        // インテントの付加情報を取得
 		Bundle extra = getIntent().getExtras();
 		if(extra != null) {
 			// 学習予定語のリストを受け取る
 			@SuppressWarnings("unchecked")
			ArrayList<WordEntity> newWords = (ArrayList<WordEntity>)extra.getSerializable(VSappConst.LEARNING_WORDS);
 			// リストにセット
 			mWordList.addAll(newWords);
 		}

 		// サブクラスで定義する処理
        initOnCreate();
    }

	@Override
	protected void onStart() {
		super.onStart();
		// サウンド使用の有無を取得
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mSoundUse = pref.getBoolean(Config.SOUND_USE, true);
		// サウンド再生用のインスタンス生成
		mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		// サウンド読み込み
		mRightSE = mSoundPool.load(getApplicationContext(), R.raw.se_right, 1);
		mWrongSE = mSoundPool.load(getApplicationContext(), R.raw.se_wrong, 1);
		// ○/×のポップ時間取得
		mPopTime = Integer.parseInt(pref.getString(Config.RES_POP_TIME, Config.DEF_RES_POP_TIME));
	}

	@Override
	protected void onStop() {
		super.onStop();
		// サウンド再生用のインスタンス解放
		mSoundPool.release();
		mSoundPool = null;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case RIGHT_RESULT:
			dialog = new ResultDialog(QuestionBase.this, ResultDialog.RIGHT, mPopTime);
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					setupNextQuestion();
				}
			});
			break;
		case WRONG_RESULT:
			dialog = new ResultDialog(QuestionBase.this, ResultDialog.WRONG, mPopTime);
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					setupNextQuestion();
				}
			});
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	protected void fin() {
		// 単語のデータを渡す
		Intent intent = new Intent(QuestionBase.this, ResultActivity.class);
		intent.putExtra(VSappConst.RIGHT_WORDS, mRightList);
		intent.putExtra(VSappConst.WRONG_WORDS, mWrongList);
		// リザルト画面に遷移
		startActivity(intent);
		// 学習画面から来た時のため，結果を返す
		setResult(RESULT_OK);
		finish();
	}

	protected void performInRight() {
		mRightList.add(mCurrentWord);
		showDialog(RIGHT_RESULT);
		// サウンドonならばSEを再生
		if(mSoundUse) {
			mSoundPool.play(mRightSE, 0.7f, 0.7f, 0, 0, 1.0f);
		}
	}
	protected void performInWrong() {
		mWrongList.add(mCurrentWord);
		showDialog(WRONG_RESULT);
		// サウンドonならばSEを再生
		if(mSoundUse) {
			mSoundPool.play(mWrongSE, 0.7f, 0.7f, 0, 0, 1.0f);
		}
	}
}
