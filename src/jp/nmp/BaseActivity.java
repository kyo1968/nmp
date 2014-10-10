package jp.nmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import jp.nmp.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Base activity class.
 * 
 * @author kyo
 * @version 1.0
 */
public class BaseActivity extends Activity {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		
//		/* Force to enable overflow menu, even if device have hardware menu key  */
//		try {
//			ViewConfiguration conf = ViewConfiguration.get(this);
//			Field field = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
//			if (field != null) {
//				field.setAccessible(true);
//				field.setBoolean(conf, false);
//			}
//		} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * Load repository from a file.
	 * 
	 * @param repo repository
	 * @return return true if it succeed.
	 */
	protected boolean openRepository(Repository repo) {
		FileInputStream is = null;
		
		/* Check secret phrase */
		String pass = repo.getPassword();
		if (pass == null || pass.trim().isEmpty()) {
			makeToast(R.string.no_secret_phrase);
			return (false);
		}
		
		try {
			/* Load a date file */
			is = openFileInput(Repository.FILENAME);
			
			/* Decrypt items */
			if (!repo.load(is)) {
				/* Failed to load items */
				makeToast(R.string.incorrect_secret_phrase);
				return (false);
			}
			return (true);
			
		} catch (FileNotFoundException e) {
			/* Display an error if  data file is not found */
			makeToast(R.string.create_repository_file);
			return (true);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				/* Ignore close error */
				makeToast(R.string.close_error);
			}
		}
	}

	/**
	 * Save repository to a file.
	 * 
	 * @param repo repository
	 * @param backup backup flag
	 * @return true if it succeed.
	 */
	protected boolean saveRepository(Repository repo, boolean backup) {
	
		/* Create backup file */
		if (backup) {
			backupRepository(repo);
		}
		
		FileOutputStream os = null;
		try {
			/* Write items to a file */
			os = openFileOutput(Repository.FILENAME);
			if (!repo.save(os)) {
				/* Failed */
				makeToast(R.string.save_error);
				return (false);
			}
			return (true);
		} catch (FileNotFoundException e) {
			/* Repository file not found */
			makeToast(R.string.no_repository_file);
			return (false);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				/* Ignore close error */
				makeToast(R.string.close_error);
			}
		}
	}
	
	/**
	 * Clear repository file.
	 * 
	 * @param repo repository
	 * @return return true if it succeed.
	 */
	public boolean clearRepository(Repository repo) {
		/* Rename current file to backup */
		backupRepository(repo);
		repo.clear();
		return (true);
	}
	
	/**
	 * Check if repository file exist.
	 * 
	 * @return true if the file exist.
	 */
	public boolean existRepositoryFile() {
		File file = null;

		/* Check file location */
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean(this.getString(R.string.pref_key_external_storage), false)) {
			/* External storage */
			File dir = getExternalFilesDir(null);
			file = new File(dir, Repository.FILENAME);
		} else {
			/* Internal storage */
			file = getFileStreamPath(Repository.FILENAME);
		}
		
		/* Check accessibility */
		if (file != null &&	file.isFile()) {
			return (true);
		}
		return (false);
	}
	
	/**
	 * Import data from external file.
	 * 
	 * @return true if it succeed.
	 */
	public boolean importRepository() {
		try {
			/* Open import file */
			FileInputStream is;
			try {
				is = openFileInput(Repository.IMPORT_FILENAME);
			} catch (FileNotFoundException e) {
				makeToast(R.string.no_import_file);
				return (false);
			}

			/* Import data from file */
			if (is != null) {
				Repository repo = Repository.getInstance();
				repo.list().clear();
				repo.importData(is);
				Toast.makeText(this, R.string.import_success, Toast.LENGTH_LONG).show();
				return (true);
			}
			
		} catch (IOException e) {
			makeToast(R.string.import_error);
		}
		return(false);
	}
	
	/**
	 * Set item value to a view.
	 * 
	 * @param v view
	 * @param value item value
	 */
	public void setValue(TextView v, String value) {
		if (value != null) {
			v.setText(value);
		}
	}

	/**
	 * Get absolute path.
	 * 
	 * @param fn file name
	 * @return absolute path
	 */
	public File getAbsolutePath(String fn) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean(this.getString(R.string.pref_key_external_storage), false)) {
			/* External storage */
			File dir = getExternalFilesDir(null);
			if (dir != null & dir.isDirectory()) {
				return (new File(dir, fn));
			}
		}
		return (getFileStreamPath(fn));
	}
	
	/**
	 * Open repository file for input.
	 * 
	 * @param fn file name
	 * @return file handler
	 * @throws FileNotFoundException no repository file found.
	 */
	public FileInputStream openFileInput(String fn) throws FileNotFoundException {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean(this.getString(R.string.pref_key_external_storage), false)) {
			/* External storage */
			File dir = getExternalFilesDir(null);
			if (dir != null) {
				File file = new File(dir, fn);
				return (new FileInputStream(file));
			}
			makeToast(R.string.no_external_storage);
		}
		
		/* Internal storage */
		return (super.openFileInput(fn));
	}
	
	/**
	 * Open repository file for output.
	 * 
	 * @param fn file name
	 * @return file handler
	 * @throws FileNotFoundException no parent directory found.
	 */
	public FileOutputStream openFileOutput(String fn) throws FileNotFoundException {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean(this.getString(R.string.pref_key_external_storage), false)) {
			/* External storage */
			File dir = getExternalFilesDir(null);
			if (dir != null) {
				if (dir.isDirectory() || dir.mkdirs()) {
					File file = new File(dir, fn);
					return (new FileOutputStream(file));
				}
				makeToast(R.string.appdir_error);
			} else {
				makeToast(R.string.no_external_storage);
			}
		}
		
		/* Internal storage */
		return (super.openFileOutput(fn, Context.MODE_PRIVATE));
	}
	
	/**
	 * Create backup file.
	 * 
	 * @param repo repository
	 */
	private void backupRepository(Repository repo) {
		File from = null;
		File to = null;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean(this.getString(R.string.pref_key_external_storage), false)) {
			/* External storage */
			File dir = getExternalFilesDir(null);
			if (dir != null) {
				from = new File(dir, Repository.FILENAME);
				to = new File(dir, Repository.BACKUP_FILENAME);
			}
			
		} else {
			/* Internal storage */
			from = getFileStreamPath(Repository.FILENAME);
			to = getFileStreamPath(Repository.BACKUP_FILENAME);
		}
		
		if (from != null && to != null) {
			repo.backup(from, to);	
		}
	}
	
	/**
	 * Display toast massage.
	 * 
	 * @param resource message resource
	 */
	protected void makeToast(int resource) {
		Toast.makeText(this, resource, Toast.LENGTH_SHORT).show();
	}
}