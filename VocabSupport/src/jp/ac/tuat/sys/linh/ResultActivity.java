package jp.ac.tuat.sys.linh;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import jp.ac.tuat.sys.kaneko.R;

public class ResultActivity extends WordListBase<Void> {

	// 復習情報のみの射影
	private static final String[] REVISE_INFO = new String[] {
		WordDbHelper.WCOL_LEVEL,
		WordDbHelper.WCOL_BOOKMARK,
		WordDbHelper.WCOL_PREV_LEARNING,
		WordDbHelper.WCOL_NEXT_LEARNING
	};
	
	private ArrayList<WordEntity> mRightList;
	private ArrayList<WordEntity> mWrongList;
	private Thread mBackgroundThread;

	private Spinner mResultSpinner;

	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_result_list);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initOnCreate() {
		// ビューの取得
        mResultSpinner = (Spinner)findViewById(R.id.result_spinner);

        // スピナー用のアダプタの作成とセット
        String right = getResources().getString(R.string.result_right);
        String wrong = getResources().getString(R.string.result_wrong);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[]{wrong, right});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mResultSpinner.setAdapter(adapter);

        // スピナーの選択が変更された際の処理
        mResultSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        		// リストに正解 or 不正解単語を表示
        		switch(position) {
        		case 0:	// 不正解
        			setList(mWrongList);
        			break;
        		case 1:	// 正解
        			setList(mRightList);
        			break;
        		}
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// 終了ボタン押下時の処理
		Button finishButton = (Button)findViewById(R.id.finish_button);
		finishButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 同期
				try {
					mBackgroundThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// アクティビティ終了
				finish();
			}
		});

		// インテントの付加情報を取得
 		Bundle extra = getIntent().getExtras();
 		if(extra != null) {
 			// 単語のリストを受け取る
 			mRightList = ((ArrayList<WordEntity>)extra.getSerializable(VSappConst.RIGHT_WORDS));
 			mWrongList = ((ArrayList<WordEntity>)extra.getSerializable(VSappConst.WRONG_WORDS));
 		}

 		// 復習優先度の更新
 		updatePriorities();
	}

	private void updatePriorities() {
		// 別スレッドで復習優先度を更新する
    	mBackgroundThread = new Thread() {
			@Override
			public void run() {
				WordDao dao = new WordDao();
				for(WordEntity word : mRightList) {
					word.updatePriority(true);
					word.updateBookmark(true);
				}
				for(WordEntity word : mWrongList) {
					word.updatePriority(false);
					word.updateBookmark(true);
				}
				dao.updateWords(mRightList, REVISE_INFO);
				dao.updateWords(mWrongList, REVISE_INFO);
			}
    	};
    	mBackgroundThread.start();
	}

	@Override
	protected List<WordEntity> doInAsyncBackground(Void... args) {
		return null;
	}

}
