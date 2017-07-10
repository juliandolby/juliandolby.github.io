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

import java.util.Locale;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.types.BuiltinFunction;
import com.ibm.jscript.types.FBSBoolean;
import com.ibm.jscript.types.FBSNull;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.util.StringUtil;

/**
 * This class allows to manipulate standard JavaScript String objects. It
 * provides for example methods to compare some strings, to look for a special
 * substring in the string, ...
 * 
 * @fbscript String
 */
public class StringPrototype extends AbstractPrototype {

    private static StringPrototype stringPrototype=new StringPrototype();

    public static StringPrototype getStringPrototype(){
        return stringPrototype;
    }

    public StringPrototype(){
        int attrib=FBSObject.P_NODELETE|FBSObject.P_READONLY;
        createProperty("toString",attrib,new StringMethod(0)); //$NON-NLS-1$
        createProperty("valueOf",attrib,new StringMethod(1)); //$NON-NLS-1$
        createProperty("charAt",attrib,new StringMethod(2)); //$NON-NLS-1$
        createProperty("charCodeAt",attrib,new StringMethod(3)); //$NON-NLS-1$
        createProperty("concat",attrib,new StringMethod(4)); //$NON-NLS-1$
        createProperty("indexOf",attrib,new StringMethod(5)); //$NON-NLS-1$
        createProperty("lastIndexOf",attrib,new StringMethod(6)); //$NON-NLS-1$
        createProperty("localeCompare",attrib,new StringMethod(7)); //$NON-NLS-1$
        createProperty("match",attrib,new StringMethod(8)); //$NON-NLS-1$
        createProperty("replace",attrib,new StringMethod(9)); //$NON-NLS-1$
        createProperty("search",attrib,new StringMethod(10)); //$NON-NLS-1$
        createProperty("slice",attrib,new StringMethod(11)); //$NON-NLS-1$
        createProperty("split",attrib,new StringMethod(12)); //$NON-NLS-1$
        createProperty("substring",attrib,new StringMethod(13)); //$NON-NLS-1$
        createProperty("toLowerCase",attrib,new StringMethod(14)); //$NON-NLS-1$
        createProperty("toLocaleLowerCase",attrib,new StringMethod(15)); //$NON-NLS-1$
        createProperty("toUpperCase",attrib,new StringMethod(16)); //$NON-NLS-1$
        createProperty("toLocaleUpperCase",attrib,new StringMethod(17)); //$NON-NLS-1$
        if(JSOptions.get().hasStringExtendedMethods()) { // Lotus extensions
	        createProperty("left",attrib,new StringMethod(18)); //$NON-NLS-1$
	        createProperty("right",attrib,new StringMethod(19)); //$NON-NLS-1$
	        createProperty("mid",attrib,new StringMethod(20)); //$NON-NLS-1$
	        createProperty("trim",attrib,new StringMethod(21)); //$NON-NLS-1$
	        createProperty("rtrim",attrib,new StringMethod(22)); //$NON-NLS-1$
	        createProperty("ltrim",attrib,new StringMethod(23)); //$NON-NLS-1$
	        createProperty("rpad",attrib,new StringMethod(24)); //$NON-NLS-1$
	        createProperty("lpad",attrib,new StringMethod(25)); //$NON-NLS-1$
	        createProperty("compareTo",attrib,new StringMethod(26)); //$NON-NLS-1$
	        createProperty("compareToIgnoreCase",attrib,new StringMethod(27)); //$NON-NLS-1$
	        createProperty("insert",attrib,new StringMethod(28)); //$NON-NLS-1$
	        createProperty("remove",attrib,new StringMethod(29)); //$NON-NLS-1$
	        createProperty("startsWith",attrib,new StringMethod(30)); //$NON-NLS-1$
	        createProperty("endsWith",attrib,new StringMethod(31)); //$NON-NLS-1$
	        createProperty("startsWithIgnoreCase",attrib,new StringMethod(32)); //$NON-NLS-1$
	        createProperty("endsWithIgnoreCase",attrib,new StringMethod(33)); //$NON-NLS-1$
	        createProperty("indexOfIgnoreCase",attrib,new StringMethod(34)); //$NON-NLS-1$
	        createProperty("lastIndexOfIgnoreCase",attrib,new StringMethod(35)); //$NON-NLS-1$
	        createProperty("isEmpty",attrib,new StringMethod(36)); //$NON-NLS-1$
	        createProperty("isStrictEmpty",attrib,new StringMethod(37)); //$NON-NLS-1$

	        // Redundancy
	        createProperty("toLower",attrib,new StringMethod(14)); //$NON-NLS-1$
	        createProperty("toUpper",attrib,new StringMethod(16)); //$NON-NLS-1$
        }

        if(JSOptions.get().hasRhinoExtensions()) { // Rhino extensions
            createProperty("substr",attrib,new StringMethod(38)); //$NON-NLS-1$
            createProperty("equals",attrib,new StringMethod(39)); //$NON-NLS-1$
            createProperty("equalsIgnoreCase",attrib,new StringMethod(40)); //$NON-NLS-1$
        }
		createProperty("fromCharCode", attrib, new StringMethod(41)); //$NON-NLS-1$
    }

    
    class StringMethod extends BuiltinFunction{
        int mIndex=-1;

