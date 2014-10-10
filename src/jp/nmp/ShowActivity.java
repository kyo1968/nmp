package jp.nmp;

import java.util.Map;

import jp.nmp.R;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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
	private EditText itemLabel;

	/**
	 * TextView: user account
	 */
	private EditText itemUser;
	
	/**
	 * TextView: password
	 */
	private EditText itemPwd;

	/**
	 * TextView: URL
	 */
	private EditText itemUrl;

	/**
	 * TextView: 1st hint
	 */
	private EditText itemHint1;

	/**
	 * TextView: 2nd hint
	 */
	private EditText itemHint2;

	/**
	 * TextView: 3rd hintl
	 */
	private EditText itemHint3;
	
	/**
	 * Item position.
	 */
	private int pos = -1;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		
		/* Set views */
		itemLabel = (EditText)findViewById(R.id.itemLabel);
		itemUser = (EditText)findViewById(R.id.itemUser);
		itemPwd = (EditText)findViewById(R.id.itemPwd);
		itemUrl = (EditText)findViewById(R.id.itemUrl);
		itemHint1 = (EditText)findViewById(R.id.itemHint1);
		itemHint2 = (EditText)findViewById(R.id.itemHint2);
		itemHint3 = (EditText)findViewById(R.id.itemHint3);
		
		/* Get a selected item */
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			pos = bundle.getInt("item");
			
			/* Get an item from repository */
			Repository repo = Repository.getInstance();
			if (pos >= 0 && pos < repo.list().size()) {
				Map<String, String> item = repo.get(pos);
				setItemInfo(item);
			}
			
			/* Mask password item */
			final String pwd = itemPwd.getText().toString();
			itemPwd.setText(R.string.masked_string);
			
			/* Register check box listener */
			CheckBox maskPwd = (CheckBox)findViewById(R.id.maskPwd);
			maskPwd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					/* Invert masked and unmasked password */
					CheckBox cb = (CheckBox)v;
					if (cb.isChecked()) {
						itemPwd.setText(R.string.masked_string);
					} else {
						itemPwd.setText(pwd);
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
			Map<String, String> it= Repository.getInstance().get(pos);
			TextView added = (TextView)view.findViewById(R.id.addDate);
			TextView updated = (TextView)view.findViewById(R.id.updateDate);
			
			if (it != null) {
				String ad = it.get(Repository.ADDED);
				String ud = it.get(Repository.UPDATED);
				
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
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*
	 * Set items.
	 * 
	 * @param item item map.
	 */
	private void setItemInfo(Map<String, String> item) {
		/* Set item values */
		setValue(itemLabel, item.get(Repository.LABEL));
		setValue(itemUser, item.get(Repository.USER));
		setValue(itemPwd, item.get(Repository.PASSWD));
		setValue(itemUrl, item.get(Repository.URL));
		setValue(itemHint1, item.get(Repository.HINT1));
		setValue(itemHint2, item.get(Repository.HINT2));
		setValue(itemHint3, item.get(Repository.HINT3));
		
		/* Cancel  the key listener  */
		itemLabel.setKeyListener(null);
		itemUser.setKeyListener(null);
		itemPwd.setKeyListener(null);
		itemUrl.setKeyListener(null);
		itemHint1.setKeyListener(null);
		itemHint2.setKeyListener(null);
		itemHint3.setKeyListener(null);
	}
}
