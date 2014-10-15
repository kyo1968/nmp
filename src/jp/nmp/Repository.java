package jp.nmp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Repository class.
 * 
 * @author kyo
 * @version 1.0
 */
public final class Repository {
	
	/**
	 * Repository file.
	 */
	public static final String FILENAME = "savefile.dat";
	
	/**
	 * Backup file.
	 */
	public static final String BACKUP_FILENAME = "savefile.dat.bak";
	
	/**
	 * Import file.
	 */
	public static final String IMPORT_FILENAME = "import.dat";
	
	/**
	 * Item key: id
	 */
	public static final String ID = "id";
	
	/**
	 * Item key: label
	 */
	public static final String LABEL = "label";
	
	/**
	 * Item key: user account
	 */
	public static final String USER = "user";
	
	/**
	 * Item key: password
	 */
	public static final String PASSWD = "passwd";

	/**
	 * Item key: URL
	 */
	public static final String URL = "url";

	/**
	 * Item key: hint 1
	 */
	public static final String HINT1 = "hint1";
	
	/**
	 * Item key: hint 2
	 */
	public static final String HINT2 = "hint2";
	
	/**
	 * Item key: hint 3
	 */
	public static final String HINT3 = "hint3";
	
	/**
	 * Item key: added
	 */
	public static final String ADDED = "added";

	/**
	 * Item key: updated 
	 */
	public static final String UPDATED = "updated";

	/**
	 * Item key: expire date
	 */
	public static final String EXPIRE = "expire";
	
	/**
	 * Item list.
	 */
	private List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
	
	/**
	 * Date formatter.
	 */
	private static final DateFormat df = SimpleDateFormat.getInstance();

	/**
	 * Date formatter.
	 */
	private static final DateFormat df2 = SimpleDateFormat.getDateInstance();

	/**
	 * Secret phrase.
	 */
	private String passWord = null;
	
	/**
	 * Repository instance.
	 */
	private static Repository me = null;
	
	/**
	 * Constructor.
	 */
	private Repository() {
	}
	
	/**
	 * Get repository instance.
	 * 
	 * @return instance.
	 */
	public static final Repository getInstance() {
		if(me == null) {
			me = new Repository();
		}
		return (me);
	}
	
	/**
	 * Free current repository.
	 */
	public static final void free() {
		if (me != null) {
			me.clear();
			me = null;
		}
	}
	
	/**
	 * Clear item list.
	 */
	public void clear () {
		passWord = null;
		itemList.clear();
	}
	
	/**
	 * Get item list.
	 * 
	 * @return item list.
	 */
	public List<Map<String, Object>> list() {
		return (itemList);
	}

	/**
	 * Add an item.
	 * 
	 * @param label label
	 * @param user user account
	 * @param passwd password
	 * @param url URL
	 * @param hint1 1st hint
	 * @param hint2 2nd hint
	 * @param hint3 3rd hint
	 * @param expire expire date
	 */
	public void add(String label, String user, String passwd, String url, String hint1, String hint2, String hint3, Date expire) {
		Map<String, Object> item = new HashMap<String, Object>();
		
		set(item, LABEL, label);
		set(item, USER, user);
		set(item, PASSWD, passwd);
		set(item, URL, url);
		set(item, HINT1, hint1);
		set(item, HINT2, hint2);
		set(item, HINT3, hint3);
		set(item, ADDED, df.format(new Date()));
		if (expire != null) {
			set(item, EXPIRE, df2.format(expire));
		}
		
		// add item to the head.
		if(itemList.size() == 0) {
			itemList.add(item);
		} else {
			itemList.add(0,  item);
		}
	}

	/**
	 * Get an item.
	 * 
	 * @param pos position
	 */
	public Map<String, Object> get(int pos) {
		return (itemList.get(pos));
	}
	
	/**
	 * Set an item.
	 * 
	 * @param pos position
	 * @param label label
	 * @param user user account
	 * @param passwd password
	 * @param url URL
	 * @param hint1 1st hint
	 * @param hint2 2nd hint
	 * @param hint3 3rd hint
	 * @param expire expire date
	 */
	public void put(int pos, String label, String user, String passwd, String url, String hint1, String hint2, String hint3, Date expire) {
		/* Get an item */
		Map<String, Object> item = get(pos);
		
		/* Set new values */
		if (item != null) {
			set(item, LABEL, label);
			set(item, USER, user);
			set(item, PASSWD, passwd);
			set(item, URL, url);
			set(item, HINT1, hint1);
			set(item, HINT2, hint2);
			set(item, HINT3, hint3);
			set(item, UPDATED, df.format(new Date()));
			
			if (expire != null) {
				set(item, EXPIRE, df2.format(expire));
			} else {
				item.remove(EXPIRE);
			}
		}
	}	

	/**
	 * Remove an item.
	 * 
	 * @param pos position
	 */
	public void remove(int pos) {
		list().remove(pos);
	}
	
	/**
	 * Move an item.
	 * 
	 * @param from current position
	 * @param to moved position
	 */
	public void move(int from, int to) {
		Map<String, Object> o = get(from);
		if (o != null) {
			
			int size = list().size();
			if (to > size - 1) {
				to = size - 1;
			}
			
			if (to < 0) {
				to = 0;
			}
			
			remove(from);
			list().add(to,  o);
		}
	}		
	
