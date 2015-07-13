package jp.ac.tuat.sys.linh;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import jp.ac.tuat.sys.kaneko.R;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private Map<String, String> mLearningModeMap = new HashMap<String, String>();
	private Map<String, String> mReviseModeMap = new HashMap<String, String>();
	private Map<String, String> mPopTimeMap      = new HashMap<String, String>();
	private Map<String, String> mSpeechLocaleMap = new HashMap<String, String>();
	private Map<String, String> mSpeechPitchMap  = new HashMap<String, String>();
	private Map<String, String> mSpeechRateMap   = new HashMap<String, String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        // 値->項目名への対応を作成
        makeMap(mLearningModeMap, R.array.learning_mode_values, R.array.learning_mode_items);
        makeMap(mReviseModeMap, R.array.revise_mode_values, R.array.revise_mode_items);
        makeMap(mPopTimeMap, R.array.result_pop_time_values, R.array.result_pop_time_items);
        makeMap(mSpeechLocaleMap, R.array.speech_loc_values, R.array.speech_loc_items);
        makeMap(mSpeechPitchMap, R.array.speech_pitch_values, R.array.speech_pitch_items);
        makeMap(mSpeechRateMap, R.array.speech_rate_values, R.array.speech_rate_items);

        // DB作成押下時の処理
        findPreference(Config.DB_CREATE).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent install = new Intent(SettingActivity.this, DbCreateActivity.class);
				startActivity(install);
				return true;
			}
		});

        // DB削除押下時の処理
        findPreference(Config.DB_DELETE).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				createDbDeleteList();
				return true;
			}
		});


        // 学習情報削除押下時の処理
        findPreference(Config.LEARNING_CLEAR).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				deleteLearningData();
				return true;
			}
		});

        // デフォルト用語のセット押下時の処理
        findPreference(Config.WORD_SET_DEF).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				setDefaultWords();
				return true;
			}
		});

        // TTSインストール押下時の処理
        findPreference(Config.TTS_INSTALL).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent install = new Intent();
				install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(install);
				return true;
			}
		});
    }

    private void createDbDeleteList() {
    	// ファイル選択用ダイアログの作成
    	AlertDialog.Builder dbList = new AlertDialog.Builder(SettingActivity.this);
    	String title = getResources().getString(R.string.choice_file);
    	dbList.setTitle(title);
    	// DBファイルのリスト作成
    	final String[] dbFileList = makeFileList(".db");
    	// 項目の設定
    	dbList.setItems(dbFileList, new DialogInterface.OnClickListener() {
    		// 項目選択時のリスナー
			public void onClick(DialogInterface parent, final int pos) {
				// 現在のDBは削除できない
				final File dbFile = new File(MainActivity.getApplicationDir(), dbFileList[pos]);
				if(!canDeleteDbFile(dbFile.getName())) {
					String str = getResources().getString(R.string.db_delete_fail);
					Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
					return;
				}
				// 確認ダイアログを出す
				String msg = getResources().getString(R.string.confirm_delete);
				new YesNoDialog(
						SettingActivity.this,
						new DialogInterface.OnClickListener() {

		    				public void onClick(DialogInterface child, int which) {
			    				// DBを削除
			    				dbFile.delete();
			    				// トーストを表示
			    				String str = getResources().getString(R.string.db_deleted);
			    				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
			    				// DBリストの初期化
			    				initDbList();
		    				}

		    			},
		    			null,
		    			msg
		    	).show();
			}
    	});
    	// キャンセルボタンを作成
    	String cancel = getResources().getString(R.string.cancel);
    	dbList.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	dbList.setCancelable(true);
    	Dialog dialog = dbList.create();
    	dialog.setCanceledOnTouchOutside(true);
    	dialog.show();
    }

    private boolean canDeleteDbFile(String dbFilename) {
    	// 現在のDBを取得
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String dbUsing = pref.getString(Config.DB_USING, WordDbHelper.DEF_DB_NAME);
		// 削除しようとしているファイルが現在のDBでなければ削除可能
		return !dbUsing.equals(dbFilename);
    }

    private void deleteLearningData() {
    	// 確認ダイアログを出す
    	String msg = getResources().getString(R.string.confirm_learning_data_delete);
    	new YesNoDialog(
    			SettingActivity.this,
    			new DialogInterface.OnClickListener() {

    				public void onClick(DialogInterface dialog, int which) {
    					// 学習データの初期化
    					WordDao dao = new WordDao();
    					dao.clearLearningData();
    					// トーストを表示
    					String str = getResources().getString(R.string.learning_data_deleted);
    					Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    				}

    			},
    			null,
    			msg
    	).show();
    }

    @Override
	protected void onResume() {
		super.onResume();
		// DBリストの初期化
		initDbList();
        // サマリーの初期化
        initAllSummaries();
        // リスナーの登録
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

    @Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

    private void initDbList() {
    	ListPreference dbUsing = (ListPreference)findPreference(Config.DB_USING);
    	setStrArrayToListPref(dbUsing, makeFileList(".db"));
    }
    /**
     * 指定された拡張子のファイルを返す
     * @param extension
     * @return ファイル名の配列
     */
    private String[] makeFileList(String extension) {
    	return MainActivity.getApplicationDir().list(new FilenameFilter() {
    		public boolean accept(File dir, String filename) {
				return filename.endsWith(".db");
			}
    	});
    }

    private void setStrArrayToListPref(ListPreference pref, String[] list) {
    	pref.setEntries(list);
    	pref.setEntryValues(list);
    }

	private void makeMap(Map<String, String> map, int fromAry, int toAry) {
    	String[] keys   = getResources().getStringArray(fromAry);
        String[] values = getResources().getStringArray(toAry);
        map.clear();
        for(int i=0; i<keys.length; i++) {
        	map.put(keys[i], values[i]);
        }
    }

	private void setDefaultWords() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		pref.edit().putString(Config.WORD_SPELL, getResources().getString(R.string.spell)).
					putString(Config.WORD_MEANING, getResources().getString(R.string.meaning)).
					putString(Config.WORD_PART, getResources().getString(R.string.part)).
					putString(Config.WORD_EX_EN, getResources().getString(R.string.example_en)).
					putString(Config.WORD_EX_JA, getResources().getString(R.string.example_ja)).
					putString(Config.WORD_CATEGORY, getResources().getString(R.string.category)).commit();
	}

	private void initAllSummaries() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		updateSummary(pref, Config.LEARNING_MODE);
		updateSummary(pref, Config.LEARNING_NUM);
		updateSummary(pref, Config.REVISE_MODE);
		updateSummary(pref, Config.REVISE_MAX);
		updateSummary(pref, Config.RES_POP_TIME);
		updateSummary(pref, Config.DB_USING);
		updateSummary(pref, Config.SPEECH_LOCALE);
        updateSummary(pref, Config.SPEECH_PITCH);
        updateSummary(pref, Config.SPEECH_RATE);
        updateSummary(pref, Config.WORD_SPELL);
        updateSummary(pref, Config.WORD_MEANING);
        updateSummary(pref, Config.WORD_PART);
        updateSummary(pref, Config.WORD_EX_EN);
        updateSummary(pref, Config.WORD_EX_JA);
        updateSummary(pref, Config.WORD_CATEGORY);
	}

	private void updateSummary(SharedPreferences pref, String key) {
		if(key.equals(Config.LEARNING_MODE)) {
			findPreference(key).setSummary(mLearningModeMap.get(pref.getString(key, Config.DEF_LEARNING_MODE)));
		}
		if(key.equals(Config.LEARNING_NUM)) {
			findPreference(key).setSummary(pref.getString(key, Config.DEF_LEARNING_NUM));
		}
		if(key.equals(Config.REVISE_MODE)) {
			findPreference(key).setSummary(mReviseModeMap.get(pref.getString(key, Config.DEF_REVISE_MODE)));
		}
		if(key.equals(Config.REVISE_MAX)) {
			findPreference(key).setSummary(pref.getString(key, Config.DEF_REVISE_MAX));
		}
		if(key.equals(Config.RES_POP_TIME)) {
			findPreference(key).setSummary(mPopTimeMap.get(pref.getString(key, Config.DEF_RES_POP_TIME)));
		}
		if(key.equals(Config.DB_USING)) {
			findPreference(key).setSummary("現在のDB：" + pref.getString(key, WordDbHelper.DEF_DB_NAME));
		}
		if(key.equals(Config.SPEECH_LOCALE)) {
			findPreference(key).setSummary("現在の言語：" + mSpeechLocaleMap.get(pref.getString(key, Config.DEF_SPEECH_LOCALE)));
		}
		if(key.equals(Config.SPEECH_PITCH)) {
			findPreference(key).setSummary(mSpeechPitchMap.get(pref.getString(key, Config.DEF_SPEECH_PITCH)));
		}
		if(key.equals(Config.SPEECH_RATE)) {
			findPreference(key).setSummary(mSpeechRateMap.get(pref.getString(key, Config.DEF_SPEECH_RATE)));
		}
		if(key.equals(Config.WORD_SPELL)) {
			findPreference(key).setTitle(pref.getString(key, getResources().getString(R.string.spell)));
		}
		if(key.equals(Config.WORD_MEANING)) {
			findPreference(key).setTitle(pref.getString(key, getResources().getString(R.string.meaning)));
		}
		if(key.equals(Config.WORD_PART)) {
			findPreference(key).setTitle(pref.getString(key, getResources().getString(R.string.part)));
		}
		if(key.equals(Config.WORD_EX_EN)) {
			findPreference(key).setTitle(pref.getString(key, getResources().getString(R.string.example_en)));
		}
		if(key.equals(Config.WORD_EX_JA)) {
			findPreference(key).setTitle(pref.getString(key, getResources().getString(R.string.example_ja)));
		}
		if(key.equals(Config.WORD_CATEGORY)) {
			findPreference(key).setTitle(pref.getString(key, getResources().getString(R.string.category)));
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		updateSummary(pref, key);
	}
}
