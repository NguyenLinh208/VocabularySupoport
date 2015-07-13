package jp.ac.tuat.sys.linh;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import jp.ac.tuat.sys.kaneko.R;

public class BookmarkListActivity extends WordListBase<Integer> {
	
	private static final int CONTEXT_MODIFY = 0;
	private static final int CONTEXT_DELETE = 1;
	private static final int CONTEXT_CANCEL = 2;

	private List<WordEntity> mWords;

	private Spinner mCategorySpinner;

	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_word_list_bookmark);
	}
	@Override
	protected void initOnCreate() {
		// ビューの取得
        mCategorySpinner = (Spinner)findViewById(R.id.category_spinner);
        
        // コンテキストメニュー＝長押しされたときのメニュー登録
        registerForContextMenu(mWordsList);
        
        // スピナー用のアダプタの作成とセット
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getBookmarkList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);

        // スピナーの選択が変更された際の処理
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				// 選択されたカテゴリの単語を探す
				search(position);
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	/**
	 * カテゴリに"全て"を加えた配列を作成
	 * @return
	 */
	private String[] getBookmarkList() { 
		String[] catList = new String[12]; 
		
		for(int i=0;i<catList.length-1;i++) {
			catList[i+1]=String.valueOf(i); 
		}
		catList[catList.length-1]=catList[catList.length-1] + "～"; 
		catList[0] = getResources().getString(R.string.all_category); 
		return catList;
	 }
	private static int upper=12;
	@Override
	protected List<WordEntity> doInAsyncBackground(Integer...args){
		WordDao dao = new WordDao(); 
		// 共通検索条件設定
		String order = WordDbHelper.WCOL_SPELL + " asc"; 
		// スペルに関して昇順ソート（降順はdesc） 
		String limit = null; 
		// 引数==0：全てのカテゴリ
		// 引数!=0：カテゴリ指定 
		if(args[0] == 0) {
			String where = null;
			String[] params = null; 
			// 検索実行
			mWords = dao.searchWord(PROJECTION, where, params, order, limit); }
		else if (args[0] == upper-1){
			String[] params = {String.valueOf(args[0]-1)};
			String where = WordDbHelper.WCOL_BOOKMARK + ">= ?" ;
			// 検索実行
			mWords = dao.searchWord(PROJECTION, where, params, order, limit); } 
		else { 
			String[] params = {String.valueOf(args[0]-1)};
			String where = WordDbHelper.WCOL_BOOKMARK + "= ?" ;
			// 検索実行 
			mWords = dao.searchWord(PROJECTION, where, params, order, limit);
			}
		return mWords;
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
		Intent intent = new Intent(BookmarkListActivity.this, RegisterActivity.class);
		intent.putExtra(VSappConst.INTENT_MODIFY, word);
		startActivity(intent);
	}

	private void deleteWord(final WordEntity word) {
		// 確認ダイアログを出す
		String msg = getResources().getString(R.string.confirm_delete);
		new YesNoDialog(
				BookmarkListActivity.this,
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
