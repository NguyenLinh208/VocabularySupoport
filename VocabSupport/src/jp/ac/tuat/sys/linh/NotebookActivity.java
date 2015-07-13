package jp.ac.tuat.sys.linh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class NotebookActivity extends Activity {
    DrawNotebook view;
    private static final int MENU_CLEAR = 0; //消去
    private static final int MENU_SAVE = 1; //保存
    private static final int PEN_OR_ERASER = 2; //消しゴムかペンか
    private static boolean next = true; //消しゴムかペンか
    // アプリの初期化
    @SuppressLint("InlinedApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ActionBarの表示
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        // 描画クラスを設定
        view = new DrawNotebook(getApplication());
        setContentView(view);
    }
    // メニューの生成イベント
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
       	menu.add(0, PEN_OR_ERASER, 0, "Pen←→Eraser");
    	menu.add(0, MENU_CLEAR, 0, "Clear");
    	menu.add(0, MENU_SAVE, 0, "Save");
    	return true;
    }
    // メニューがクリックされた時のイベント
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch ( item.getItemId() ) {
    	case PEN_OR_ERASER: //保存
    		if(next){
    			view.seteraser(false);
    			next=false;
    		}
    		else{
    			view.seteraser(true);
    			next=true;
    		}
    		break;
    	case MENU_CLEAR: //　消去
    		view.clearDrawList();
    		break;
    	case MENU_SAVE: //保存
    		view.saveToFile();
    		break;
		}
    	return true;
    }
    protected void onStop(){
    	super.onStop();
    	view.saveToCacheFile();// 画面を一時ファイルに保存する
    	// 復元は view.onSizeChanged() で行う
    }
}

