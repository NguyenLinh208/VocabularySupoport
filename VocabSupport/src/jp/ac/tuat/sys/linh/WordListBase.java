package jp.ac.tuat.sys.linh;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import jp.ac.tuat.sys.kaneko.R;

public abstract class WordListBase<T> extends Activity implements TextToSpeech.OnInitListener {

	// 必要な情報のみの射影
	public static final String[] PROJECTION = WordDbHelper.WORD_ALL_INFO;

	private static final int WORD_DIALOG = 0;
	private static final int WORD_DIALOG_NO_BOOKMARK = 1;
	
	private static int dialog_mode = 0;

	protected ListView mWordsList;
	protected WordListAdapter mListAdapter;
	protected WordDialog mWordDialog;
	protected WordEntity mShowWord;
	protected TextToSpeech mTTS;

	// 抽象メソッド：setContentView(～)を行うのみ
	protected abstract void setContentView();
	// 抽象メソッド：onCreate()時の処理を記述
	protected abstract void initOnCreate();
	// 抽象メソッド：非同期スレッドで行う単語検索時処理の内容
	protected abstract List<WordEntity> doInAsyncBackground(T... args);

	private void createViews() {

		// サブクラスで定義する処理
		setContentView();

        // ビューの取得
        mWordsList  = (ListView)findViewById(R.id.words_list);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		dialog_mode = Integer.parseInt(pref.getString(Config.DIALOG_MODE, Config.DEF_DIALOG_MODE));
		
        // リスト用のアダプタの作成とセット
        mListAdapter = new WordListAdapter(this);
        mWordsList.setAdapter(mListAdapter);

        // リストが押された際の処理
        mWordsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				// 選択された単語を取得
				ListView list   = (ListView)parent;
				WordEntity word = (WordEntity)list.getItemAtPosition(position);
				// 単語をダイアログ表示
				mShowWord = word;
				
				removeDialog(dialog_mode);
				showDialog(dialog_mode);
			}
		});

        // TTSの作成
        //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(pref.getBoolean(Config.TTS_USE, true)) {
        	mTTS = new TextToSpeech(this, this);
        } else {
        	mTTS = null;
        }

        // サブクラスで定義する処理
        initOnCreate();
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 最初はIMEキーボードを表示しない
        KeyboardUtil.initHide(this);
        // 各種ビューの作成
        createViews();
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case WORD_DIALOG:
			dialog = new WordDialog(WordListBase.this, mShowWord, mTTS);
			break;
		case WORD_DIALOG_NO_BOOKMARK:
			dialog = new WordDialogNoBookmark(WordListBase.this, mShowWord, mTTS);
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

    public void onInit(int status) {
    	if(status == TextToSpeech.SUCCESS) {
    		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    		// 設定されたロケールの取得
    		Locale loc = getLocale(pref.getString(Config.SPEECH_LOCALE, Config.DEF_LOCALE));
    		// 指定されたロケールが使用可能かを判定
    		if(isLocaleAvailable(loc)) {
    			mTTS.setLanguage(loc);
    			// ピッチと速さの読み込み
    			float pitch = Float.parseFloat(pref.getString(Config.SPEECH_PITCH, Config.DEF_SPEECH_PITCH));
    			float rate  = Float.parseFloat(pref.getString(Config.SPEECH_RATE, Config.DEF_SPEECH_RATE));
    			// 読み込んだ設定情報に基づき，ピッチと速さを設定
    			mTTS.setPitch(pitch);
    			mTTS.setSpeechRate(rate);
    		} else {
    			// トーストを表示
    			String message = "Error: Locale " + loc.getLanguage() + " is not available.";
    			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    		}
    	}
	}

    private boolean isLocaleAvailable(Locale loc) {
    	return mTTS.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE;
    }

    private Locale getLocale(String lang) {
    	if(lang.equals(Config.LOC_US)) {
    		return Locale.US;
    	}
    	if(lang.equals(Config.LOC_EN)) {
    		return Locale.ENGLISH;
    	}
    	if(lang.equals(Config.LOC_FR)) {
    		return Locale.FRENCH;
    	}
    	if(lang.equals(Config.LOC_GA)) {
    		return Locale.GERMAN;
    	}
    	if(lang.equals(Config.LOC_IT)) {
    		return Locale.ITALIAN;
    	}
    	return Locale.getDefault();
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
		if(mTTS != null) {
			mTTS.shutdown();
		}
	}

    protected void search(T... args) {
    	// 非同期タスクで実行
    	// UIスレッドでは検索中ダイアログを表示
    	new AsyncTask<T, Void, List<WordEntity>>() {

    		private ProgressDialog progressDialog;

    		@Override
			protected void onPreExecute() {
    			// 検索中というダイアログを表示
    			// !!!ダイアログに渡すコンテキストはアクティビティ!!!
				progressDialog = new ProgressDialog(WordListBase.this);
				progressDialog.setTitle(R.string.progress_search);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(true);
				progressDialog.show();
			}

    		@Override
			protected List<WordEntity> doInBackground(T... args) {
				return doInAsyncBackground(args);
			}

			@Override
			protected void onPostExecute(List<WordEntity> result) {
				progressDialog.dismiss();
				// 検索結果をアダプタにセット
		    	setList(result);
			}

			@Override
			protected void onCancelled() {
				progressDialog.dismiss();
				this.cancel(true);
			}

    	}.execute(args);
    }

    protected void setList(List<WordEntity> list) {
    	if(list != null) {
	    	// アダプタにセット
	    	mListAdapter.clear();
	    	mListAdapter.addAll(list);
			// データの変更を通知
			mListAdapter.notifyDataSetChanged();
    	}
    }
}
