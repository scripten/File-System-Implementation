public class Partition {

    private Sector [] sectors;
    public int ns;

    public Partition(int ns) {
	    this.ns = ns;
	    sectors = new Sector[ns];
	    for (int i=0 ; i<ns ; i++)
	        sectors[i] = new Sector();
    }

    // copy sector n of this partition into the given sector
    public void readSector(Sector s, int n) {
	    sectors[n].dmaTransfer(s);
    }

    // copy the given sector into sector n of this partition
    public void writeSector(Sector s, int n) {
	    s.dmaTransfer(sectors[n]);
    }

}
