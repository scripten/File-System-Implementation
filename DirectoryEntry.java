/*
// Kevin Meeks
// CIS 310 - Operating Systems
// Assignment 4 - File System
// 11/20/2012
*/

public class DirectoryEntry {

    public String name;			// Name of the directory entry
    public Filesystem fs;		// Filesystem the entry belongs to
    public int dirEntryOffset;	// Offset into sector zero where the name resides
    public int sect;			// Start sector of the data in this entry
    public int size;			// Size of this entry

    public DirectoryEntry(String name, Filesystem fs,
			  int dirEntryOffset, int sect, int size) {
	    this.name = name;
	    this.fs = fs;
	    this.dirEntryOffset = dirEntryOffset;
	    this.sect = sect;
	    this.size = size;
    }

    public String toString() {
	    return "name=" + name + " offset=" + dirEntryOffset +
	        " sect=" + sect + " size=" + size;
    }
}
