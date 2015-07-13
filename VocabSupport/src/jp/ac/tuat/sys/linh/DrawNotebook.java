package jp.ac.tuat.sys.linh;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.view.MotionEvent;

// 描画クラスの定義
@SuppressLint("SimpleDateFormat")
public class DrawNotebook extends android.view.View {
	private int bmpx, bmpy;
	private Bitmap bmp = null;
	private Canvas bmpCanvas;
	private Point oldpos = new Point(-1,-1);
	private static boolean ondraw = true;
	private static boolean ispen = true;
	
	protected DrawNotebook(Context c) {
		super(c);
		setFocusable(true);
	}
	protected void clearDrawList() {
		bmpCanvas.drawColor(Color.WHITE);
		//描画属性を設定
		 Paint paint = new Paint();
		 paint.setColor(Color.BLUE); //罫線の色は青
		 paint.setStyle(Paint.Style.FILL); //実線
		 paint.setStrokeWidth(4);
		 paint.setAntiAlias(true);
		 // 罫線を描画
		 for (int i=80; i<bmpy; i+=80)
			 bmpCanvas.drawLine(0, i, bmpx, i, paint);
		invalidate();
	}
	// 画面サイズが変更された時
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w,h,oldw,oldh);
		if (bmp == null) {
			bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
		}
		bmpCanvas = new Canvas(bmp);
		bmpCanvas.drawColor(Color.WHITE);
		this.loadFromCacheFile();
	}

	// 描画イベント
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(bmp, 0, 0, null);
		bmpx = bmp.getWidth();
		bmpy = bmp.getHeight();
		if(ondraw){
			//描画属性を設定
			 Paint paint = new Paint();
			 paint.setColor(Color.BLUE); //罫線の色は青
			 paint.setStyle(Paint.Style.FILL); //実線
			 paint.setStrokeWidth(4);
			 paint.setAntiAlias(true);
			 // 罫線を描画
			 for (int i=80;i<bmpy;i+=80)
				 bmpCanvas.drawLine(0, i, bmpx, i, paint);
			 ondraw = false;
		}
		if(!ispen){
			//描画属性を設定
			 Paint paint = new Paint();
			 paint.setColor(Color.BLUE); //罫線の色は青
			 paint.setStyle(Paint.Style.FILL); //実線
			 paint.setStrokeWidth(4);
			 paint.setAntiAlias(true);
			 // 罫線を描画
			 for (int i=80;i<bmpy;i+=80)
				 bmpCanvas.drawLine(0, i, bmpx, i, paint);
		}
	}
	// タッチイベント
	public boolean onTouchEvent(MotionEvent event) {
		 // 描画位置の確認
		 Point cur = new Point((int)event.getX(), (int)event.getY());
		 if (oldpos.x < 0) { oldpos = cur; }
		 // 描画属性を設定
		 Paint paint = new Paint();
		 if(ispen){
			 paint.setColor(Color.BLACK);
			 paint.setStrokeWidth(4);
		}
		 else{
			 paint.setColor(Color.WHITE);
			 paint.setStrokeWidth(20);
		 }
		 paint.setStyle(Paint.Style.FILL);

		 paint.setAntiAlias(true);
		 // 線を描画
		 bmpCanvas.drawLine(oldpos.x, oldpos.y, cur.x, cur.y, paint);
		 oldpos = cur;
		 // 指を持ち上げたら座標をリセット
		 if (event.getAction() == MotionEvent.ACTION_UP) {
			 oldpos = new Point(-1, -1);
		 }
		 invalidate();
		 return true;
	 }
	// 保存パスの取得処理
	private File getSavePath() {
		File path;
		if (hasSDCard()) { // SDカードがあればそこを保存ディレクトリとする
			path = new File(getSDCardPath() + "/MyPhoto/");
			path.mkdir();
		} else {
			path = Environment.getDataDirectory();
		}
		return path;
	}
	private String getCacheFilename() {
		File f = getSavePath();
		return f.getAbsolutePath() + "/cache.png";
	}
	private String getSaveFilename() {
		File path = getSavePath();
		Date d = new Date();
		String fname = path.getAbsolutePath() + "/";
		SimpleDateFormat fileName = new SimpleDateFormat("yyyyMMdd_HHmmss");
		fname += String.format(fileName.format(d)+".png");
		return fname;
	}
	private boolean loadFromFile(String filename) {
		try {
			File f = new File(filename);
			if (!f.exists()) { return false; }
			Bitmap tmp = BitmapFactory.decodeFile(filename);
			bmpCanvas.drawColor(Color.WHITE);
			bmpCanvas.drawBitmap(tmp, 0, 0, null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	private void loadFromCacheFile() {
		this.loadFromFile(getCacheFilename());
	}
	protected void saveToCacheFile() {
		this.saveToFile(getCacheFilename());
	}
	protected void saveToFile() {
		this.saveToFile(getSaveFilename());
	}
	private void saveToFile(String filename) { // 画像をファイルに書き込む
		try {
			FileOutputStream out = new FileOutputStream(filename);
			bmp.compress(CompressFormat.PNG, 100, out);
			out.flush(); out.close();
		} catch(Exception e) {}
	}
	// ペンと消しゴムを入れ替える。
	protected void seteraser(boolean mode) {
		ispen = mode;
	}
	// SDカードの確認処理
	private boolean hasSDCard() { // SDカードがあるか?
		String status = Environment.getExternalStorageState();
		return status.equals(Environment.MEDIA_MOUNTED);
	}
	private String getSDCardPath() {
		File path = Environment.getExternalStorageDirectory();
		return path.getAbsolutePath();
	}	
}