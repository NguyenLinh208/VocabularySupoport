package jp.ac.tuat.sys.linh;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import jp.ac.tuat.sys.kaneko.R;

public class ReviseSentenceSelectActivity extends Activity {

	private static final int REQ_CODE_REVISE = 3;
	private static final int[] QUESTION_NUM = {5, 10, 15, 20, 25, 30};
	private static final int INIT_QUESTION_NUM = 1;

	private Spinner mQuestionNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise_select_sentence);

        // ビューの取得
        mQuestionNum = (Spinner)findViewById(R.id.number_spinner);

        // スピナー用のアダプタの作成
        String[] spinList = new String[QUESTION_NUM.length];
        for(int i=0; i<QUESTION_NUM.length; i++) {
        	spinList[i] = QUESTION_NUM[i] + "問";
        }
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinList);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // スピナーにアダプタをセット
        // 最初の選択は"20問"にしておく
        mQuestionNum.setAdapter(spinAdapter);
        mQuestionNum.setSelection(INIT_QUESTION_NUM);

        // リストにアダプタをセット
        ListView list = (ListView)findViewById(R.id.modes_list);
        list.setAdapter(createModeList());

        // リストが押された際の処理
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				ListView list = (ListView)parent;
				ModeItem mode = (ModeItem)list.getItemAtPosition(position);
				// 復習語のリストを作成し，復習を開始する
				makeLearningListInBackground(mode.getModeNum());
			}
		});

        // 終了ボタン押下時の処理
 		Button finishButton = (Button)findViewById(R.id.finish_button);
 		finishButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// アクティビティ終了
 				finish();
 			}
 		});
    }

    private ModeListAdapter createModeList() {
    	// リスト用のアダプタの作成
    	ModeListAdapter listAdapter = new ModeListAdapter(this);
    	String title;
    	String desc;

    	// 三択モード
    	title = getResources().getString(R.string.learning_q3_mode);
    	desc  = getResources().getString(R.string.learning_q3_mode_desc);
        ModeItem mode1 = new ModeItem(Config.MODE_QUESTION_3, title, desc);
        // 四択モード
        title = getResources().getString(R.string.learning_q4_mode);
    	desc  = getResources().getString(R.string.learning_q4_mode_desc);
        ModeItem mode2 = new ModeItem(Config.MODE_QUESTION_4, title, desc);
        // 想起モード
        title = getResources().getString(R.string.learning_hide_mode);
    	desc  = getResources().getString(R.string.learning_hide_mode_desc);
        ModeItem mode3 = new ModeItem(Config.MODE_MEANING_HIDE, title, desc);

        listAdapter.add(mode1);
        listAdapter.add(mode2);
        listAdapter.add(mode3);

        return listAdapter;
    }

    private void makeLearningListInBackground(final int modeNum) {
    	new AsyncTask<Void, Void, ArrayList<WordEntity>>() {

    		private ProgressDialog progressDialog;

    		@Override
			protected void onPreExecute() {
    			// 検索中というダイアログを表示
				progressDialog = new ProgressDialog(ReviseSentenceSelectActivity.this);
				progressDialog.setTitle(R.string.progress_search);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(true);
				progressDialog.show();
			}

    		@Override
			protected ArrayList<WordEntity> doInBackground(Void... args) {
    			return makeLearningList();
			}

			@Override
			protected void onPostExecute(ArrayList<WordEntity> result) {
				progressDialog.dismiss();
				// 復習モード開始
				startLearning(modeNum, result);
			}

			@Override
			protected void onCancelled() {
				progressDialog.dismiss();
				this.cancel(true);
			}

    	}.execute();
    }

    private ArrayList<WordEntity> makeLearningList() {
    	WordDao dao = new WordDao();
		// 検索実行
    	String where = WordDbHelper.WCOL_LEVEL + " > ?";
    	String param = "0";
    	String order = WordDbHelper.WCOL_NEXT_LEARNING + " asc";	// 学習予定が早い順
		List<WordEntity> search = dao.searchWord(WordDbHelper.WORD_ALL_INFO, where, new String[]{param}, order);
		// 学習予定語の配列を作成
		ArrayList<WordEntity> result = new ArrayList<WordEntity>();
		// 最大問題数を取得
		int revMax = getReviseMax();
		// 復習優先度を更新
		// priSumに優先度の総和を取得
		long priSum = 0;
		for(Iterator<WordEntity> it = search.iterator(); it.hasNext();) {
			WordEntity word = it.next();
			word.calcPriority();
			// 優先度が最大の場合かつ，リストに空きがある場合，学習予定語に設定
			if(word.isPriorityMax()) {
				if(result.size() < revMax) {
					result.add(word);
					it.remove();
				}
			} else {
				priSum += word.getPriority();
			}
		}
		// ランダムに問題数分の単語を取得する
		// ただし，優先度が高いものほどでやすくなる
		int revNow = result.size();
		for(int i=0; i<revMax-revNow; i++) {
			long tarNum = (long)Math.floor(Math.random() * priSum);
			long nowPri = 0;
			// 優先度を足していって，tarNumを超えた地点にある単語を選択
			for(Iterator<WordEntity> it = search.iterator(); it.hasNext();) {
				WordEntity word = it.next();
				nowPri += word.getPriority();
				if(nowPri > tarNum) {
					// リストresultに格納し，searchからは削除する
					result.add(word);
					it.remove();
					// 優先度の総和から，削除した単語の優先度を引く
					priSum -= word.getPriority();
					break;
				}
			}

		}

		return result;
    }

    private int getReviseMax() {
    	return QUESTION_NUM[mQuestionNum.getSelectedItemPosition()];
    }

    private void startLearning(int modeNum, ArrayList<WordEntity> reviseList) {
    	Intent intent = null;

		switch(modeNum) {
		case Config.MODE_QUESTION_3:	// モード；三択問題
			// 学習語リストを渡す
			intent = new Intent(ReviseSentenceSelectActivity.this, QuestionSentence3Activity.class);
			intent.putExtra(VSappConst.LEARNING_WORDS, reviseList);
			break;
		case Config.MODE_QUESTION_4:	// モード：四択問題
			// 学習語リストを渡す
			intent = new Intent(ReviseSentenceSelectActivity.this, QuestionSentence4Activity.class);
			intent.putExtra(VSappConst.LEARNING_WORDS, reviseList);
			break;
		case Config.MODE_MEANING_HIDE:	// モード：想起
			// 学習語リストを渡す
			intent = new Intent(ReviseSentenceSelectActivity.this, SpellHideQuestion.class);
			intent.putExtra(VSappConst.LEARNING_WORDS, reviseList);
			break;
		}

		// 学習画面に遷移
		startActivityForResult(intent, REQ_CODE_REVISE);
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQ_CODE_REVISE) {
			if(resultCode == Activity.RESULT_OK) {
				finish();
			}
		}
	}

    /**
     *
     * ModeItem.java
     *
     */
    private class ModeItem {
    	private int mModeNum;
    	private String mName;
    	private String mDescription;

    	private ModeItem(int num, String name, String desc) {
    		mModeNum = num;
    		mName = name;
    		mDescription = desc;
    	}

    	private int getModeNum() {
    		return mModeNum;
    	}
    	private String getName() {
    		return mName;
    	}
    	private String getDesc() {
    		return mDescription;
    	}
    }

    /**
     *
     * ModeListAdapter.java
     *
     */
    private class ModeListAdapter extends ArrayAdapter<ModeItem> {

    	private ModeListAdapter(Context context) {
    		super(context, 0);
    	}

    	public View getView(int position, View convertView, ViewGroup parent) {
    		View view;

    		if(convertView == null) {
    			LayoutInflater inflater = LayoutInflater.from(getContext());
    			view = inflater.inflate(R.layout.mode_list_row, parent, false);
    		} else {
    			view = convertView;
    		}

    		// アイテムの取得
    		ModeItem mode = getItem(position);
    		// 1行目
    		TextView row1 = (TextView)view.findViewById(R.id.row_text1);
    		row1.setText(mode.getName());
    		// 2行目
    		TextView row2 = (TextView)view.findViewById(R.id.row_text2);
    		row2.setText(mode.getDesc());

    		return view;
    	}
    }
}
