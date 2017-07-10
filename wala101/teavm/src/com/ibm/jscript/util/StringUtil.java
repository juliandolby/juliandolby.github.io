/*
 * IBM Confidential 
 * OCO Source Materials 
 * 
 * $$FILENAME$$
 * 
 * (C) Copyright IBM Corp. 2004, 2005
 * The source code for this program is not published or otherwise 
 * divested of its trade secrets, irrespective of what has been 
 * deposited with the U. S. Copyright Office. 
 * All rights reserved.
 */
package com.ibm.jscript.util;

import com.ibm.jscript.JScriptResources;


/**
 * <P>This class contains helpers for string manipulation.</P>
 * All the functionnalities are implemented through static functions because
 * providing its own string class is not easy (nobody else but you knows its
 * existence).<BR>
 */
public class StringUtil {

	/**
	 * Create a string from a character.
	 * @return the created string.
	 */
	public static String toStrng(char ch) {
		// Optimize that for 1.4?
		char[] array = new char[] {ch};
		return new String(array);
	}
			
    /**
     * Test the string equality, ignoring the case.
     * The 2 parameters can be null as far they are assimiled as empty.
     */
    public static final boolean equalsIgnoreCase( String s1, String s2 ) {
        if( s1==null || s2==null ) {
            return isEmpty(s1)==isEmpty(s2);
        }
        return s1.equalsIgnoreCase(s2);
    }

    /**
     * Compares two strings.
     * The comparison is based on the Unicode value of each character in
     * the strings.
     * @param   s1 the first string
     * @param   s2 the second string
     * @return  the value <code>0</code> if s1 is equal to s2. A value less than
     *          <code>0</code> if s1<s2 and a value greater than <code>0</code>
     *          if s1>s2.
     */
    public static int compareTo(String s1, String s2) {
        boolean e1 = isEmpty(s1);
        boolean e2 = isEmpty(s2);
        if( e1 || e2  ) {
            if( e1 && !e2 ) return -1;
            if( !e1 && e2 ) return 1;
            return 0;
        }
        return s1.compareTo(s2);
    }

    /**
     * Compares two strings ignoring the case.
     * The comparison is based on the Unicode value of each character in
     * the strings. When sun will provide such a native String function, it will
     * be more optimized.
     * @param   s1 the first string
     * @param   s2 the second string
     * @return  the value <code>0</code> if s1 is equal to s2. A value less than
     *          <code>0</code> if s1<s2 and a value greater than <code>0</code>
     *          if s1>s2.
     */
    public static int compareToIgnoreCase(String s1, String s2) {
        boolean e1 = isEmpty(s1);
        boolean e2 = isEmpty(s2);
        if( e1 || e2  ) {
            if( e1 && !e2 ) return -1;
            if( !e1 && e2 ) return 1;
            return 0;
        }
        return s1.compareToIgnoreCase(s2);
    }

    /**
     * Test the string equality.
     * The 2 parameters can be null as far they are assimiled as empty.
     */
    public static final boolean equals( String s1, String s2 ) {
        if( s1==null || s2==null ) {
            return isEmpty(s1)==isEmpty(s2);
        }
        return s1.equals(s2);
    }

	public static boolean isEmpty(String s) {
		return s==null || s.length()==0;
	}

	public static final boolean isSpace( String s ) {
        if( s!=null ) {
            int len = s.length();
            for( int i=0; i<len; i++ ) {
                char c = s.charAt(i);
                if( c!=' ' && c!='\t' && c!='\n' && c!='\r' ) {
                    return false;
                }
            }
        }
        return true;
    }
	
    /**
     * Format a string.
     */
    public static final String format( String fmt, Object p1, Object p2, Object p3, Object p4, Object p5 ) {
        if( fmt!=null ) {
            FastStringBuffer buffer = new FastStringBuffer();
            return buffer.appendFormat( fmt, p1, p2, p3, p4, p5 ).toString();
        }
        return ""; //$NON-NLS-1$
    }
    public static final String format( String fmt, Object p1, Object p2, Object p3, Object p4 ) {
        return format( fmt, p1, p2, p3, p4, null );
    }
    public static final String format( String fmt, Object p1, Object p2, Object p3 ) {
        return format( fmt, p1, p2, p3, null, null );
    }
    public static final String format( String fmt, Object p1, Object p2 ) {
        return format( fmt, p1, p2, null, null, null );
    }
    public static final String format( String fmt, Object p1 ) {
        return format( fmt, p1, null, null, null, null );
    }
    public static final String format( String fmt ) {
        return format( fmt, null, null, null, null, null );
    }

    /**
     * Convert to a string.
     */
    public static String toString( Object o ) {
        return o!=null ? o.toString() : ""; //$NON-NLS-1$
    }
    public static String toString( int i ) {
        return Integer.toString(i);
    }

    public static final String repeat( char toRepeat, int count ) {
        StringBuffer b =  new StringBuffer();
        for( int i=0; i<count; i++ ) {
        	b.append( toRepeat );
        }
        return b.toString();
    }
    public static final String repeat( String toRepeat, int count ) {
        StringBuffer b =  new StringBuffer();
        for( int i=0; i<count; i++ ) {
        	b.append( toRepeat );
        }
        return b.toString();
    }

