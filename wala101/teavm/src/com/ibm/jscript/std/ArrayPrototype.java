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

import java.util.Iterator;
import java.util.Set;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.BuiltinFunction;
import com.ibm.jscript.types.Descriptor;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.util.QuickSort;


/**
 * This class allows to manipulate standard JavaScript Array objects.
 * @fbscript Array
 */
public class ArrayPrototype extends AbstractPrototype {

    private static ArrayPrototype arrayPrototype=new ArrayPrototype();

    public static ArrayPrototype getFBSArrayPrototype(){
        return arrayPrototype;
    }

    public ArrayPrototype(){
        int attrib=FBSObject.P_NODELETE|FBSObject.P_READONLY;
        createProperty("toString",attrib,new ArrayMethod(0)); //$NON-NLS-1$
        createProperty("toLocaleString",attrib,new ArrayMethod(1)); //$NON-NLS-1$
        createProperty("concat",attrib,new ArrayMethod(2)); //$NON-NLS-1$
        createProperty("join",attrib,new ArrayMethod(3)); //$NON-NLS-1$
        createProperty("pop",attrib,new ArrayMethod(4)); //$NON-NLS-1$
        createProperty("push",attrib,new ArrayMethod(5)); //$NON-NLS-1$
        createProperty("reverse",attrib,new ArrayMethod(6)); //$NON-NLS-1$
        createProperty("shift",attrib,new ArrayMethod(7)); //$NON-NLS-1$
        createProperty("slice",attrib,new ArrayMethod(8)); //$NON-NLS-1$
        createProperty("sort",attrib,new ArrayMethod(9)); //$NON-NLS-1$
        createProperty("splice",attrib,new ArrayMethod(10)); //$NON-NLS-1$
        createProperty("unshift",attrib,new ArrayMethod(11)); //$NON-NLS-1$
    }


    public void getAllPropertiesForHelp(Set set){
//        Descriptor.Method m0 = new Descriptor.Method("length","():I");
        Descriptor.Field f0 = new Descriptor.Field("length",FBSType.intType,false); //$NON-NLS-1$
        set.add(f0);

        Iterator it=getPropertyKeys();
        if( it!=null ) {
            while(it.hasNext() ) {
                String key = (String)it.next();
                try {
                    FBSValue v=get(key);
                    if(v instanceof ArrayMethod) {
                        ArrayMethod f= (ArrayMethod)v;
                        String[] args=f.getCallParameters();
                        if(args!=null) {
                            for(int i=0; i<args.length; i++) {
                                Descriptor.Method m=new Descriptor.Method(key, args[i]);
                                set.add(m);
                            }
                        } else {
                            Descriptor.Method m = new Descriptor.Method(key,FBSType.undefinedType,null);
                            set.add(m);
                        }
                    }
                } catch( InterpretException e ) {}
            }
        }
    }

    class ArrayMethod extends BuiltinFunction{
        int mIndex=-1;

        public ArrayMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
                switch(mIndex) {
                    case 0: // toString
                    case 1: // toLocaleString
                        return new String[] {"():T"}; //$NON-NLS-1$
                    case 2: // concat
                        return new String[] {"(objects:Ljava.lang.Object;):Lcom.ibm.jscript.std.ArrayObject;"}; //$NON-NLS-1$
                    case 3: // join
                        return new String[] {"(separator:C):T"}; //$NON-NLS-1$
                    case 4: // pop
                        return new String[] {"():Ljava.lang.Object;"}; //$NON-NLS-1$
                    case 5: // push
                        return new String[] {"(objects:Ljava.lang.Object;):I"}; //$NON-NLS-1$
                    case 6: // reverse
                        return new String[] {"():Lcom.ibm.jscript.std.ArrayObject;"}; //$NON-NLS-1$
                    case 7: // shift
                        return new String[] {"():Ljava.lang.Object;"}; //$NON-NLS-1$
                    case 8: // slice
                        return new String[] {"(start:Iend:I):Lcom.ibm.jscript.std.ArrayObject;"}; //$NON-NLS-1$
                    case 9: // sort
                        return new String[] {"(comparefn:Ljava.lang.Object;):Lcom.ibm.jscript.std.ArrayObject;"}; //$NON-NLS-1$
                    case 10: // splice
                        return new String[] {"(start:Ideletecount:I):Lcom.ibm.jscript.std.ArrayObject;"}; //$NON-NLS-1$
                    case 11: // unshift
                        return new String[] {"(objects:Ljava.lang.Object;):Lcom.ibm.jscript.std.ArrayObject;"}; //$NON-NLS-1$
                }
                return super.getCallParameters();
        }


