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
package com.ibm.jscript.types;

import java.text.DateFormat;
import java.util.TimeZone;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.std.DateObject;
import com.ibm.jscript.std.StringObject;
import com.ibm.jscript.util.DateUtilities;
import com.ibm.jscript.util.StringParser;
import com.ibm.jscript.util.StringUtil;

public final class FBSString extends FBSValue {

    public static final FBSString emptyString=new FBSString();

    private String value="";

    public static final FBSString get(String s) {
        if( s==null || s.length()==0 ) {
            return emptyString;
        }
        return new FBSString(s);
    }

    public static final FBSString get(char c) {
        return new FBSString(c);
    }

    private FBSString() {
    }

    private FBSString(char c) {
        value= new String(new char[]{c});
    }

    private FBSString(String v) {
        if ( v == null ){
            value="";
        }else{
            value=v;
        }
    }

    public int getType(){
        return STRING_TYPE;
    }

    public FBSType getFBSType() {
        return FBSType.stringType;
    }

    public boolean isString() {
        return true;
    }

    public boolean isJavaAssignableTo(Class c) {
        if( c.isAssignableFrom(String.class) ) {
        	return true;
        }
    	if( c==Character.TYPE || c.isAssignableFrom(Character.class) ) {
    		// A single character string is convertible
            if( value.length()==1 ) {
            	return true;
            }
            return false;
    	}
        return false;
    }

    public Object toJavaObject(Class _class) throws InterpretException{
        if (_class==null || _class.isAssignableFrom(String.class) ) {
            return value;
        }
        if (_class==Character.TYPE || _class.isAssignableFrom(Character.class) ) {
            if( value.length()==1 ) {
                return new Character(value.charAt(0));
            }
        }
        throw new InterpretException(StringUtil.format("can't convert FBString to {0}",_class.getName()));
    }

    public String stringValue(){
        return value;
    }

    public double numberValue(){
    	int end = value.length();
    	while(end>0 && isSpace(value.charAt(end-1))) {
    		end--;
    	}
    	int start = 0;
    	while(start<end && isSpace(value.charAt(start))) {
    		start++;
    	}
    	String v = value;
    	if(start>0 || end<value.length()) {
    		v = value.substring(start,end);
    	}
    	if(v.length()==0) {
    		return 0;
    	}
    	if( v.startsWith("0x") || v.startsWith("0X") ) {
			String imageWithout0x = v.substring(2);
			return (double)Long.parseLong(imageWithout0x, 16);
    	}
        try {
            return Double.parseDouble(v);
        } catch(Exception e){
            return Double.NaN;
        }
    }
    private static boolean isSpace(char c) {
    	// PHIL: the spec handles more characters...
    	return c==' ' || c=='\t' || c=='\r' || c=='\n'; 
    }

    public boolean booleanValue(){
        return !isEmpty();
    }

    public DateObject dateValueJS() {
        java.util.Date dt = parseUtilDate(value);
        if(dt!=null) {
            return new DateObject(dt.getTime());
        }
        return null;
    }

    public FBSValue toFBSPrimitive(){
        return this;
    }

    public FBSBoolean toFBSBoolean(){
        return FBSBoolean.get(booleanValue());
    }

    public FBSNumber toFBSNumber(){
        return FBSNumber.get(numberValue());
    }

    public FBSString toFBSString(){
        return this;
    }


    public boolean isEmpty(){
        return (value!=null && !value.equals(""))?false:true;
    }

    public String getTypeAsString(){
        return "string";
    }

    public String toString(){
        return "FBSString : \""+value+"\"";
    }

    public FBSObject toFBSObject(){
        try{
            return (FBSObject)FBSUtility.wrap(new StringObject(this));
        }catch(Exception e){
        }
        return null;
    }

    public boolean isJavaNative(){
        return false;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility: Date parsing
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public static java.util.Date parseUtilDate(String s) {
        if(StringUtil.isEmpty(s)) {
            return null;
        }
        // Try an XMI parsing
        try {
            java.util.Date result = (java.util.Date)readXMIDate(s, java.util.Date.class);
            if(result!=null) {
                return result;
            }
        } catch(Exception e) {}
        // Try a standard parsing
        try {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).parse(s);
        } catch(Exception e) {}
        try {
            return DateFormat.getDateInstance(DateFormat.SHORT).parse(s);
        } catch(Exception e) {}
        try {
            return DateFormat.getTimeInstance(DateFormat.SHORT).parse(s);
        } catch(Exception e) {}
        return null;
    }

    public static java.sql.Date parseDate(String s) {
        if(StringUtil.isEmpty(s)) {
            return null;
        }
        // Try an XMI parsing
        try {
            java.sql.Date result = (java.sql.Date)readXMIDate(s, java.sql.Date.class);
            if(result!=null) {
                return result;
            }
        } catch(Exception e) {}
        // Try a standard parsing
        try {
            return new java.sql.Date(DateFormat.getDateInstance(DateFormat.SHORT).parse(s).getTime());
        } catch(Exception e) {}
        return null;
    }

