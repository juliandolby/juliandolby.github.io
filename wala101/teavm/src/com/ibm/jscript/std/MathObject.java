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
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.types.BuiltinFunction;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

/**
 * This class represents the standard JavaScript Math type.
 */
public class MathObject extends FBSDefaultObject {

    public static final double E = Math.E;
    public static final double LN10 = 2.302585092994046;
    public static final double LN2 = 0.6931471805599453;
    public static final double LOG2E = 1.4426950408889634d;
    public static final double LOG10E = 0.4342944819032518d;
    public static final double PI = Math.PI;
    public static final double SQRT1_2 = 0.7071067811865476d;
    public static final double SQRT2 = 1.4142135623730951d;
    
    
    public MathObject(){
        int attrib=FBSObject.P_NODELETE|FBSObject.P_READONLY|FBSObject.P_NOENUM;
        createProperty("abs",attrib,new MathMethod(0)); //$NON-NLS-1$
        createProperty("acos",attrib,new MathMethod(1)); //$NON-NLS-1$
        createProperty("asin",attrib,new MathMethod(2)); //$NON-NLS-1$
        createProperty("atan",attrib,new MathMethod(3)); //$NON-NLS-1$
        createProperty("atan2",attrib,new MathMethod(4)); //$NON-NLS-1$
        createProperty("ceil",attrib,new MathMethod(5)); //$NON-NLS-1$
        createProperty("cos",attrib,new MathMethod(6)); //$NON-NLS-1$
        createProperty("exp",attrib,new MathMethod(7)); //$NON-NLS-1$
        createProperty("floor",attrib,new MathMethod(8)); //$NON-NLS-1$
        createProperty("log",attrib,new MathMethod(9)); //$NON-NLS-1$
        createProperty("max",attrib,new MathMethod(10)); //$NON-NLS-1$
        createProperty("min",attrib,new MathMethod(11)); //$NON-NLS-1$
        createProperty("pow",attrib,new MathMethod(12)); //$NON-NLS-1$
        createProperty("random",attrib,new MathMethod(13)); //$NON-NLS-1$
        createProperty("round",attrib,new MathMethod(14)); //$NON-NLS-1$
        createProperty("sin",attrib,new MathMethod(15)); //$NON-NLS-1$
        createProperty("sqrt",attrib,new MathMethod(16)); //$NON-NLS-1$
        createProperty("tan",attrib,new MathMethod(17)); //$NON-NLS-1$
        if(JSOptions.get().hasMathExtensions()) {
            createProperty("toRadians",attrib,new MathMethod(18)); //$NON-NLS-1$
            createProperty("rad",attrib,new MathMethod(18)); //$NON-NLS-1$
            createProperty("toDegrees",attrib,new MathMethod(19)); //$NON-NLS-1$
            createProperty("deg",attrib,new MathMethod(19)); //$NON-NLS-1$
            createProperty("IEEEremainder",attrib,new MathMethod(20)); //$NON-NLS-1$
            createProperty("rint",attrib,new MathMethod(21)); //$NON-NLS-1$
        }
        
        createProperty("E",attrib,FBSNumber.get(E)); //$NON-NLS-1$
        createProperty("LN10",attrib,FBSNumber.get(LN10)); //$NON-NLS-1$
        createProperty("LN2",attrib,FBSNumber.get(LN2)); //$NON-NLS-1$
        createProperty("LOG2E",attrib,FBSNumber.get(LOG2E)); //$NON-NLS-1$
        createProperty("LOG10E",attrib,FBSNumber.get(LOG10E)); //$NON-NLS-1$
        createProperty("PI",attrib,FBSNumber.get(PI)); //$NON-NLS-1$
        createProperty("SQRT1_2",attrib,FBSNumber.get(SQRT1_2)); //$NON-NLS-1$
        createProperty("SQRT2",attrib,FBSNumber.get(SQRT2)); //$NON-NLS-1$
    }
    
    class MathMethod extends BuiltinFunction{
        int mIndex=-1;