        public StringMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
            switch(mIndex) {
                case 0: //toString
                case 1: //valueOf
                	return new String[] {"():T"}; //$NON-NLS-1$
                case 2: //charAt
                	return new String[] {"(pos:I):T"}; //$NON-NLS-1$
                case 3: //charCodeAt
                	return new String[] {"(pos:I):I"}; //$NON-NLS-1$
                case 4: //concat
                	return new String[] {"(str:N):T"}; //$NON-NLS-1$
                case 5: //indexOf
                case 6: //lastIndexOf
                	return new String[] {"(searchString:T):I","(searchString:Tposition:I):I"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 7: //localeCompare
                	return new String[] {"(that:T):I"}; //$NON-NLS-1$
                case 8: //match
                	return new String[] {"(regExp:T):I","(regExp:R):I"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 9: //replace
                	//TODO: regexp & function here...
                	return new String[] {"(searchValue:TreplaceValue:T):T"}; //$NON-NLS-1$
                case 10: //search
                	return new String[] {"(regExp:R):I"}; //$NON-NLS-1$
                case 11: //slice
                	return new String[] {"(start:I):T","(start:Iend:I):T"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 12: //split
                	return new String[] {"(separator:Tlimit:I):T","(separator:Rlimit:I):T"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 13: //substring
                	return new String[] {"(start:I):T","(start:Iend:I):T"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 14: //toLowercase
                case 15: //toLocaleLowercase
                case 16: //toUpperCase
                case 17: //toLocaleUpperCase
                	return new String[] {"():T"}; //$NON-NLS-1$
                case 18: //left
                	return new String[] {"(count:I):T"}; //$NON-NLS-1$
                case 19: //right
                	return new String[] {"(count:I):T"}; //$NON-NLS-1$
                case 20: //mid
                	return new String[] {"(pos:I):T","(pos:Icount:I):T"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 21: //trim
                case 22: //rtrim
                case 23: //ltrim
                	return new String[] {"():T"}; //$NON-NLS-1$
                case 24: //rpad
                case 25: //lpad
                	return new String[] {"(ch:Tlength:I):T"}; //$NON-NLS-1$
                case 26: //compareTo
                case 27: //compareToIgnoreCase
                	return new String[] {"(str:T):I"}; //$NON-NLS-1$
                case 28: //insert
                	return new String[] {"(substr:Tindex:I):I","(substr:Tindex:Icount:I):I"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 29: //remove
                	return new String[] {"(substr:Tindex:I):I","(substr:Tindex:Icount:I):I"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 30: //startsWith
                case 31: //endsWith
                case 32: //startsWithIgnoreCase
                case 33: //endsWithIgnoreCase
                	return new String[] {"(str:T):Z"}; //$NON-NLS-1$
                case 34: //indexOfIgnoreCase
                case 35: //lastIndexOfIgnoreCase
                	return new String[] {"(searchString:T):I","(searchString:Tposition:I):I"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 36: //isEmpty
                case 37: //isStrictEmpty
                	return new String[] {"():Z"}; //$NON-NLS-1$
                case 38: //substr
                    return new String[] {"(start:I):T","(start:Ilength:I):T"}; //$NON-NLS-1$ //$NON-NLS-2$
                case 39: //equals
                case 40: //equalsIgnoreCase
                    return new String[] {"(str:N):Z"}; //$NON-NLS-1$
			case 41: // fromCharCode
				return new String[] { "(code:N):Z" }; //$NON-NLS-1$
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
                	if(_this instanceof StringObject ) {
                		return ((StringObject)_this).getFBSString();
                	}
				if(_this==null) {
					return FBSString.emptyString;
				}
				throw new InterpretException(JScriptResources
						.getString("StringPrototype.ObjNotAString.Exception")); //$NON-NLS-1$
                }
                case 1: {     // valueOf
                	if(_this instanceof StringObject ) {
                		return ((StringObject)_this).getFBSString();
                	}
				if(_this==null) {
					return FBSString.emptyString;
				}
				throw new InterpretException(JScriptResources
						.getString("StringPrototype.ObjNotAString.Exception")); //$NON-NLS-1$
                }
                case 2: {     // charAt
				if (_this!=null && args.size() >= 1) {
                    	String jstring = _this.stringValue();
                    	int index = args.get(0).intValue();
                    	if (index<0 || index>=jstring.length()){
                    		return FBSString.emptyString;
                    	}
                        return FBSString.get(jstring.charAt(index));
                    }
            		return FBSString.emptyString;
                }
                case 3: {     // charCodeAt
				if (_this!=null && args.size() >= 1) {
                    	String jstring = _this.stringValue();
                    	int index = args.get(0).intValue();
                    	if (index<0 || index>=jstring.length()){
                    		return FBSNumber.NaN;
                    	}
                        return FBSNumber.get((int)jstring.charAt(index));
                    }
            		return FBSNumber.NaN;
                }
                case 4: {     // concat
                	StringBuffer b = new StringBuffer();
                	b.append(_this.stringValue());
                	for(int i=0; i<args.getCount(); i++ ) {
                    	b.append(args.get(i).stringValue());
                	}
                	return FBSString.get(b.toString());
                }
                case 5: {     // indexOf
				if (_this!=null && args.size() >= 1) {
                    	String jstring = _this.stringValue();
                    	String searchString = args.get(0).stringValue();;
                    	if(args.getCount()>=2) {
                        	int pos = args.get(1).intValue(); 
                        	return FBSNumber.get(jstring.indexOf(searchString,pos));
                    	} else {
                    		return FBSNumber.get(jstring.indexOf(searchString));
                    	}
                    }
            		return FBSNumber.MinusOne;
                }
                case 6: {     // lastIndexOf
				if (_this!=null && args.size() >= 1) {
                    	String jstring = _this.stringValue();
                    	String searchString = args.get(0).stringValue();
                    	if(args.getCount()>=2) {
                        	int pos = args.get(1).intValue(); // Manage NaN?
                        	return FBSNumber.get(jstring.lastIndexOf(searchString,pos));
                    	} else {
                    		return FBSNumber.get(jstring.lastIndexOf(searchString));
                    	}
                    }
            		return FBSNumber.MinusOne;
                }
                case 7: {     // localeCompare
				if (_this!=null && args.size() >= 1) {
                    	String jstring = _this.stringValue();
                    	String that = args.get(0).stringValue();
                        return FBSNumber.get(java.text.Collator.getInstance().compare(jstring,that));
                    }
            		return FBSNumber.NaN;
                }
                case 8: {     // match
				if (_this!=null && args.size() >= 1) {
                    	RegExpObject regExp = null;
	                	Object obj = args.get(0);
						FBSValueVector result = null;
                    	if( _this instanceof RegExpObject ) {
                    		regExp = (RegExpObject)_this;
	                		result = regExp.match(args.get(0).stringValue());
                    	} else {
							if(obj instanceof RegExpObject)
								regExp = (RegExpObject)obj;
							else
								regExp = new RegExpObject(args.get(0).stringValue());
							
	                		result = regExp.match(_this.stringValue());
                    	}
						if (result == null)
							return FBSNull.nullValue;
						else
							return new ArrayObject(result);
                    }
            		return FBSUndefined.undefinedValue;
                }
        		
                case 9: {     // replace
				if (_this!=null && args.size() >= 2) {
                    	//TODO: manage regexp here
                    	if( (args.get(0) instanceof RegExpObject) || (args.get(1) instanceof RegExpObject) ) {
                        	if( (args.get(0) instanceof RegExpObject) && (args.get(1) instanceof RegExpObject) ) {
                        		throw new InterpretException(JScriptResources.getString("StringPrototype.CannotUseREs.Exception")); //$NON-NLS-1$
                    	}
                        	String s = _this.stringValue();
                        	RegExpObject re = (RegExpObject)(args.get(0) instanceof RegExpObject ? args.get(0) : args.get(1));
                        	String repstr = (String)(args.get(0) instanceof RegExpObject ? args.get(1).stringValue() : args.get(0).stringValue());
                        	return re.replace(s, repstr);
                    	}
                    	
                    	//TODO: manage functions here
                    	if( args.get(1) instanceof FunctionObject ) {
                    		throw new InterpretException(JScriptResources.getString("StringPrototype.ReplaceFuncCallNotImplemented.Exception")); //$NON-NLS-1$
                    	}                    	
                    	String s = _this.stringValue();
                    	String search = args.get(0).stringValue();
                    	String replace = args.get(1).stringValue();
                    	return FBSString.get(StringUtil.replaceFirst(s,search,replace));
                    }
            		return FBSUndefined.undefinedValue;
                }
                case 10: {     // search
				if (_this!=null && args.size() >= 1) {
                    	RegExpObject regExp = null;
    						Object obj = args.get(0);
                    	if( _this instanceof RegExpObject ) {
                    		regExp = (RegExpObject)_this;
                        		return regExp.search(args.get(0).stringValue());
                    	} else {
    							if(obj instanceof RegExpObject)
    								regExp = (RegExpObject)obj;
    							else
    								regExp = new RegExpObject(args.get(0).stringValue());
    							
                    		return regExp.search(_this.stringValue());
                    	}
                    }
            		return FBSUndefined.undefinedValue;
                }
                case 11: {     // slice
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	int r2 = s.length();
                    	int r3 = args.get(0).intValue();
                    	int r4 = args.isUndefined(1) ? r2 : args.get(1).intValue();
                    	int r5 = r3<0 ? Math.max(r2+r3,0) : Math.min(r3,r2);
                    	int r6 = r4<0 ? Math.max(r2+r4,0) : Math.min(r4,r2);
                    	int r7 = Math.max(r6-r5,0);
                    	return FBSString.get(s.substring(r5,r5+r7));
                    }
				return FBSString.emptyString;
                }
                case 12: {     // split
				if (_this!=null && args.size() >= 1) {
                    	RegExpObject regExp = null;
    						Object obj = args.get(0);
    					FBSNumber limit = null;
    					if(args.size()>1)
    						limit = args.get(1).toFBSNumber();
    					else
    						limit = FBSNumber.Zero;
    					
                    	if( _this instanceof RegExpObject ) {
                    		regExp = (RegExpObject)_this;
                        		return new ArrayObject(regExp.split(args.get(0).stringValue(), limit.intValue()));
                    	} else {
    							if (obj instanceof RegExpObject)
    								regExp = (RegExpObject)obj;
    							else
    								regExp = new RegExpObject(args.get(0).stringValue());
                    		return new ArrayObject(regExp.split(_this.stringValue(), limit.intValue()));
                    	}
                    }
            		return FBSUndefined.undefinedValue;
                }
                case 13: {     // substring
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	int r2 = s.length();
                    	int r3 = args.get(0).intValue();
                    	int r4 = args.isUndefined(1) ? r2 : args.get(1).intValue();
                    	int r5 = Math.min(Math.max(r3,0),r2);
                    	int r6 = Math.min(Math.max(r4,0),r2);
                    	int r7 = Math.min(r5,r6);
                    	int r8 = Math.max(r5,r6);
                    	return FBSString.get(s.substring(r7,r8));
                    }
				if (_this!=null) {
					return _this.toFBSString();
				}
				return FBSString.emptyString;
			}
                case 14: {     // toLowercase
				if (_this!=null ) {
                	String s = _this.stringValue();
                	return FBSString.get(s.toLowerCase());
                }
				return FBSString.emptyString;
			}
                case 15: {     // toLocaleLowercase
				if (_this!=null ) {
                	String s = _this.stringValue();
                	return FBSString.get(s.toLowerCase(Locale.getDefault()));
                }
				return FBSString.emptyString;
			}
                case 16: {     // toUpperCase
				if (_this!=null ) {
                	String s = _this.stringValue();
                	return FBSString.get(s.toUpperCase());
                }
				return FBSString.emptyString;
			}
                case 17: {     // toLocaleUpperCase
				if (_this!=null ) {
                	String s = _this.stringValue();
                	return FBSString.get(s.toUpperCase(Locale.getDefault()));
                }
				return FBSString.emptyString;
			}
                
            	///////////////////////////////////////////////////////////////
            	// Lotus extensions
                case 18: {     // left
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	int count = args.get(0).intValue();
                    	if (s.length()==0 || count<=0){
                    		return FBSString.emptyString;
                    	}
                    	if (count>=s.length()){
                    		count=s.length();
                    	}
                        return FBSString.get(s.substring(0,count));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 19: {     // right
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	int count = args.get(0).intValue();
                    	if (s.length()==0 || count<=0){
                    		return FBSString.emptyString;
                    	}
                    	if (count>=s.length()){
                    		count=s.length();
                    	}
                        int index=s.length()-count;
                        return FBSString.get(s.substring(index,s.length()));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 20: {     // mid
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	int index = args.get(0).intValue();
                    	int count = args.isUndefined(1) ? s.length()-index : args.get(1).intValue();
                    	if (s.length()==0 || count<=0){
                    		return FBSString.emptyString;
                    	}
                        if (index<0){
                            index=0;
                        }
                        if (count>s.length()-index){
                            count=s.length()-index;
                        }
                        return FBSString.get(s.substring(index,index+count));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 21: {     // trim
				if (_this!=null ) {
                	return FBSString.get(_this.stringValue().trim());
                }
				return FBSString.emptyString;
			}
                case 22: {     // rtrim
				if (_this!=null ) {
                	return FBSString.get(StringUtil.rtrim(_this.stringValue()));
                }
				return FBSString.emptyString;
			}
                case 23: {     // ltrim
				if (_this!=null ) {
                	return FBSString.get(StringUtil.ltrim(_this.stringValue()));
                }
				return FBSString.emptyString;
			}
                case 24: {     // rpad
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	String ch = args.get(0).stringValue();
                    	int length = args.get(1).intValue();
                        if (length<=0){
                            return FBSString.get(s);
                        }
                        int ln = s.length();
                        int cnt=length-ln;
                        if (cnt<=0){
                            return FBSString.get(s);
                        }
                        StringBuffer strBuf=new StringBuffer();
                        strBuf.append(s);
                        for(int i=0;i<cnt;i++){
                            strBuf.append(ch);
                        }
                        return FBSString.get(strBuf.toString());
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 25: {     // lpad
				if (_this!=null && args.size() >= 2) {
                    	String s = _this.stringValue();
                    	String ch = args.get(0).stringValue();
                    	int length = args.get(1).intValue();
                        if (length<=0){
                            return FBSString.get(s);
                        }
                        int ln = s.length();
                        int cnt=length-ln;
                        if (cnt<=0){
                            return FBSString.get(s);
                        }
                        StringBuffer strBuf=new StringBuffer();
                        for(int i=0;i<cnt;i++){
                            strBuf.append(ch);
                        }
                        strBuf.append(s);
                        return FBSString.get(strBuf.toString());
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 26: {     // compareTo
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	String cmp = args.get(0).stringValue();
                    	return FBSNumber.get(s.compareTo(cmp));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 27: {     // compareToIgnoreCase
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	String cmp = args.get(0).stringValue();
                    	return FBSNumber.get(s.compareToIgnoreCase(cmp));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 28: {     // insert
				if (_this!=null && args.size() >= 2) {
                    	String s = _this.stringValue();
                    	String substr = args.get(0).stringValue();
                    	int index = args.get(1).intValue();
                    	int count = args.isUndefined(2) ? substr.length() : args.get(2).intValue();
                        if (substr.length()==0 || count<=0){
                            return FBSString.get(s);
                        }
                        if (count<substr.length()){
                            substr=substr.substring(0,count);
                        }
                        StringBuffer strBuf= new StringBuffer();
                        if (index<=0){
                            strBuf.append(substr).append(s);
                        }else
                        if(index>=s.length()){
                            strBuf.append(s).append(substr);
                        }else{
                            strBuf.append(s).insert(index,substr);
                        }
                        return FBSString.get(strBuf.toString());
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 29: {     // remove
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	int index = args.get(0).intValue();
                    	int count = args.isUndefined(1) ? s.length()-index : args.get(1).intValue();
                        StringBuffer strBuf=new StringBuffer(s);
                        strBuf.delete(index,index+count);
                        return FBSString.get(strBuf.toString());
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 30: {     // startsWith
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	String cmp = args.get(0).stringValue();
                    	return FBSBoolean.get(s.startsWith(cmp));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 31: {     // endsWith
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue();
                    	String cmp = args.get(0).stringValue();
                    	boolean ignoreCase = args.isUndefined(1) ? false : args.get(1).booleanValue();
                    	return FBSBoolean.get(s.endsWith(cmp));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 32: {     // startsWithIgnoreCase
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue().toLowerCase();
                    	String cmp = args.get(0).stringValue().toLowerCase();
                    	return FBSBoolean.get(s.startsWith(cmp));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 33: {     // endsWithIgnoreCase
				if (_this!=null && args.size() >= 1) {
                    	String s = _this.stringValue().toLowerCase();
                    	String cmp = args.get(0).stringValue().toLowerCase();
                    	return FBSBoolean.get(s.endsWith(cmp));
                    }
                    return FBSUndefined.undefinedValue;
                }
                case 34: {     // indexOfIgnoreCase
				if (_this!=null && args.size() >= 1) {
                    	String jstring = _this.stringValue().toLowerCase();
                    	String searchString = args.get(0).stringValue().toLowerCase();
                    	if(args.getCount()>=2) {
                        	int pos = args.get(1).intValue(); 
                        	return FBSNumber.get(jstring.indexOf(searchString,pos));
                    	} else {
                    		return FBSNumber.get(jstring.indexOf(searchString));
                    	}
                    }
            		return FBSNumber.MinusOne;
                }
                case 35: {     // lastIndexOfIgnoreCase
				if (_this!=null && args.size() >= 1) {
                    	String jstring = _this.stringValue().toLowerCase();
                    	String searchString = args.get(0).stringValue().toLowerCase();
                    	if(args.getCount()>=2) {
                        	int pos = args.get(1).intValue(); // Manage NaN?
                        	return FBSNumber.get(jstring.lastIndexOf(searchString,pos));
                    	} else {
                    		return FBSNumber.get(jstring.lastIndexOf(searchString));
                    	}
                    }
            		return FBSNumber.MinusOne;
                }
                case 36: {     // isEmpty
				if (_this!=null ) {
                	String jstring = _this.stringValue();
                    return FBSBoolean.get(StringUtil.isEmpty(jstring));
                }
				return FBSString.emptyString;
			}
                case 37: {     // isStrictEmpty
				if (_this!=null ) {
                	String jstring = _this.stringValue();
                    return FBSBoolean.get(StringUtil.isSpace(jstring));
                }
				return FBSString.emptyString;
			}

                case 38: {     // substr
				if (_this!=null && args.size() >= 1) {
                        String s = _this.stringValue();
                        int length = s.length();
                        
                        int start = args.get(0).intValue();
                        if(start<0) {
                            start = Math.max(length+start,0);
                        } else if(start>length) {
                            return FBSString.emptyString;
                        }
                        int count = args.isUndefined(1) ? length : args.get(1).intValue();
                        if(count<=0) {
                            return FBSString.emptyString;
                        }
                        int end = start+count;
                        if(end>length) {
                            end = length;
                        }
                        return FBSString.get(s.substring(start,end));
                    }
                    return _this;
                }

                case 39: {     // equals
				if (_this!=null && args.size() >= 1) {
                        String s = _this.stringValue();
                        String to = args.get(0).stringValue();
                        return FBSBoolean.get(s.equals(to));
                    }
                    return FBSBoolean.FALSE;
                }

                case 40: {     // equalsIgnoreCase
				if (_this!=null && args.size() >= 1) {
                        String s = _this.stringValue();
                        String to = args.get(0).stringValue();
                        return FBSBoolean.get(s.equalsIgnoreCase(to));
                    }
                    return FBSBoolean.FALSE;
                }

			case 41: { // fromCharCode
				StringBuffer b = new StringBuffer();
				for (int i = 0; i < args.size(); i++) {
					int code = (int) args.get(i).intValue();
					b.append((char) code);
				}
				return FBSString.get(b.toString());
			}
            }
            throw new InterpretException(JScriptResources.getString("StringPrototype.StringFuncNotFound.Exception")); //$NON-NLS-1$
        }
    }

}