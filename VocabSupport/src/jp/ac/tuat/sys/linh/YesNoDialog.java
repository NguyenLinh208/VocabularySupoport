package jp.ac.tuat.sys.linh;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import jp.ac.tuat.sys.kaneko.R;

public class YesNoDialog extends AlertDialog.Builder {

	public YesNoDialog(Context context,
			DialogInterface.OnClickListener posListener,
			DialogInterface.OnClickListener negListener,
			String title, String message) {
		super(context);
		setTitle(title);
		setMessage(message);
		String yes = context.getResources().getString(R.string.yes);
		String no  = context.getResources().getString(R.string.no);
		setPositiveButton(yes, posListener);
		setNegativeButton(no, negListener);
		setCancelable(true);
	}

	// タイトル省略時はR.string.confirm
	public YesNoDialog(Context context,
			DialogInterface.OnClickListener posListener,
			DialogInterface.OnClickListener negListener,
			String message) {
		this(context, posListener, negListener, context.getResources().getString(R.string.confirm), message);
	}
}
