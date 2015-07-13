package jp.ac.tuat.sys.linh;

public class Config {

	private Config() {}

	// 設定情報にアクセスするキー
	public static final String TTS_CHECKED = "key_tts_checked";

	// 学習モードを表す定数
	public static final int MODE_QUESTION_3   = 0;
	public static final int MODE_QUESTION_4   = 1;
	public static final int MODE_MEANING_HIDE = 2;
	
	// 復習モードを表す定数
	public static final int NOT_ALL = 3;
	public static final int ALL = 4;

	// 初期値
	public static final String DEF_LEARNING_MODE = "1";
	public static final String DEF_LEARNING_NUM  = "5";
	public static final String DEF_REVISE_MODE   = "3";
	public static final String DEF_REVISE_MAX    = "15";
	public static final String DEF_RES_POP_TIME  = "600";
	public static final String DEF_SPEECH_LOCALE = "US";
	public static final String DEF_SPEECH_PITCH  = "1.0";
	public static final String DEF_SPEECH_RATE   = "1.0";
	public static final String DEF_DIALOG_MODE   = "1";

	// ロケール
	public static final String LOC_US = "US";
	public static final String LOC_EN = "ENGLISH";
	public static final String LOC_FR = "FRENCH";
	public static final String LOC_GA = "GARMAN";
	public static final String LOC_IT = "ITALIAN";
	public static final String DEF_LOCALE = LOC_US;

	// プリファレンスアクセスのためのキー
	public static final String LEARNING_MODE  = "key_learning_mode";
	public static final String LEARNING_NUM   = "key_learning_num";
	public static final String REVISE_MODE   = "key_revise_mode";
	public static final String REVISE_MAX     = "key_revise_max";
	public static final String RES_POP_TIME   = "key_result_pop_time";
	public static final String LEARNING_CLEAR = "key_learning_data_clear";
	public static final String DIALOG_MODE    = "key_dialog_mode";

	public static final String DB_CREATE = "key_db_create";
	public static final String DB_USING  = "key_db_using";
	public static final String DB_DELETE = "key_db_delete";

	public static final String SOUND_USE     = "key_sound_use";
	public static final String TTS_USE       = "key_tts_use";
	public static final String SPEECH_LOCALE = "key_speech_locale";
	public static final String SPEECH_PITCH  = "key_speech_pitch";
	public static final String SPEECH_RATE   = "key_speech_rate";
	public static final String TTS_INSTALL   = "key_tts_install";

	public static final String WORD_SPELL    = "key_word_spell";
	public static final String WORD_MEANING  = "key_word_meaning";
	public static final String WORD_PART     = "key_word_part";
	public static final String WORD_EX_EN    = "key_word_example_en";
	public static final String WORD_EX_JA    = "key_word_example_ja";
	public static final String WORD_CATEGORY = "key_word_category";
	public static final String WORD_SET_DEF  = "key_word_set_default";

	// デバッグ用
	public static final String PASSED_DAYS = "key_passed_days";
}