	/**
	 * Set value to an item.
	 * 
	 * @param item item
	 * @param key key of item
	 * @param value value of item 
	 */
	private void set(Map<String, Object>item, String key, String value) {
		//if (value != null && value.length() > 0) {
		if (value != null) {
			item.put(key, value);
		}
	}
	
	/**
	 * Load repository from input stream.
	 * 
	 * @param is input stream
	 * @return true if it succeed.
	 */
	public boolean load(InputStream is) {
		/* Failed if no password is specified*/
		if (passWord == null) {
			return (false);
		}
		
		/* Load items by specified password */
		return (load(passWord, is));
	}
	
	/**
	 * Load repository with password.
	 * 
	 * @param sk secret phrase
	 * @param is input stream
	 * @return true if it succeed.
	 */
	private boolean load(String sk, InputStream is) {
		
		itemList.clear();
		ByteArrayOutputStream os = null;
		try {
			/* Load data to binary array */
			int len;
			byte [] buf = new byte [1024];
			os = new ByteArrayOutputStream();
			
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
			os.flush();
			
			/* Decrypt them with specified password.*/
			Crypto crypt = new Crypto();
			byte [] data = os.toByteArray();
			if (sk != null) {
				data = crypt.decrypt(sk.getBytes(), data);
				
				/* Check result */
				if (data == null) {
					return (false);
				}
			}
			
			/* Convert them from binary to item */
			fromBinary(data);
			return (true);
			
		} catch (IOException e) {
			/* Failed to load */
			itemList.clear();
			return (false);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * Save repository.
	 * 
	 * @param os output stream
	 * @return true if it succeed.
	 */
	public boolean save(OutputStream os) {
		/* Failed if no password is specified */
		if (passWord == null) {
			return (false);
		}

		/* Save data with specified password */
		return (save(passWord, os));
	}

	/**
	 * Save repository.
	 * 
	 * @param sk secret phrase
	 * @param os output stream
	 * @param ture if it succeed.
	 */
	private boolean save(String sk, OutputStream os) {
		
		try {
			/* Convert them from item to binary */
			Crypto crypt = new Crypto();
			byte [] data = toBinary();
			
			/* Encrypt them if password is specified */
			if (sk != null) {
				data = crypt.encrypt(sk.getBytes(), data);
				if (data == null) {
					return (false);
				}
			}
			
			/* Save to file */
			os.write(data);
			return (true);
			
		} catch (IOException e) {
			/* Failed to save */
			return (false);
		}
	}
	
	/**
	 * Convert to binary.
	 * 
	 * @return binary
	 * @throws IOException Conversion error.
	 */
	private byte [] toBinary() throws IOException {
		
		ByteArrayOutputStream os = null;
		PrintWriter w = null;
		try {
			os = new ByteArrayOutputStream();
			w = new PrintWriter(os);
			
			/* write comments */
			w.println("# updated at " + new Date());
			w.println();
			
			/* write items */
			int i = 0;
			for(Map<String, Object> e : itemList) {

				/* write Sections */
				w.println("[" + i + "]");
				
				/* write properties */
				for (Entry<String, Object> e2 : e.entrySet()) {
					String key = e2.getKey();
					Object value = e2.getValue();
					w.println(key + "=" + value);
				}

				i++;
			}
			
			w.flush();
			return (os.toByteArray());
			
		} finally {
			/* close writer */
			if (w != null) {
				w.close();
			}
			
			if (os != null) {
				os.close();
			}
		}
	}
	
	/**
	 * Convert from binary.
	 * 
	 * @param data binary
	 * @throws IOException Conversion error.
	 */
	private void fromBinary(byte [] data) throws IOException {

		ByteArrayInputStream is = null;
		try {
			is = new ByteArrayInputStream(data);
			importData(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
	
	/**
	 * Convert from binary.
	 * 
	 * @param is input stream
	 * @throws IOException Conversion error.
	 */
	public void importData(InputStream is) throws IOException {
		
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(is));
			Pattern p0 =Pattern.compile("\\[\\s*(.+)\\s*\\]");	/* Section */
			Pattern p1 =Pattern.compile("(.+?)=(.+)");			/* Properties */

			String s;
			Map<String, Object> cm = null;
			
			/* Read each line */
			while ((s = r.readLine()) != null) {
				/* Ignore comment */
				s = s.trim();
				if (s == null || s.length() == 0 || s.charAt(0) == '#') {
					continue;
				}
				
				Matcher m = p0.matcher(s);
				if (m.find()) {
					/* Create section */
					String key = m.group(1);
					if (key != null) {
						cm = new HashMap<String, Object>();
						itemList.add(cm);
					}
				} else if (cm != null ) {
					/* Register properties */
					Matcher m1 = p1.matcher(s);
					if (m1.find()) {
						String key = m1.group(1);		/* Property key */
						String value = m1.group(2);		/* Property value */
						cm.put(key.trim(), value.trim());
					}
				}
			}
			
		} finally {
			/* Close reader */
			if (r != null)
				r.close();
		}
	}
	
	/**
	 * Create backup file.
	 * 
	 * @param current repository file
	 * @param backup backup file
	 */
	public void backup(File current, File backup) {
		if (current.isFile()) {
			current.renameTo(backup);
		}
	}
	
	/**
	 * Set secret phrase.
	 * 
	 * @param passWord  secret phrase (no encryption if null is specified)
	 */
	public void setPassword(String passWord) {
		this.passWord = passWord;
	}
	
	/**
	 * Get secret phrase.
	 * 
	 * @return secret phrase
	 */
	public String getPassword() {
		return (passWord);
	}
}