        public FBSValue call(FBSValueVector args, FBSObject _this) throws JavaScriptException {
            return FBSUndefined.undefinedValue;
        }

        //Unlike other objects, here it is important to pass the context !!!
        //Because of a call back function in the sort() method, that function must execute
        // in the context.
        public FBSValue call(IExecutionContext context, FBSValueVector args, FBSObject _this) throws JavaScriptException {
            try{
                int l = 0;
                StringBuffer sb = null;
                String str = null;
                FBSValue v;
                switch(mIndex){
                    case 0:     // toString
                    case 1: {     // toLocaleString
                    	if(_this instanceof ArrayObject) {
	                        ArrayObject array=(ArrayObject)_this;
	                        l = array.get("length").intValue(); //$NON-NLS-1$
	                        sb = new StringBuffer();
	                        for(int i=0; i<l; i++){
	                            sb.append(array.get(i).stringValue());
	                            if (i<l-1){
	                                sb.append(',');
	                            }
	                        }
	                        str = sb.toString();
	                        return FBSString.get(str);
                    	}
                    	return FBSString.emptyString;
                    }
                    
                    case 2:{     // concat
                    	ArrayObject array=(ArrayObject)_this;
                        l = array.get("length").intValue(); //$NON-NLS-1$
                        int s = args.size();
                        ArrayObject newArray = new ArrayObject(l+s);
                        int index=0;
                        for(int i=0; i<l+s; i++){
                            if (i<l){
                                FBSValue tmpv = array.get(i);
                                if (tmpv.isArray()){
                                    FBSValue.IValues values = tmpv.getValues();
                                    for(; values.hasNext();){
                                        newArray.put(index++,values.next());
                                    }
                                }else{
                                    newArray.put(index++,array.get(i));
                                }
                            }else
                            if (i>=l && i<l+s){
                                FBSValue tmpv = args.getFBSValue(i-l);
                                if (tmpv.isArray()){
                                    FBSValue.IValues values = tmpv.getValues();
                                    for(; values.hasNext();){
                                        newArray.put(index++,values.next());
                                    }
                                }else{
                                    newArray.put(index++,args.getFBSValue(i-l));
                                }
                            }
                        }
                        return newArray;
                    }
                    case 3: {     // join
                        ArrayObject array=(ArrayObject)_this;
                        String sep = ","; //$NON-NLS-1$
                        if (args.size()>0){
                            String sep1=args.getFBSValue(0).stringValue();
                            sep = sep1!=null?sep1:sep;
                        }
                        l = array.get("length").intValue(); //$NON-NLS-1$
                        sb = new StringBuffer();
                        for(int i=0; i<l; i++){
                            sb.append(array.get(i).toFBSString().stringValue());
                            if (i<l-1){
                                sb.append(sep);
                            }
                        }
                        str = sb.toString();
                        return FBSString.get(str);
                    }    
                    case 4: {     // pop
                        ArrayObject array=(ArrayObject)_this;
                        l = array.get("length").intValue(); //$NON-NLS-1$
                        if (l<=0){
                            return FBSUndefined.undefinedValue;
                        }
                        v = array.get(l-1);
                        array.put("length",FBSNumber.get(l-1)); //$NON-NLS-1$
                        return v;
                    }
                    case 5: {     // push
                        ArrayObject array=(ArrayObject)_this;
                        l = array.get("length").intValue(); //$NON-NLS-1$
                        for(int i=0; i<args.size(); i++){
                            array.put(l+i,args.getFBSValue(i));
                        }
                        return array.get("length"); //$NON-NLS-1$
                    }
                    case 6: {    // reverse
                        ArrayObject array=(ArrayObject)_this;
                        l = array.get("length").intValue(); //$NON-NLS-1$
                        int m = (int)Math.floor(l/2);
                        for(int i=0; i<m; i++){
                            int i1 = l-i-1;
                            FBSValue v0 = array.get(i);
                            FBSValue v1 = array.get(i1);
                            array.put(i,v1);
                            array.put(i1,v0);
                        }
                        return array;
                    }
                    case 7: {     // shift
                        ArrayObject array=(ArrayObject)_this;
                        int L = array.get("length").intValue(); //$NON-NLS-1$
                        if (L<=0){
                            return FBSUndefined.undefinedValue;
                        }
                        v = array.get(0);
                        for(int i=1; i<L; i++){
                            FBSValue v0 = array.get(i);
                            array.put(i-1,v0);
                        }
                        array.put("length",FBSNumber.get(L-1)); //$NON-NLS-1$
                        array.delete(String.valueOf(L));
                        return v;
                    }
                    case 8:{     // slice
                        ArrayObject array=(ArrayObject)_this;
                        if (args.size()==0){
                            return FBSUndefined.undefinedValue;
                        }
                        if (args.size()==1){
                            args.add(FBSUndefined.undefinedValue);
                        }
                        l = array.get("length").intValue(); //$NON-NLS-1$
                        int s = args.getFBSValue(0).intValue();
                        if (s<0){
                            s = Math.max(l+s,0);
                        }else{
                            s = Math.min(s,l);
                        }

                        int e;
                        if (args.getFBSValue(1)==FBSUndefined.undefinedValue){
                            e = l;
                        }else{
                            e = args.getFBSValue(1).intValue();
                        }

                        if (e<0){
                            e = Math.max(l+e,0);
                        }else{
                            e = Math.min(e,l);
                        }
                        ArrayObject newArray = new ArrayObject(e-s);
                        for(int i=s; i<e; i++){
                            FBSValue v0 = array.get(i);
                            newArray.put(i-s,v0);
                        }
                        return newArray;
                    }

                    case 9: {     // sort
/*                        if (args.size()<1){
                            return array;
                        }*/
//                        if (args.getFBSValue(0) instanceof FBSObject){
                        ArrayObject array=(ArrayObject)_this;

                        FBSObject function = args.size()>0?(FBSObject)args.getFBSValue(0):null;

                        JavaScriptException ex = (new QuickSort(){
                            int size;
                            FBSObject function;
                            ArrayObject array;
                            FBSValueVector args = new FBSValueVector();
                            IExecutionContext context;
                            JavaScriptException exception = null;

                            public JavaScriptException sortArray(IExecutionContext context, ArrayObject array, FBSObject function){
                                this.context = context;
                                this.array = array;
                                this.function = function;
                                this.size = array.getArrayLength();
                                this.sort();
                                return exception;
                            }

                            public int getCount(){
                                return size;
                            }

                            public int compare(int idx1, int idx2){
                                if (exception!=null){
                                    return 0; // avoid problems if an exception occured;
                                }
                                try{
                                    if(function!=null){
                                        args.clear();
                                        args.add(array.get(idx1));
                                        args.add(array.get(idx2));
                                        FBSValue v = function.call(context,args,array);
                                        return v.intValue();
                                    }else{
                                        String val1=array.get(idx1).stringValue();
                                        String val2=array.get(idx2).stringValue();
                                        return val1.compareTo(val2);
                                    }
                                }catch(JavaScriptException e){
                                    size = 0; // cancel the sort operation
                                    exception = e;
                                }
                                return 0;
                            }

                            public void exchange(int idx1, int idx2){
                                try{
                                    if (exception!=null){
                                        return ; // avoid problems if an exception occured;
                                    }
                                    FBSValue v = array.get(idx1);
                                    array.put(idx1,array.get(idx2));
                                    array.put(idx2,v);
                                }catch(JavaScriptException e){
                                    size = 0; // cancel the sort operation
                                    exception = e;
                                }
                            }
                        }).sortArray(context,array,function);

                        if (ex!=null){
                            throw ex;
                        }
//                        }
                        return array;
                    }

                    case 10:{    // splice
                        ArrayObject array=(ArrayObject)_this;
                        if (args.size()<2){
                            return FBSUndefined.undefinedValue;
                        }
                        l = array.get("length").intValue(); //$NON-NLS-1$
                        int s = args.getFBSValue(0).intValue();
                        int cnt = args.getFBSValue(1).intValue();
                        int argCnt = args.size()-2;
                        ArrayObject newArray = new ArrayObject(l-cnt+argCnt);
                        int j=0;
                        for (int i=0; i<s; i++){
                            FBSValue v1 = array.get(i);
                            newArray.put(i,v1);
                        }
                        j=s;
                        for (int i=0; i<argCnt; i++){
                            FBSValue v1 = args.getFBSValue(i+2);
                            newArray.put(j++,v1);
                        }
                        for (int i=s+cnt; i<l; i++){
                            FBSValue v1 = array.get(i);
                            newArray.put(j++,v1);
                        }
//                            newArray.put("length",FBSNumber.get(l-cnt+argCnt));
                        return newArray;
                    }

                    case 11:{    // unshift
                        ArrayObject array=(ArrayObject)_this;
                        l = array.get("length").intValue(); //$NON-NLS-1$
                        int s = args.size();
                        if (s==0){
                            return array;
                        }

                        for (int i=l-1; i>=0; i--){
                            FBSValue v1 = array.get(i);
                            array.delete(String.valueOf(i));
                            array.put(i+s,v1);
                        }

                        for( int j=0; j<s; j++){
                            array.put(j,args.getFBSValue(j));
                        }

                        array.put("length", FBSNumber.get(s+l)); //$NON-NLS-1$
                        return array;
                    }
                }
            }catch(Exception e){
                throw new InterpretException(e,JScriptResources.getString("ArrayPrototype.IllegalArrayFunctionCall.Exception")); //$NON-NLS-1$
            }
            throw new InterpretException(JScriptResources.getString("ArrayPrototype.ArrayFunctionNotFound.Exception")); //$NON-NLS-1$

        }
    }

    public String getName(){
        return "Array"; //$NON-NLS-1$
    }

