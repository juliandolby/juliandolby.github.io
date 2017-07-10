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
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.util.DToA;

/**
 * This class represents the standard JavaScript Number type.
 * @fbscript Number
 */
public class NumberObject extends AbstractPrimitiveObject {

    private FBSNumber primitive;

    /**
     * The value of Number.MAX_VALUE is the largest positive finite value of the number type,
     * which is approximately 1.7976931348623157 E308.
     */
    public static final double MAX_VALUE = Double.MAX_VALUE;

    /**
     * The value of Number.MIN_VALUE is the smallest positive value of the number type,
     * which is approximately 5 × E-324 .
     */
    public static final double MIN_VALUE = Double.MIN_VALUE;

    /**
     * Not a number value.
     */
    public static final double NaN = Double.NaN;

    /**
     * The value of Number.NEGATIVE_INFINITY is \u2212\u221E.
     */
    public static final double NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY;

    /**
     * The value of Number.POSITIVE_INFINITY is +\u221E.
     */
    public static final double POSITIVE_INFINITY = Double.POSITIVE_INFINITY;

    
    public NumberObject(FBSNumber value) {
    	super(NumberPrototype.getNumberPrototype());
        this.primitive = value;
    }
    

    ////// Constructor
    //TODO: metadata...
    public boolean supportConstruct(){
        return true;
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        if(args.getCount()==0) {
            return new NumberObject(FBSNumber.Zero);
        }
        if(args.getCount()>=1) {
            FBSNumber d = args.get(0).toFBSNumber();
            return new NumberObject(d);
        }
        throw new InterpretException(JScriptResources.getString("NumberObject.InvalidArgs.Exception")); //$NON-NLS-1$
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException {
        if(args.getCount()==0) {
            return FBSNumber.Zero;
        }
        return args.get(0).toFBSNumber();
    }
    
    public boolean supportCall() {
        return true;
    }
    
    public boolean equals( Object o ) {
    	if( o instanceof NumberObject ) {
    		NumberObject w = (NumberObject)o;
			return primitive.doubleValue()==w.primitive.doubleValue();
    	}
    	if( o instanceof FBSNumber ) {
    		FBSNumber w = (FBSNumber)o;
			return primitive.numberValue()==w.numberValue();
    	}
    	return false;
    }

    public FBSValue getPrimitiveValue() {
    	return primitive;
    }

    public FBSNumber getFBSNumber() {
    	return primitive;
    }

    public String getTypeAsString(){
        return "Number"; //$NON-NLS-1$
    }

    public boolean isJavaAssignableTo(Class c) {
    	return  (c==Byte.TYPE || c==Byte.class)
    		  ||(c==Short.TYPE || c==Short.class)
	  		  ||(c==Integer.TYPE || c==Integer.class)
	  		  ||(c==Long.TYPE || c==Long.class) 
	  		  ||(c==Float.TYPE || c==Float.class) 
	  		  ||(c==Double.TYPE || c==Double.class)
	          || c==NumberObject.class;
    }

    public Object toJavaObject(Class c){
    	if(c==Byte.TYPE || c==Byte.class) {
            return new Byte((byte)this.primitive.numberValue());
    	}
    	if(c==Short.TYPE || c==Short.class) {
            return new Short((short)this.primitive.numberValue());
    	}
    	if(c==Integer.TYPE || c==Integer.class) {
            return new Integer((int)this.primitive.numberValue());
    	}
    	if(c==Long.TYPE || c==Long.class) {
            return new Long((long)this.primitive.numberValue());
    	}
    	if(c==Float.TYPE || c==Float.class) {
            return new Float((float)this.primitive.numberValue());
    	}
    	if(c==Double.TYPE || c==Double.class) {
            return new Double((double)this.primitive.numberValue());
    	}
    	//TODO: Big decimal? Big integer?
        if( c==NumberObject.class ) { // ??? PHIL: I think this is not needed....
            return this;
        }
        throw new RuntimeException("Can't convert "+this.getClass().getName()+" to "+c.getName());
    }

    /**
     * @return a string representing the value of this object.
     */
    public String toString(int radix){
        if( radix==10 ) {
            return toFixed();
        }
        return Long.toString(primitive.longValue(),radix);
    }

    /**
     * @return a string representing the value of this object.
     */
    public String toString(){
        return DToA.toStandard(primitive.doubleValue());
    }

    /**
     * @return a string representing the value of this object in locale format.
     */
    public String toLocaleString(int radix){
        if( radix==10 ) {
            return Double.toString(primitive.doubleValue());
        }
        return Long.toString(primitive.longValue(),radix);
    }

    /**
     * @return the value of this object.
     */
    public double valueOf(){
        return primitive.doubleValue();
    }

    /**
     * @return a string containing the number represented in fixed-point notation
     * with fractionDigits digits after the decimal point.
     */
    public String toFixed(){
        return DToA.toFixed(primitive.doubleValue());
    }

    /**
     * @return a string containing the number represented in fixed-point notation
     * with fractionDigits digits after the decimal point.
     */
    public String toFixed(int precision){
        return DToA.toFixed(primitive.doubleValue(),precision);
    }

    /**
     * @return a string containing the number represented in exponential notation
     * with one digit before the significand's decimal point and f digits after
     * the significand's decimal point.
     */
    public String toExponential(){
        return DToA.toExponential(primitive.doubleValue());
    }

    /**
     * @return a string containing the number represented in exponential notation
     * with one digit before the significand's decimal point and f digits after
     * the significand's decimal point.
     */
    public String toExponential(int precision){
        return DToA.toExponential(primitive.doubleValue(),precision);
    }

    /**
     * @return a string containing the number represented either in exponential notation
     * with one digit before the significand's decimal point and f-1 digits after
     * the significand's decimal point or in fixed notation with f significant digits.
     */
    public String toPrecision(){
        return DToA.toPrecision(primitive.doubleValue());
    }

    /**
     * @return a string containing the number represented either in exponential notation
     * with one digit before the significand's decimal point and f-1 digits after
     * the significand's decimal point or in fixed notation with f significant digits.
     */
    public String toPrecision(int precision){
        return DToA.toPrecision(primitive.doubleValue(),precision);
    }
}