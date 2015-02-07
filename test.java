import java.util.*;
import java.io.*;

public class test {

    public static void main (String [] args) throws Exception {
        Filesystem FS = new Filesystem();
		FS.load("FS");
		File file = new File("tellme");
        FileReader reader = FS.open("tellme");
		PrintStream ps = new PrintStream(file);
		int ch = 0;
        while(ch != -1) {
            ch = reader.getChar();
            ps.print((char)ch);
        }
    }
}
