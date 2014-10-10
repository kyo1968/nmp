package jp.nmp;

import java.util.Map;
import jp.nmp.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Update activity class.
 * 
 * @author kyo
 * @version 1.0
 */public final class UpdateActivity extends BaseActivity {

	/**
	 * Random password generator.
	 */
	private static final RandGenerator rand = new RandGenerator();
	
	/**
	 * Position of selected item.
	 */
	private int pos = -1;

	/**
	 * EditText: label
	 */
	private EditText itemLabel;

	/**
	 * EditText: user account
	 */
	private EditText itemUser;
	
	/**
	 * EditText: password
	 */
	private EditText itemPwd;
	
	/**
	 * EditText: URL
	 */
	private EditText itemUrl;
	
	/**
	 * EditText: 1st hint
	 */
	private EditText itemHint1;
	
	/**
	 * EditText: 2nd hint
	 */
	private EditText itemHint2;
	
	/**
	 * EditText: 3rd hint
	 */
	private EditText itemHint3;
	
	/**
	 * ImageButton: generate password
	 */
	private ImageButton genPwd;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_update);
		
		/* Set views */
		itemLabel = (EditText)findViewById(R.id.itemLabel);
		itemUser = (EditText)findViewById(R.id.itemUser);
		itemPwd = (EditText)findViewById(R.id.itemPwd);
		itemUrl = (EditText)findViewById(R.id.itemUrl);
		itemHint1 = (EditText)findViewById(R.id.itemHint1);
		itemHint2 = (EditText)findViewById(R.id.itemHint2);
		itemHint3 = (EditText)findViewById(R.id.itemHint3);
		genPwd = (ImageButton)findViewById(R.id.genPwd);
		
		/* Get a selected item */
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			int i = bundle.getInt("item");
			
			/* Get an item from repository */
			Repository repo = Repository.getInstance();
			if (i < repo.list().size()) {
				Map<String, String> item = repo.get(i);
				setItemInfo(item);
				pos = i;
			}
		}
		
		genPwd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				generatePassword();
			}
		});
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* Inflate the menu; this adds items to the action bar if it is present. */
		getMenuInflater().inflate(R.menu.update, menu);
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void onBackPressed() {
		/* Confirm to finish this activity if view is dirty */
		if (!isDirty()) {
			super.onBackPressed();
		} else {
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.title_update)
			.setMessage(R.string.confirm_discard)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_save:
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.title_update)
			.setMessage(R.string.confirm_update)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if (saveItem()) {
						finish();
					}
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
			return(true);
			
		case R.id.menu_genpwd:
			generatePassword();
			return (true);
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Save items to repository file.
	 * 
	 * @return true if it succeed.
	 */
	private boolean saveItem() {
		
		/* Get values from controls */
		String label = itemLabel.getText().toString();
		String user = itemUser.getText().toString();
		String passwd = itemPwd.getText().toString();
		String url = itemUrl.getText().toString();
		String hint1 = itemHint1.getText().toString();
		String hint2 = itemHint2.getText().toString();
		String hint3 = itemHint3.getText().toString();
		
		/* Check required values */
		if (label == null || label.length() == 0) {
			new DialogBuilder(this).error(R.string.require_label);
			return (false);
		}

		if (user == null || user.length() == 0) {
			new DialogBuilder(this).error(R.string.require_user);
			return (false);
		}

		if (passwd == null || passwd.length() == 0) {
			new DialogBuilder(this).error(R.string.require_password);
			return (false);
		}
				
		/* Update items */
		Repository repo = Repository.getInstance();
		if (pos < 0) {		/* Add new one */
			repo.add(label, user, passwd, url, hint1, hint2, hint3);
		} else {				/* Update exist */
			repo.put(pos, label, user, passwd, url, hint1, hint2, hint3);
		}
		
		/* Save repository */
		if (!saveRepository(repo, true)) {
			/* remove an added item if failed */
			if (pos < 0) {
				repo.remove(0);
			}
			return (false);
		}
		return (true);
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
	}

	/**
	 * Check dirty flags.
	 * 
	 * @return true if view is dirty.
	 */
	private boolean isDirty() {
		/* TODO: not implemented yet */
		return (true);
	}
	
	/**
	 * Generate password.
	 */
	private void generatePassword() {
		/* Initiate a dialog */
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.activity_genpwd, null);
		
		SeekBar seekBar = (SeekBar)view.findViewById(R.id.seekBar); 
		final TextView pwdLen = (TextView)view.findViewById(R.id.pwdLength);
		final EditText pwdEdit = (EditText)view.findViewById(R.id.itemPwd);
		
		/* Register password generation listener */
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				/* Generate password */
				int len = seekBar.getProgress();
				pwdEdit.setText(rand.generate(len));
				pwdLen.setText(String.valueOf(len));
			}
		});
		
		/* Initiate a dialog */
		AlertDialog.Builder dialog = new AlertDialog.Builder(this)
		//.setIcon(R.drawable.ic_launcher)
		.setTitle(R.string.title_genpwd)
		.setView(view)
		.setCancelable(true)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				/* Set password to a parent item */
				itemPwd.setText(pwdEdit.getText().toString());
			}
		})
		.setNegativeButton(android.R.string.cancel, null);
		dialog.show();
	}
}
