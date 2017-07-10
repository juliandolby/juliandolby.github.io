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
 *
 */
public final class SystemCache {

    private String name;
    private int maxSize;
    private int size;
    private MapEntry listStart;
    private MapEntry listEnd;
    private MapEntry[] entries;

    private static class MapEntry {
        MapEntry    nextHash;
        MapEntry    prevList;
        MapEntry    nextList;
        int         hashCode;
        String      key;
        Object      value;
        MapEntry( String key, Object value ) {
            this.key = key;
            this.value = value;
            this.hashCode = key.hashCode();
        }
    }

    public SystemCache(String name, int maxSize) {
        this.name = name;
        this.maxSize = maxSize;
        this.entries = new MapEntry[211];
    }

    public int size() {
        return size;
    }

    public synchronized Object get( String key ) {
        //TDiag.trace( "Getting '{0}'", key );
        MapEntry e = getEntry(key);
        if( e!=null ) {
            moveToStart(e);
            return e.value;
        }
        return null;
    }

    public synchronized void put( String key, Object value ) {
        if( maxSize>0 ) {
            MapEntry e = getEntry(key);
            if( e!=null ) {
                e.value = value;
                moveToStart(e);
            } else {
                e = new MapEntry(key,value);
                // Remove the eldest one?
                if( size==maxSize ) {
                    removeEntry(listEnd);
                }
                // Insert the new entry in the HashTable
                int slot = getSlot(e.hashCode);
                e.nextHash = entries[slot];
                entries[slot] = e;
                // And in the list
                if( listStart!=null ) {
                    listStart.prevList = e;
                }
                e.nextList = listStart;
                listStart = e;
                if( listEnd==null ) {
                    listEnd = e;
                }
                size++;
            }
        }
    }

    private final int getSlot(int hashCode) {
        return (hashCode & 0x7FFFFFFF) % entries.length;
    }
    private final MapEntry getEntry( String key ) {
        int hashCode = key.hashCode();
        for( MapEntry e=entries[getSlot(hashCode)]; e!=null; e=e.nextHash ) {
            if( e.hashCode==hashCode && e.key.equals(key) ) {
                return e;
            }
        }
        return null;
    }

    private final void moveToStart( MapEntry entry ) {
        if( entry!=listStart ) {
            // Remove it from the list
            entry.prevList.nextList = entry.nextList;
            if( entry.nextList!=null ) {
                entry.nextList.prevList = entry.prevList;
            } else {
                listEnd = entry.prevList;
            }
            // And add it a the top
            listStart.prevList = entry;
            entry.nextList = listStart;
            listStart = entry;
        }
    }

    private final void removeEntry(MapEntry entry) {
        int slot = getSlot(entry.hashCode);
        // Remove the entry from the hashtable
        MapEntry prev = entries[slot];
        MapEntry e = prev;
        while (e != null) {
            MapEntry next = e.nextHash;
            if (e==entry) {
                if (prev == e) {
                    entries[slot] = next;
                } else {
                    prev.nextHash = next;
                }
                break;
            }
            prev = e;
            e = next;
        }
        // Remove the entry from the linked list
        if( entry.prevList!=null ) {
            entry.prevList.nextList = entry.nextList;
        } else {
            listStart = entry.nextList;
        }
        if( entry.nextList!=null ) {
            entry.nextList.prevList = entry.prevList;
        } else {
            listEnd = entry.prevList;
        }
        // Decrease the map count
        size--;
    }

    private String getDisplayKey(String key) {
        return key.length()>32 ? key.substring(0,32) : key;
    }

// FOR DEBUG ONLY
//    private void dumpList() {
//        TDiag.trace( "list({0}), start={1}, end={2}", TString.toString(size), listStart!=null ? listStart.key : "<null>", listEnd!=null ? listEnd.key : "<null>" );
//        for( MapEntry e=listStart; e!=null; e=e.nextList ) {
//            TDiag.trace( "{0}, prev={1}, next ={2}", e.key, e.prevList!=null ? e.prevList.key : "<null>", e.nextList!=null ? e.nextList.key : "<null>" );
//        }
//    }
}