        public MathMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
            switch(mIndex) {
                case 0: //abs
                case 1: //acos
                case 2: //asin
                case 3: //atan
                	return new String[] {"(value:D):D"}; //$NON-NLS-1$
                case 4: //atan2
                	return new String[] {"(y:Dx:D):D"}; //$NON-NLS-1$
                case 5: //ceil
                case 6: //cos
                case 7: //exp
                case 8: //floor
                case 9: //log
                	return new String[] {"(value:D):D"}; //$NON-NLS-1$
                case 10: //max
                case 11: //min
                	return new String[] {"(v1:Dv2:D):D"}; //$NON-NLS-1$
                case 12: //pow
                	return new String[] {"(x:Dy:D):D"}; //$NON-NLS-1$
                case 13: //random
                	return new String[] {"():D"}; //$NON-NLS-1$
                case 14: //round
                case 15: //sin
                case 16: //sqrt
                case 17: //tan
                case 18: //toRadians
                case 19: //toDegrees
                	return new String[] {"(value:D):D"}; //$NON-NLS-1$
                case 20: //IEEEremainder
                	return new String[] {"(f1:Df2:D):D"}; //$NON-NLS-1$
                case 21: //rint
                	return new String[] {"(value:D):D"}; //$NON-NLS-1$
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
	            case 0: {//abs
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(abs(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 1: {//acos
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(acos(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 2: {//asin
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(asin(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 3: {//atan
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(atan(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 4: {//atan2
	            	if(args.getCount()>=2) {
	            		double x = args.get(0).doubleValue();
	            		double y = args.get(1).doubleValue();
	            		return FBSNumber.get(atan2(x,y));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 5: {//ceil
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(ceil(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 6: {//cos
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(cos(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 7: {//exp
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(exp(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 8: {//floor
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(floor(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 9: {//log
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(log(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 10: {//max
	            	double max = NumberObject.NEGATIVE_INFINITY;
	            	for( int i=0; i<args.getCount(); i++ ) {
	            		double d = args.get(i).doubleValue();
	            		if(Double.isNaN(d)) {
	            			return FBSNumber.NaN;
	            		}
	            		if(i==0) {
	            			max = d;
	            		} else {
	            			if(d>max) {
	            				max = d;
	            			}
	            		}
	            	}
	            	return FBSNumber.get(max);
	            }
	            case 11: {//min
	            	double min = NumberObject.POSITIVE_INFINITY;
	            	for( int i=0; i<args.getCount(); i++ ) {
	            		double d = args.get(i).doubleValue();
	            		if(Double.isNaN(d)) {
	            			return FBSNumber.NaN;
	            		}
	            		if(i==0) {
	            			min = d;
	            		} else {
	            			if(d<min) {
	            				min = d;
	            			}
	            		}
	            	}
	            	return FBSNumber.get(min);
	            }
	            case 12: {//pow
	            	if(args.getCount()>=2) {
	            		double x = args.get(0).doubleValue();
	            		double y = args.get(1).doubleValue();
	            		return FBSNumber.get(pow(x,y));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 13: {//random
            		return FBSNumber.get(random());
	            }
	            case 14: {//round
	            	if(!args.isUndefined(0)) {
	            		double d = args.get(0).doubleValue();
	            		if( Double.isNaN(d) ) {
	            			return FBSNumber.NaN;
	            		}
	            		if( d==Double.POSITIVE_INFINITY ) {
	            			return FBSNumber.POSITIVE_INFINITY;
	            		}
	            		if( d==Double.NEGATIVE_INFINITY ) {
	            			return FBSNumber.NEGATIVE_INFINITY;
	            		}
	            		return FBSNumber.get(round(d));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 15: {//sin
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(sin(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 16: {//sqrt
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(sqrt(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }
	            case 17: {//tan
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(tan(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }	

	            
	            ///////////////////////////////////////////////////////////////
            	// Extensions
                case 18: {//toRadians
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(toRadians(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }	
                case 19: {//toDegrees
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(toDegrees(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }	
                case 20: {//IEEEremainder
	            	if(!args.isUndefined(0) && !args.isUndefined(1)) {
	            		return FBSNumber.get(IEEEremainder(args.get(0).doubleValue(),args.get(1).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }	
                case 21: {//rint
	            	if(!args.isUndefined(0)) {
	            		return FBSNumber.get(rint(args.get(0).doubleValue()));
	            	}
	            	return FBSNumber.NaN;
	            }	
            }
            throw new InterpretException(JScriptResources.getString("MathObject.MathFuncNotFound.Exception")); //$NON-NLS-1$
        }
    }

    /**
     * Returns the trigonometric sine of an angle.  Special cases:
     * <ul><li>If the argument is NaN or an infinity, then the
     * result is NaN.
     * <li>If the argument is positive zero, then the result is
     * positive zero; if the argument is negative zero, then the
     * result is negative zero.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   an angle, in radians.
     * @return  the sine of the argument.
     */
    public static double sin(double a) {
    	return Math.sin(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the trigonometric cosine of an angle. Special case:
     * <ul><li>If the argument is NaN or an infinity, then the
     * result is NaN.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   an angle, in radians.
     * @return  the cosine of the argument.
     */
    public static double cos(double a) {
    	return Math.cos(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the trigonometric tangent of an angle.  Special cases:
     * <ul><li>If the argument is NaN or an infinity, then the result
     * is NaN.
     * <li>If the argument is positive zero, then the result is
     * positive zero; if the argument is negative zero, then the
     * result is negative zero</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   an angle, in radians.
     * @return  the tangent of the argument.
     */
    public static double tan(double a) {
    	return Math.tan(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the arc sine of an angle, in the range of -<i>pi</i>/2 through
     * <i>pi</i>/2. Special cases:
     * <ul><li>If the argument is NaN or its absolute value is greater
     * than 1, then the result is NaN.
     * <li>If the argument is positive zero, then the result is positive
     * zero; if the argument is negative zero, then the result is
     * negative zero.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   the <code>double</code> value whose arc sine is to
     *              be returned.
     * @return  the arc sine of the argument.
     */
    public static double asin(double a) {
    	return Math.asin(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the arc cosine of an angle, in the range of 0.0 through
     * <i>pi</i>.  Special case:
     * <ul><li>If the argument is NaN or its absolute value is greater
     * than 1, then the result is NaN.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   the <code>double</code> value whose arc cosine is to
     *              be returned.
     * @return  the arc cosine of the argument.
     */
    public static double acos(double a) {
    	return Math.acos(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the arc tangent of an angle, in the range of -<i>pi</i>/2
     * through <i>pi</i>/2.  Special cases:
     * <ul><li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is positive zero, then the result is positive
     * zero; if the argument is negative zero, then the result is
     * negative zero.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   the <code>double</code> value whose arc tangent is to
     *              be returned.
     * @return  the arc tangent of the argument.
     */
    public static double atan(double a) {
    	return Math.atan(a); // default impl. delegates to StrictMath
    }

    /**
     * Converts an angle measured in degrees to the equivalent angle
     * measured in radians.
     *
     * @param   angdeg   an angle, in degrees
     * @return  the measurement of the angle <code>angdeg</code>
     *          in radians.
     */
    public static double toRadians(double angdeg) {
    	return Math.toRadians(angdeg);
    }

    /**
     * Converts an angle measured in radians to the equivalent angle
     * measured in degrees.
     *
     * @param   angrad   an angle, in radians
     * @return  the measurement of the angle <code>angrad</code>
     *          in degrees.
     */
    public static double toDegrees(double angrad) {
    	return Math.toDegrees(angrad);
    }

    /**
     * Returns the exponential number <i>e</i> (i.e., 2.718...) raised to
     * the power of a <code>double</code> value.  Special cases:
     * <ul><li>If the argument is NaN, the result is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is negative infinity, then the result is
     * positive zero.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   a <code>double</code> value.
     * @return  the value <i>e</i><sup>a</sup>, where <i>e</i> is the base of
     *          the natural logarithms.
     */
    public static double exp(double a) {
    	return Math.exp(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the natural logarithm (base <i>e</i>) of a <code>double</code>
     * value.  Special cases:
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is negative infinity.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   a number greater than <code>0.0</code>.
     * @return  the value ln&nbsp;<code>a</code>, the natural logarithm of
     *          <code>a</code>.
     */
    public static double log(double a) {
    	return Math.log(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the correctly rounded positive square root of a
     * <code>double</code> value.
     * Special cases:
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is positive
     * infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is the same as the argument.</ul>
     * Otherwise, the result is the <code>double</code> value closest to
     * the true mathetmatical square root of the argument value.
     *
     * @param   a   a <code>double</code> value.
     * <!--@return  the value of &radic;&nbsp;<code>a</code>.-->
     * @return  the positive square root of <code>a</code>.
     *          If the argument is NaN or less than zero, the result is NaN.
     */
    public static double sqrt(double a) {
    	return Math.sqrt(a); // default impl. delegates to StrictMath
				   // Note that hardware sqrt instructions
				   // frequently can be directly used by JITs
				   // and should be much faster than doing
				   // Math.sqrt in software.
    }

    /**
     * Computes the remainder operation on two arguments as prescribed
     * by the IEEE 754 standard.
     * The remainder value is mathematically equal to
     * <code>f1&nbsp;-&nbsp;f2</code>&nbsp;&times;&nbsp;<i>n</i>,
     * where <i>n</i> is the mathematical integer closest to the exact
     * mathematical value of the quotient <code>f1/f2</code>, and if two
     * mathematical integers are equally close to <code>f1/f2</code>,
     * then <i>n</i> is the integer that is even. If the remainder is
     * zero, its sign is the same as the sign of the first argument.
     * Special cases:
     * <ul><li>If either argument is NaN, or the first argument is infinite,
     * or the second argument is positive zero or negative zero, then the
     * result is NaN.
     * <li>If the first argument is finite and the second argument is
     * infinite, then the result is the same as the first argument.</ul>
     *
     * @param   f1   the dividend.
     * @param   f2   the divisor.
     * @return  the remainder when <code>f1</code> is divided by
     *          <code>f2</code>.
     */
    public static  double IEEEremainder(double f1, double f2) {
        return Math.IEEEremainder(f1, f2);
    }

    /**
     * Returns the smallest (closest to negative infinity)
     * <code>double</code> value that is not less than the argument and is
     * equal to a mathematical integer. Special cases:
     * <ul><li>If the argument value is already equal to a mathematical
     * integer, then the result is the same as the argument.
     * <li>If the argument is NaN or an infinity or positive zero or negative
     * zero, then the result is the same as the argument.
     * <li>If the argument value is less than zero but greater than -1.0,
     * then the result is negative zero.</ul>
     * Note that the value of <code>Math.ceil(x)</code> is exactly the
     * value of <code>-Math.floor(-x)</code>.
     *
     * @param   a   a <code>double</code> value.
     * <!--@return  the value &lceil;&nbsp;<code>a</code>&nbsp;&rceil;.-->
     * @return  the smallest (closest to negative infinity)
     *          <code>double</code> value that is not less than the argument
     *          and is equal to a mathematical integer.
     */
    public static double ceil(double a) {
    	return Math.ceil(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the largest (closest to positive infinity)
     * <code>double</code> value that is not greater than the argument and
     * is equal to a mathematical integer. Special cases:
     * <ul><li>If the argument value is already equal to a mathematical
     * integer, then the result is the same as the argument.
     * <li>If the argument is NaN or an infinity or positive zero or
     * negative zero, then the result is the same as the argument.</ul>
     *
     * @param   a   a <code>double</code> value.
     * <!--@return  the value &lfloor;&nbsp;<code>a</code>&nbsp;&rfloor;.-->
     * @return  the largest (closest to positive infinity)
     *          <code>double</code> value that is not greater than the argument
     *          and is equal to a mathematical integer.
     */
    public static double floor(double a) {
    	return Math.floor(a); // default impl. delegates to StrictMath
    }

    /**
     * Returns the <code>double</code> value that is closest in value to
     * <code>a</code> and is equal to a mathematical integer. If two
     * <code>double</code> values that are mathematical integers are equally
     * close to the value of the argument, the result is the integer value
     * that is even. Special cases:
     * <ul><li>If the argument value is already equal to a mathematical
     * integer, then the result is the same as the argument.
     * <li>If the argument is NaN or an infinity or positive zero or negative
     * zero, then the result is the same as the argument.</ul>
     *
     * @param   a   a <code>double</code> value.
     * @return  the closest <code>double</code> value to <code>a</code> that is
     *          equal to a mathematical integer.
     */
    public static double rint(double a) {
    	return Math.rint(a); // default impl. delegates to StrictMath
    }

    /**
     * Converts rectangular coordinates (<code>b</code>,&nbsp;<code>a</code>)
     * to polar (r,&nbsp;<i>theta</i>).
     * This method computes the phase <i>theta</i> by computing an arc tangent
     * of <code>a/b</code> in the range of -<i>pi</i> to <i>pi</i>. Special
     * cases:
     * <ul><li>If either argument is NaN, then the result is NaN.
     * <li>If the first argument is positive zero and the second argument
     * is positive, or the first argument is positive and finite and the
     * second argument is positive infinity, then the result is positive
     * zero.
     * <li>If the first argument is negative zero and the second argument
     * is positive, or the first argument is negative and finite and the
     * second argument is positive infinity, then the result is negative zero.
     * <li>If the first argument is positive zero and the second argument
     * is negative, or the first argument is positive and finite and the
     * second argument is negative infinity, then the result is the
     * <code>double</code> value closest to pi.
     * <li>If the first argument is negative zero and the second argument
     * is negative, or the first argument is negative and finite and the
     * second argument is negative infinity, then the result is the
     * <code>double</code> value closest to -pi.
     * <li>If the first argument is positive and the second argument is
     * positive zero or negative zero, or the first argument is positive
     * infinity and the second argument is finite, then the result is the
     * <code>double</code> value closest to pi/2.
     * <li>If the first argument is negative and the second argument is
     * positive zero or negative zero, or the first argument is negative
     * infinity and the second argument is finite, then the result is the
     * <code>double</code> value closest to -pi/2.
     * <li>If both arguments are positive infinity, then the result is the
     * <code>double</code> value closest to pi/4.
     * <li>If the first argument is positive infinity and the second argument
     * is negative infinity, then the result is the <code>double</code>
     * value closest to 3*pi/4.
     * <li>If the first argument is negative infinity and the second argument
     * is positive infinity, then the result is the <code>double</code> value
     * closest to -pi/4.
     * <li>If both arguments are negative infinity, then the result is the
     * <code>double</code> value closest to -3*pi/4.</ul>
     * <p>
     * A result must be within 2 ulps of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   a <code>double</code> value.
     * @param   b   a <code>double</code> value.
     * @return  the <i>theta</i> component of the point
     *          (<i>r</i>,&nbsp;<i>theta</i>)
     *          in polar coordinates that corresponds to the point
     *          (<i>b</i>,&nbsp;<i>a</i>) in Cartesian coordinates.
     */
    public static double atan2(double a, double b) {
    	return Math.atan2(a, b); // default impl. delegates to StrictMath
    }

    /**
     * Returns of value of the first argument raised to the power of the
     * second argument. Special cases:
     * <ul><li>If the second argument is positive or negative zero, then the
     * result is 1.0.
     * <li>If the second argument is 1.0, then the result is the same as the
     * first argument.
     * <li>If the second argument is NaN, then the result is NaN.
     * <li>If the first argument is NaN and the second argument is nonzero,
     * then the result is NaN.
     * <li>If the absolute value of the first argument is greater than 1 and
     * the second argument is positive infinity, or the absolute value of the
     * first argument is less than 1 and the second argument is negative
     * infinity, then the result is positive infinity.
     * <li>If the absolute value of the first argument is greater than 1 and
     * the second argument is negative infinity, or the absolute value of the
     * first argument is less than 1 and the second argument is positive
     * infinity, then the result is positive zero.
     * <li>If the absolute value of the first argument equals 1 and the
     * second argument is infinite, then the result is NaN.
     * <li>If the first argument is positive zero and the second argument is
     * greater than zero, or the first argument is positive infinity and the
     * second argument is less than zero, then the result is positive zero.
     * <li>If the first argument is positive zero and the second argument is
     * less than zero, or the first argument is positive infinity and the
     * second argument is greater than zero, then the result is positive
     * infinity.
     * <li>If the first argument is negative zero and the second argument is
     * greater than zero but not a finite odd integer, or the first argument
     * is negative infinity and the second argument is less than zero but not
     * a finite odd integer, then the result is positive zero.
     * <li>If the first argument is negative zero and the second argument is
     * a positive finite odd integer, or the first argument is negative
     * infinity and the second argument is a negative finite odd integer,
     * then the result is negative zero.
     * <li>If the first argument is negative zero and the second argument is
     * less than zero but not a finite odd integer, or the first argument is
     * negative infinity and the second argument is greater than zero but not
     * a finite odd integer, then the result is positive infinity.
     * <li>If the first argument is negative zero and the second argument is
     * a negative finite odd integer, or the first argument is negative
     * infinity and the second argument is a positive finite odd integer,
     * then the result is negative infinity.
     * <li>If the first argument is less than zero and the second argument is
     * a finite even integer, then the result is equal to the result of
     * raising the absolute value of the first argument to the power of the
     * second argument.
     * <li>If the first argument is less than zero and the second argument
     * is a finite odd integer, then the result is equal to the negative of
     * the result of raising the absolute value of the first argument to the
     * power of the second argument.
     * <li>If the first argument is finite and less than zero and the second
     * argument is finite and not an integer, then the result is NaN.
     * <li>If both arguments are integers, then the result is exactly equal
     * to the mathematical result of raising the first argument to the power
     * of the second argument if that result can in fact be represented
     * exactly as a double value.</ul>
     *
     * <p>(In the foregoing descriptions, a floating-point value is
     * considered to be an integer if and only if it is a fixed point of the
     * method {@link #ceil <tt>ceil</tt>} or, which is the same thing, a fixed
     * point of the method {@link #floor <tt>floor</tt>}. A value is a fixed
     * point of a one-argument method if and only if the result of applying
     * the method to the value is equal to the value.)
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   a <code>double</code> value.
     * @param   b   a <code>double</code> value.
     * @return  the value <code>a<sup>b</sup></code>.
     */
    public static double pow(double a, double b) {
    	return Math.pow(a, b); // default impl. delegates to StrictMath
    }

    /**
     * Returns the closest <code>int</code> to the argument. The
     * result is rounded to an integer by adding 1/2, taking the
     * floor of the result, and casting the result to type <code>int</code>.
     * In other words, the result is equal to the value of the expression:
     * <p><pre>(int)Math.floor(a + 0.5f)</pre>
     * <p>
     * Special cases:
     * <ul><li>If the argument is NaN, the result is 0.
     * <li>If the argument is negative infinity or any value less than or
     * equal to the value of <code>Integer.MIN_VALUE</code>, the result is
     * equal to the value of <code>Integer.MIN_VALUE</code>.
     * <li>If the argument is positive infinity or any value greater than or
     * equal to the value of <code>Integer.MAX_VALUE</code>, the result is
     * equal to the value of <code>Integer.MAX_VALUE</code>.</ul>
     *
     * @param   a   a <code>float</code> value.
     * @return  the value of the argument rounded to the nearest
     *          <code>int</code> value.
     */
    public static int round(float a) {
    	return Math.round(a);
    }

    /**
     * Returns the closest <code>long</code> to the argument. The result
     * is rounded to an integer by adding 1/2, taking the floor of the
     * result, and casting the result to type <code>long</code>. In other
     * words, the result is equal to the value of the expression:
     * <p><pre>(long)Math.floor(a + 0.5d)</pre>
     * <p>
     * Special cases:
     * <ul><li>If the argument is NaN, the result is 0.
     * <li>If the argument is negative infinity or any value less than or
     * equal to the value of <code>Long.MIN_VALUE</code>, the result is
     * equal to the value of <code>Long.MIN_VALUE</code>.
     * <li>If the argument is positive infinity or any value greater than or
     * equal to the value of <code>Long.MAX_VALUE</code>, the result is
     * equal to the value of <code>Long.MAX_VALUE</code>.</ul>
     *
     * @param   a   a <code>double</code> value.
     * @return  the value of the argument rounded to the nearest
     *          <code>long</code> value.
     */
    public static long round(double a) {
    	return Math.round(a);
    }


    /**
     * Returns a <code>double</code> value with a positive sign, greater
     * than or equal to <code>0.0</code> and less than <code>1.0</code>.
     * Returned values are chosen pseudorandomly with (approximately)
     * uniform distribution from that range.
     * <p>
     * When this method is first called, it creates a single new
     * pseudorandom-number generator, exactly as if by the expression
     * <blockquote><pre>new java.util.Random</pre></blockquote>
     * This new pseudorandom-number generator is used thereafter for all
     * calls to this method and is used nowhere else.
     * <p>
     * This method is properly synchronized to allow correct use by more
     * than one thread. However, if many threads need to generate
     * pseudorandom numbers at a great rate, it may reduce contention for
     * each thread to have its own pseudorandom-number generator.
     *
     * @return  a pseudorandom <code>double</code> greater than or equal
     * to <code>0.0</code> and less than <code>1.0</code>.
     */
    public static double random() {
        return Math.random();
    }

    /**
     * Returns the absolute value of an <code>int</code> value.
     * If the argument is not negative, the argument is returned.
     * If the argument is negative, the negation of the argument is returned.
     * <p>
     * Note that if the argument is equal to the value of
     * <code>Integer.MIN_VALUE</code>, the most negative representable
     * <code>int</code> value, the result is that same value, which is
     * negative.
     *
     * @param   a   an <code>int</code> value.
     * @return  the absolute value of the argument.
     */
    public static double abs(double a) {
    	return Math.abs(a);
    }

}