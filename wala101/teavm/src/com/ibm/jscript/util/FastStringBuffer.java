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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.ibm.jscript.JScriptResources;

/**
 * Fast String buffer.</P>
 * This class has almost all the methods found in the standard <CODE>StringBuffer</CODE>
 * but is faster for many reasons :<BR>
 * <UL>
 *      <LI>It has no synchronized methods => don't access a buffer from differents
 *          threads!
 *      <LI>It cannot be shared (shared attribute does not exist)
 *      <LI>The memory allocation scheme is different
 * </UL>
 * It also has bonus methods which are very helpful.
 */
public class FastStringBuffer {

    /** The value is used for character storage. */
    private char[] value;

    /** The count is the number of characters in the buffer. */
    private int count;

    /** The delta for incrementing the size of the buffer */
    private static final int DELTA = 128;

    /**
     * Constructs a string buffer with no characters in it and an
     * initial capacity of DELTA characters.
     */
    public FastStringBuffer() {
        this(DELTA);
    }

    /**
     * Constructs a string buffer with no characters in it and an
     * initial capacity specified by the <code>length</code> argument.
     * @param      length   the initial capacity.
     */
    public FastStringBuffer(int length) {
        value = new char[length];
        count = 0;
    }

    /**
     * Constructs a string buffer from an array of characters.
     * The array is not initially copied into the string buffer, but rather referenced.
     * @param buffer of characters to be managed by this class
     * @param count, the number (in char) of useful information contained in the array.
     * @exception IllegalArgumentException , if buffer parameter is null.
     */
    public FastStringBuffer(char[] buffer, int count) {
        if (buffer==null){
            throw new java.lang.IllegalArgumentException(JScriptResources.getString("FastStringBuffer.NullValue.Exception")); //$NON-NLS-1$
        }
        value=buffer;
        this.count=count<0?0:count;

    }

    /**
     * Constructs a string buffer so that it represents the same
     * sequence of characters as the string argument. The initial
     * capacity of the string buffer is <code>DELTA</code> plus the length
     * of the string argument.
     * @param   str   the initial contents of the buffer.
     */
    public FastStringBuffer(String str) {
        this(str.length() + DELTA);
        append(str);
    }

    /**
     * Returns the length (character count) of this string buffer.
     * @return  the number of characters in this string buffer.
     */
    public final int length() {
        return count;
    }

    /**
     * Returns the current capacity of the String buffer. The capacity
     * is the amount of storage available for newly inserted
     * characters; beyond which an allocation will occur.
     * @return  the current capacity of this string buffer.
     */
    public final int capacity() {
        return value.length;
    }

    /**
     * Expand the capacity of the StringBuffer.
     * @param the minimum capacity needed
     */
    private final void expandCapacity(int minimumCapacity) {
        int newCapacity = Math.max( (value.length + 1) * 2, minimumCapacity );
        if( newCapacity-value.length<DELTA ) {
            newCapacity = value.length+DELTA;
        }
        char newValue[] = new char[newCapacity];
        System.arraycopy(value, 0, newValue, 0, count);
        value = newValue;
    }

    /**
     * Returns the character at a specific index in this string buffer.
     * <p>
     * The first character of a string buffer is at index
     * <code>0</code>, the next at index <code>1</code>, and so on, for
     * array indexing.
     * <p>
     * The index argument must be greater than or equal to
     * <code>0</code>, and less than the length of this string buffer.
     *
     * @param      index   the index of the desired character.
     * @return     the character at the specified index of this string buffer.
     * @see        com.ibm.jscript.util.FastStringBuffer#length()
     * @exception  StringIndexOutOfBoundsException  if the index is invalid.
     * @see        com.ibm.jscript.util.StringBuffer#length() */
     public final char charAt(int index) {
        /*#IF DEBUG*/
            if ((index < 0) || (index >= count)) {
                throw new StringIndexOutOfBoundsException(index);
            }
        /*#ENDIF*/
        return value[index];
     }

