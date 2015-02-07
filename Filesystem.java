/*
// Kevin Meeks
// CIS 310 - Operating Systems
// Assignment 4 - File System
// 11/20/2012
*/

import java.util.*;
import java.io.*;
import java.lang.*;

public class Filesystem {

    public Partition part;	// Partition containing actual sectors in file system

    private Sector dir;		// Contains data for sector zero (For easy access)
    private Sector bitmap;	// Contains data for sector one (For easy access)
    private int ns;			// Number of sectors in the file system

    public Filesystem (int ns) throws Exception{
        // Create partition with ns sectors
        if(ns < 2 || ns > 512) {
            System.out.println("Sector count invalid. Must be between 2 and 512.");
            throw new Exception();
        }
        part = new Partition(ns);
        this.ns = ns;
        // Initialize sector zero
        dir = new Sector();
        dir.ba[0] = 0;
        part.writeSector(dir, 0);
        // Initialize sector one
        bitmap = new Sector();
        bitmap.ba[0] = 1;
        bitmap.ba[1] = 1;
        for(int i = ns; i < 512; i++) {
            bitmap.ba[i] = 1;
        }
        part.writeSector(bitmap, 1);
    }

    public Filesystem () {
    }

	// Opens a directory iterator
    private DirectoryIterator openDir () {
        return new DirectoryIterator(this, dir);
    }

	// Looks up a file with the provided name using the directory iterator class
    public DirectoryEntry lookup (String name) {
        DirectoryIterator dirIter = openDir();
        DirectoryEntry dirEnt = null;
        while(dirIter.hasNext()) {
            DirectoryEntry cur = dirIter.next();
            if(cur.name.equals(name)) {
                dirEnt = cur;
                break;
            }
        }
        return dirEnt;
    }

	// Dumps the bytes in each sector along with the number of sectors in this file system
    public void dump () {
        System.out.println(ns);
        for(int s = 0; s < ns; s++) {
            if(bitmap.ba[s] == 1) {
                Sector buffer = new Sector();
                part.readSector(buffer, s);
                for(int i = 0; i < 512; i++)
                    System.out.println((int)buffer.ba[i]&0xff);
            }
        }
    }

	// Loads a filesystem into memory from a prior dump
    public void load (String filename) throws FileNotFoundException{
        File file = new File(filename);
        if(!file.exists())
            throw new FileNotFoundException();
        Scanner sc = new Scanner(file);
		// Initialize fields
        ns = sc.nextInt();
        this.part = new Partition(ns);
        this.dir = new Sector();
        this.bitmap = new Sector();
		// Build and write sectors zero and one
        for(int i = 0; i < 512; i++)
            this.dir.ba[i] = (byte)sc.nextInt();
        for(int i = 0; i < 512; i++)
            this.bitmap.ba[i] = (byte)sc.nextInt();
        this.part.writeSector(dir, 0);
        this.part.writeSector(bitmap, 1);
        int offset = 1024;
        int currentSector = 1;
		// Write the remaining sectors
        while(currentSector < ns) {
            if(this.bitmap.ba[currentSector] == 1) {
                Sector buffer = new Sector();
                for(int i = 0; i < 512; i++) {
                    if(!sc.hasNext())
                        break;
                    buffer.ba[i] = (byte)sc.nextInt();
                }
                this.part.writeSector(buffer, currentSector);
            }
            currentSector++;
        }
    }

	// Opens a file with the name provided
    public FileReader open (String name) {
        DirectoryEntry dirEnt = lookup(name);
        if(dirEnt == null)
            return null;
        return new FileReader(dirEnt);
    }

	// Creates a new directory entry in an allocation of free space, then returns a FileWriter
	// with that directory to copy data to
    public FileWriter create (String name) {
		// Check if file exists yet or if there is not enough space
        if(lookup(name) != null)
            return null;
        Allocate alloc = thaw();
        if((alloc.start + alloc.sects) > ns)
            return null;
        DirectoryIterator iter = openDir();
        int count  = 0;
        int offset = 0;
		// Find where the directory entry will begin
        DirectoryEntry temp = iter.next();
        while(count < alloc.start && temp != null) {
            count += temp.sect;
            offset = temp.dirEntryOffset + temp.name.length() + 2;
            temp = iter.next();
        }
		// Create the directory entry...
        DirectoryEntry dirEnt = new DirectoryEntry(name, this,
                                offset, alloc.start,
                                (alloc.sects * 512));
		// And write it to sector zero
        dir.i2bb(alloc.start, offset);
        dir.i2bb(dirEnt.size, offset + 2);
		for(int i = 0; i < name.length(); i++) {
            dir.ba[offset + 4 + i] = (byte)name.charAt(i);
        }
        dir.ba[offset + 4 + name.length()] = (byte)0;
        return new FileWriter(dirEnt, alloc);
    }

	// Removes a directory from the file system and moves the remaining directories back
	// and returns the number of removed directories
    public int remove (String name) {
        int result = -1;
        DirectoryEntry dirEnt = lookup(name);
        if(dirEnt == null)
            return result;
        DirectoryIterator iter = openDir();
		DirectoryEntry cur = iter.next();
        while(!cur.name.equals(dirEnt.name) && iter.hasNext()) {
			cur = iter.next();
		}
        return result;
    }

    // return an Allocate object by scanning the bitmap
    // for the maximum size collection of contiguous free sectors
    // in the filesystem and marking them as allocated.
    // if there are no free sectors, return null
    public Allocate thaw() {
        int sects = 0; // how many free sectors have been found so far
        int start = 0; // where the current largest chunk of free sectors starts
        int scan = 2; // start scanning at sector two (why?)
        while (scan < ns) {
            if (bitmap.ba[scan] == 0) {
                // we have found a free sector
                // now look for more of them, beginning at scan+1
                int nxt = scan+1;
                while (nxt < ns && bitmap.ba[nxt] == 0)
                    nxt++;
                // at this point, nxt is just beyond the last free sector
                // starting at scan
                int count = nxt - scan; // the count of free sectors found
                if (count > sects) {
                    // we have a new maximum
                    sects = count;
                    start = scan;
                }
                scan = nxt; // where to start the search for more chunks
            } else
                scan++;
        }
        if (sects > 0) {
            for (int i=0 ; i<sects ; i++)
                bitmap.ba[start+i] = 1; // mark everything as allocated
            return new Allocate(start, sects);
        } else
            return null;
    }

    // freezes sectors in the bitmap as used
    // by freeing the sectors beginning with the {\tt free} offset
    // up to the end of the allocation given by the {\tt Allocate} object.
    // Note that {\tt sects} sectors had already been marked
    // as tentatively used by the create method, so we only need
    // to mark the unused sectors as free.
    public void freeze(Allocate a, int free) {
        while (free < a.sects) {
            bitmap.ba[a.start+free] = 0;
            free++;
        }
    }
}
