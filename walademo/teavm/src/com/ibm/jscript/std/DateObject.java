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
package com.ibm.jscript.std;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.util.FastStringBuffer;


/**
 * This class allows to manipulate standard JavaScript Date objects.
 * <br>
 * This class differs from the ECMA spec as it uses internally the Java coding
 * format. This ensure the greatest compatibility with the Java language.<br>
 * 
 * This class provides several constructors, with some arguments to represent
 * year, month, day, minutes, and so on.
 *
 * @fbscript Date
 */
public class DateObject extends FBSDefaultObject {

	/*
     * We need to derive the gregorian calendar because getTimeInMillis() &
     * setTimeInMillis() are protected (until JDK1.4) in the Calendar class.
     */
    private static final class GregorianCalendarEx extends GregorianCalendar {
    	GregorianCalendarEx() {
    	}
    	GregorianCalendarEx(TimeZone tz) {
    		super(tz);
    	}
        public final long getMillis() {
            return getTimeInMillis();
        }
        public final void setMillis(long ms) {
            setTimeInMillis(ms);
        }
    }
	
	private static GregorianCalendarEx staticCalendar = new GregorianCalendarEx();
	private static GregorianCalendarEx utcCalendar = new GregorianCalendarEx(TimeZone.getTimeZone("GMT"));
	
    private static DateFormat UTC_FORMAT = DateFormat.getDateTimeInstance();

    static{
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
    }
    

    private long utcTime;

    public DateObject(DateObject date) {
    	super(DatePrototype.getDatePrototype());
        this.utcTime = date.utcTime;
    }

    /**
     * Creates a new date representing the current date.
     */
    public DateObject() {
    	super(DatePrototype.getDatePrototype());
        this.utcTime = System.currentTimeMillis();
    }


    /**
     * Creates a new date object representing year , month , day , hours , minutes , seconds
     * and milli seconds.
     * @param year, year of the date.
     * @param month, month of the date.
     * @param date, day of the date.
     * @param hours, hours of the date.
     * @param minutes, minutes of the date.
     * @param seconds, seconds of the date.
     * @param ms, milli-seconds of the date.
     * @fbscript
     */
    public DateObject(int year, int month, int date, int hours, int minutes, int seconds, int ms){
    	super(DatePrototype.getDatePrototype());
    	synchronized(staticCalendar) {
    		staticCalendar.set(year,month,date,hours,minutes,seconds);
    		staticCalendar.set(GregorianCalendar.MILLISECOND,ms);
            this.utcTime = staticCalendar.getMillis();
        }
    }

    /**
     * Creates a new date represented by the given time.
     * @param time, number of milliseconds in UTC
     * @fbscript
     */
    public DateObject(long time) {
    	super(DatePrototype.getDatePrototype());
    	this.utcTime = time;
    }

    public DateObject(Date date) {
    	super(DatePrototype.getDatePrototype());
        this.utcTime = date.getTime();
    }

    public DateObject(String str) {
    	super(DatePrototype.getDatePrototype());
        parse(str);
    }
    
    ////// Constructor
    //TODO: metadata...
    public boolean supportConstruct(){
        return true;
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        // Default ctor, today's date
        if(args.getCount()==0) {
            return new DateObject();
        }
        // Parametrized ctor
        if(args.getCount()==1) {
            FBSValue v = args.get(0);
            if( v.isNumber() ) {
                return new DateObject(v.longValue());
            }
            return new DateObject(v.stringValue());
        }
        // Parametrized ctor
        if(args.getCount()==0) {
            return new DateObject();
        }
        int year = args.get(0).intValue();
        int month = args.get(1).intValue();
        int date = args.isUndefined(2) ? 1 : args.get(2).intValue();
        int hours = args.isUndefined(3) ? 1 : args.get(3).intValue();
        int minutes = args.isUndefined(4) ? 1 : args.get(4).intValue();
        int seconds = args.isUndefined(5) ? 1 : args.get(5).intValue();
        int ms = args.isUndefined(6) ? 1 : args.get(6).intValue();
        return new DateObject(year,month,date,hours,minutes,seconds,ms);
    }
    
