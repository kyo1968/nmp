package jp.nmp;

import java.util.Map;

import jp.nmp.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Main activity class.
 *  
 * @author kyo
 * @version 1.0
  */
public final class MainActivity extends BaseActivity {
	
	/**
	 * Exit application if back button twice pressed.
	 */
	public boolean goTofinish = false;
	
	/**
	 * A list-view adapter.
	 */
	private SimpleAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/* List-view settings */
		Repository repo = Repository.getInstance();
		adapter = new SimpleAdapter(this, repo.list(), R.layout.listview_layout,
				new String[] {Repository.LABEL, Repository.HINT1}, 
				new int[] {R.id.text1, R.id.text2});
		
		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(MainActivity.this, ShowActivity.class);
				intent.putExtra("item", arg2);
				startActivity(intent);
			}
		});
		lv.setHeaderDividersEnabled(true);
		lv.setAdapter(adapter);
		registerForContextMenu(lv);

		/* Enter exiting secret phrase or new phrase */
		enterPhrase();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBackPressed() {
		/* Exit if back button twice pressed */
		if (goTofinish) {
			goTofinish = false;
			
			/* Save repository if secret phrase is specified */
			Repository repo = Repository.getInstance();
			if (repo.getPassword() != null) {
				saveRepository(repo, false);
			}
			
			super.onBackPressed();		// Calling default callback to finish
		} else {
			makeToast(R.string.exit_twice_back);
			goTofinish = true;
			
			/* Cancel to press back after 2 second */
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					goTofinish = false;
				}
			}, 2000);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Repository.free();	/* free the repository */
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_add:	/* Add new */
			startActivity(new Intent(this, UpdateActivity.class));
			return (true);
			
		case R.id.menu_phrase: /* Change secret phrase */
			newPhrase();
			return (true);
			
		case R.id.menu_clear:	/* Clear secret phrase */
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.title_clear)
			.setMessage(R.string.confirm_clear)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Repository repo = Repository.getInstance();
					clearRepository(repo);
					adapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
			return(true);
	
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return (true);
			
		case R.id.menu_import:
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.title_import)
			.setMessage(getString(R.string.confirm_import, getAbsolutePath(Repository.IMPORT_FILENAME)))
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					importRepository();
					adapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
			return (true);

		default:
			return (super.onOptionsItemSelected(item));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.select, menu);
		menu.setHeaderTitle(R.string.title_select);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Repository repo = Repository.getInstance();
		final int pos = info.position;

		switch(item.getItemId()) {
		case R.id.menu_launch:	/* Launch web site */
			if (pos < repo.list().size()) {
				Map<String, String> it = repo.get(pos);
				launchUrl(it.get(Repository.URL));
			}
			return true;
			
		case R.id.menu_up:
			if (pos < repo.list().size()) {
				repo.move(pos, pos - 1);
				adapter.notifyDataSetChanged();
			}			
			return true;

		case R.id.menu_down:
			if (pos < repo.list().size()) {
				repo.move(pos, pos + 1);
				adapter.notifyDataSetChanged();
			}			
			return true;

		case R.id.menu_edit:		/* Edit item */
			Intent intent = new Intent(this, UpdateActivity.class);
			intent.putExtra("item", pos);
			startActivity(intent);
			return true;
			
		case R.id.menu_delete:	/* Delete item */
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.title_delete)
			.setMessage(R.string.confirm_delete)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					removeItem(pos);
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
			return true;
			
		default:
			return (super.onContextItemSelected(item));
		}
	}
	
	/**
	 * Show secret phrase dialog.
	 */
	private void enterPhrase() {
		/* Set existing secret phrase */
		if (existRepositoryFile()) {
			/* Initiate secret phrase dialog */
			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.activity_phrase, null);
			final EditText pwd = (EditText) view.findViewById(R.id.secretPhrase);
			
			/* Initiate secret phrase dialog */
			new AlertDialog.Builder(this)
			//.setIcon(R.drawable.ic_launcher)
			.setTitle(R.string.enter_phrase)
			.setView(view)
			.setCancelable(true)
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			})
			.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					/* Decrypt repository */
					String pass = pwd.getText().toString();
					Repository repo = Repository.getInstance();
					repo.setPassword(pass);
					
					/* Load items */
					if (!openRepository(repo)) {
						finish();
					}
					
					/* Update item list-view */
					adapter.notifyDataSetChanged();
				}
			})
			.show();
		} else {
			/* Set new secret phrase */
			newPhrase2();
		}
	}

	/**
	 * Enter new password.
	 */
	private void newPhrase() {
		/* Initiate secret phrase dialog */
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.activity_new_phrase, null);
		
		final EditText new1 = (EditText) view.findViewById(R.id.newPhrase1);
		final EditText new2 = (EditText) view.findViewById(R.id.newPhrase2);
		final EditText old = (EditText) view.findViewById(R.id.oldPhrase);
		
		/* Initiate secret phrase dialog */
		AlertDialog.Builder dialog = new AlertDialog.Builder(this)
		//.setIcon(R.drawable.ic_launcher)
		.setTitle(R.string.enter_phrase)
		.setView(view)
		.setCancelable(true)
		.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				/* Get secret phrase */
				Repository repo = Repository.getInstance();
				String cp = repo.getPassword(); 
				String np1 = new1.getText().toString();
				String np2 = new2.getText().toString();
				String op= old.getText().toString();
				
				if (cp == null || !cp.equals(op)) {
					/* Check current secret phrase */
					makeToast(R.string.unmatched_old_phrase);
				} else if (np1 == null || np2 == null || np1.trim().isEmpty() || !np1.equals(np2)) {
					/* Check new secret phrase */
					makeToast(R.string.unmatched_new_phrase);
				} else {
					/* Set new secret phrase to a repository */
					repo.setPassword(np2);
					makeToast(R.string.apply_new_phrase);
					
					/* save repository with new secret phrase */
					saveRepository(repo, true);
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, null);
		dialog.show();
	}

	/**
	 * Enter new password without old password.
	 */
	private void newPhrase2() {
		/* Initiate secret phrase dialog */
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.activity_new_phrase2, null);
		
		final EditText new1 = (EditText) view.findViewById(R.id.newPhrase1);
		final EditText new2 = (EditText) view.findViewById(R.id.newPhrase2);
		
		/* Initiate secret phrase dialog */
		AlertDialog.Builder dialog = new AlertDialog.Builder(this)
		//.setIcon(R.drawable.ic_launcher)
		.setTitle(R.string.enter_phrase)
		.setView(view)
		.setCancelable(true)
		.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		})
		.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				/* Get secret phrase */
				Repository repo = Repository.getInstance();
				String np1 = new1.getText().toString();
				String np2 = new2.getText().toString();
				
				if (np1 == null || np2 == null || np1.trim().isEmpty() || !np1.equals(np2)) {
					/* Check new secret phrase */
					makeToast(R.string.unmatched_new_phrase);
					finish();
				} else {
					/* Set new secret phrase to a repository */
					repo.setPassword(np2);
					makeToast(R.string.apply_new_phrase);
				}
			}
		});
		dialog.show();
	}

	/**
	 * Delete an item.
	 * 
	 * @param pos position of deleted item.
	 */
	private void removeItem(int pos) {
		/* Remove item */
		Repository repo = Repository.getInstance();
		repo.remove(pos);
		
		/* Save repository */
		if (saveRepository(repo, true)) {
			adapter.notifyDataSetChanged();	/* Update item list-view */
		}
	}
	
	/**
	 * Launch website.
	 * 
	 * @param url URL
	 */
	private void launchUrl(String url) {
		if (url == null || url.trim().isEmpty()) {
			makeToast(R.string.no_available_website);
		} else {
			/* Add protocol string */
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "http://" + url;
			}
			/* Launch web browser */
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
	}
	
}
