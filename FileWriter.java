/*
// Kevin Meeks
// CIS 310 - Operating Systems
// Assignment 4 - File System
// 11/20/2012
*/

public class FileWriter {

    private DirectoryEntry dir;	// Entry being written to
    private int cursor;			// Current location in file
    private Allocate alloc;		// Allocate being used to track space
    private Sector buffer;		// Buffer file that will be used as temporary storage

    public FileWriter (DirectoryEntry dir, Allocate alloc) {
        this.dir = dir;
        this.alloc = alloc;
        buffer = new Sector();
        cursor = 0;
    }

	// Places character provided into the current file at the current cursor position
    public int putChar (int ch) {
        if(cursor < (alloc.sects * 512)) {
            if(cursor % 512 == 0 && cursor != 0) {
                dir.fs.part.writeSector(buffer, dir.sect + (cursor / 512) - 1);
                buffer = new Sector();
            }
			buffer.ba[cursor % 512] = (byte)ch;
            cursor++;
            return ch;
        } else
            return -1;
    }

	// Writes any spilled data into the next sector and marks it as used
    public void close () {
        dir.size = cursor;
        if(cursor % 512 != 0)
            dir.fs.part.writeSector(buffer, dir.sect + (cursor / 512) - 1);
        dir.fs.freeze(alloc, (cursor / 512));
    }
}
