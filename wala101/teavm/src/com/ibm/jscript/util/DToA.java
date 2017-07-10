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



/**
 * IEEE number formatting.
 */
public class DToA {

    // ==================================================================================
    // Implementation that uses Java formatting classes
    // ==================================================================================

    public static String toStandard(double value) {
        if (value == 0.0)
            return "0"; //$NON-NLS-1$

        // Try to format it as a long
        // This removes the trailing empty decimals
        long l = (long)value;
        if( ((double)l)==value ) {
            return Long.toString(l);
        }

        // Standard double formatting
        return standardFormat.format(value);
    }

    public static String toFixed(double value){
        return toFixed(value,0);
    }

    public static String toFixed(double value, int precision){
       if( precision<0 ) precision = 0;
       if( precision>20 ) precision = 20;
       return fixedFormats[precision].format(value);
    }

    public static String toExponential(double value) {
        return toExponential(value,0);
    }

    public static String toExponential(double value, int precision) {
       if( precision<0 ) precision = 0;
       if( precision>20 ) precision = 20;
       return expFormats[precision].format(value);
    }

    public static String toPrecision(double value) {
        if( value<1e-21 || value>1e21 ) {
            return toExponential(value);
        }
        return toFixed(value);
    }

    public static String toPrecision(double value, int precision){
        if( value<1e-21 || value>1e21 ) {
            return toExponential(value,precision);
        }
        return toFixed(value,precision);
    }


   // Initialize the formats
   private static java.text.DecimalFormat[] fixedFormats = new java.text.DecimalFormat[21];
   private static java.text.DecimalFormat[] expFormats = new java.text.DecimalFormat[21];
   private static java.text.DecimalFormat standardFormat;

   static {
       java.text.DecimalFormatSymbols usSymbols=new java.text.DecimalFormatSymbols(new java.util.Locale("en", "US")); //$NON-NLS-1$ //$NON-NLS-2$
       for( int i=0; i<fixedFormats.length; i++ ) {
           String fmt = null;
           if(i==0) {
               fmt = "0"; //$NON-NLS-1$
           } else {
               fmt = "0."+StringUtil.repeat('0',i); //$NON-NLS-1$
           }
           //TDiag.trace( "fix[{0}]={1}", TString.toString(i), fmt );
           fixedFormats[i] = new java.text.DecimalFormat(fmt, usSymbols);
       }
       for( int i=0; i<expFormats.length; i++ ) {
           String fmt = null;
           if(i==0) {
               fmt = "0E0"; //$NON-NLS-1$
           } else {
               fmt = "0."+StringUtil.repeat('0',i)+"E0"; //$NON-NLS-1$ //$NON-NLS-2$
           }
           //TDiag.trace( "exp[{0}]={1}", TString.toString(i), fmt );
           expFormats[i] = new java.text.DecimalFormat(fmt);
       }
       standardFormat = new java.text.DecimalFormat( "0.####################", usSymbols ); //$NON-NLS-1$
   }

/*
    // Performance test
    public static void main( String[] args ) {
        double d = 4568.25689;
        TDiag.trace( "toStandard={0}",toStandard(d));
        TDiag.trace( "_toStandard={0}",_toStandard(d));

        // Value test
        for( int i=0; i<21; i++ ) {
            String s1=toFixed(d, i);
            String s2=_toFixed(d, i);
            if( !s1.equals(s2) ) {
                TDiag.trace( "toFixed(), #{0}, '{1}' '{2}'", TString.toString(i), s1, s2 );
            }
            s1=toExponential(d, i);
            s2=_toExponential(d, i);
            if( !s1.equals(s2) ) {
                TDiag.trace( "toExponential(), #{0}, '{1}' '{2}'", TString.toString(i), s1, s2 );
            }
        }

        // Perfomance test
        for( int i=0; i<21; i++ ) {
            checkToExp(d, i);
            checkToFixed(d, i);
            _checkToExp(d, i);
            _checkToFixed(d, i);
        }
        checkToStandard(d);
        _checkToStandard(d);

        TDiag.trace( "end..." );
    }

   private static void checkToExp(double d, int prec) {
       for( int i=0; i<1000; i++ ) {
           toExponential(d,prec);
       }
   }
   private static void checkToFixed(double d, int prec) {
       for( int i=0; i<1000; i++ ) {
           toFixed(d,prec);
       }
   }
   private static void checkToStandard(double d) {
       for( int i=0; i<10000; i++ ) {
           toStandard(d);
       }
   }

   private static void _checkToExp(double d, int prec) {
       for( int i=0; i<1000; i++ ) {
           _toExponential(d,prec);
       }
   }
   private static void _checkToFixed(double d, int prec) {
       for( int i=0; i<1000; i++ ) {
           _toFixed(d,prec);
       }
   }
   private static void _checkToStandard(double d) {
       for( int i=0; i<10000; i++ ) {
           _toStandard(d);
       }
   }
*/

/*
Javascript test

function run() {
    f(1);
    f(1.2568);
    f(1.1);
    f(1.5);
    f(1.568964564);
    f(0.00065899);
    f(789635.2656);
    f(12345678901234567890123.123456);
}

function f(n) {
    print ("");
    print( "FIXED\tn="+n+",\t2:"+n.toFixed(2)+",\t4:"+n.toFixed(4)+",\t6:"+n.toFixed(6) );
    print( "EXPON\t n="+n+",\t2:"+n.toExponential(2)+",\t4:"+n.toExponential(4)+",\t6:"+n.toExponential(6) );
    print( "PRECI\t n="+n+",\t2:"+n.toPrecision(2)+",\t4:"+n.toPrecision(4)+",\t6:"+n.toPrecision(6) );
}

*/
}