    /**
     * Characters are copied from this string buffer into the
     * destination character array <code>dst</code>. The first character to
     * be copied is at index <code>srcBegin</code>; the last character to
     * be copied is at index <code>srcEnd-1.</code> The total number of
     * characters to be copied is <code>srcEnd-srcBegin</code>. The
     * characters are copied into the subarray of <code>dst</code> starting
     * at index <code>dstBegin</code> and ending at index:
     * <p><blockquote><pre>
     *     dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param      srcBegin   start copying at this offset in the string buffer.
     * @param      srcEnd     stop copying at this offset in the string buffer.
     * @param      dst        the array to copy the data into.
     * @param      dstBegin   offset into <code>dst</code>.
     * @exception  StringIndexOutOfBoundsException  if there is an invalid
     *               index into the buffer.
     */
    public final void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        /*#IF DEBUG*/
            if ((srcBegin < 0) || (srcBegin >= count)) {
                throw new StringIndexOutOfBoundsException(srcBegin);
            }
            if ((srcEnd < 0) || (srcEnd > count)) {
                throw new StringIndexOutOfBoundsException(srcEnd);
            }
        /*#ENDIF*/
        if (srcBegin < srcEnd) {
            System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
        }
    }

    /**
     * The character at the specified index of tiis string buffer is set
     * to <code>ch</code>.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than the length of this string buffer.
     *
     * @param      index   the index of the character to modify.
     * @param      ch      the new character.
     * @exception  StringIndexOutOfBoundsException  if the index is invalid.
     * @see        com.ibm.jscript.util.StringBuffer#length()
     */
    public final void setCharAt(int index, char ch) {
        /*#IF DEBUG*/
            if ((index < 0) || (index >= count)) {
                throw new StringIndexOutOfBoundsException(index);
            }
        /*#ENDIF*/
        value[index] = ch;
    }

    /**
     * Appends the string representation of the <code>Object</code>
     * argument to this string buffer.
     * <p>
     * The argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then appended to this string buffer.
     *
     * @param   obj   an <code>Object</code>.
     * @return  this string buffer.
     * @see     com.ibm.jscript.util.String#valueOf(java.lang.Object)
     * @see     com.ibm.jscript.util.StringBuffer#append(java.lang.String)
     */
    public final FastStringBuffer append(Object obj) {
        return append(String.valueOf(obj));
    }

    /**
     * Appends the string to this string buffer.
     * <p>
     * The characters of the <code>String</code> argument are appended, in
     * order, to the contents of this string buffer, increasing the
     * length of this string buffer by the length of the argument.
     *
     * @param   str   a string.
     * @return  this string buffer.
     */
    public final FastStringBuffer append(String str) {
        if (str == null) {
            str = String.valueOf(str);
        }

        int len = str.length();
        int newcount = count + len;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }
        str.getChars(0, len, value, count);
        count = newcount;
        return this;
    }

    /**
     * Appends the string representation of the <code>char</code> array
     * argument to this string buffer.
     * <p>
     * The characters of the array argument are appended, in order, to
     * the contents of this string buffer. The length of this string
     * buffer increases by the length of the argument.
     *
     * @param   str   the characters to be appended.
     * @return  this string buffer.
     */
    public final FastStringBuffer append(char str[]) {
        int len = str.length;
        int newcount = count + len;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }
        System.arraycopy(str, 0, value, count, len);
        count = newcount;
        return this;
    }

    /**
     * Appends the string representation of a subarray of the
     * <code>char</code> array argument to this string buffer.
     * <p>
     * Characters of the character array <code>str</code>, starting at
     * index <code>offset</code>, are appended, in order, to the contents
     * of this string buffer. The length of this string buffer increases
     * by the value of <code>len</code>.
     *
     * @param   str      the characters to be appended.
     * @param   offset   the index of the first character to append.
     * @param   len      the number of characters to append.
     * @return  this string buffer.
     */
    public final FastStringBuffer append(char str[], int offset, int len) {
        int newcount = count + len;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }
        System.arraycopy(str, offset, value, count, len);
        count = newcount;
        return this;
    }

    /**
     * Appends the string representation of the <code>boolean</code>
     * argument to the string buffer.
     * <p>
     * The argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then appended to this string buffer.
     *
     * @param   b   a <code>boolean</code>.
     * @return  this string buffer.
     * @see     com.ibm.jscript.util.String#valueOf(boolean)
     * @see     com.ibm.jscript.util.StringBuffer#append(java.lang.String)
     */
    public final FastStringBuffer append(boolean b) {
        return append(String.valueOf(b));
    }

    /**
     * Appends the string representation of the <code>char</code>
     * argument to this string buffer.
     * <p>
     * The argument is appended to the contents of this string buffer.
     * The length of this string buffer increases by <code>1</code>.
     *
     * @param   ch   a <code>char</code>.
     * @return  this string buffer.
     */
    public final FastStringBuffer append(char c) {
        int newcount = count + 1;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }
        value[count++] = c;
        return this;
    }

    /**
     * Appends the string representation of the <code>int</code>
     * argument to this string buffer.
     * <p>
     * The argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then appended to this string buffer.
     *
     * @param   i   an <code>int</code>.
     * @return  this string buffer.
     * @see     com.ibm.jscript.util.String#valueOf(int)
     * @see     com.ibm.jscript.util.StringBuffer#append(java.lang.String)
     */
    public final FastStringBuffer append(int i) {
        return append(String.valueOf(i));
    }

    /**
     * Appends the string representation of the <code>long</code>
     * argument to this string buffer.
     * <p>
     * The argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then appended to this string buffer.
     *
     * @param   l   a <code>long</code>.
     * @return  this string buffer.
     */
    public final FastStringBuffer append(long l) {
        return append(String.valueOf(l));
    }

    /**
     * Appends the string representation of the <code>float</code>
     * argument to this string buffer.
     * <p>
     * The argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then appended to this string buffer.
     *
     * @param   f   a <code>float</code>.
     * @return  this string buffer.
     */
    public final FastStringBuffer append(float f) {
        return append(String.valueOf(f));
    }

    /**
     * Appends the string representation of the <code>double</code>
     * argument to this string buffer.
     * <p>
     * The argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then appended to this string buffer.
     *
     * @param   d   a <code>double</code>.
     * @return  this string buffer.
     */
    public final FastStringBuffer append(double d) {
        return append(String.valueOf(d));
    }

    /**
     * Inserts the string representation of the <code>Object</code>
     * argument into this string buffer.
     * <p>
     * The second argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then inserted into this string buffer at the indicated
     * offset.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      b        an <code>Object</code>.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    /**
     * Inserts the string into this string buffer.
     * <p>
     * The characters of the <code>String</code> argument are inserted, in
     * order, into this string buffer at the indicated offset. The length
     * of this string buffer is increased by the length of the argument.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      str      a string.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, String str) {
        /*#IF DEBUG*/
            if ((offset < 0) || (offset > count)) {
                throw new StringIndexOutOfBoundsException();
            }
        /*#ENDIF*/
        int len = str.length();
        int newcount = count + len;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }
        System.arraycopy(value, offset, value, offset + len, count - offset);
        str.getChars(0, len, value, offset);
        count = newcount;
        return this;
    }

    /**
     * Inserts the string representation of the <code>char</code> array
     * argument into this string buffer.
     * <p>
     * The characters of the array argument are inserted into the
     * contents of this string buffer at the position indicated by
     * <code>offset</code>. The length of this string buffer increases by
     * the length of the argument.
     *
     * @param      offset   the offset.
     * @param      ch       a character array.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, char str[]) {
        /*#IF DEBUG*/
            if ((offset < 0) || (offset > count)) {
                throw new StringIndexOutOfBoundsException();
            }
        /*#ENDIF*/
        int len = str.length;
        int newcount = count + len;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }
        System.arraycopy(value, offset, value, offset + len, count - offset);
        System.arraycopy(str, 0, value, offset, len);
        count = newcount;
        return this;
    }

    /**
     * Inserts the string representation of the <code>boolean</code>
     * argument into this string buffer.
     * <p>
     * The second argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then inserted into this string buffer at the indicated
     * offset.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      b        a <code>boolean</code>.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, boolean b) {
        return insert(offset, String.valueOf(b));
    }

    /**
     * Inserts the string representation of the <code>char</code>
     * argument into this string buffer.
     * <p>
     * The second argument is inserted into the contents of this string
     * buffer at the position indicated by <code>offset</code>. The length
     * of this string buffer increases by one.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      ch       a <code>char</code>.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, char c) {
        int newcount = count + 1;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }
        System.arraycopy(value, offset, value, offset + 1, count - offset);
        value[offset] = c;
        count = newcount;
        return this;
    }

    /**
     * Inserts the string representation of the second <code>int</code>
     * argument into this string buffer.
     * <p>
     * The second argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then inserted into this string buffer at the indicated
     * offset.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      b        an <code>int</code>.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, int i) {
        return insert(offset, String.valueOf(i));
    }

    /**
     * Inserts the string representation of the <code>long</code>
     * argument into this string buffer.
     * <p>
     * The second argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then inserted into this string buffer at the indicated
     * offset.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      b        a <code>long</code>.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, long l) {
        return insert(offset, String.valueOf(l));
    }

    /**
     * Inserts the string representation of the <code>float</code>
     * argument into this string buffer.
     * <p>
     * The second argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then inserted into this string buffer at the indicated
     * offset.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      b        a <code>float</code>.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, float f) {
        return insert(offset, String.valueOf(f));
    }

    /**
     * Inserts the string representation of the <code>double</code>
     * argument into this string buffer.
     * <p>
     * The second argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then inserted into this string buffer at the indicated
     * offset.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      b        a <code>double</code>.
     * @return     this string buffer.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public final FastStringBuffer insert(int offset, double d) {
        return insert(offset, String.valueOf(d));
    }

    /**
     * The character sequence contained in this string buffer is
     * replaced by the reverse of the sequence.
     *
     * @return  this string buffer.
     */
    public final FastStringBuffer reverse() {
        int n = count - 1;
        for (int j = (n-1) >> 1; j >= 0; --j) {
            char temp = value[j];
            value[j] = value[n - j];
            value[n - j] = temp;
        }
        return this;
    }

    /**
     * Converts to a string representing the data in this string buffer.
     * A new <code>String</code> object is allocated and initialized to
     * contain the character sequence currently represented by this
     * string buffer. This <code>String</code> is then returned. Subsequent
     * changes to the string buffer do not affect the contents of the
     * <code>String</code>.
     *
     * @return  a string representation of the string buffer.
     */
    public String toString() {
        return new String(this.value, 0, count);
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    //  BONUS CONSTRUCTORS & METHODS
    //
    //  These methods are specific to <CODE>TStringBuffer</CODE> and cannot be
    //  found in the standard <CODE>StringBuffer</CODE>.
    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a string buffer so that it represents the same
     * sequence of characters as the string argument. The initial
     * capacity of the string buffer is <code>DELTA</code> plus the length
     * of the string argument.
     * @param   str   the initial contents of the buffer.
     * @param   srcBegin   start copying at this offset in the string.
     * @param   srcEnd     stop copying at this offset in the string.
     */
    public FastStringBuffer(String str, int srcBegin, int srcEnd) {
        this(DELTA + srcEnd-srcBegin );
        append(str,srcBegin,srcEnd);
    }

    /**
     * Clear the content of the buffer.
     * Simply the count is reseted. No allocation is done.
     */
    public final void clear() {
        count = 0;
    }

    /**
     * Compare the buffer to a string.
     */
    public boolean equals( String str ) {
        if( str!=null && str.length()==count ) {
            for( int i=0; i<count; i++ ) {
                if( value[i]!=str.charAt(i) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     *
     */
    public final boolean startsWith( char c ) {
        return count>0 && value[0]==c;
    }

    /**
     * Append a repeated character string.
     * @param toRepeat the string to repeat
     * @param count the number of repetition
     */
    public final void repeat( String toRepeat, int count ) {
        for( int i=0; i<count; i++ ) {
            append(toRepeat);
        }
    }

    /**
     * Append a repeated character.
     * @param toRepeat the character to repeat
     * @param count the number of repetition
     */
    public final void repeat( char toRepeat, int count ) {
        for( int i=0; i<count; i++ ) {
            append(toRepeat);
        }
    }

    /**
     *
     */
    public final FastStringBuffer append( FastStringBuffer b, int srcBegin, int srcEnd ) {
        if (b == null) {
            return this;
        }
        if( srcBegin>=srcEnd ) {
            return this;
        }

        // Ensure that the buffer is zied enough
        int newcount = count + srcEnd-srcBegin;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }

        // Copy the desired characters
        b.getChars(srcBegin, srcEnd, value, count);
        count = newcount;
        return this;
    }
    public final FastStringBuffer append( FastStringBuffer b ) {
        if( b!=null ) {
            append( b, 0, b.length() );
        }
        return this;
    }

    /**
     *
     */
    public final FastStringBuffer append( String str, int srcBegin, int srcEnd ) {
        // Null value has a representation!
        if (str == null) {
            str = String.valueOf(str);
        }

        // Ensure that the buffer is sized enough
        int newcount = count + srcEnd-srcBegin;
        if (newcount > value.length) {
            expandCapacity(newcount);
        }

        // Copy the desired characters
        str.getChars(srcBegin, srcEnd, value, count);
        count = newcount;
        return this;
    }

    /**
     * Load the content of a IO reader.
     */
    public final FastStringBuffer load( Reader r ) {
        clear();
        return append(r);
    }

    /**
     * Append the content of a IO reader.
     */
    public final FastStringBuffer append( Reader r ) {
        char[] b = new char[8192];
        int len = 0;
        try {
            while( (len=r.read(b))>0 ) {
                append( b, 0, len );
            }
        } catch( IOException e ) {
            throw new RuntimeException(e.getMessage());
        }
        return this;
    }

    /**
     * Save the content to a writer.
     */
    public final void save( Writer w ) {
        try {
            w.write(value,0,count);
        } catch( IOException e ) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Append a formatted string.
     * A call to <CODE>TString.toString()</CODE> is made for each object. By this
     * way, arrays are properly dumped.
     */
    public final FastStringBuffer appendFormat( String fmt, Object p0, Object p1, Object p2, Object p3, Object p4) {
        if( fmt!=null ) {
            int pos = 0; int fmtLength = fmt.length(); int nextObject = 0; boolean not_valid = false;
            while(true) {
                // check if previous loop found a valid string format of {0} to {8}
            	if (not_valid == true) {
            		nextObject++; not_valid = false; //skip the '{' which has no valid format of {n}
            	}
                nextObject = fmt.indexOf('{', nextObject); //find next '{'
            	if( nextObject<0 ) {
                    nextObject = fmtLength;
                }
                // Copy the part of the string
                if( nextObject>pos ) {
                    int newcount = count + nextObject-pos;
                    if (newcount > value.length) {
                        expandCapacity(newcount);
                    }
                    fmt.getChars(pos, nextObject, value, count);
                    count = newcount;
                    pos = nextObject;
                }
                // find a valid {n} and append corresponding strings (0 =< n < 9)
                if (pos < fmtLength) {
                	if (pos < fmtLength-2) {
                		char c = fmt.charAt(pos+1);  // c = index of n inside { }
                		if (Character.isDigit(c)) {
                			char c2 = fmt.charAt(pos+2);
                			if (c2 == '}') {
                				switch( c ) {
                 					case '0': {
                 						pos = pos + 3;
                 						nextObject = pos;
                 						not_valid = false;
                 						if(p0!=null) {
	                 						append(StringUtil.toString(p0));
                 						}
                 					} break;
	                     			case '1': {
                     					pos = pos + 3;
                 						nextObject = pos;
                 						not_valid = false;
	                     				if(p1!=null) {
	                     					append(StringUtil.toString(p1));
	                     				}
	                     			} break;
	                     			case '2': {
                     					pos = pos + 3;   
                 						nextObject = pos;
                 						not_valid = false;
	                     				if(p2!=null) {
	                     					append(StringUtil.toString(p2));
	                     				}
	                     			} break;
	                     			case '3': {
                     					pos = pos + 3;
                 						nextObject = pos;
                 						not_valid = false;
	                     				if(p3!=null) {
	                     					append(StringUtil.toString(p3));
	                     				}
	                     			} break;
	                     			case '4': {
                     					pos = pos + 3;
                 						nextObject = pos;
                 						not_valid = false;
	                     				if(p4!=null) {
	                     					append(StringUtil.toString(p4));
	                     				}
	                     			} break;
	                     		}   //end switch
	                     	} else {   // end if c2 == '}'
	                     		not_valid = true;
	                     	}
	                     } else {	// end if c.isDigit
	                     	not_valid = true;
	                     }
	                } else {   	// end if pos < length-2
	                	not_valid = true;
	                	//break;
	                }
                } else {
                	not_valid = true;
                	break;//end if pos <length
                }
            }	// end while loops
        }	// end if fmt != null
        return this;
    } 	// end appendFormat
    
    
    /*
    public final FastStringBuffer appendFormat( String fmt, Object p1, Object p2, Object p3, Object p4, Object p5 ) {
        if( fmt!=null ) {
            int pos = 0; int fmtLength = fmt.length();
            while(true) {
                // Find the next '%' or the end of the line....
                int nextObject = fmt.indexOf('%',pos);
                if( nextObject<0 ) {
                    nextObject = fmtLength;
                }
                // Copy the part of the string
                if( nextObject>pos ) {
                    int newcount = count + nextObject-pos;
                    if (newcount > value.length) {
                        expandCapacity(newcount);
                    }
                    fmt.getChars(pos, nextObject, value, count);
                    count = newcount;
                    pos = nextObject;
                }
                // Parse the '%' or stop the search
                if( pos<fmtLength ) {
                    pos++; // Skip the '%'
                    if( pos<fmt.length() ) {
                        char c = fmt.charAt(pos++); // Get the generic char
                        switch( c ) {
                            case '1': {
                                if(p1!=null) {
                                    append(StringUtil.toString(p1));
                                }
                            } break;
                            case '2': {
                                if(p2!=null) {
                                    append(StringUtil.toString(p2));
                                }
                            } break;
                            case '3': {
                                if(p3!=null) {
                                    append(StringUtil.toString(p3));
                                }
                            } break;
                            case '4': {
                                if(p4!=null) {
                                    append(StringUtil.toString(p4));
                                }
                            } break;
                            case '5': {
                                if(p5!=null) {
                                    append(StringUtil.toString(p5));
                                }
                            } break;
                            case '%': {
                                append('%');
                            } break;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return this;
    }
    public final FastStringBuffer appendFormat( String fmt, Object p1, Object p2, Object p3, Object p4 ) {
        return appendFormat( fmt, p1, p2, p3, p4, null );
    }
    public final FastStringBuffer appendFormat( String fmt, Object p1, Object p2, Object p3 ) {
        return appendFormat( fmt, p1, p2, p3, null, null );
    }
    public final FastStringBuffer appendFormat( String fmt, Object p1, Object p2 ) {
        return appendFormat( fmt, p1, p2, null, null, null );
    }
    public final FastStringBuffer appendFormat( String fmt, Object p1 ) {
        return appendFormat( fmt, p1, null, null, null, null );
    }
    public final FastStringBuffer appendFormat( String fmt ) {
        return appendFormat( fmt, null, null, null, null, null );
    }
*/

    /**
     * Print a formatted string with a '\n' at the end.
     */
    public final FastStringBuffer prt( String fmt, Object p1, Object p2, Object p3, Object p4, Object p5 ) {
        if( prtIndent!=null ) {
            append( prtIndent );
        }
        appendFormat( fmt, p1, p2, p3, p4, p5 );
        append( '\n' );
        return this;
    }
    public final FastStringBuffer prt( String fmt, Object p1, Object p2, Object p3, Object p4 ) {
        return prt( fmt, p1, p2, p3, p4, null );
    }
    public final FastStringBuffer prt( String fmt, Object p1, Object p2, Object p3 ) {
        return prt( fmt, p1, p2, p3, null, null );
    }
    public final FastStringBuffer prt( String fmt, Object p1, Object p2 ) {
        return prt( fmt, p1, p2, null, null, null );
    }
    public final FastStringBuffer prt( String fmt, Object p1 ) {
        return prt( fmt, p1, null, null, null, null );
    }
    public final FastStringBuffer prt( String fmt ) {
        return prt( fmt, null, null, null, null, null );
    }

    /**
     * To append indent if necessary, the given string with no formatting, and \n.
     */
    public final FastStringBuffer prtNoFormatting( String str ) {
        if( prtIndent!=null ) {
            append( prtIndent );
        }
        append( str );
        append( '\n' );
        return this;
    }



    private int indentLevel;
    /**
     * Increment the indent.
     * Next time 'prt' method is called, the printed line will be more indented
     */
    public void incIndent() {
        indentLevel++;
        setPrtIndent(FastStringBuffer.getIndentString(indentLevel));
    }
    /**
     * Decrement the indent.
     * Next time 'prt' method is called, the printed line will be less indented
     */
    public void decIndent() {
        indentLevel--;
        setPrtIndent(FastStringBuffer.getIndentString(indentLevel));
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public static String getIndentString(int level) {
        switch(level) {
            case 0:     return ""; //$NON-NLS-1$
            case 1:     return "  "; //$NON-NLS-1$
            case 2:     return "    "; //$NON-NLS-1$
            case 3:     return "      "; //$NON-NLS-1$
            case 4:     return "        "; //$NON-NLS-1$
        }
        return StringUtil.repeat( "  ", level ); //$NON-NLS-1$
    }

    /**
     * Append the indent string to the buffer.
     * Note : You don't need to call this method if you use 'prt' method,
     * because the indent string is already printed in 'prt' method.
     */
    public FastStringBuffer prtIndent() {
        if( prtIndent!=null ) {
            append( prtIndent );
        }
        return this;
    }


    public String getPrtIndent() {
        return prtIndent;
    }
    public void setPrtIndent(String prtIndent) {
        this.prtIndent = prtIndent;
    }
    private String prtIndent;


    /**
     * Convert the buffer to an array of chars.
     * @return an array of chars containing all the chars
     */
    public final char[] toCharArray() {
        char[] c = new char[count];
        System.arraycopy( this.value, 0, c, 0, count );
        return c;
    }

    /**
     * Extract a part of the string buffer to a string.
     * @return the substring
     */
    public final String substring( int first, int last ) {
        return new String(this.value, first, last-first);
    }

    /**
     * Add a field definition (ie fieldName+value) to existing string. Note that
     * a separator is appended if the buffer is not empty.
     * Mainly used for debug purpose (content of an object).
     * @param fieldName the fieldName. If this field is null, "=" is not appended
     * @param value the field value
     * @param sep the optional separator between fields. Default to <CODE>';'</CODE>
     */
    public final FastStringBuffer addFieldDef( String fieldName, String value, String sep ) {
        if( length()!=0 ) append( sep );
        if( fieldName!=null ) {
            append( fieldName );
            append( "=" ); //$NON-NLS-1$
        }
        append( value );
        return this;
    }
    public final FastStringBuffer addFieldDef( String fieldName, String value ) {
        return addFieldDef( fieldName, value, ";" ); //$NON-NLS-1$
    }

    /**
     * Adding symbolic value corresponding to a flag.
     * @param fldValue the entire field value
     * @param tstFlag the flag to teste
     * @param symbol the symbolic value corresponding to the flag
     */
    public final FastStringBuffer addSymbolic( boolean flag, String symbol, String sep ) {
        if( flag ) {
            addFieldDef( null, symbol, sep );
        }
        return this;
    }
    public final FastStringBuffer addSymbolic( boolean flag, String symbol ) {
        return addSymbolic( flag, symbol, "|" ); //$NON-NLS-1$
    }
    public final FastStringBuffer addSymbolic( int fldValue, int tstFlag, String symbol, String sep ) {
        return addSymbolic( (fldValue&tstFlag)!=0, symbol, sep );
    }
    public final FastStringBuffer addSymbolic( int fldValue, int tstFlag, String symbol ) {
        return addSymbolic( fldValue, tstFlag, symbol, "|" ); //$NON-NLS-1$
    }

    /**
     * Delete a part of the buffer.
     */
    public final void delete( int begin, int end ) {
        /*#IF DEBUG*/
            if ((begin < 0) || (begin >= count)) {
                throw new StringIndexOutOfBoundsException(begin);
            }
            if ((end < 0) || (end > count)) {
                throw new StringIndexOutOfBoundsException(end);
            }
            if (end<begin) {
                throw new StringIndexOutOfBoundsException(end);
            }
        /*#ENDIF*/
        if (begin < end) {
            System.arraycopy(value, end, value, begin, count-end);
            count -= (end-begin);
        }
    }

    /**
     * Replace a buffer part.
     */
    public final void replace( int begin, int end, String replace ) {
        delete( begin, end );
        insert( begin, replace );
    }

    /**
     * Replace a buffer part.
     */
    public final void replace( int begin, int end, char[] replace ) {
        delete( begin, end );
        insert( begin, replace );
    }

    /**
     * Replace a buffer part.
     */
    public final void replace( int begin, int end, FastStringBuffer replace ) {
        delete( begin, end );
        insert( begin, replace.toString() ); // TODO: optimize it ?
    }

    /**
     * Replace a set of Strings.
     */
    public final void replace( String oldString, String newString ) {
        int pos = 0;
        while(pos<length()) {
            pos = indexOf(oldString,pos);
            if( pos>=0 ) {
                replace( pos, pos+oldString.length(), newString );
                pos += newString.length();
            } else {
                return;
            }
        }
    }

    /**
     * Search an instance of a char.
     */
    public int indexOf( char c, int begin ) {
        for( int i=begin; i<count; i++ ) {
            if( value[i]==c ) {
                return i;
            }
        }
        return -1;
    }
    public int indexOf( char c ) {
        return indexOf(c,0);
    }

    /**
     * Search an instance of a string.
     */
    public int indexOf( String string, int begin ) {
        int length = string.length();
        if( length>0 ) {
            char fc = string.charAt(0);
mainloop:
            for( int i=begin; i<=count-length; i++ ) {
                // Check for the first character
                if( value[i]==fc ) {
                    // And compare each others
                    for( int j=1; j<length; j++ ) {
                        if( value[i+j]!=string.charAt(j) ) {
                            continue mainloop; // next match
                        }
                    }

                    // Ok, found at i
                    return i;
                }
            }
        }
        return -1;
    }
    public int indexOf( String string ) {
        return indexOf(string,0);
    }

    /**
     * Search the last instance of a char.
     */
    public int lastIndexOf( char c, int end ) {
        for( int i=end; i>=0; i-- ) {
            if( value[i]==c ) {
                return i;
            }
        }
        return -1;
    }
    public int lastIndexOf( char c ) {
        return lastIndexOf(c,count-1);
    }

    /**
     * Get the last character contained in the buffer
     * @return the last character, '\0' if the buffer is empty
     */
    public char getLastChar() {
        if( count>0 ) {
            return value[count-1];
        }
        return '\0';
    }

    /**
     * Append a list of values
     */
    public void appendList( String[] items, String sep, String prefix, String suffix ) {
        if( prefix!=null ) {
            append(prefix);
        }
        if( items!=null ) {
            for( int i=0; i<items.length; i++ ) {
                if( i>0 ) {
                    append(sep);
                }
                append(items[i]);
            }
        }
        if( suffix!=null ) {
            append(suffix);
        }
    }

    /**
     * Append a java string to the buffer.
     * All the java escape charcters are transformed.
     */
    public FastStringBuffer appendJavaString(String s, boolean addQuotes) {
        if( addQuotes ) {
            append( '"' );
        }
        int length = s.length();
        for( int i=0; i<length; i++ ) {
            char c = s.charAt(i);
            switch(c) {
                case '\b':  append( "\\b" );  break; //$NON-NLS-1$
                case '\t':  append( "\\t" );  break; //$NON-NLS-1$
                case '\n':  append( "\\n" );  break; //$NON-NLS-1$
                case '\f':  append( "\\f" );  break; //$NON-NLS-1$
                case '\r':  append( "\\r" );  break; //$NON-NLS-1$
                case '\'':  append( "\\'" );  break; //$NON-NLS-1$
                case '\"':  append( "\\\"" ); break; //$NON-NLS-1$
                case '\\':  append( "\\\\" ); break; //$NON-NLS-1$
                default : {
                    if( c<32 || c>255 ) {
                        append( "\\u" ); //$NON-NLS-1$
                        append( StringUtil.toUnsignedHex(c,4) );
                    } else {
                        append(c);
                    }
                }
            }
        }
        if( addQuotes ) {
            append( '"' );
        }
        return this;
    }

/*
    public static void main( String[] arg ) {
        test( "{0} is a test{1}", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "{0} is %%a test{1}", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "{0} is a {0}{1}test{1}", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "% is a test{1}", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "{0} is a test%", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "{0} is a test%", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "{0} is a test{1}", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "{0} is a test{1}", ">1<", ">2<", ">3<", ">4<", ">5<" );
        test( "{0} is a test{1}", ">1<", ">2<", ">3<", ">4<", ">5<" );
        TStringBuffer b = new TStringBuffer(256);
        long ts1 = System.currentTimeMillis();
        for( int i=0; i<10000; i++ ) {
            b.clear();
            b.oldAppendFormat("My format 1",null,null,null,null,null);
            b.oldAppendFormat("My format {0} is {1}",null,null,null,null,null);
            b.oldAppendFormat("this is a big string {0} with some format inside {1}",null,null,null,null,null);
        }
        long ts2 = System.currentTimeMillis();
        for( int i=0; i<10000; i++ ) {
            b.clear();
            b.appendFormat("My format 1",null,null,null,null,null);
            b.appendFormat("My format {0} is {1}",null,null,null,null,null);
            b.appendFormat("this is a big string {0} with some format inside {1}",null,null,null,null,null);
        }
        long ts3 = System.currentTimeMillis();
        TDiag.trace( "Old format={0}", TString.toString(ts2-ts1) );
        TDiag.trace( "New format={0}", TString.toString(ts3-ts2) );
    }
    private static void test( String fmt, Object p1, Object p2, Object p3, Object p4, Object p5 ) {
        TStringBuffer b = new TStringBuffer(256);
        String s1 = b.appendFormat(fmt,p1,p2,p3,p4,p5).toString();
        b.clear();
        String s2 = b.oldAppendFormat(fmt,p1,p2,p3,p4,p5).toString();
        if( !s1.equals(s2) ) {
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println( "Error in fmt '"+fmt+"'" );
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println( "    "+s1 );
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println( "    "+s2 );
        }
    }
    public final TStringBuffer oldAppendFormat( String fmt, Object p1, Object p2, Object p3, Object p4, Object p5 ) {
        for( int i=0; i<fmt.length(); ) {
            char c = fmt.charAt(i++);
            if( c=='%' ) {
                if( i<fmt.length() ) {
                    c = fmt.charAt(i++);
                    switch( c ) {
                        case '1': {
                            if(p1!=null) {
                                append(TString.toString(p1));
                            }
                        } break;
                        case '2': {
                            if(p2!=null) {
                                append(TString.toString(p2));
                            }
                        } break;
                        case '3': {
                            if(p3!=null) {
                                append(TString.toString(p3));
                            }
                        } break;
                        case '4': {
                            if(p4!=null) {
                                append(TString.toString(p4));
                            }
                        } break;
                        case '5': {
                            if(p5!=null) {
                                append(TString.toString(p5));
                            }
                        } break;
                        case '%': {
                            append('%');
                        } break;
                    }
                }
            } else {
                append(c);
            }
        }
        return this;
    }
*/
}
