/*
// Kevin Meeks
// CIS 310 - Operating Systems
// Assignment 4 - File System
// 11/20/2012
*/

import java.util.*;

public class DirectoryIterator implements Iterator<DirectoryEntry> {

    private Sector dir;		// Copy of sector zero
    private Filesystem fs;	// Filesystem containing entries

    private int offset;		// Current offset into sector zero
    private int entries;	// Number of entries currently passed

    public DirectoryIterator (Filesystem fs, Sector dir) {
        this.dir = dir;
        this.fs = fs;
        this.offset = 0;
    }

	// Returns true if another directory entry is available, false otherwise
    public boolean hasNext () {
        if(entries > 85)
            return false;
        for(int i = offset; i < i + 6; i++) {
            if(i == 512)
                break;
            if(dir.ba[i] != 0)
                return true;
        }
        return false;
    }

	// Returns a fully constructed directory entry
    public DirectoryEntry next () {
        if(offset == 512 || dir.ba[offset] == 0 && dir.ba[offset] == 0)
            return null;
        StringBuffer nameBuffer = new StringBuffer();
        int initialOffset = offset;
        int sect = dir.bb2i(offset);
        offset += 2;
        int size = dir.bb2i(offset);
		offset += 2;
        while(offset < 512 && dir.ba[offset] != 0) {
            nameBuffer.append((char)((int)dir.ba[offset]));
            offset++;
        }
		offset++;
        String name = nameBuffer.toString();
        return new DirectoryEntry(name, fs, initialOffset, sect, size);
    }

    public void remove () throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
