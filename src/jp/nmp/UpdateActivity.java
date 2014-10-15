package jp.nmp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import jp.nmp.R;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
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
 */
public final class UpdateActivity extends BaseActivity {

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
	 * EditText: expire date
	 */
	private Button itemExpire;
	
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
		itemExpire = (Button)findViewById(R.id.itemExpire);
		
		/* Get a selected item */
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			int i = bundle.getInt("item");
			
			/* Get an item from repository */
			Repository repo = Repository.getInstance();
			if (i < repo.list().size()) {
				Map<String, Object> item = repo.get(i);
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
		
		itemExpire.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectExpireDate();
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
			/* Save an item */
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
			/* Generate password */
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
		String expire = itemExpire.getText().toString();
		
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
		
		/* Parse expire date */
		Date expireDate = null;
		if (expire != null && !expire.equals(getString(R.string.default_date))) {
			try {
				DateFormat df = SimpleDateFormat.getDateInstance();
				expireDate = df.parse(expire);
			} catch (ParseException e) {
				makeToast(getString(R.string.dateformat_error, expire));
				return(false);
			}
		}
				
		/* Update items */
		Repository repo = Repository.getInstance();
		if (pos < 0) {		/* Add new one */
			repo.add(label, user, passwd, url, hint1, hint2, hint3, expireDate);
		} else {				/* Update exist */
			repo.put(pos, label, user, passwd, url, hint1, hint2, hint3, expireDate);
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
	private void setItemInfo(Map<String, Object> item) {
		/* Set item values */
		setValue(itemLabel, (String)item.get(Repository.LABEL));
		setValue(itemUser, (String)item.get(Repository.USER));
		setValue(itemPwd, (String)item.get(Repository.PASSWD));
		setValue(itemUrl, (String)item.get(Repository.URL));
		setValue(itemHint1,(String) item.get(Repository.HINT1));
		setValue(itemHint2,(String) item.get(Repository.HINT2));
		setValue(itemHint3, (String)item.get(Repository.HINT3));
		
		String expire = (String)item.get(Repository.EXPIRE);
		if (expire == null) {
			expire = getString(R.string.default_date);
		}
		setValue(itemExpire, expire);
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
	
	/**
	 * Select expire date.
	 */
	private void selectExpireDate() {
		/* Get current expire date */
		Calendar c = Calendar.getInstance();
		try {
			DateFormat df = SimpleDateFormat.getDateInstance();
			Date d = df.parse(itemExpire.getText().toString());
			c.setTime(d);
		} catch (ParseException e) {
			/* Set current date if it has no value */
			c.setTime(new Date());
		}
		
		/* Initiate a date picker dialog */
		int year = c.get(Calendar.YEAR); 
		int month = c.get(Calendar.MONTH); 
		int day = c.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				itemExpire.setText(getString(R.string.date_format, year, monthOfYear + 1, dayOfMonth));
			}
		}, year, month, day);
		
		/* Activate clear button */
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.clear_date), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				itemExpire.setText(R.string.default_date);
			}
		});
		
		dialog.show();
	}
}
