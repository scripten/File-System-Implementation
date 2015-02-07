public class Sector {

    public byte [] ba;
    public static final int SECTORSZ = 512;

    public Sector() {
    	ba = new byte[SECTORSZ]; // could make this a final int instead
    }

    // transfer this sector into the given sector
    public void dmaTransfer(Sector s) {
	    for (int i = 0 ; i < SECTORSZ ; i++)
	        s.ba[i] = this.ba[i];
    }

    // converts two bytes of this sector beginning at the given offset
    // position into an int, using little endian encoding
    public int bb2i (int offset) {
    	return (ba[offset]&0xff) | (ba[offset+1]&0xff)<<8;
    }
    
    // converts a given int into two bytes of this sector beginning at
    // the given offset position, using little endian encoding
    public void i2bb (int i, int offset) {
	    ba[offset] = (byte)i;
	    ba[offset+1] = (byte)(i>>8);
    }
    
}
