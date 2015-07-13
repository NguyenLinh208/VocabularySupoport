package jp.ac.tuat.sys.linh;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import jp.ac.tuat.sys.kaneko.R;

public class WordListActivity extends WordListBase<String>{

	// コンテキストメニューで使用するID
	private static final int CONTEXT_MODIFY = 0;
	private static final int CONTEXT_DELETE = 1;
	private static final int CONTEXT_CANCEL = 2;

	private EditText mSearchWord;

	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_word_list);
	}

	@Override
	protected void initOnCreate() {
		// ビューの取得
        mSearchWord = (EditText)findViewById(R.id.search_text);
        
        // コンテキストメニュー＝長押しされたときのメニュー登録
        registerForContextMenu(mWordsList);

        // 検索ボタンクリック
        Button button = (Button)findViewById(R.id.search_button);
        button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 検索実行
				search(mSearchWord.getText().toString());
				// キーボードを隠す
				KeyboardUtil.hide(getApplicationContext(), v);
			}
        });
	}

	@Override
	protected List<WordEntity> doInAsyncBackground(String... args) {
		WordDao dao = new WordDao();
    	// 検索条件設定
		String where = WordDbHelper.WCOL_SPELL + " like ?";	// 部分一致
		String param = "%" + args[0] + "%";					// 条件：searchSpellを一部分に含む（%は任意の0文字以上）
		String order = WordDbHelper.WCOL_SPELL + " asc";	// スペルに関して昇順ソート（降順はdesc）
		// 検索実行
		return dao.searchWord(PROJECTION, where, new String[]{param}, order);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 初期データの表示
		search(mSearchWord.getText().toString());
	}
	
    /**
     * コンテキストメニュー生成処理
     */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		WordEntity word = (WordEntity)mWordsList.getItemAtPosition(info.position);
		// コンテキストメニューの設定
		menu.setHeaderTitle(word.getSpell());
		// add(int groupId, int itemId, int order, CharSequence title)
		menu.add(Menu.NONE, CONTEXT_MODIFY, Menu.NONE, R.string.update);
		menu.add(Menu.NONE, CONTEXT_DELETE, Menu.NONE, R.string.delete);
		menu.add(Menu.NONE, CONTEXT_CANCEL, Menu.NONE, R.string.cancel);
	}

	/**
	 * コンテキストメニュー押下時処理
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		WordEntity word = (WordEntity)mWordsList.getItemAtPosition(info.position);

		switch(item.getItemId()) {
		case CONTEXT_MODIFY:	// 修正
			// 修正処理
			modifyWord(word);
			return true;
		case CONTEXT_DELETE:	// 削除
			// 削除処理
			deleteWord(word);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void modifyWord(WordEntity word) {
		// 修正画面に遷移
		Intent intent = new Intent(WordListActivity.this, RegisterActivity.class);
		intent.putExtra(VSappConst.INTENT_MODIFY, word);
		startActivity(intent);
	}

	private void deleteWord(final WordEntity word) {
		// 確認ダイアログを出す
		String msg = getResources().getString(R.string.confirm_delete);
		new YesNoDialog(
				WordListActivity.this,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						//  単語データベースから削除
						WordDao dao = new WordDao();
						dao.deleteWord(word.getId());
						// トーストを表示
						String del = getResources().getString(R.string.word_deleted);
						Toast.makeText(getApplicationContext(), del, Toast.LENGTH_SHORT).show();
						// リストをリフレッシュ
						mListAdapter.remove(word);
						mListAdapter.notifyDataSetChanged();
					}

				},
				null,
				msg
		).show();
	}
}
