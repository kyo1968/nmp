package jp.nmp;

import jp.nmp.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;

/**
 * ダイアログクラス
 *
 * 各種ダイアログを表示する。
 */
public class DialogBuilder extends Builder {
	
	/**
	 * コンストラクタ
	 * 
	 * @param context コンテキスト
	 */
	public DialogBuilder(Context context) {
		super(context);
	}
	
	/**
	 * エラーダイアログを表示する。
	 * 
	 * @param msg メッセージのリソースID
	 * @return エラーダイアログ
	 */
	public AlertDialog error(int msg) {
		return(setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error)
				.setMessage(msg)
				.setPositiveButton(android.R.string.ok, null)
				.setCancelable(true)
				.show());
	}
	
	/**
	 * 情報ダイアログを表示する。
	 * 
	 * @param msg メッセージのリソースID
	 * @return 情報ダイアログ
	 */
	public AlertDialog info(int title, int msg) {
		return(setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.info)
		.setMessage(msg)
		.setPositiveButton(android.R.string.ok, null)
		.setCancelable(true)
		.show());
	}
}