   public static final String replaceFirst( String source, String value, String replace ) {
       if( StringUtil.isEmpty(source) || StringUtil.isEmpty(value) ) {
           return ""; //$NON-NLS-1$
       }
       if(replace==null ) {
           replace = ""; //$NON-NLS-1$
       }
       int idx = source.indexOf(value);
       if( idx>=0 ) {
           // Initialize the buffer with the begining of the string
           FastStringBuffer buffer = FastStringBufferPool.get();
           try {
               buffer.append( source, 0, idx );
               // Append the change to the str
               buffer.append(replace);
               int next = idx+value.length();
               // Append the string up to the next occurence of the value
               buffer.append( source, next, source.length() );

               return buffer.toString();
           } finally {
               FastStringBufferPool.recycle(buffer);
           }
       }

       // Nothing changed in the string
       return source;
   }

    public static final String toUnsignedHex( int value, int nChars ) {
        FastStringBuffer b = FastStringBufferPool.get();
        try {
            String s = Integer.toHexString(value);
            for( int i=7; i>=0; i-- ) {
                int v = (value >>> (i*4)) & 0x0F;
                if( b.length()>0 || v!=0 || i<nChars || i==0 ) {
                    b.append(hexChar(v));
                }
            }
            if( nChars>0 && b.length()>nChars ) {
                throw new NumberFormatException(
                                StringUtil.format( JScriptResources.getString("StringUtil.HexNumTooBig.Exception"), //$NON-NLS-1$
                                        StringUtil.toString(value),
                                        StringUtil.toString(nChars) ));
            }
            return b.toString();
        } finally {
            FastStringBufferPool.recycle(b);
        }
    }
    public static final char hexChar(int v) {
        return (char)((v>=10) ? (v-10+'A') : (v+'0'));
    }
    public static final int hexValue(char c) {
        if( c>='0' && c<='9' ) {
            return c-'0';
        }
        if( c>='A' && c<='F' ) {
            return c-'A'+10;
        }
        if( c>='a' && c<='f' ) {
            return c-'a'+10;
        }
        // Do not throw an exception...
        return -1;
    }
    
    
    public static String toJavaString(String s, boolean addQuotes) {
        FastStringBuffer b = FastStringBufferPool.get();
        try {
            b.appendJavaString(s,addQuotes);
            return b.toString();
        } finally {
            FastStringBufferPool.recycle(b);
        }
    }
    /**
     * Breaks a string into an array of substrings, with the delimeter removed.
     * The delimeter characters are listed in a separate string. Note that
     * <CODE>java.util.StringTokenizer</CODE> offers similar functionality.
     * This method differs by the fact that it returns all of the tokens at once,
     * using a String array, and does not require the explicit creation of a tokenizer
     * object. This function also returns empty strings when delimiters are concatened,
     * which is not the case of <CODE>java.util.StringTokenizer</CODE>. Finally,
     * the delimiter is unique and passed as a char.<BR>
     * @param s the string to split
     * @param c the delimeter
     * @return the substrings of s
     */
    public static String[] splitString( String s, char sep, boolean trim ) {
        if( s==null ) {
            return new String[0];
        }
        return splitString( null, 0, s, 0, sep, trim );
    }
    private static String[] splitString( String[] result, int count, String s, int pos, char sep, boolean trim ) {
        int newPos = s.indexOf(sep,pos);
        if( newPos>=0 ) {
            result = splitString( null, count+1, s, newPos+1, sep, trim );
            result[count] = s.substring( pos, newPos );
        } else {
            result = new String[count+1];
            result[count] = s.substring( pos );
        }
        if(trim) {
            result[count] = result[count].trim();
        }
        return result;
    }
    public static final String[] splitString(String s, char sep) {
        return splitString(s,sep,false);
    }

    /**
     * Trims the space characters from the beginning of a string.
     * For example, the call <CODE>ltrim ("eeTennessee", 'e')</CODE>
     * returns the string "Tennessee".<BR>
     * @param s the string to edit
     * @return the trimmed string
     */
    public static final String ltrim(String s) {
        int count = s.length();
        int st = 0;
        while ((st < count) && isSpace(s.charAt(st))) {
            st++;
        }
        return st>0 ? s.substring( st, count ) : s;
    }
    private static boolean isSpace( char c ) {
        return c <= ' ';
    }

    /**
     * Trims the space characters from the end of a string.
     * For example, the call <CODE>rtrim ("Tennessee", 'e')</CODE>
     * returns the string "Tenness".<BR>
     * All characters that have codes less than or equal to
     * <code>'&#92;u0020'</code> (the space character) are considered to be
     * white space.
     * @param s the string to edit
     * @return the trimmed string
     */
    public static final String rtrim(String s) {
        int count = s.length();
        int len = count;
        while ((0 < len) && isSpace(s.charAt(len-1)) ) {
            len--;
        }
        return (len < count) ? s.substring(0, len) : s;
    }

    /**
     *
     */
    public static final String replace( String source, String value, String replace ) {
        if( StringUtil.isEmpty(source) || StringUtil.isEmpty(value) ) {
            return ""; //$NON-NLS-1$
        }
        if(replace==null ) {
            replace = ""; //$NON-NLS-1$
        }
        int idx = source.indexOf(value);
        if( idx>=0 ) {
            // Initialize the buffer with the begining of the string
            FastStringBuffer buffer = FastStringBufferPool.get();
            try {
                buffer.append( source, 0, idx );
                int next;
                do {
                    // Append the change to the str
                    buffer.append(replace);
                    next = idx+value.length();

                    // And search for the next occurence
                    idx = source.indexOf(value,next);

                    // Append the string up to the next occurence of the value
                    buffer.append( source, next, idx>=0 ? idx : source.length() );
                } while(idx>=0);

                return buffer.toString();
            } finally {
                FastStringBufferPool.recycle(buffer);
            }
        }

        // Nothing changed in the string
        return source;
    }
}
