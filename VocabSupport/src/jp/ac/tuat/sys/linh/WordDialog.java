package jp.ac.tuat.sys.linh;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.database.sqlite.SQLiteDatabase;

import jp.ac.tuat.sys.kaneko.R;

public class WordDialog extends Dialog implements OnClickListener {

	private static WordEntity mWord  = null;
	private TextToSpeech mTTS = null;
	private String mSpeakText = "";
	private Button bookmark_off=null;
	private Button bookmark_on=null;
	private static WordDbHelper mDbHelper = WordDbHelper.getInstance();
	
	public WordDialog(Context context, WordEntity word, TextToSpeech tts) {
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
		setContentView(R.layout.word_dialog);

		// テキストの設定
		setupText();
		
		bookmark_off=(Button)findViewById(R.id.bookmark_off_button);
		bookmark_off.setOnClickListener(this);
		
		bookmark_on=(Button)findViewById(R.id.bookmark_on_button);
		bookmark_on.setOnClickListener(this);
		
		// ダイアログ外をタッチすると消す
		this.setCanceledOnTouchOutside(true);
	}
	
	public void onClick(View v) {
		if (v == bookmark_off){	

			if(mWord.getBookmark()>0){
				mWord.setBookmark(mWord.getBookmark()-1);
				UpdateBookmark(mWord,mWord.getBookmark()); //毎回データベースに書き込むの必要か？
				setupText();
			}
		}
		else if (v == bookmark_on){
		        mWord.setBookmark(mWord.getBookmark()+1);
		        UpdateBookmark(mWord,mWord.getBookmark()); //毎回データベースに書き込むの必要か？
		        setupText();
		}
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
	
	private void UpdateBookmark(WordEntity word,int data){
		// データベース取得
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
	
		// しおりを増減させる
		db.execSQL("update " + WordDbHelper.WORD_TABLE + " set " + WordDbHelper.WCOL_BOOKMARK + " = " + data + " where " + WordDbHelper.KEY + " == " + word.getId());
		db.close();
	}

//	private int GetBookmark(){ 
//		// データベース取得 
//		SQLiteDatabase db = mDbHelper.getReadableDatabase();
//		
//		//取得
//		Cursor c = db.query(WordDbHelper.WORD_TABLE,
//				new String[] { "bookmark" },
//				WordDbHelper.KEY + " == ?",
//				new String[]{String.valueOf(mWord.getId())}, null, null, null);
//		c.moveToFirst();
//		int bookmark = Integer.parseInt(c.getString(0)); 
//		db.close();
//	    return bookmark;
//	}
	
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