//------------------------------------------------------------------------------
// dummy methods for help
    public static class Array{}

    /**
    * @fbscript
    */
    public String toString(){return null;}

    /**
    * @fbscript
    */
    public String toLocaleString(){return null;}
    /**
    * @return an array containing the array elements of the object followed by the array elements of each argument in order.
    * @fbscript
    */
    public Array concat(){return null;}

    /**
    * The elements of the array are converted to strings, and these strings are then concatenated,
    *  separated by occurrences of the separator.
    *  If no separator is provided, a single comma is used as the separator.
    *  @param separator to be used between 2 elemets.
    *  @return a string resulting form the concatenation of all elements
    *  @fbscript
    */
    public String join(char separator){return null;}

    /**
    * The last element of the array is removed from the array and returned.
    * @return the last element of the array
    * @fbscript
    */
    public Object pop(){return null;}

    /**
    * Appends the arguments to the end of the array, in the order in which they appear.
    * The new length of the array is returned as the result of the call.
    * @return the new length o fthe array
    * @fbscript
    */
    public int push(){return 0;}

    /**
    * The elements of the array are rearranged so as to reverse their order.
    * @return the array itself.
    * @fbscript
    */
    public Array reverse(){return null;}

    /**
    * The first element of the array is removed from the array and returned.
    * @return the first element of the array
    * @fbscript
    */
    public Object shift(){return null;}

    /**
    * Extracts a subarray from this array.
    * The slice method takes two arguments, start and end, and returns
    * an array containing the elements of the array from element start up to,
    * but not including, element end (or through the end of the array if end is undefined).
    * If start is negative, it is treated as (length+start) where length is the length of the array.
    * If end is negative, it is treated as (length+end) where length is the length of the array.
    * @return array
    * @fbscript
    */
    public Array slice(int start, int end){return null;}

    /**
    * The elements of this array are sorted.
    * The sort is not necessarily stable (that is, elements that compare equal do not necessarily remain in their original order).
    * If comparefn is not undefined, it should be a function that accepts two arguments x and y and returns a negative value if x < y, zero if x = y, or a positive value if x > y.
    * @fbscript
    */
    public Array sort(Object comparefn){return null;}

    /**
     * When the splice method is called with two or more arguments start, deleteCount and (optionally) item1, item2,
     * etc., the deleteCount elements of the array starting at array index start are replaced by the arguments item1, item2,
     * etc.
     * @fbscript
     */
    public Array splice(int start, int deletcount){return null;}

    /**
     * The arguments are prepended to the start of the array, such that their order
     * within the array is the same as the order in which they appear in the argument list.
     * @fbscript
     */
    public Array unshift(){return null;}

}
