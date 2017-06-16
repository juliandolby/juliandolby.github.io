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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Some date and time manipulation.
 * This class handles the standard java.util.Date class as well as its extensions, found in
 * the java.sql package.<BR>
 */
public final class DateUtilities {

    /**
     * java.util.Date gregorian structure representation.
     */
    public static class DateStruct {
        public DateStruct() {
            this( System.currentTimeMillis() );
        }
        /**
         * @param year - the normal year (ex : 2003)
         * @param month - the month between 1-12.
         */
        public DateStruct( int year, int month, int day, int hour, int minute, int second, int millis ) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            this.millis = millis;
        }
        public DateStruct( int year, int month, int day, int hour, int minute, int second ) {
            this( year, month, day, hour, minute, second, 0 );
        }
        public DateStruct( java.util.Date dt ) {
            this( dt.getTime() );
        }
        public DateStruct( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                this.year = gregorianCalendar.get(Calendar.YEAR);
                this.month = gregorianCalendar.get(Calendar.MONTH)+1;
                this.day = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
                this.hour = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
                this.minute = gregorianCalendar.get(Calendar.MINUTE);
                this.second = gregorianCalendar.get(Calendar.SECOND);
                this.millis = gregorianCalendar.get(Calendar.MILLISECOND);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
//        public String toISOString() {
//            return   TString.format( "{0}-{1}-{2}",
//                        TString.toString(year),
//                        TString.toString(month),
//                        TString.toString(day))
//                    + TString.format( " {0}:{1}:{2}.{3}",
//                        TString.toString(hour),
//                        TString.toString(minute),
//                        TString.toString(second)
//                        TString.toString(millis) );
//        }
        public java.util.Date createDate() {
            return createDate(year,month,day,hour,minute,second,millis);
        }
        public static final java.util.Date createDate( int year, int month, int day, int hour, int minute, int second ) {
            return createDate(year,month,day,hour,minute,second,0);
        }
        public static final java.util.Date createDate( int year, int month, int day, int hour, int minute, int second, int millis ) {
            return new java.util.Date( createDateAsLong(year,month,day,hour,minute,second,millis) );
        }
        /**
         *
         * @param y The year (already adjusted, no need to add 1900 or 2000)
         */
        public static final long createDateAsLong( int y, int m, int d, int h, int n, int s, int ms ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.set(y,m-1,d,h,n,s);
                gregorianCalendar.set(GregorianCalendar.MILLISECOND,ms);
                return gregorianCalendar.getMillis();
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        /**
         *
         * @param y The year (already adjusted, no need to add 1900 or 2000)
         */
        public static final long createDateAsLong( int y, int m, int d, int h, int n, int s ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.set(y,m-1,d,h,n,s);
                gregorianCalendar.set(GregorianCalendar.MILLISECOND,0);
                return gregorianCalendar.getMillis();
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public String toString() {
            return toString( year, month, day, hour, minute, second, millis );
        }
        public static String toString( long ms ) {
            return new DateStruct(ms).toString();
        }
        public static String toString( java.util.Date dt ) {
            // return TFormatter.getDefaultDateFormatter().format(dt);
            return new DateStruct(dt).toString();
        }
        public static String toString(int year, int month, int day, int hour, int minute, int second, int millis) {
            return   StringUtil.format( "{0}-{1}-{2}", //$NON-NLS-1$
                        StringUtil.toString(year),
                        StringUtil.toString(month),
                        StringUtil.toString(day))
                    + StringUtil.format( " {0}:{1}:{2}", //.{3}", //$NON-NLS-1$
                        StringUtil.toString(hour),
                        StringUtil.toString(minute),
                        StringUtil.toString(second) );
//                        TString.toString(millis));
        }
        public static int getYear( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                return gregorianCalendar.get(Calendar.YEAR);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public static int getYear( java.util.Date dt ) {
            return getYear(dt.getTime());
        }
        public static int getMonth( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                return gregorianCalendar.get(Calendar.MONTH)+1;
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public static int getMonth( java.util.Date dt ) {
            return getMonth(dt.getTime());
        }
        public static int getDay( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                return gregorianCalendar.get(Calendar.DAY_OF_MONTH);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public static int getDay( java.util.Date dt ) {
            return getDay(dt.getTime());
        }
        public static int getHour( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                return gregorianCalendar.get(Calendar.HOUR_OF_DAY);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public static int getHour( java.util.Date dt ) {
            return getHour(dt.getTime());
        }
        public static int getMinute( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                return gregorianCalendar.get(Calendar.MINUTE);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public static int getMinute( java.util.Date dt ) {
            return getMinute(dt.getTime());
        }
        public static int getSecond( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                return gregorianCalendar.get(Calendar.SECOND);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public static int getSecond( java.util.Date dt ) {
            return getSecond(dt.getTime());
        }
        public static int getMilliSecond( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                return gregorianCalendar.get(Calendar.MILLISECOND);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public static int getMilliSecond( java.util.Date dt ) {
            return getMilliSecond(dt.getTime());
        }
        public int day;
        public int month;
        public int year;
        public int hour;
        public int minute;
        public int second;
        public int millis;
    }

    /**
     * java.sql.Date gregorian structure representation.
     */
    public static class SQLDateStruct {
        public SQLDateStruct( int year, int month, int day ) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
        public SQLDateStruct( java.sql.Date dt ) {
            this( dt.getTime() );
        }
        public SQLDateStruct( long dt ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(dt);
                this.year = gregorianCalendar.get(Calendar.YEAR);
                this.month = gregorianCalendar.get(Calendar.MONTH)+1;
                this.day = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public java.sql.Date createDate() {
            return createDate(year,month,day);
        }
        public static java.sql.Date createDate(int year, int month, int day) {
            return new java.sql.Date( createDateAsLong(year,month,day) );
        }
        /**
         *
         * @param y The year (already adjusted, no need to add 1900 or 2000)
         */
        public static final long createDateAsLong( int y, int m, int d ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.set(y,m-1,d,0,0,0);
                gregorianCalendar.set(GregorianCalendar.MILLISECOND,0);
                return gregorianCalendar.getMillis();
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public String toString() {
            return toString(year,month,day);
        }
        public static String toString( long ms ) {
            return new SQLDateStruct(ms).toString();
        }
        public static String toString( java.sql.Date dt ) {
            // return TFormatter.getDefaultDateFormatter().format(createDate());
            return new SQLDateStruct(dt).toString();
        }
        public static String toString( int year, int month, int day ) {
            //  return TFormatter.getDefaultDateFormatter().format(createDate());
            return StringUtil.format( "{0}-{1}-{2}", //$NON-NLS-1$
                        StringUtil.toString(year),
                        StringUtil.toString(month),
                        StringUtil.toString(day));
        }
        public int day;
        public int month;
        public int year;
    }

    /**
     * java.sql.Time gregorian structure representation.
     */
    public static class SQLTimeStruct {
        public SQLTimeStruct( int hour, int minute, int second ) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }
        public SQLTimeStruct( java.sql.Time ti ) {
            this( ti.getTime() );
        }
        public SQLTimeStruct( long ti ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(ti);
                this.hour = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
                this.minute = gregorianCalendar.get(Calendar.MINUTE);
                this.second = gregorianCalendar.get(Calendar.SECOND);
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public java.sql.Time createTime() {
            return createTime(hour,minute,second);
        }
        public static final java.sql.Time createTime( int hour, int minute, int second ) {
            return new java.sql.Time( createTimeAsLong(hour,minute,second) );
        }
        public static final long createTimeAsLong( int h, int n, int s, int ms ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.set(0,0,0,h,n,s);
                gregorianCalendar.set(GregorianCalendar.MILLISECOND,ms);
                return gregorianCalendar.getMillis();
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public static final long createTimeAsLong( int h, int n, int s ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.set(0,0,0,h,n,s);
                gregorianCalendar.set(GregorianCalendar.MILLISECOND,0);
                return gregorianCalendar.getMillis();
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public String toString() {
            return toString(hour,minute,second);
        }
        public static String toString( long ms ) {
            return new SQLTimeStruct(ms).toString();
        }
        public static String toString( java.sql.Time ti ) {
            // return TFormatter.getDefaultTimeFormatter().format(ti);
            return new SQLTimeStruct(ti).toString();
        }
        public static String toString(int hour, int minute, int second) {
            // return TFormatter.getDefaultTimeFormatter().format(createTime());
            return StringUtil.format( "{0}:{1}:{2}", //$NON-NLS-1$
                        StringUtil.toString(hour),
                        StringUtil.toString(minute),
                        StringUtil.toString(second) );
        }
        public int hour;
        public int minute;
        public int second;
    }

    /**
     * java.sql.Timestamp gregorian structure representation.
     */
    public static class SQLDatetimeStruct {
        public SQLDatetimeStruct( int year, int month, int day, int hour, int minute, int second, int nano ) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            this.nano = nano;
        }
        public SQLDatetimeStruct( int year, int month, int day, int hour, int minute, int second ) {
            this( year, month, day, hour, minute, second, 0 );
        }
        public SQLDatetimeStruct( java.sql.Timestamp ts ) {
            this( ts.getTime(), ts.getNanos() );
        }
        public SQLDatetimeStruct( long ts, int nano ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.setMillis(ts);
                this.year = gregorianCalendar.get(Calendar.YEAR);
                this.month = gregorianCalendar.get(Calendar.MONTH)+1;
                this.day = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
                this.hour = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
                this.minute = gregorianCalendar.get(Calendar.MINUTE);
                this.second = gregorianCalendar.get(Calendar.SECOND);
                this.nano = nano;
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public SQLDatetimeStruct( long ts ) {
            this( ts, 0 );
        }
        public java.sql.Timestamp createDatetime() {
            return createDatetime( year, month, day, hour, minute, second, nano );
        }
        public static final java.sql.Timestamp createDatetime( int year, int month, int day, int hour, int minute, int second ) {
            return new java.sql.Timestamp( createDatetimeAsLong(year,month,day,hour,minute,second) );
        }
        public static final java.sql.Timestamp createDatetime( int year, int month, int day, int hour, int minute, int second, int nano ) {
            java.sql.Timestamp ts = new java.sql.Timestamp( createDatetimeAsLong(year,month,day,hour,minute,second) );
            if( nano!=0 ) {
                ts.setNanos(nano);
            }
            return ts;
        }
        /**
         *
         * @param y The year (already adjusted, no need to add 1900 or 2000)
         */
        public static final long createDatetimeAsLong( int y, int m, int d, int h, int n, int s, int ms ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.set(y,m-1,d,h,n,s);
                gregorianCalendar.set(GregorianCalendar.MILLISECOND,ms);
                return gregorianCalendar.getMillis();
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        /**
         *
         * @param y The year (already adjusted, no need to add 1900 or 2000)
         */
        public static final long createDatetimeAsLong( int y, int m, int d, int h, int n, int s ) {
            TGregorianCalendar gregorianCalendar = getCalendar();
            try {
                gregorianCalendar.set(y,m-1,d,h,n,s);
                gregorianCalendar.set(GregorianCalendar.MILLISECOND,0);
                return gregorianCalendar.getMillis();
            } finally {
                recycleCalendar(gregorianCalendar);
            }
        }
        public String toString() {
            return toString(year,month,day,hour,minute,second,nano);
        }
        public static String toString( long ms ) {
            return new SQLDatetimeStruct(ms).toString();
        }
        public static String toString( long ms, int nano ) {
            return new SQLDatetimeStruct(ms,nano).toString();
        }
        public static String toString( java.sql.Timestamp ts ) {
//             return TFormatter.getDefaultDatetimeFormatter().format(ts);
            return new SQLDatetimeStruct(ts).toString();
        }
        public static String toString( int year, int month, int day, int hour, int minute, int second, int nano ) {
            // return TFormatter.getDefaultDatetimeFormatter().format(createDatetime());
            return   StringUtil.format( "{0}-{1}-{2}", //$NON-NLS-1$
                        StringUtil.toString(year),
                        StringUtil.toString(month),
                        StringUtil.toString(day))
                    + StringUtil.format( " {0}:{1}:{2}", //.{3}", //$NON-NLS-1$
                        StringUtil.toString(hour),
                        StringUtil.toString(minute),
                        StringUtil.toString(second) );
                        //TString.toString(nano));
        }
        public int day;
        public int month;
        public int year;
        public int hour;
        public int minute;
        public int second;
        public int nano;
    }

    /**
     * Create a date from gregorian parameters.
     * @return a java.sql.Date object
     */
    public static final java.sql.Date createDate( int year, int month, int day ) {
        return SQLDateStruct.createDate(year,month,day);
    }

    /**
     * Create a time from gregorian parameters.
     * @return a java.sql.Time object
     */
    public static final java.sql.Time createTime( int hour, int minute, int second ) {
        return SQLTimeStruct.createTime(hour, minute, second);
    }

    /**
     * Create a timestamp from gregorian parameters.
     * @return a java.sql.Timestamp object
     */
    public static final java.sql.Timestamp createTimestamp( int year, int month, int day, int hour, int minute, int second, int nano ) {
        return SQLDatetimeStruct.createDatetime(year, month, day, hour, minute, second, nano);
    }

    /**
     * Create a timestamp from gregorian parameters.
     * @return a java.sql.Timestamp object
     */
    public static final java.sql.Timestamp createTimestamp( int year, int month, int day, int hour, int minute, int second ) {
        return SQLDatetimeStruct.createDatetime(year, month, day, hour, minute, second, 0);
    }

    /**
     * Convert a date to a datetime.
     */
    public static final java.sql.Timestamp convertDateToDatetime( java.sql.Date dt ) {
        SQLDateStruct s = new SQLDateStruct(dt);
        return SQLDatetimeStruct.createDatetime( s.year, s.month, s.day, 0, 0, 0 );
    }

    /**
     * Convert a datetime to a date.
     */
    public static final java.sql.Date convertDatetimeToDate( java.sql.Timestamp ts ) {
        SQLDatetimeStruct s = new SQLDatetimeStruct(ts);
        return SQLDateStruct.createDate( s.year, s.month, s.day );
    }

    /**
     * Convert a time to a datetime.
     */
    public static final java.sql.Timestamp convertTimeToDatetime( java.sql.Time ti ) {
        SQLTimeStruct s = new SQLTimeStruct(ti);
        return SQLDatetimeStruct.createDatetime( 0, 0, 0, s.hour, s.minute, s.second );
    }

    /**
     * Convert a datetime to a time.
     */
    public static final java.sql.Time convertDatetimeToTime( java.sql.Timestamp ts ) {
        SQLDatetimeStruct s = new SQLDatetimeStruct(ts);
        return SQLTimeStruct.createTime( s.hour, s.minute, s.second );
    }


    /**
     * Parse an ISO date.
     * The format is supposed to be smthg like YYYY-MM-DDTHH:NN:SS.
     * Anyway, if the year is composed by only 2 characters, the value is adjusted (+1900 or +2000).
     *
     * This method is used for example to read a default value in an input bean. If the date cannot be
     * parsed or is a silly date, the default value will be accepted anyway.
     */
    public static long createDate(String s) {
        int year=0, month=0, day=0;
        int hour=0, minute=0, second=0, nanos = 0;
        StringParser parser = new StringParser(s);

        // Begin by the date, without any leading char
        int firstInteger = parser.getNextInteger();

        if( firstInteger!=Integer.MIN_VALUE ) {
            // Convert the year if and only if 2 characters
            if (parser.getCurrentPosition() <= 2) {
                year = convertYearIfInf100(firstInteger);
            } else {
                year=firstInteger;
            }
            if( !parser.match('-') && !parser.match('.')&& !parser.match('/') ) { return Long.MIN_VALUE; }

            month = parser.getNextInteger();
            if( month==Integer.MIN_VALUE ) { return Long.MIN_VALUE; }
            if( !parser.match('-') && !parser.match('.')&& !parser.match('/') ) { return Long.MIN_VALUE; }

            day = parser.getNextInteger();
            if( day==Integer.MIN_VALUE ) { return Long.MIN_VALUE; }
        }

        // Then, check if the time is valid, leading with a 'T'
        if( !parser.isEOF() ) {
            if( !parser.match('T') && !parser.match('t') && !parser.match(' ') && !parser.match('-') ) { return Long.MIN_VALUE; }

            hour = parser.getNextInteger();
            if( hour==Integer.MIN_VALUE ) { return Long.MIN_VALUE; }
            if( !parser.match(':') && !parser.match('.') ) { return Long.MIN_VALUE; }

            minute = parser.getNextInteger();
            if( minute==Integer.MIN_VALUE ) { return Long.MIN_VALUE; }
            if( !parser.match(':') && !parser.match('.') ) { return Long.MIN_VALUE; }

            second = parser.getNextInteger();
            if( second==Integer.MIN_VALUE ) { return Long.MIN_VALUE; }
        }

        TGregorianCalendar gregorianCalendar = getCalendar();
        try {
            // We just do a basic test, to ensure that the date is not "silly"
            if( day<1 || day>31 ) return Long.MIN_VALUE;
            if( month<1 || month>12 ) return Long.MIN_VALUE;
            gregorianCalendar.set(year,month-1,day,hour,minute,second);
            gregorianCalendar.set(GregorianCalendar.MILLISECOND,0);
            return gregorianCalendar.getMillis();
        } finally {
            recycleCalendar(gregorianCalendar);
        }
    }

    /*
     * We need to derive the gregorian calendar because getTimeInMillis() &
     * setTimeInMillis() are protected (?!?) in the Calendar class.
     */
    public static final class TGregorianCalendar extends GregorianCalendar {
        public final long getMillis() {
            return getTimeInMillis();
        }
        public final void setMillis(long ms) {
            setTimeInMillis(ms);
        }
    }

    private static TGregorianCalendar[] gregorianCalendars = new TGregorianCalendar[16];
    private static int gcCount = 0;

    /**
     * To obtain a TGregorianCalendar.
     * Remember to call recycleCalendar when object is no more used.
     */
    public static TGregorianCalendar getCalendar() {
        synchronized(gregorianCalendars) {
            if( gcCount==0 ) {
                return new TGregorianCalendar();
            }
            return gregorianCalendars[--gcCount];
        }
    }

    public static void recycleCalendar( TGregorianCalendar cal ) {
        cal.setTimeZone(TimeZone.getDefault());
        synchronized(gregorianCalendars) {
            if( gcCount<gregorianCalendars.length ) {
                gregorianCalendars[gcCount++] = cal;
            }
        }
    }

    public static final int limitDate = 39;

    /**
     * If a year is composed by only 2 characters, add 1900 or 2000.
     * If a year is composed by more than 2 characters : no change
     */
    public static int convertYear(String st) {
        int result = Integer.parseInt(st);
        if (st.length()<=2) {
            return convertYearIfInf100(result);
        }
        return result;
    }
    /**
     * Add 1900 or 2000 to the given year, if the given int is between 0 and 99.
     * (else the year is returned without any change)
     * This method shoud be called for a year value that comes from a 2-characters long string
     */
    public static int convertYearIfInf100(int year) {
        if( year>=0 && year<=limitDate ) {
            year+=2000;
        }
        if( year>limitDate && year<=99 ) {
            year+=1900;
        }
        return year;
    }



    /**
     * Test method
     */
    public static void main(String[] args) {
        long t1 = createDate("0001-01-01T00:00:00"); //$NON-NLS-1$
        java.util.Date d1 = new java.util.Date(t1);
        com.ibm.jscript.util.TDiag.getOutputStream().println("d1 : "+d1); //$NON-NLS-1$
        long t2 = createDate("50-01-01T00:00:00"); //$NON-NLS-1$
        java.util.Date d2 = new java.util.Date(t2);
        com.ibm.jscript.util.TDiag.getOutputStream().println("d2 : "+d2); //$NON-NLS-1$
        long t3 = createDate("20-01-01T00:00:00"); //$NON-NLS-1$
        java.util.Date d3 = new java.util.Date(t3);
        com.ibm.jscript.util.TDiag.getOutputStream().println("d3 : "+d3); //$NON-NLS-1$

    }

}