    public boolean supportCall() {
        return true;
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException {
        return FBSUtility.wrap((new DateObject()).toString());
    }
    
    public boolean equals( Object o ) {
    	if( o instanceof DateObject ) {
    		DateObject w = (DateObject)o;
    		return utcTime==w.utcTime;
    	}
    	return false;
    }

    public String getTypeAsString(){
        return "Date"; //$NON-NLS-1$
    }

    public String getName(){
        return "Date"; //$NON-NLS-1$
    }


    public String stringValue(){
       return this.toString();
    }


    public double numberValue() {
		return getTime();
	}

    public DateObject dateValueJS() {
        return this;
    }

	public boolean isJavaAssignableTo(Class c) {
		return java.util.Date.class.isAssignableFrom(c);
	}

	public FBSValue toFBSPrimitive() {
		//return FBSUtility.wrap(stringValue());
		return FBSNumber.get(this.valueOf());
	}

	public Object toJavaObject(Class c) {
		if (c == null) {
			return new java.sql.Timestamp(utcTime); // PHIL: default when, specifically for Variants...
		}
		if (c == java.util.Date.class) {
			return new java.util.Date(utcTime);
		}
		if (c == java.sql.Date.class) {
			return new java.sql.Date(utcTime);
		}
		if (c == java.sql.Time.class) {
			return new java.sql.Time(utcTime);
		}
		if (c == java.sql.Timestamp.class) {
			return new java.sql.Timestamp(utcTime);
		}
		throw new RuntimeException("Can't convert " + this.getClass().getName()
				+ " to " + c.getName());
	}
    
    /**
	 * Creates a new date object represented by the string 'str' in ISO format.
	 * 
	 * @param str,
	 *            string representing the date in ISO format.
	 * @return date object.
	 * @fbscript
	 */
    public static DateObject parseISO(String str)throws com.ibm.jscript.InterpretException{
        return new DateObject(com.ibm.jscript.ASTTree.ASTLiteral.parseDate(str));
    }

    /**
     * Creates a new date object represented by the string 'str'.
     * @param str, string representing the date in local format.
     * @return date object.
     * @fbscript
     */
    public static DateObject parse(String str){
        try{
            DateFormat df = DateFormat.getInstance();
            return new DateObject(df.parse(str));
        }catch(Exception e){
        }
        return null;
    }


    /**
     * Calculates the UTC time in milliseconds.
     * @param year
     * @param month
     * @return number of milliseconds representing the UTC time.
     * @fbscript
     */
    public static long UTC(int year, int month){
        long t = UTC(year,month,1,0,0,0,0);
        return t;
    }

    /**
     * Calculates the UTC time in milliseconds.
     * @param year
     * @param month
     * @param date
     * @return number of milliseconds representing the UTC time.
     * @fbscript
     */
    public static long UTC(int year, int month, int date){
        return UTC(year,month,date,0,0,0,0);
    }

    /**
     * Calculates the UTC time in milliseconds.
     * @param year
     * @param month
     * @param date
     * @param hours
     * @return number of milliseconds representing the UTC time.
     * @fbscript
     */
    public static long UTC(int year, int month, int date, int hours){
        return UTC(year,month,date,hours,0,0,0);
    }

    /**
     * Calculates the UTC time in milliseconds.
     * @param year
     * @param month
     * @param date
     * @param hours
     * @param minutes
     * @return number of milliseconds representing the UTC time.
     * @fbscript
     */
    public static long UTC(int year, int month, int date, int hours, int minutes){
        return UTC(year,month,date,hours,minutes,0,0);
    }

    /**
     * Calculates the UTC time in milliseconds.
     * @param year
     * @param month
     * @param date
     * @param hours
     * @param minutes
     * @param seconds
     * @return number of milliseconds representing the UTC time.
     * @fbscript
     */
    public static long UTC(int year, int month, int date, int hours, int minutes, int seconds){
        return UTC(year,month,date,hours,minutes,seconds,0);
    }

    /**
     * Calculates the UTC time in milliseconds.
     * @param year
     * @param month
     * @param date
     * @param hours
     * @param minutes
     * @param seconds
     * @param ms
     * @fbscript
     */
    public static long UTC(int year, int month, int date, int hours, int minutes, int seconds, int ms){
    	synchronized(utcCalendar) {
    		utcCalendar.set(year,month,date,hours,minutes,seconds);
    		utcCalendar.set(GregorianCalendar.MILLISECOND,ms);
            return utcCalendar.getMillis();
        }
    }

    /**
     * @return the UTC date of this date object.
     * @fbscript
     */
    public long valueOf(){
        return utcTime;
    }

    /**
     * @return the UTC date of this date object.
     * @fbscript
     */
    public long getTime(){
        return utcTime;
    }

    /**
     * @return the local year of this date object.
     * @fbscript
     */
    public int getYear(){
        return getFullYear();
    }

    /**
     * @return the local year of this date object.
     * @fbscript
     */
    public int getFullYear(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            return staticCalendar.get(Calendar.YEAR);
        }
    }

