import java.util.Arrays;

/*

*/
public class PCB{

    public String[] id; // holds the file name and its hashed integer name
    public int ACC = 0;
    public int PSIAR = 210/*300*/;
    public int SAR = 0;
    public int SDR = 0;
    public int TMPR = 0;
    public int CSIAR = 0;
    public String IR = "";
    public int MIR = 0;

    public String processState = "READY";

    public int CPUTime;

    @Override
    public String toString() {
        return "Process { " +
                " id=" + Arrays.toString(id) +
                ", ACC=" + ACC +
                ", PSIAR=" + PSIAR +
                ", SAR=" + SAR +
                ", SDR=" + SDR +
                ", TMPR=" + TMPR +
                ", CSIAR=" + CSIAR +
                ", IR=" + IR +
                ", MIR=" + MIR +
                ", MIR=" + IR +
                ", processState='" + processState + '\''+
                " }";
    }


    public void RESET_REGISTERS() {

        this.ACC =
                this.PSIAR =
                        this.SAR =
                                this.SDR =
                                        this.TMPR =
                                                this.CSIAR =
                                                        this.MIR = 0;
        this.IR = "";

    }
    PCB(String processName, int CPUTime){

        this.id = new String[2];

        this.id[0] = processName; // process name
        this.id[1] = String.valueOf(processName.hashCode()); // hashed value of process so it can be id'ed

        // burst time for the process to complete
        this.CPUTime = CPUTime;

    }
}
