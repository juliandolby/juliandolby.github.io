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

import com.ibm.jscript.types.FBSDefaultObject;


/**
 * Base class for prototype.
 * This class simply provides helpers that are not available to FBSDefaultObject
 */
public class AbstractPrototype extends FBSDefaultObject {

    public AbstractPrototype(){
        super(ObjectPrototype.getObjectPrototype());
    }
    
    public AbstractPrototype(FBSDefaultObject prototype){
        super(prototype);
    }
}