    /**
     * @return the UTC year of this date object.
     * @fbscript
     */
    public int getUTCFullYear(){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            return utcCalendar.get(Calendar.YEAR);
        }
    }

    /**
     * @return the local month of this date object.
     * @fbscript
     */
    public int getMonth(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            return staticCalendar.get(Calendar.MONTH);
        }
    }

    /**
     * @return the UTC month of this date object.
     * @fbscript
     */
    public int getUTCMonth(){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            return utcCalendar.get(Calendar.MONTH);
        }
    }

    /**
     * @return the local day of month of this date object.
     * @fbscript
     */
    public int getDate(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            return staticCalendar.get(Calendar.DAY_OF_MONTH);
        }
    }

    /**
     * @return the UTC day of month of this date object.
     * @fbscript
     */
    public int getUTCDate(){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            return utcCalendar.get(Calendar.DAY_OF_MONTH);
        }
    }

    /**
     * Get the day of the week.
     * The value is returned as an integer, where:
     * <ul>
     *   <li>0: Sunday
     *   <li>1: Monday
     *   <li>...
     * </ul>
     * @return the local day of week of this date object.
     * @fbscript
     */
    public int getDay(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            return staticCalendar.get(Calendar.DAY_OF_WEEK)-1;
        }
    }

    /**
     * @return the UTC day of week of this date object.
     * @fbscript
     */
    public int getUTCDay(){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            return utcCalendar.get(Calendar.DAY_OF_WEEK)-1;
        }
    }

    /**
     * @return the local hours of this date object.
     * @fbscript
     */
    public int getHour(){
        return getHours();
    }

    /**
     * @return the local hours of this date object.
     * @fbscript
     */
    public int getHours(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            return staticCalendar.get(Calendar.HOUR_OF_DAY);
        }
    }

    /**
     * @return the UTC hours of this date object.
     * @fbscript
     */
    public int getUTCHours(){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            return utcCalendar.get(Calendar.HOUR_OF_DAY);
        }
    }

    /**
     * @return the local minutes of this date object.
     * @fbscript
     */
    public int getMinute(){
        return getMinutes();
    }

    /**
     * @return the local minutes of this date object.
     * @fbscript
     */
    public int getMinutes(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            return staticCalendar.get(Calendar.MINUTE);
        }
    }

    /**
     * @return the UTC minutes of this date object.
     * @fbscript
     */
    public int getUTCMinutes(){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            return utcCalendar.get(Calendar.MINUTE);
        }
    }

    /**
     * @return the local seconds of this date object.
     * @fbscript
     */
    public int getSeconds(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            return staticCalendar.get(Calendar.SECOND);
        }
    }

    /**
     * @return the local seconds of this date object.
     * @fbscript
     */
    public int getSecond(){
        return getSeconds();
    }


    /**
     * @return the UTC seconds of this date object.
     * @fbscript
     */
    public int getUTCSeconds(){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            return utcCalendar.get(Calendar.SECOND);
        }
    }

    /**
     * @return the local milliseconds of this date object.
     * @fbscript
     */
    public int getMilliseconds(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            return staticCalendar.get(Calendar.MILLISECOND);
        }
    }

    /**
     * @return the UTC milliseconds of this date object.
     * @fbscript
     *
     */
    public int getUTCMilliseconds(){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            return utcCalendar.get(Calendar.MILLISECOND);
        }
    }

    /**
     * @return the time offset of the current geaographical area.
     * @fbscript
     */
    public int getTimezoneOffset(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            TimeZone tz = staticCalendar.getTimeZone();
            int offset;
        	if (tz.inDaylightTime(new Date(utcTime))) {
        	    offset = tz.getRawOffset() + tz.getDSTSavings();
        	} else {
        		return tz.getRawOffset();
        	}
            int minutes =  -(offset / 1000 / 60);
            return minutes;
    	}
    }

    /**
     * Sets a new time of this date object.
     * @param time, UTC time.
     * @return the new time.
     * @fbscript
     */
    public long setTime(long time){
        this.utcTime = time;
        return time;
    }

    /**
     * Sets the local milliseconds of this date object
     * @param ms, milliseconds in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setMilliseconds(int ms){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.MILLISECOND,ms);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC milliseconds of this date object
     * @param ms, milliseconds in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCMilliseconds(int ms){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.MILLISECOND,ms);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local seconds of this date object
     * @param sec, seconds in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setSeconds(int sec){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.SECOND,sec);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local milliseconds of this date object
     * @param sec, seconds in local time.
     * @param ms, milliseconds in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setSeconds(int sec, int ms){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.SECOND,sec);
            staticCalendar.set(Calendar.MILLISECOND,ms);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC seconds of this date object
     * @param sec, seconds in UTC time.
     * @return the new UTC time
     * @fbscript
     * @fbscript
     */
    public long setUTCSeconds(int sec){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.SECOND,sec);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC seconds of this date object
     * @param sec, seconds in UTC time.
     * @param ms, milliseconds in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCSeconds(int sec, int ms){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.SECOND,sec);
            utcCalendar.set(Calendar.MILLISECOND,ms);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local minutes of this date object
     * @param min, minutes in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setMinutes(int min){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.MINUTE,min);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local minutes of this date object
     * @param min, minutes in local time.
     * @param sec, seconds in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setMinutes(int min, int sec){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.MINUTE,min);
            staticCalendar.set(Calendar.SECOND,sec);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local minutes of this date object
     * @param min, minutes in local time.
     * @param sec, seconds in local time.
     * @param ms, milliseconds in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setMinutes(int min, int sec, int ms){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.MINUTE,min);
            staticCalendar.set(Calendar.SECOND,sec);
            staticCalendar.set(Calendar.MILLISECOND,ms);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC minutes of this date object
     * @param min, minutes in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCMinutes(int min){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.MINUTE,min);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC minutes of this date object
     * @param min, minutes in UTC time.
     * @param sec, seconds in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCMinutes(int min, int sec){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.MINUTE,min);
            utcCalendar.set(Calendar.SECOND,sec);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC minutes of this date object
     * @param min, minutes in UTC time.
     * @param sec, seconds in UTC time.
     * @param ms, milliseconds in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCMinutes(int min, int sec, int ms){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.MINUTE,min);
            utcCalendar.set(Calendar.SECOND,sec);
            utcCalendar.set(Calendar.MILLISECOND,ms);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local hours of this date object
     * @param hour, hour in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setHours(int hour){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.HOUR_OF_DAY,hour);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local hours of this date object
     * @param hour, hour in local time.
     * @param min, minutes in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setHours(int hour, int min){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.HOUR_OF_DAY,hour);
            staticCalendar.set(Calendar.MINUTE,min);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local hours of this date object
     * @param hour, hour in local time.
     * @param min, minutes in local time.
     * @param sec, seconds in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setHours(int hour, int min, int sec){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.HOUR_OF_DAY,hour);
            staticCalendar.set(Calendar.MINUTE,min);
            staticCalendar.set(Calendar.SECOND,sec);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local hours of this date object
     * @param hour, hour in local time.
     * @param min, minutes in local time.
     * @param sec, seconds in local time.
     * @param ms, milliseconds in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setHours(int hour, int min, int sec, int ms){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.HOUR_OF_DAY,hour);
            staticCalendar.set(Calendar.MINUTE,min);
            staticCalendar.set(Calendar.SECOND,sec);
            staticCalendar.set(Calendar.MILLISECOND,ms);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC hours of this date object
     * @param hour, hour in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCHours(int hour){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.HOUR_OF_DAY,hour);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC hours of this date object
     * @param hour, hour in UTC time.
     * @param min, minutes in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCHours(int hour, int min){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.HOUR_OF_DAY,hour);
            utcCalendar.set(Calendar.MINUTE,min);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC hours of this date object
     * @param hour, hour in UTC time.
     * @param min, minutes in UTC time.
     * @param sec, seconds in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCHours(int hour, int min, int sec){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.HOUR_OF_DAY,hour);
            utcCalendar.set(Calendar.MINUTE,min);
            utcCalendar.set(Calendar.SECOND,sec);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
      * Sets the UTC hours of this date object
     * @param hour, hour in UTC time.
     * @param min, minutes in UTC time.
     * @param sec, seconds in UTC time.
     * @param ms, milliseconds in UTC time.
     * @return the new UTC time
    * @fbscript
     */
    public long setUTCHours(int hour, int min, int sec, int ms){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.HOUR_OF_DAY,hour);
            utcCalendar.set(Calendar.MINUTE,min);
            utcCalendar.set(Calendar.SECOND,sec);
            utcCalendar.set(Calendar.MILLISECOND,ms);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local day of month of this date object
     * @param date, day of month in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setDate(int date){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.DAY_OF_MONTH,date);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC day of month of this date object
     * @param date, day of month in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCDate(int date){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.DAY_OF_MONTH,date);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local month of this date object
     * @param month, month in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setMonth(int month){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.MONTH,month);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local month of this date object
     * @param month, month in local time.
     * @param date, day of month in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setMonth(int month, int date){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.MONTH,month);
            staticCalendar.set(Calendar.DAY_OF_MONTH,date);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC month of this date object
     * @param month, month in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCMonth(int month){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.MONTH,month);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC month of this date object
     * @param month, month in UTC time.
     * @param date, day of month in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCMonth(int month, int date){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.MONTH,month);
            utcCalendar.set(Calendar.DAY_OF_MONTH,date);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local year of this date object
     * @param year, year in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setFullYear(int year){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.YEAR,year);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local year of this date object
     * @param year, year in local time.
     * @param month, month in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setFullYear(int year, int month){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.YEAR,year);
            staticCalendar.set(Calendar.MONTH,month);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the local year of this date object
     * @param year, year in local time.
     * @param month, month in local time.
     * @param date, day of month in local time.
     * @return the new UTC time
     * @fbscript
     */
    public long setFullYear(int year, int month, int date){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            staticCalendar.set(Calendar.YEAR,year);
            staticCalendar.set(Calendar.MONTH,month);
            staticCalendar.set(Calendar.DAY_OF_MONTH,date);
            this.utcTime = staticCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC year of this date object
     * @param year, year in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCFullYear(int year){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.YEAR,year);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC year of this date object
     * @param year, year in UTC time.
     * @param month, month in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCFullYear(int year, int month){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.YEAR,year);
            utcCalendar.set(Calendar.MONTH,month);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * Sets the UTC year of this date object
     * @param year, year in UTC time.
     * @param month, month in UTC time.
     * @param date, day of month in UTC time.
     * @return the new UTC time
     * @fbscript
     */
    public long setUTCFullYear(int year, int month, int date){
    	synchronized(utcCalendar) {
    		utcCalendar.setMillis(utcTime);
            utcCalendar.set(Calendar.YEAR,year);
            utcCalendar.set(Calendar.MONTH,month);
            utcCalendar.set(Calendar.DAY_OF_MONTH,date);
            this.utcTime = utcCalendar.getMillis();
            return utcTime;
        }
    }

    /**
     * @return a string representing the date of this date object.
     * @fbscript
     */
    public String toString(){
        String s = DateFormat.getInstance().format(new Date(utcTime));
        return s;
    }

    /**
     * @return a string representing the date of this date object.
     * @fbscript
     */
    public String toDateString(){
        return DateFormat.getDateInstance().format(new Date(utcTime));
    }

    /**
     * @return a string representing the time of this date object.
     * @fbscript
     */
    public String toTimeString(){
        return DateFormat.getTimeInstance().format(new Date(utcTime));
    }

    /**
     * @return a string representing the date of this date object, in local format.
     * @fbscript
     */
    public String toLocaleString(){
        return DateFormat.getDateTimeInstance().format(new Date(utcTime));
    }

    /**
     * @return a string representing the date of this date object, in local format.
     * @fbscript
     */
    public String toLocaleDateString(){
        return DateFormat.getDateInstance().format(new Date(utcTime));
    }

    /**
     * @return a string representing the time this date object, in local format.
     * @fbscript
     */
    public String toLocaleTimeString(){
        return DateFormat.getTimeInstance().format(new Date(utcTime));
    }

    /**
     * @return a string representing the date of this date object, in UTC format.
     * @fbscript
     */
    public String toUTCString(){
        return UTC_FORMAT.format(new Date(utcTime));
    }

    /**
     * @return a string representing the date of this date object, in ISO format.
     * @fbscript
     */
    public String toISOString(){
    	synchronized(staticCalendar) {
    		staticCalendar.setMillis(utcTime);
            FastStringBuffer sb=new FastStringBuffer();
            int n;
            n=staticCalendar.get(Calendar.YEAR);
            if (n<10) {
                sb.append("000"); //$NON-NLS-1$
            } else if (n<100) {
                sb.append("00"); //$NON-NLS-1$
            } else if (n<1000) {
                sb.append('0');
            }
            sb.append(n);
            sb.append('-');
            n=staticCalendar.get(Calendar.MONTH)+(1-Calendar.JANUARY);
            if (n<10) {
                sb.append('0');
            }
            sb.append(n);
            sb.append('-');
            n=staticCalendar.get(Calendar.DAY_OF_MONTH);
            if (n<10) {
                sb.append('0');
            }
            sb.append(n);
            sb.append('-');
            n=staticCalendar.get(Calendar.HOUR);
            if (n<10) {
                sb.append('0');
            }
            sb.append(n);
            sb.append('.');
            n=staticCalendar.get(Calendar.MINUTE);
            if (n<10) {
                sb.append('0');
            }
            sb.append(n);
            sb.append('.');
            n=staticCalendar.get(Calendar.SECOND);
            if (n<10) {
                sb.append('0');
            }
            sb.append(n);
            return sb.toString();
        }
    }

    public DateObject adjust(int years, int months, int days, int hours, int minutes, int seconds, int ms, boolean localTime ) {
    	GregorianCalendarEx cal = localTime ? staticCalendar : utcCalendar;
    	synchronized(cal) {
    		cal.setMillis(utcTime);
    		cal.add(GregorianCalendar.YEAR, years);
    		cal.add(GregorianCalendar.MONTH, months);
    		cal.add(GregorianCalendar.DAY_OF_MONTH, days);
    		cal.add(GregorianCalendar.HOUR, hours);
    		cal.add(GregorianCalendar.MINUTE, minutes);
    		cal.add(GregorianCalendar.SECOND, seconds);
            DateObject result = new DateObject(cal.getMillis());
            return result;
    	}
    }

    public static void main(String[] args){

    com.ibm.jscript.util.TDiag.getOutputStream().println("test : "+NumberFormat.getInstance().format(34000.5600)); //$NON-NLS-1$
    Locale.setDefault(Locale.US);
    com.ibm.jscript.util.TDiag.getOutputStream().println("test : "+NumberFormat.getInstance().format(34000.5600)); //$NON-NLS-1$

        try {


            SimpleDateFormat dfSimple = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z"); //$NON-NLS-1$

            Date sansTZ = dfSimple.parse("01/01/1970 00:00:00"); //$NON-NLS-1$
//            Date enGMT = df.parse("01/01/1970 00:00:00 +0000");
//            Date enFrance = df.parse("01/01/1970 02:00:00 +0100");
            Date enGMT = df.parse("01/01/1970 00:00:00 GMT"); //$NON-NLS-1$
            Date enFrance = df.parse("01/01/1970 00:00:00 CET"); //$NON-NLS-1$
            Date aLA = df.parse("01/01/1970 00:00:00 PST"); //$NON-NLS-1$

            com.ibm.jscript.util.TDiag.getOutputStream().println("test : "+sansTZ.getTime()); //$NON-NLS-1$
            com.ibm.jscript.util.TDiag.getOutputStream().println("test : "+enGMT.getTime()); //$NON-NLS-1$
            com.ibm.jscript.util.TDiag.getOutputStream().println("test : "+enFrance.getTime()); //$NON-NLS-1$
            com.ibm.jscript.util.TDiag.getOutputStream().println("test : "+aLA.getTime()); //$NON-NLS-1$

        }catch(Exception e){com.ibm.jscript.util.TDiag.exception(e);}



//        Locale.setDefault(Locale.FRANCE);
//        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        DateObject dob = new DateObject(2002,9,27,0, 0, 0, 0);
        long l = dob.getTime();
//        dob.setHours()
        DateObject dob2 = new DateObject(2002,9,27,4, 0, 0, 0);
        long l2 = dob2.getTime();
        long l3 = l2-l;

        com.ibm.jscript.util.TDiag.getOutputStream().println("test diff : "+ l3/3600000 ); //$NON-NLS-1$

//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test getTime : "+dob.getTime());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test tostring : "+dob.toString());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("nb jours : "+ (l/86400000));

//        DateObject dob2 = new DateObject(2002,9,27,0, 0, 0);
//        long l2 = dob.getTime();
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test getTime : "+dob2.getTime());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test tostring : "+dob2.toString());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("nb jours : "+ (l/86400000));


        GregorianCalendar cal = new GregorianCalendar(2002,9,27,0, 0, 0);
        com.ibm.jscript.util.TDiag.getOutputStream().println("test dst : "+cal.get(Calendar.DST_OFFSET)); //$NON-NLS-1$
        com.ibm.jscript.util.TDiag.getOutputStream().println("test zone offset : "+cal.get(Calendar.ZONE_OFFSET)); //$NON-NLS-1$

        java.util.Date javad = cal.getTime();
        com.ibm.jscript.util.TDiag.getOutputStream().println("test before : "+javad.toString()); //$NON-NLS-1$
        long t = javad.getTime();

        cal.add(Calendar.HOUR, 4);
        java.util.Date after = cal.getTime();
        com.ibm.jscript.util.TDiag.getOutputStream().println("test after : "+after.toString()); //$NON-NLS-1$

        GregorianCalendar cal2 = new GregorianCalendar(2002,9,27,4, 0, 0);
        com.ibm.jscript.util.TDiag.getOutputStream().println("test dst : "+cal2.get(Calendar.DST_OFFSET)); //$NON-NLS-1$
        com.ibm.jscript.util.TDiag.getOutputStream().println("test zone offset : "+cal2.get(Calendar.ZONE_OFFSET)); //$NON-NLS-1$
        java.util.Date javad2 = cal2.getTime();
        long t2 = javad2.getTime();
        long t3 = t2-l;
        com.ibm.jscript.util.TDiag.getOutputStream().println("test diff : "+ t3/3600000 ); //$NON-NLS-1$
//        javad.compareTo()




//
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("autre test -------");
//        DateObject dob = new DateObject(1995,11, 31, 23, 59, 0);
//        long l = dob.getTime();
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test getTime : "+dob.getTime());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test tostring : "+dob.toString());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("nb jours : "+ (l/86400000));

