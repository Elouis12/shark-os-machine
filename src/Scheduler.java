/*

    Part of the Round Robin Scheduler

    1. tracks the Arrival Time (AT) of each process

    2. holds the Quantum Time (QT) given/allocated for all processes


*/

import java.util.HashMap;
import java.util.Map;

public class Scheduler {

    // each process is scheduled for 3 seconds
    public int TQ = 3;

    // holds the arrival times of the processes
    public Map<Integer, PCB> pcbArrivalTimes;

    Scheduler(){

        pcbArrivalTimes = new HashMap<>();
    }
}
