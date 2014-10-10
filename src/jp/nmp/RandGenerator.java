package jp.nmp;

import android.annotation.SuppressLint;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * Random Password Generator.
 * 
 * @author kyo
 * @version 1.0
 */
public final class RandGenerator {
	
	/**
	 * Numeric Characters.
	 */
	public static final String DEFAULT_DIGITS = "0123456789";
	
	/**
	 * Alphabetic Characters.
	 */
	public static final String DEFAULT_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	
	/**
	 * Symbolic Characters.
	 */
	public static final String DEFAULT_SYMBOLS = "!#$%&*+-./=?@_";
	
	/**
	 * Digit flag.
	 */
	private boolean digits = true;
	
	/**
	 * Lower case alphabetics.
	 */
	private boolean lowerLetters = true;
	
	/**
	 * Upper case alphabetics.
	 */
	private boolean upperLetters = true;
	
	/**
	 * Default symbols.
	 */
	private String symbols = DEFAULT_SYMBOLS;

	/**
	 * Generate random password string.
	 * 
	 * @param length password length
	 * @return password string
	 */
	public String generate(int length) {
		
		StringBuffer buf = new StringBuffer();
		
		if (digits) {
			buf.append(DEFAULT_DIGITS);
		}
		
		if (lowerLetters) {
			buf.append(DEFAULT_LETTERS);
		}

		if (upperLetters) {
			buf.append(DEFAULT_LETTERS.toUpperCase(Locale.US));
		}
		
		if (symbols != null) {
			buf.append(symbols);
		}
		
		return (random(length, buf.toString()));
	}
	
	/**
	 * Generate random password string which uses specified characters.
	 * 
	 * @param length password length
	 * @param seq base characters
	 * @return password string
	 */
	@SuppressLint("TrulyRandom")
	private String random(int length, CharSequence seq) {
		SecureRandom rand = new SecureRandom();
		int n = seq.length();
		
		/* pick a character from specified characters at random position */
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			rand.setSeed(i);
			int c = rand.nextInt(n);
			buf.append(seq.charAt(c));
		}
		
		return (buf.toString());
	}
	
	/**
	 * Set digit.
	 * 
	 * @param flag digit flag
	 */
	public void setDigits(boolean flag) {
		digits = flag;
	}
	
	/**
	 * Set lower case alphabetics.
	 * 
	 * @param flag lower case alphabetics flag
	 */
	public void setLowerLetters(boolean flag) {
		lowerLetters = flag;
	}
	
	/**
	 * Set upper case alphabetics.
	 * 
	 * @param flag upper case alphabetics flag
	 */
	public void setUpperLetters(boolean flag) {
		upperLetters = flag;
	}

	/**
	 * Set Symbols.
	 * 
	 * @param symbols symbol string.
	 */
	public void setSymbolicChars(String symbols) {
		
		/* not use symbols if null is specified */
		if (symbols == null) {
			this.symbols = null;
			return;
		}

		/* sanitize */
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < symbols.length(); i++) {
			char c = symbols.charAt(i);
			
			/* remove unavailable symbol */
			if (DEFAULT_SYMBOLS.indexOf(c) >= 0) {
				buf.append(c);
			}
		}
		
		this.symbols = buf.toString();
	}
}