//        GregorianCalendar cal = new GregorianCalendar(1995,11, 31, 23, 59, 0);
//        java.util.Date javad = cal.getTime();
//        long l2 = javad.getTime();
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test getTime : "+javad.getTime());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test tostring : "+javad.toString());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("nb jours : "+ (l2/86400000));

//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test 1 : "+dob.toLocaleString());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test 1 : "+dob.toLocaleDateString());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test 1 : "+dob.toLocaleTimeString());
//        DateObject dd = parse("1/14/03 4:33 AM");
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test 4 : "+dd.toString());
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("test 4 : "+dd.toLocaleString());
//        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
//        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("Test : "+   df.format(new Date()));
//        DateFormat df1 = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("Test2 : "+   df1.format(new Date()));
//        DateFormat dfa = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT, Locale.US);
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("Test3 : "+   dfa.format(new Date()));
//        DateFormat dfz = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG, Locale.US);
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("Test4 : "+   dfz.format(new Date()));
//        DateFormat dfe = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("Test5 : "+   dfe.format(new Date()));
//        DateFormat dfr = DateFormat.getTimeInstance(DateFormat.LONG, Locale.US);
//        com.ibm.workplace.designer.util.TDiag.getOutputStream().println("Test6 : "+   dfr.format(new Date()));


