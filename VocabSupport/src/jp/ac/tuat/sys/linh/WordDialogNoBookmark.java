package jp.ac.tuat.sys.linh;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import jp.ac.tuat.sys.kaneko.R;

public class WordDialogNoBookmark extends Dialog {

	private static WordEntity mWord  = null;
	private TextToSpeech mTTS = null;
	private String mSpeakText = "";

	public WordDialogNoBookmark(Context context, WordEntity word, TextToSpeech tts) {
		super(context, R.style.WordDialogTheme);
		mWord = word;
		mTTS  = tts;
	}

	@Override
	/**
	 * 最初のshow()メソッド実行時に呼ばれる
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.word_dialog_no_bookmark);

		// テキストの設定
		setupText();
			
		// ダイアログ外をタッチすると消す
		this.setCanceledOnTouchOutside(true);
	}
	
	private void setupText() {
		if(mTTS != null) {
			readyTTS();
		}
		
		// 表示する文字列を取得
		String[] text = mWord.toString().split("\n", -1);
		// 各種テキストを設定
		TextView spell = (TextView)findViewById(R.id.word_dialog_title);
		spell.setText(text[0] + " [" + mWord.getBookmark() + "]");
		TextView line1 = (TextView)findViewById(R.id.word_dialog_line1);
		line1.setText(text[1]);
		// 例文は省略可能
		TextView line2 = (TextView)findViewById(R.id.word_dialog_line2);
		if(text[2].equals("")) {
			line2.setHeight(0);
		} else {
			line2.setText(text[2]);
		}
		TextView line3 = (TextView)findViewById(R.id.word_dialog_line3);
		if(text[3].equals("")) {
			line3.setHeight(0);
		} else {
			line3.setText(text[3]);
		}

		// スペルを読み上げテキストに設定
		mSpeakText = text[0];
	}
	
	private void readyTTS() {
		ImageView note = (ImageView)findViewById(R.id.note_button);
		note.setVisibility(View.VISIBLE);
		note.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mTTS.isSpeaking()) {
					mTTS.stop();
				}
				mTTS.speak(mSpeakText, TextToSpeech.QUEUE_FLUSH, null);
			}
		});
	}
	
}
