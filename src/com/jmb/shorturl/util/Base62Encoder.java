package com.jmb.shorturl.util;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Base62Encoder {

	private static final Logger log = Logger.getLogger(Base62Encoder.class);

	private static final char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	private static final int arrLength = alphabet.length;

	public int decode(String toEncode) {
		return decode(toEncode.toCharArray());
	}

	public int decode(char[] toEncode) {
		int toReturn = 0;
		for (int i = 0; i < toEncode.length; i++) {
			toReturn = toReturn * arrLength + indexOf(toEncode[i]);
		}
		return toReturn;
	}

	public char[] encode(int toDecode) {
		String toReturn = new String();
		if (toDecode == 0) {
			toReturn = String.valueOf(alphabet[toDecode]);
			return toReturn.toCharArray();
		} else {
			while (toDecode > 0) {
				toReturn += alphabet[toDecode % arrLength];
				toDecode = toDecode / arrLength;
			}
			return reverse(toReturn.toCharArray());
		}
	}

	private int indexOf(char c) {
		for (int i = 0; i < arrLength; i++) {
			if (c == alphabet[i]) {
				return i;
			}
		}
		return -1;
	}

	private char[] reverse(char[] toReverse) {
		char[] reversed = new char[toReverse.length];
		for (int i = 0; i < toReverse.length; i++) {
			reversed[i] = toReverse[(toReverse.length - 1) - i];
		}
		return reversed;
	}

}