//        TestUTCDates();
//        TestLocalDates();
//        TestLocalDateString();
//        TestUTCDateString();
//        TestCurrentTime();
//        TestDiffDates();
        DateObject dobj = new DateObject(2002,0,1,0,0,0,0);
        Date d = new Date();
//        for (int i=1;i<30;i++){
/*
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(i);
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(dobj.toDateString());
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(dobj.toLocaleDateString());
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(dobj.getDay());
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(dobj.getMonth());
            com.ibm.workplace.designer.util.TDiag.getOutputStream().println("-------------------------------------");
            dobj = new DateObject((long)(dobj.numberValue()+msPerDay));
*/
//            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(i);
//            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(dobj.toDateString());
//            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(dobj.toLocaleDateString());
//            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(dobj.getDay());
//            com.ibm.workplace.designer.util.TDiag.getOutputStream().println(dobj.getMonth());
//            com.ibm.workplace.designer.util.TDiag.getOutputStream().println("-------------------------------------");
//            dobj = new DateObject((long)(dobj.numberValue()+msPerDay));
//
//        }
    }

    private static void TestUTCDates(){
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
        DateObject dobj = new DateObject();
        com.ibm.jscript.util.TDiag.getOutputStream().println("offset = "+dobj.getTimezoneOffset()); //$NON-NLS-1$
        for(int y =1970; y<2010; y++){   // year
            com.ibm.jscript.util.TDiag.getOutputStream().println(y);
            for(int m=0; m<12; m++){ // month
                for(int d=1;d<31;d++){
                    c.set(y,m,d,0,0,0);
                    long tc = c.getTime().getTime();
                    long td = dobj.UTC(y,m,d,0,0,0,0);
                    if (Math.abs(tc-td)>=1000){
                        com.ibm.jscript.util.TDiag.getOutputStream().print(y+" , "+m+" , "+d+" : "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        com.ibm.jscript.util.TDiag.getOutputStream().print("JavaUTCDate = "+tc+" , EcmaUTCDate = "+td); //$NON-NLS-1$ //$NON-NLS-2$

                        long dif = tc-td;
                        com.ibm.jscript.util.TDiag.getOutputStream().print("  <<<<<<<<<<<< ("+dif+")"); //$NON-NLS-1$ //$NON-NLS-2$
                        com.ibm.jscript.util.TDiag.getOutputStream().print("\r\n"); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    private static void TestLocalDates(){
        Calendar c = Calendar.getInstance();
        DateObject dobj = new DateObject();
        com.ibm.jscript.util.TDiag.getOutputStream().println("offset = "+dobj.getTimezoneOffset()); //$NON-NLS-1$
        for(int y =1970; y<2010; y++){   // year
            com.ibm.jscript.util.TDiag.getOutputStream().println(y);
            for(int m=0; m<12; m++){ // month
                for(int d=1;d<31;d++){
                    c.set(y,m,d,0,0,0);
                    long tc = c.getTime().getTime();
                    dobj = new DateObject(y,m,d,0,0,0,0);
                    long td = dobj.getTime();

                    if (Math.abs(tc-td)>=1000){
                        com.ibm.jscript.util.TDiag.getOutputStream().print(y+" , "+m+" , "+d+" : "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        com.ibm.jscript.util.TDiag.getOutputStream().print("JavaLocalDate = "+tc+" , EcmaLocalDate = "+td); //$NON-NLS-1$ //$NON-NLS-2$

                        long dif = tc-td;
                        com.ibm.jscript.util.TDiag.getOutputStream().print("  <<<<<<<<<<<< ("+dif+")"); //$NON-NLS-1$ //$NON-NLS-2$
                        com.ibm.jscript.util.TDiag.getOutputStream().print("\n"); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    private static void TestLocalDateString(){
        Calendar c = Calendar.getInstance();
        DateObject dobj = new DateObject();
        DateFormat df = DateFormat.getInstance();
        com.ibm.jscript.util.TDiag.getOutputStream().println("offset = "+dobj.getTimezoneOffset()); //$NON-NLS-1$
        for(int y =1970; y<2010; y++){   // year
            com.ibm.jscript.util.TDiag.getOutputStream().println(y);
            for(int m=0; m<12; m++){ // month
                for(int d=1;d<31;d++){
                    c.set(y,m,d,0,0,0);
                    long tc = c.getTime().getTime();
                    dobj = new DateObject(y,m,d,0,0,0,0);
                    long td = dobj.getTime();
                    com.ibm.jscript.util.TDiag.getOutputStream().println(y+" , "+m+" , "+d+" : "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    com.ibm.jscript.util.TDiag.getOutputStream().println("EcmaLocalTime = "+dobj.toLocaleString()); //$NON-NLS-1$
                    com.ibm.jscript.util.TDiag.getOutputStream().println("JavaLocalTime = "+df.getDateTimeInstance().format(c.getTime())); //$NON-NLS-1$
                    com.ibm.jscript.util.TDiag.getOutputStream().println("========================================"); //$NON-NLS-1$
                }
            }
        }
    }

    private static void TestUTCDateString(){
        Calendar c = Calendar.getInstance();
        DateObject dobj = new DateObject();
        DateFormat df = DateFormat.getInstance();
        com.ibm.jscript.util.TDiag.getOutputStream().println("offset = "+dobj.getTimezoneOffset()); //$NON-NLS-1$
        for(int y =1970; y<2010; y++){   // year
            com.ibm.jscript.util.TDiag.getOutputStream().println(y);
            for(int m=0; m<12; m++){ // month
                for(int d=1;d<31;d++){
                    c.set(y,m,d,0,0,0);
                    long tc = c.getTime().getTime();
                    dobj = new DateObject(y,m,d,0,0,0,0);
                    long td = dobj.getTime();
                    com.ibm.jscript.util.TDiag.getOutputStream().println(y+" , "+m+" , "+d+" : "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    com.ibm.jscript.util.TDiag.getOutputStream().println("EcmaLocalTime = "+dobj.toLocaleString()); //$NON-NLS-1$
                    com.ibm.jscript.util.TDiag.getOutputStream().println("JavaLocalTime = "+df.getDateTimeInstance().format(c.getTime())); //$NON-NLS-1$
                    com.ibm.jscript.util.TDiag.getOutputStream().println("EcmaLocalTime = "+dobj.toUTCString()); //$NON-NLS-1$
                    com.ibm.jscript.util.TDiag.getOutputStream().println("========================================"); //$NON-NLS-1$
                }
            }
        }
    }

    private static void TestCurrentTime(){
        Calendar c = Calendar.getInstance();
        c.set(2001,10,30,13,35,0);
        DateObject dobj = new DateObject(2001,10,30,13,35,0,0);
        DateFormat df = DateFormat.getInstance();

        com.ibm.jscript.util.TDiag.getOutputStream().println("Java \n===="); //$NON-NLS-1$
        com.ibm.jscript.util.TDiag.getOutputStream().println("UTC ms     = "+c.getTime().getTime()); //$NON-NLS-1$
        com.ibm.jscript.util.TDiag.getOutputStream().println("Local date = "+df.getDateTimeInstance().format(c.getTime())); //$NON-NLS-1$


        com.ibm.jscript.util.TDiag.getOutputStream().println("ECMA \n===="); //$NON-NLS-1$
        com.ibm.jscript.util.TDiag.getOutputStream().println("UTC ms     = "+dobj.getTime()); //$NON-NLS-1$
        com.ibm.jscript.util.TDiag.getOutputStream().println("Local date = "+dobj.toLocaleString()); //$NON-NLS-1$
        com.ibm.jscript.util.TDiag.getOutputStream().println("UTC   date = "+dobj.toUTCString()); //$NON-NLS-1$
    }

    public static void TestDiffDates(){
        com.ibm.jscript.util.TDiag.getOutputStream().println("Start"); //$NON-NLS-1$
        long max=0,min=0;
        for(int i=0;i<10000;i++){
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
            DateObject dobj = new DateObject();
            c.set(1970,0,1,0,0,0);
            long tc = c.getTime().getTime();
            long td = dobj.UTC(1970,0,1,0,0,0,0);
            long dif = Math.abs(tc-td);
            max = Math.max(max,dif);
            min = Math.min(min,dif);
            if (dif>=1000){
//                long dif = tc-td;
                com.ibm.jscript.util.TDiag.getOutputStream().print("  <<<<<<<<<<<< ("+dif+")"); //$NON-NLS-1$ //$NON-NLS-2$

                com.ibm.jscript.util.TDiag.getOutputStream().print("\n"); //$NON-NLS-1$
            }
        }
       com.ibm.jscript.util.TDiag.getOutputStream().println("finish"); //$NON-NLS-1$
       com.ibm.jscript.util.TDiag.getOutputStream().println("max = "+max); //$NON-NLS-1$
       com.ibm.jscript.util.TDiag.getOutputStream().println("min = "+min); //$NON-NLS-1$

    }

}