package jp.ac.tuat.sys.linh;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import jp.ac.tuat.sys.kaneko.R;

public class RegisterActivity extends Activity {

	// 修正情報のみの射影
	protected static final String[] MODIFY_PROJECTION = new String[] {
		WordDbHelper.WCOL_SPELL,
		WordDbHelper.WCOL_MEANING,
		WordDbHelper.WCOL_PART,
		WordDbHelper.WCOL_EXAMPLE_EN,
		WordDbHelper.WCOL_EXAMPLE_JA,
		WordDbHelper.WCOL_CATEGORY
	};

	private EditText mSpellEdit;
	private EditText mMeaningEdit;
	private EditText mExampleEnEdit;
	private EditText mExampleJaEdit;
	private Spinner mPartSpinner;
	private Spinner mCategorySpinner;
	private Button mRegisterButton;

	private String mWordSpell;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 最初はIMEキーボードを表示しない
        KeyboardUtil.initHide(this);
        setContentView(R.layout.activity_register);

        // 各種ビューの取得
        mSpellEdit = (EditText)findViewById(R.id.spell_edit);
        mMeaningEdit = (EditText)findViewById(R.id.meaning_edit);
        mExampleEnEdit = (EditText)findViewById(R.id.example_en_edit);
        mExampleJaEdit = (EditText)findViewById(R.id.example_ja_edit);
        mRegisterButton = (Button)findViewById(R.id.register_button);
        mPartSpinner = (Spinner)findViewById(R.id.part_spinner);
        mCategorySpinner = (Spinner)findViewById(R.id.category_spinner);

        // スピナー用のアダプタの作成とセット
        WordDao dao = new WordDao();
        ArrayAdapter<String> prtAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dao.getParts());
        prtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPartSpinner.setAdapter(prtAdapter);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dao.getCategories());
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(catAdapter);

        // 終了ボタン押下時の処理
  		Button finishButton = (Button)findViewById(R.id.finish_button);
  		finishButton.setOnClickListener(new OnClickListener() {
  			public void onClick(View v) {
  				// アクティビティ終了
  				finish();
  			}
  		});

	     // インテントの付加情報を取得
		Bundle extra = getIntent().getExtras();
		// 付加情報あり＝単語の修正の場合
		if(extra != null) {
			// 単語の情報を受け取り，フィールドにセット
			final WordEntity word = (WordEntity)extra.getSerializable(VSappConst.INTENT_MODIFY);
			setFields(word);
			mRegisterButton.setText(R.string.update);
			mRegisterButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(checkFormat()) {
						updateWord(word.getId());	// DBを更新
						KeyboardUtil.hide(getApplicationContext(), v);
					}
				}
			});
		// 付加情報なし＝単語の登録の場合
		} else {
			mRegisterButton.setText(R.string.register);
			mRegisterButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(checkFormat()) {
						insertWord();				// DBに登録
						KeyboardUtil.hide(getApplicationContext(), v);
					}
				}
			});
		}
    }

    @Override
	protected void onResume() {
		super.onResume();
		// 設定した用語のセット
		setFieldWords();
	}

    private void setFieldWords() {
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	// ビューの取得
    	TextView spellView    = (TextView)findViewById(R.id.word_spell);
    	TextView meaningView  = (TextView)findViewById(R.id.word_meaning);
    	TextView partView     = (TextView)findViewById(R.id.word_part);
    	TextView exenView     = (TextView)findViewById(R.id.word_example_en);
    	TextView exjaView     = (TextView)findViewById(R.id.word_example_ja);
    	TextView categoryView = (TextView)findViewById(R.id.word_category);
    	// 用語のセット
    	mWordSpell = pref.getString(Config.WORD_SPELL, getResources().getString(R.string.spell));
    	spellView.setText(mWordSpell);
    	meaningView.setText(pref.getString(Config.WORD_MEANING, getResources().getString(R.string.meaning)));
    	partView.setText(pref.getString(Config.WORD_PART, getResources().getString(R.string.part)));
    	exenView.setText(pref.getString(Config.WORD_EX_EN, getResources().getString(R.string.example_en)));
    	exjaView.setText(pref.getString(Config.WORD_EX_JA, getResources().getString(R.string.example_ja)));
    	categoryView.setText(pref.getString(Config.WORD_CATEGORY, getResources().getString(R.string.category)));
    }

    private boolean checkFormat() {
    	if(mSpellEdit.getText().toString().equals("")) {
    		Toast.makeText(getApplicationContext(), "Error: " + mWordSpell + " must not be blank.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	return true;
    }

	private void insertWord() {
		// 単語データベースに登録
		WordDao dao = new WordDao();
		dao.insertWord(getWord());
		// トーストを表示
		String msg = getResources().getString(R.string.word_registered);
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		// 入力フィールドクリア
		clearFields();
    }

	private void updateWord(int id) {
		// 単語データベースを更新
		WordDao dao = new WordDao();
		dao.updateWord(id, getWord(), MODIFY_PROJECTION);
		// トーストを表示
		String msg = getResources().getString(R.string.word_updated);
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		// 入力フィールドクリア
		clearFields();
		// 終了
		finish();
    }

	private WordEntity getWord() {
		WordEntity word = new WordEntity();
		// スペル
		word.setSpell(mSpellEdit.getText().toString());
		// 意味
		word.setMeaning(mMeaningEdit.getText().toString());
		// 品詞
		word.setPartId(mPartSpinner.getSelectedItemPosition());
		// 例文
		word.setExampleEn(mExampleEnEdit.getText().toString());
		// 和訳
		word.setExampleJa(mExampleJaEdit.getText().toString());
		// カテゴリ
		word.setCategoryId(mCategorySpinner.getSelectedItemPosition());

		return word;
	}

	private void clearFields() {
		mSpellEdit.setText("");
		mMeaningEdit.setText("");
		mPartSpinner.setSelection(0);
		mExampleEnEdit.setText("");
		mExampleJaEdit.setText("");
		mCategorySpinner.setSelection(0);
	}

	private void setFields(WordEntity word) {
		mSpellEdit.setText(word.getSpell());
		mMeaningEdit.setText(word.getMeaning());
		mPartSpinner.setSelection(word.getPartId());
		mExampleEnEdit.setText(word.getExampleEn());
		mExampleJaEdit.setText(word.getExampleJa());
		mCategorySpinner.setSelection(word.getCategoryId());
	}

//	private final InputFilter[] mEnglishFilter = {new EnglishFilter()};
//	private class EnglishFilter implements InputFilter {
//
//		// destのdstart～dendをsourceのstart～endで置き換えるときに呼ばれる
//		// 返り値は置き換える値（nullを指定するとそのまま）
//		public CharSequence filter(CharSequence source, int start, int end,
//				Spanned dest, int dstart, int dend) {
//			if(source.toString().matches("^[a-zA-Z\\s]+$")) {
//				return null;
//			} else {
//				return "";
//			}
//		}
//
//	}
}
