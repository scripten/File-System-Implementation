/*
// Kevin Meeks
// CIS 310 - Operating Systems
// Assignment 4 - File System
// 11/20/2012
*/

public class FileReader {

    private DirectoryEntry dir;	// Entry being read from
    private int cursor;			// Current location in file

    public FileReader (DirectoryEntry dir) {
        this.dir = dir;
        cursor = 0;
    }

	// Returns character (as an int) from the current location in the file, then advances
    public int getChar () {
        if(cursor <= dir.size) {
            Sector buffer = new Sector();
			int result = 0;
			if(cursor < 511) {
				dir.fs.part.readSector(buffer, dir.sect - 1);
				result = (int)(buffer.ba[cursor]&0xff);
			} else {
				dir.fs.part.readSector(buffer, dir.sect + (cursor / 512) - 1);
				result = (int)(buffer.ba[cursor % 512]&0xff);
			}
            cursor++;
            return result;
        } else	// Returns -1 if file is fully read
            return -1;
    }

    public void close () {
    }
}
