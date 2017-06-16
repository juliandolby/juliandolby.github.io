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
import java.util.Date;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.BuiltinFunction;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

/**
 * Date object prototype.
 */
public class DatePrototype extends AbstractPrototype {

    private static DatePrototype datePrototype=new DatePrototype();

    public static DatePrototype getDatePrototype(){
        return datePrototype;
    }

    public DatePrototype(){
        int attrib=FBSObject.P_NODELETE|FBSObject.P_READONLY;
        createProperty("toString",attrib,new DateMethod(0)); //$NON-NLS-1$
        createProperty("valueOf",attrib,new DateMethod(1)); //$NON-NLS-1$
        

        createProperty("parseISO",attrib,new DateMethod(2)); //$NON-NLS-1$
        createProperty("parse",attrib,new DateMethod(3)); //$NON-NLS-1$
        createProperty("UTC",attrib,new DateMethod(4)); //$NON-NLS-1$
        createProperty("getTime",attrib,new DateMethod(5)); //$NON-NLS-1$
        createProperty("getYear",attrib,new DateMethod(6)); //$NON-NLS-1$
        createProperty("getFullYear",attrib,new DateMethod(7)); //$NON-NLS-1$
        createProperty("getUTCFullYear",attrib,new DateMethod(8)); //$NON-NLS-1$
        createProperty("getMonth",attrib,new DateMethod(9)); //$NON-NLS-1$
        createProperty("getUTCMonth",attrib,new DateMethod(10)); //$NON-NLS-1$
        createProperty("getDate",attrib,new DateMethod(11)); //$NON-NLS-1$
        createProperty("getUTCDate",attrib,new DateMethod(12)); //$NON-NLS-1$
        createProperty("getDay",attrib,new DateMethod(13)); //$NON-NLS-1$
        createProperty("getUTCDay",attrib,new DateMethod(14)); //$NON-NLS-1$
        createProperty("getHour",attrib,new DateMethod(15)); //$NON-NLS-1$
        createProperty("getHours",attrib,new DateMethod(16)); //$NON-NLS-1$
        createProperty("getUTCHours",attrib,new DateMethod(17)); //$NON-NLS-1$
        createProperty("getMinute",attrib,new DateMethod(18)); //$NON-NLS-1$
        createProperty("getMinutes",attrib,new DateMethod(19)); //$NON-NLS-1$
        createProperty("getUTCMinutes",attrib,new DateMethod(20)); //$NON-NLS-1$
        createProperty("getSeconds",attrib,new DateMethod(21)); //$NON-NLS-1$
        createProperty("getSecond",attrib,new DateMethod(22)); //$NON-NLS-1$
        createProperty("getUTCSeconds",attrib,new DateMethod(23)); //$NON-NLS-1$
        createProperty("getMilliseconds",attrib,new DateMethod(24)); //$NON-NLS-1$
        createProperty("getUTCMilliseconds",attrib,new DateMethod(25)); //$NON-NLS-1$
        createProperty("getTimezoneOffset",attrib,new DateMethod(26)); //$NON-NLS-1$
        createProperty("setTime",attrib,new DateMethod(27)); //$NON-NLS-1$
        createProperty("setMilliseconds",attrib,new DateMethod(28)); //$NON-NLS-1$
        createProperty("setUTCMilliseconds",attrib,new DateMethod(29)); //$NON-NLS-1$
        createProperty("setSeconds",attrib,new DateMethod(30)); //$NON-NLS-1$
        createProperty("setUTCSeconds",attrib,new DateMethod(31)); //$NON-NLS-1$
        createProperty("setMinutes",attrib,new DateMethod(32)); //$NON-NLS-1$
        createProperty("setUTCMinutes",attrib,new DateMethod(33)); //$NON-NLS-1$
        createProperty("setHours",attrib,new DateMethod(34)); //$NON-NLS-1$
        createProperty("setUTCHours",attrib,new DateMethod(35)); //$NON-NLS-1$
        createProperty("setDate",attrib,new DateMethod(36)); //$NON-NLS-1$
        createProperty("setUTCDate",attrib,new DateMethod(37)); //$NON-NLS-1$
        createProperty("setMonth",attrib,new DateMethod(38)); //$NON-NLS-1$
        createProperty("setUTCMonth",attrib,new DateMethod(39)); //$NON-NLS-1$
        createProperty("setFullYear",attrib,new DateMethod(40)); //$NON-NLS-1$
        createProperty("setUTCFullYear",attrib,new DateMethod(41)); //$NON-NLS-1$
        createProperty("toDateString",attrib,new DateMethod(42)); //$NON-NLS-1$
        createProperty("toTimeString",attrib,new DateMethod(43)); //$NON-NLS-1$
        createProperty("toLocaleString",attrib,new DateMethod(44)); //$NON-NLS-1$
        createProperty("toLocaleDateString",attrib,new DateMethod(45)); //$NON-NLS-1$
        createProperty("toLocaleTimeString",attrib,new DateMethod(46)); //$NON-NLS-1$
        createProperty("toUTCString",attrib,new DateMethod(47)); //$NON-NLS-1$
        createProperty("toISOString",attrib,new DateMethod(48)); //$NON-NLS-1$
    }



