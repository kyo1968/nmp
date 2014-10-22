package jp.nmp;

import java.util.Map;

import jp.nmp.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 *  Show Activity class.
 *  
 * @author kyo
 * @version 1.0
 */
public final class ShowActivity extends BaseActivity {
	
	/**
	 * TextView: label
	 */
	private TextView itemLabel;

	/**
	 * TextView: user account
	 */
	private TextView itemUser;
	
	/**
	 * TextView: password
	 */
	private TextView itemPwd;

	/**
	 * TextView: URL
	 */
	private TextView itemUrl;

	/**
	 * TextView: 1st hint
	 */
	private TextView itemHint1;

	/**
	 * TextView: 2nd hint
	 */
	private TextView itemHint2;

	/**
	 * TextView: 3rd hint
	 */
	private TextView itemHint3;
	
	/**
	 * TextView: expire date
	 */
	private TextView itemExpire;
	
	/**
	 * Item position.
	 */
	private int pos = -1;
	
	/**
	 * Non-masked password.
	 */
	private String nonMasked;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		
		/* Set views */
		itemLabel = (TextView)findViewById(R.id.itemLabel);
		itemUser = (TextView)findViewById(R.id.itemUser);
		itemPwd = (TextView)findViewById(R.id.itemPwd);
		itemUrl = (TextView)findViewById(R.id.itemUrl);
		itemHint1 = (TextView)findViewById(R.id.itemHint1);
		itemHint2 = (TextView)findViewById(R.id.itemHint2);
		itemHint3 = (TextView)findViewById(R.id.itemHint3);
		itemExpire = (TextView)findViewById(R.id.itemExpire);
		
		/* Get a selected item */
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			pos = bundle.getInt("item");
			
			/* Get an item from repository */
			Repository repo = Repository.getInstance();
			if (pos >= 0 && pos < repo.list().size()) {
				Map<String, Object> item = repo.get(pos);
				setItemInfo(item);
			}
			
			/* Mask password item */
			nonMasked = itemPwd.getText().toString();
			itemPwd.setText(R.string.masked_string);
			
			/* Register check box listener */
			ToggleButton maskPwd = (ToggleButton)findViewById(R.id.maskPwd);
			maskPwd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					/* Invert masked and unmasked password */
					ToggleButton c = (ToggleButton)v;
					if (c.isChecked()) {
						itemPwd.setText(R.string.masked_string);
					} else {
						itemPwd.setText(nonMasked);
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* Inflate the menu; this adds items to the action bar if it is present. */
		getMenuInflater().inflate(R.menu.show, menu);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (!pref.getBoolean(getString(R.string.pref_key_copy_password), false)) {
			menu.findItem(R.id.menu_copypwd).setEnabled(false);
		}
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_property:
			/* Initiate secret phrase dialog */
			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.activity_property, null);
			
			/* Set time stamp */
			Map<String, Object> it= Repository.getInstance().get(pos);
			TextView added = (TextView)view.findViewById(R.id.addDate);
			TextView updated = (TextView)view.findViewById(R.id.updateDate);
			
			if (it != null) {
				String ad = (String)it.get(Repository.ADDED);
				String ud = (String)it.get(Repository.UPDATED);
				
				if (ad != null)
					added.setText(ad);
				
				if (ud != null) 
					updated.setText(ud);
			}
			
			/* Initiate secret phrase dialog */
			new AlertDialog.Builder(this)
			//.setIcon(R.drawable.ic_launcher)
			.setTitle(R.string.title_property)
			.setView(view)
			.setCancelable(true)
			.setPositiveButton(android.R.string.ok, null)
			.show();
			return(true);
			
		case R.id.menu_copypwd:
			/* Copy password to the clipboard */
			copyText(nonMasked);
			
			/* Display clipboard notification */
			Toast.makeText(this, R.string.copy_pwd_to_clipboard, Toast.LENGTH_LONG).show(); 
			return (true);
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*
	 * Set items.
	 * 
	 * @param item item map.
	 */
	private void setItemInfo(Map<String, Object> item) {
		/* Set item values */
		setValue(itemLabel, (String)item.get(Repository.LABEL));
		setValue(itemUser, (String)item.get(Repository.USER));
		setValue(itemPwd, (String)item.get(Repository.PASSWD));
		setValue(itemUrl, (String)item.get(Repository.URL));
		setValue(itemHint1, (String)item.get(Repository.HINT1));
		setValue(itemHint2, (String)item.get(Repository.HINT2));
		setValue(itemHint3, (String)item.get(Repository.HINT3));
		
		String expire = (String)item.get(Repository.EXPIRE);
		if (expire == null) {
			expire = getString(R.string.default_date);
		}
		setValue(itemExpire, expire);
	}
	
	/**
	 * Copy text to clipboard.
	 * 
	 * @param text text.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void copyText(String text) {
		/* Copy password to the clipboard */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			/* This part is written with new API later than HONEYCOMB */
			android.content.ClipboardManager c = (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
			android.content.ClipData clipData = android.content.ClipData.newPlainText("text label", text);
			c.setPrimaryClip(clipData);
		} else {
			/* This part is written with deprecated API earlier than HONEYCOMB */
			android.text.ClipboardManager c = (android.text.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
			c.setText(text);
		}
	}
}
