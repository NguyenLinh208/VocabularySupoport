package jp.ac.tuat.sys.linh;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jp.ac.tuat.sys.kaneko.R;

public class WordListAdapter extends ArrayAdapter<WordEntity> {

	public WordListAdapter(Context context) {
		super(context, 0);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		// ビューの取得
		// 新規作成されるのは最初の数回のみで，
		// あとで使いまわされる（convertView != null)
		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.word_list_row, parent, false);
		} else {
			view = convertView;
		}

		// アイテムの取得
		WordEntity word = getItem(position);
		// toString()で得られる文字列を改行文字で区切る
		String[] text = word.toString().split("\n");
		// 1行目
		TextView row1 = (TextView)view.findViewById(R.id.row_text1);
		row1.setText(text[0]);
		// 2行目
		TextView row2 = (TextView)view.findViewById(R.id.row_text2);
		row2.setText(text[1]);

		return view;
	}

	public void addAll(List<WordEntity> list) {
		for(WordEntity word : list) {
			add(word);
		}
	}
}