    public static java.sql.Date parseDate(String s, String def) {
        return parseDate(StringUtil.isEmpty(s)?def:s);
    }
    /**
     * Parse an XMI  date
     * Returns a date in XSP server time zone (if the XML doc contains
     * tz indication different from the server, the date is converted. )
     */
    public static java.util.Date readXMIDate(String s, Class javaClass) {
        long xmlDate = readXMIDate(s);
        if(xmlDate!=Long.MIN_VALUE) {
            if(javaClass==java.util.Date.class) {
                return new java.util.Date(xmlDate);
            }
            if(javaClass==java.sql.Date.class) {
                return new java.sql.Date(xmlDate);
            }
            if(javaClass==java.sql.Time.class) {
                return new java.sql.Time(xmlDate);
            }
            if(javaClass==java.sql.Timestamp.class) {
                return new java.sql.Timestamp(xmlDate);
            }
        }
        return null;
    }

    public static long readXMIDate(String s) {
        int year = 0, month = 0, day = 0;
        int hour = 0, minute = 0, second = 0, nanos = 0;
        boolean tzIndication = false;
        boolean tzSignPlus = false; // time zone offset sign
        int h2 = 0, m2 = 0; // time zone offset
        // The date use the format
        // YYYY-MM-DDTHH:NN:SS
        // could be followed by "Z" or " +or- HH:mm" (time zone offset)
        StringParser parser = new StringParser(s);

        // Begin by the date, without any leading char
        int firstInteger = parser.getNextInteger();
        if(firstInteger!=Integer.MIN_VALUE) {
            year = firstInteger;
            if(!parser.match('-')) {
                return Long.MIN_VALUE; }

            month = parser.getNextInteger();
            if(month==Integer.MIN_VALUE) {
                return Long.MIN_VALUE; }
            if(!parser.match('-')) {
                return Long.MIN_VALUE; }

            day = parser.getNextInteger();
            if(day==Integer.MIN_VALUE) {
                return Long.MIN_VALUE; }
        }

        // Then, check if the time is valid, leading with a 'T'
        if(!parser.isEOF()) {
            if(!parser.match('T')) {
                return Long.MIN_VALUE; }

            hour = parser.getNextInteger();
            if(hour==Integer.MIN_VALUE) {
                return Long.MIN_VALUE; }
            if(!parser.match(':')) {
                return Long.MIN_VALUE; }

            minute = parser.getNextInteger();
            if(minute==Integer.MIN_VALUE) {
                return Long.MIN_VALUE; }
            if(!parser.match(':')) {
                return Long.MIN_VALUE; }

            second = parser.getNextInteger();
            if(second==Integer.MIN_VALUE) {
                return Long.MIN_VALUE; }

            if(parser.match('.')) {
                int ptr = parser.getCurrentPosition();
                int frac = parser.getNextInteger();
                if(frac==Integer.MIN_VALUE) {
                    return Long.MIN_VALUE; }
                int ndigits = parser.getCurrentPosition()-ptr;
                if(ndigits<6) {
                    nanos = frac*pow10[ndigits];
                }
            }
            // Get the time diff
            if(parser.match('Z')) {
                tzIndication = true; // h2=0 and m2=0
            } else {
                tzSignPlus = parser.match('+');
                if(parser.match('-')||tzSignPlus) {
                    tzIndication = true;
                    h2 = parser.getNextInteger();
                    if(h2==Integer.MIN_VALUE) {
                        return Long.MIN_VALUE; }
                    if(!parser.match(':')) {
                        return Long.MIN_VALUE; }
                    m2 = parser.getNextInteger();
                    if(m2==Integer.MIN_VALUE) {
                        return Long.MIN_VALUE; }
                }
            }
        }

        // The date must be finished
        if(!parser.isEOF()) {
            return Long.MIN_VALUE; }

        long xmlDate = DateUtilities.SQLDatetimeStruct.createDatetimeAsLong(year, month, day, hour, minute, second);
        // if the time zone indication in the XML is DIFFERENT from XSP server TZ :
        // the date must be converted
        if(tzIndication) {
            long xmlOffsetindication;
            if(tzSignPlus) {
                xmlOffsetindication = h2*60*60*1000+m2*60*1000;
            } else {
                xmlOffsetindication = -h2*60*60*1000-m2*60*1000;
            }
            long serverOffsetIndication = TimeZone.getDefault().getRawOffset();
            // conversion if offsets are not the same
            if(xmlOffsetindication!=serverOffsetIndication) {
                xmlDate = xmlDate-xmlOffsetindication+serverOffsetIndication;
            }
        }

        return xmlDate;
    }

    private static int[] pow10 = {
        100000, 10000, 1000, 100, 10, 1};
    
}