    ////// Methods
    class DateMethod extends BuiltinFunction{
        int mIndex=-1;

        public DateMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
            switch(mIndex) {
                case 0: //toString
                	return new String[] {"():T"}; //$NON-NLS-1$
                case 1: //valueOf
                	return new String[] {"():T"}; //$NON-NLS-1$
            	case 2:  // DateObject parseISO(String str)throws com.ibm.jscript.InterpretException{
            	case 3:  // DateObject parse(String str){
                	return new String[] {"(str:T):Y"}; //$NON-NLS-1$
            	case 4:  // long UTC(int year, int month, int date, int hours, int minutes, int seconds, int ms){
                	return new String[] {"(year:Imonth:I):J","(year:Imonth:Idate:I):J","(year:Imonth:Idate:Ihours:I):J","(year:Imonth:Idate:Ihours:Iminutes:I):J","(year:Imonth:Idate:Ihours:Iminutes:Iseconds:I):J","(year:Imonth:Idate:Ihours:Iminutes:Iseconds:Ims:I):J"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            	case 5:  // long getTime(){
                	return new String[] {"():J"}; //$NON-NLS-1$
            	case 6:  // int getYear(){
            	case 7:  // int getFullYear(){
            	case 8:  // int getUTCFullYear(){
            	case 9:  // int getMonth(){
            	case 10:  // int getUTCMonth(){
            	case 11:  // int getDate(){
            	case 12:  // int getUTCDate(){
            	case 13:  // int getDay(){
            	case 14:  // int getUTCDay(){
            	case 15:  // int getHour(){
            	case 16:  // int getHours(){
            	case 17:  // int getUTCHours(){
            	case 18:  // int getMinute(){
            	case 19:  // int getMinutes(){
            	case 20:  // int getUTCMinutes(){
            	case 21:  // int getSeconds(){
            	case 22:  // int getSecond(){
            	case 23:  // int getUTCSeconds(){
            	case 24:  // int getMilliseconds(){
            	case 25:  // int getUTCMilliseconds(){
            	case 26:  // int getTimezoneOffset(){
                	return new String[] {"():I"}; //$NON-NLS-1$
            	case 27:  // long setTime(long time){
                	return new String[] {"(time:J):J"}; //$NON-NLS-1$
            	case 28:  // long setMilliseconds(int ms){
            	case 29:  // long setUTCMilliseconds(int ms){
                	return new String[] {"(ms:I):J"}; //$NON-NLS-1$
            	case 30:  // long setSeconds(int sec, int ms){
            	case 31:  // long setUTCSeconds(int sec, int ms){
                	return new String[] {"(sec:Ims:I):J","(sec:I):J"}; //$NON-NLS-1$ //$NON-NLS-2$
            	case 32:  // long setMinutes(int min, int sec, int ms){
            	case 33:  // long setUTCMinutes(int min, int sec, int ms){
                	return new String[] {"(min:Isec:Ims:I):J","(min:Isec:I):J","(min:I):J"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            	case 34:  // long setHours(int hour, int min, int sec, int ms){
            	case 35:  // long setUTCHours(int hour, int min, int sec, int ms){
                	return new String[] {"(hours:Imin:Isec:Ims:I):J","(hours:Imin:Isec:I):J","(hours:Imin:I):J","(hours:I):J"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            	case 36:  // long setDate(int date){
            	case 37:  // long setUTCDate(int date){
                	return new String[] {"(date:I):J"}; //$NON-NLS-1$
            	case 38:  // long setMonth(int month, int date){
            	case 39:  // long setUTCMonth(int month, int date){
                	return new String[] {"(month:Idate:I):J","(month:I):J"}; //$NON-NLS-1$ //$NON-NLS-2$
            	case 40:  // long setFullYear(int year, int month, int date){
            	case 41:  // long setUTCFullYear(int year, int month, int date){
                	return new String[] {"(year:Imonth:Idate:I):J","(year:Imonth:I):J","(year:I):J"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            	case 42:  // String toDateString(){
            	case 43:  // String toTimeString(){
            	case 44:  // String toLocaleString(){
            	case 45:  // String toLocaleDateString(){
            	case 46:  // String toLocaleTimeString(){
            	case 47:  // String toUTCString(){
            	case 48:  // String toISOString(){
                	return new String[] {"():T"}; //$NON-NLS-1$
            }
            return super.getCallParameters();
        }


        public FBSValue call(FBSValueVector args, FBSObject _this) throws JavaScriptException {
            return FBSUndefined.undefinedValue;
        }

        public FBSValue call(IExecutionContext context, FBSValueVector args, FBSObject _this) throws JavaScriptException {
            switch(mIndex){

            	///////////////////////////////////////////////////////////////
            	// Standard ECMA functions
                case 0: {     // toString
                	if(_this instanceof DateObject ) {
                		DateObject dt = (DateObject)_this;
                        String s = DateFormat.getInstance().format(new Date(dt.getTime()));
                		return FBSString.get(_this.toString());
                	}
                	throw new InterpretException(JScriptResources.getString("DatePrototype.NotAStringType.Exception")); //$NON-NLS-1$
                }
                case 1: {     // valueOf
                	if(_this instanceof DateObject ) {
                		DateObject dt = (DateObject)_this;
                		return FBSNumber.get(dt.getTime());
                	}
                	throw new InterpretException(JScriptResources.getString("DatePrototype.NotAStringType.Exception")); //$NON-NLS-1$
                }

            	case 2: { // DateObject parseISO(String str)throws com.ibm.jscript.InterpretException{
            		if(args.size()>=1) {
            			String str = args.get(0).stringValue();
            			return new DateObject(com.ibm.jscript.ASTTree.ASTLiteral.parseDate(str));
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 3: { // DateObject parse(String str){
            		if(args.size()>=1) {
            			String str = args.get(0).stringValue();
                        try{
                            DateFormat df = DateFormat.getInstance();
                            return new DateObject(df.parse(str));
                        }catch(Exception e){
                        }
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 4: { // long UTC(int year, int month, int date, int hours, int minutes, int seconds, int ms){
            		if(args.size()>=2) {
            			int year = args.get(0).intValue();
//                        if (year>=0 && year<=99){
//                            year += 1900;
//                        }
            			int month = args.get(1).intValue();
            			int date = args.isUndefined(2) ? 1 : args.get(2).intValue();
            			int hours = args.isUndefined(3) ? 0 : args.get(3).intValue();
            			int minutes = args.isUndefined(4) ? 0 : args.get(4).intValue();
            			int seconds = args.isUndefined(5) ? 0 : args.get(5).intValue();
            			int ms = args.isUndefined(6) ? 0 : args.get(6).intValue();
                        return new DateObject(DateObject.UTC(year,month,date,hours,minutes,seconds,ms));
            		}
                }

            	case 5: { // long getTime(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getTime());
                }

            	case 6: { // int getYear(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getYear());
                }

            	case 7: { // int getFullYear(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getFullYear());
                }

            	case 8: { // int getUTCFullYear(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getUTCFullYear());
                }

            	case 9: { // int getMonth(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getMonth());
                }

            	case 10: { // int getUTCMonth(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getUTCMonth());
                }

            	case 11: { // int getDate(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getDate());
                }

            	case 12: { // int getUTCDate(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getUTCDate());
                }

            	case 13: { // int getDay(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getDay());
                }

            	case 14: { // int getUTCDay(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getUTCDay());
                }

            	case 15: { // int getHour(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getHour());
                }

            	case 16: { // int getHours(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getHours());
                }

            	case 17: { // int getUTCHours(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getUTCHours());
                }

            	case 18: { // int getMinute(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getMinute());
                }

            	case 19: { // int getMinutes(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getMinutes());
                }

            	case 20: { // int getUTCMinutes(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getUTCMinutes());
                }

            	case 21: { // int getSeconds(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getSeconds());
                }

            	case 22: { // int getSecond(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getSecond());
                }

            	case 23: { // int getUTCSeconds(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getUTCSeconds());
                }

            	case 24: { // int getMilliseconds(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getMilliseconds());
                }

            	case 25: { // int getUTCMilliseconds(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getUTCMilliseconds());
                }

            	case 26: { // getTimezoneOffset(){
            		DateObject dt = (DateObject)_this;
            		return FBSNumber.get(dt.getTimezoneOffset());
                }

            	case 27: { // long setTime(long time){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		long time = args.get(0).longValue();
                		return FBSNumber.get(dt.setTime(time));
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 28: { // long setMilliseconds(int ms){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int time = args.get(0).intValue();
                		return FBSNumber.get(dt.setMilliseconds(time));
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 29: { // long setUTCMilliseconds(int ms){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int time = args.get(0).intValue();
                		return FBSNumber.get(dt.setUTCMilliseconds(time));
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 30: { // long setSeconds(int sec, int ms){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int time = args.get(0).intValue();
                		if(args.isUndefined(2)) {
                			return FBSNumber.get(dt.setSeconds(time));
                		} else {
                    		int ms = args.get(1).intValue();
                			return FBSNumber.get(dt.setSeconds(time,ms));
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 31: { // long setUTCSeconds(int sec, int ms){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int time = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setUTCSeconds(time));
                		} else {
                    		int ms = args.get(1).intValue();
                			return FBSNumber.get(dt.setUTCSeconds(time,ms));
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 32: { // long setMinutes(int min, int sec, int ms){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int min = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setMinutes(min));
                		} else {
                			int sec = args.get(1).intValue();
                    		if(args.isUndefined(2)) {
                    			return FBSNumber.get(dt.setMinutes(min,sec));
                    		} else {
                    			int ms = args.get(2).intValue();
                    			return FBSNumber.get(dt.setMinutes(min,sec,ms));
                    		}
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 33: { // long setUTCMinutes(int min, int sec, int ms){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int min = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setUTCMinutes(min));
                		} else {
                			int sec = args.get(1).intValue();
                    		if(args.isUndefined(2)) {
                    			return FBSNumber.get(dt.setUTCMinutes(min,sec));
                    		} else {
                    			int ms = args.get(2).intValue();
                    			return FBSNumber.get(dt.setUTCMinutes(min,sec,ms));
                    		}
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 34: { // long setHours(int hour, int min, int sec, int ms){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int hour = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setHours(hour));
                		} else {
                			int min = args.get(1).intValue();
                    		if(args.isUndefined(2)) {
                    			return FBSNumber.get(dt.setHours(hour,min));
                    		} else {
                    			int sec = args.get(1).intValue();
                        		if(args.isUndefined(3)) {
	                    			return FBSNumber.get(dt.setHours(hour,min,sec));
                        		} else {
                        			int ms = args.get(3).intValue();
                        			return FBSNumber.get(dt.setHours(hour,min,sec,ms));
                        		}
                    		}
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 35: { // long setUTCHours(int hour, int min, int sec, int ms){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int hour = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setUTCHours(hour));
                		} else {
                			int min = args.get(1).intValue();
                    		if(args.isUndefined(2)) {
                    			return FBSNumber.get(dt.setUTCHours(hour,min));
                    		} else {
                    			int sec = args.get(1).intValue();
                        		if(args.isUndefined(3)) {
	                    			return FBSNumber.get(dt.setUTCHours(hour,min,sec));
                        		} else {
                        			int ms = args.get(3).intValue();
                        			return FBSNumber.get(dt.setUTCHours(hour,min,sec,ms));
                        		}
                    		}
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 36: { // long setDate(int date){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int time = args.get(0).intValue();
                		return FBSNumber.get(dt.setDate(time));
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 37: { // long setUTCDate(int date){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int time = args.get(0).intValue();
                		return FBSNumber.get(dt.setUTCDate(time));
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 38: { // long setMonth(int month, int date){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int month = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setMonth(month));
                		} else {
                    		int date = args.get(1).intValue();
                			return FBSNumber.get(dt.setMonth(month,date));
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 39: { // long setUTCMonth(int month, int date){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int month = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setUTCMonth(month));
                		} else {
                    		int date = args.get(1).intValue();
                			return FBSNumber.get(dt.setUTCMonth(month,date));
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 40: { // long setFullYear(int year, int month, int date){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int year = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setFullYear(year));
                		} else {
                			int month = args.get(1).intValue();
                    		if(args.isUndefined(2)) {
                    			return FBSNumber.get(dt.setFullYear(year,month));
                    		} else {
                    			int date = args.get(2).intValue();
                    			return FBSNumber.get(dt.setFullYear(year,month,date));
                    		}
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 41: { // long setUTCFullYear(int year, int month, int date){
            		if(args.size()>=1) {
                		DateObject dt = (DateObject)_this;
                		int year = args.get(0).intValue();
                		if(args.isUndefined(1)) {
                			return FBSNumber.get(dt.setUTCFullYear(year));
                		} else {
                			int month = args.get(1).intValue();
                    		if(args.isUndefined(2)) {
                    			return FBSNumber.get(dt.setUTCFullYear(year,month));
                    		} else {
                    			int date = args.get(2).intValue();
                    			return FBSNumber.get(dt.setUTCFullYear(year,month,date));
                    		}
                		}
            		}
            		return FBSUndefined.undefinedValue;
                }

            	case 42: { // String toDateString(){
            		DateObject dt = (DateObject)_this;
            		return FBSString.get(dt.toDateString());
                }

            	case 43: { // String toTimeString(){
            		DateObject dt = (DateObject)_this;
            		return FBSString.get(dt.toTimeString());
                }

            	case 44: { // String toLocaleString(){
            		DateObject dt = (DateObject)_this;
            		return FBSString.get(dt.toLocaleString());
                }

            	case 45: { // String toLocaleDateString(){
            		DateObject dt = (DateObject)_this;
            		return FBSString.get(dt.toLocaleDateString());
                }

            	case 46: { // String toLocaleTimeString(){
            		DateObject dt = (DateObject)_this;
            		return FBSString.get(dt.toLocaleTimeString());
                }

            	case 47: { // String toUTCString(){
            		DateObject dt = (DateObject)_this;
            		return FBSString.get(dt.toUTCString());
                }

            	case 48: { // String toISOString(){
            		DateObject dt = (DateObject)_this;
            		return FBSString.get(dt.toISOString());
                }
            }
            throw new InterpretException(JScriptResources.getString("DatePrototype.StringFuncNotFound.Exception")); //$NON-NLS-1$
        }
    }

}