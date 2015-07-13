package jp.ac.tuat.sys.linh;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {

	private KeyboardUtil() {}

	// キーボードを隠す
	public static void hide(Context context, View view) {
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	// 最初にキーボードを表示しない
	public static void initHide(Activity activity) {
		activity.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

}
