package jp.ac.tuat.sys.linh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import jp.ac.tuat.sys.kaneko.R;

public class DbCreateActivity extends Activity implements OnClickListener, android.content.DialogInterface.OnClickListener {

	private static final String ENCODE = "UTF-8";

	private EditText mDbFileName;
	private EditText mDataFileName;
	private CheckBox mDeleteCheck;
	private String[] mFileList;
	private List<WordEntity> mData;
	private String[] mParts;
	private String[] mCategories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_create);
        KeyboardUtil.initHide(this);

        // 各種ビューの取得
        mDbFileName = (EditText)findViewById(R.id.name_edit);
        mDataFileName = (EditText)findViewById(R.id.data_file_edit);
        mDeleteCheck = (CheckBox)findViewById(R.id.delete_check);
        Button dataFileSearch = (Button)findViewById(R.id.data_file_search_button);
        Button createButton = (Button)findViewById(R.id.db_create_button);

        // リスナーの登録
        dataFileSearch.setOnClickListener(this);
        createButton.setOnClickListener(this);

        // 終了ボタン押下時の処理
  		Button finishButton = (Button)findViewById(R.id.finish_button);
  		finishButton.setOnClickListener(new OnClickListener() {
  			public void onClick(View v) {
  				// アクティビティ終了
  				finish();
  			}
  		});
    }

	@Override
	protected void onResume() {
		super.onResume();
		// csvファイル名の一覧を取得
        setFileList();
	}

	private void setFileList() {
    	mFileList = MainActivity.getApplicationDir().list(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".csv");
			}
        });
    }

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.data_file_search_button:
			showFileSelectDialog();
			break;
		case R.id.db_create_button:
			checkDbFileExists();
			break;
		}
		// キーボードを隠す
		KeyboardUtil.hide(getApplicationContext(), v);
	}

	/**
	 * ファイル選択ダイアログの表示
	 */
	private void showFileSelectDialog() {
		String title  = getResources().getString(R.string.choice_file);
		String cancel = getResources().getString(R.string.cancel);
		Dialog dialog = new AlertDialog.Builder(this)
							.setTitle(title)
							.setItems(mFileList, this)
							.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.setCancelable(true)
							.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	/**
	 * DBファイルがすでに存在しているか確認し，存在している場合は削除許可を求める
	 * 存在していない，もしくは削除しても良いならば，createDatabaseを呼び出す
	 */
	private void checkDbFileExists() {
		// DBファイルの存在確認
		String dbFilename = mDbFileName.getText().toString();
		if(!dbFilename.endsWith(".db")) {
			dbFilename += ".db";
		}
		final File dbFile = new File(MainActivity.getApplicationDir(), dbFilename);
		// 存在している場合，削除の確認
		if(dbFile.exists()) {
			// 確認ダイアログを出す
			String title = getResources().getString(R.string.confirm);
			String msg   = getResources().getString(R.string.confirm_db_delete);
			new YesNoDialog(
					DbCreateActivity.this,
					// 削除許可の場合，DB作成へ
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							createDatabase(dbFile);
						}
					},
					null,
					title,
					msg
			).show();
		} else {
			// ファイルが存在しない場合，DB作成へ
			createDatabase(dbFile);
		}
	}

	/**
	 * DBを作成する．
	 * すでにファイルが存在している場合，削除する．
	 * @param dbFile
	 */
	private void createDatabase(File dbFile) {
		// ファイル名取得
		String csvFilename = mDataFileName.getText().toString();
		// データ読み込み
		// 成功したらtrueが返る
		if(readData(csvFilename)) {
			// 存在しているDBを削除
			dbFile.delete();
			// 別スレッドでDBを作成
			createDatabaseInBackground(dbFile.getName(), csvFilename);
		} else {
			// 何らかの原因でDBファイルの作成に失敗
			String msg = getResources().getString(R.string.db_create_fail);
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void createDatabaseInBackground(final String dbFilename, final String csvFilename) {
		// 非同期タスクで実行
    	// UIスレッドでは検索中ダイアログを表示
    	new AsyncTask<Void, Void, Void>() {

    		private ProgressDialog progressDialog;

    		@Override
			protected void onPreExecute() {
    			// 作成中というダイアログを表示
				progressDialog = new ProgressDialog(DbCreateActivity.this);
				progressDialog.setTitle(R.string.progress_create);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(true);
				progressDialog.show();
			}

    		@Override
			protected Void doInBackground(Void... args) {
    			// データベース作成
    			WordDbHelper.initDataBase(getApplicationContext(), dbFilename);
    			// データの挿入
    			WordDao dao = new WordDao();
    			dao.insertWords(mData);
    			dao.remakeParts(mParts);
    			dao.remakeCategories(mCategories);
    			return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();
				// DB作成後にcsvファイルを削除する場合，削除処理
				if(mDeleteCheck.isChecked()) {
					deleteCsvFile(csvFilename);
					// csvファイル名の一覧を再取得
			        setFileList();
				}
				// トーストを表示
				String msg = getResources().getString(R.string.db_create_success);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				// 入力フィールドクリア
				clearFields();
			}

			@Override
			protected void onCancelled() {
				progressDialog.dismiss();
				this.cancel(true);
			}

    	}.execute();
	}

	private boolean readData(String filename) {
		BufferedReader br = null;
		int lineNum = 4;
		try {
			//br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.ej200)));
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(MainActivity.getApplicationDir(), filename)), ENCODE));
			// 1行目は読み飛ばす
			br.readLine();
			// 2行目：品詞
			mParts = br.readLine().split(",");
			// 3行目：カテゴリ
			mCategories = br.readLine().split(",");
			// 4行目以降：データ
			mData = new ArrayList<WordEntity>();
			String line;
			while((line = br.readLine()) != null) {
				// 単語データを作成する
				WordEntity word = new WordEntity();
				// csvファイルの形式：
				// spell,meaning,part,category,exen,exja
				String[] info = line.split(",");
				// 必須情報が欠けている場合，エラーを吐く
				if(info.length < 3) {
					throw new IllegalFormatException();
				}
				int id;
				for(int i=0; i<info.length; i++) {
					switch(i) {
					case 0:	// 必須：スペル
						word.setSpell(info[i]);
						break;
					case 1:	// 必須：意味
						word.setMeaning(info[i]);
						break;
					case 2:	// 必須：品詞
						id = ArrayUtil.linearSearch(mParts, info[i]);
						if(id < 0) {
							throw new IllegalFormatException();
						} else {
							word.setPartId(id);
						}
						break;
					case 3:	// 任意：カテゴリ
						id = ArrayUtil.linearSearch(mCategories, info[i]);
						if(id < 0) {
							throw new IllegalFormatException();
						} else {
							word.setCategoryId(id);
						}
						break;
					case 4:	// 任意：例文
						word.setExampleEn(info[i]);
						break;
					case 5:	// 任意：和訳
						word.setExampleJa(info[i]);
						break;
					}
				}
				// リストに追加
				mData.add(word);
				// 行数を増やす（エラー表示用）
				lineNum ++;
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(), "Error: cannot find the file : " + filename, Toast.LENGTH_SHORT).show();
			return false;
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Error: cannot read the file : " + filename, Toast.LENGTH_SHORT).show();
			return false;
		} catch (IllegalFormatException e) {
			Toast.makeText(getApplicationContext(), "Error: illegal format : " + filename + "(line"+lineNum+")", Toast.LENGTH_SHORT).show();
			return false;
		} finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch(IOException e) {
				return false;
			}
		}

		return true;
	}

	private void deleteCsvFile(String filename) {
		File file = new File(MainActivity.getApplicationDir(), filename);
		file.delete();
	}

	private void clearFields() {
		mDbFileName.setText("");
		mDataFileName.setText("");
		mDeleteCheck.setChecked(false);
	}

	/**
	 *  ファイル選択ダイアログのリスナー
	 */
	public void onClick(DialogInterface dialog, int which) {
		mDataFileName.setText(mFileList[which]);
	}

	@SuppressWarnings("serial")
	private class IllegalFormatException extends Exception {}

}
