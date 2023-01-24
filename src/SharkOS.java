/*

    THE

 */

import java.io.File;
import java.util.*;


public class SharkOS {

    // to store information about the processes in a control block
    ArrayList<PCB> pcb;

    // to place instructions into memory
    FileHandler fileHandler;

    // processes to execute that arrive
    RequestQueue requestQueue;

    private String[] MEMORY;

    // saves the states of the registers to a string which gets outputted to a file
    // when the process is done
    static String registersOutput = "";

    private String[] programs = {"program1.txt",
                                "program2.txt",
                                "program3.txt",
                                "program4.txt",
                                "program5.txt",
                                "program6.txt"};


    SharkOS() {

        this.INIT_RESOURCES();

        this.runSharkOS();

    }

    /* GETS ALL THE PROCESS'S FROM THE DIRECTORY (MACHINE) */
    /* ADD ALL PROGRAMS TO A PCB BLOCK AND TO THE ARRIVAL TIMES TO THE SCHEDULER */
    public void setProcesses(){

        File[] files = new File("./programs/").listFiles();

        pcb = new ArrayList<>();

        // find all processes
        assert files != null;
        for (File file : files) {

            // it is a valid file and it ends in .txt
            if (file.isFile() && file.getName().contains("txt") && !file.getName().contains("outputs")) {

                String fileName = file.getName();

                int CPUTime = randomTime()/*5*/;

                PCB newProcess = new PCB( fileName, CPUTime );

                // add new PCB to container
                pcb.add( newProcess );

                int arrivalTime = randomTime();

                // for the purposes of this OS, if more than one process has the same time
                // it won't evaluate the process, this prevents duplicates
                while( requestQueue.scheduler.pcbArrivalTimes.containsKey(arrivalTime) ){

                    arrivalTime = randomTime();
                }

                // add tasks/processes to scheduler from the request queue class
                requestQueue.scheduler.pcbArrivalTimes.put( arrivalTime, newProcess );
            }
        }

    }

    /* LOAD RESOURCES THE OS WLL NEED TO RUN ALL PROGRAMS */
    public void INIT_RESOURCES() {

        this.MEMORY = new String[1024];

        this.requestQueue = new RequestQueue();

        this.fileHandler = new FileHandler();

        this.setProcesses();

    }

    /* LOAD PROCESS'S INSTRUCTIONS TO MEMORY */
    public void loadProgram(String programFile) {

        fileHandler.readFile(programFile, MEMORY, requestQueue.front().PSIAR);

    }

    /* gives random BURST TIME / CPUTIME for each process between 0 - 10 to 'simulate' a process running at a given time on a system */
    public int randomTime(){

        int value = (int)( Math.random() * 10) + 1;

        return value;
    }

    public void runSharkOS() {

        int maxArrivalTime = Collections.max(requestQueue.scheduler.pcbArrivalTimes.keySet());

        System.out.println("TIME QUANTUM FOR ALL PROCESSES IN THE REQUEST QUEUE: " + requestQueue.scheduler.TQ  + " SECONDS\n\n");

        for (int arrivalTime = 0; arrivalTime <= maxArrivalTime; arrivalTime += 1) {

            Map pcbArrivalTimes = requestQueue.scheduler.pcbArrivalTimes;

            // if a process is to arrive at a time
            if ( pcbArrivalTimes.containsKey(arrivalTime)) {

                // get the process that is to arrive at that time
                PCB currentProcess = requestQueue.scheduler.pcbArrivalTimes.get(arrivalTime);

                // check if process was not already added by looking at the id
                // this would occur in the event the interrupt handler in the
                // RUNJOBS() function added the next jobs to come if the current one
                // was taking too long

                if (
                        !currentProcess.processState.equalsIgnoreCase("finished")
                    ) {

                    // add it to the request queue to be executed
                    requestQueue.enqueue(currentProcess);
                    System.out.print("[ RUNNING PROCESS ] " + requestQueue.front().id[0] + " ARRIVED AT " + arrivalTime + " SECONDS");
                    System.out.println(" WITH A BURST TIME OF " + requestQueue.front().CPUTime + " SECONDS"  + "\n" );

                }

                // run the jobs currently in the request queue
                RUNJOBS(arrivalTime);

//                System.out.println( "\t\t\t SYSTEM QUEUE " + requestQueue.string() );


            }

        }

        // when no more jobs are arriving
        System.out.println("JOBS ALL DONE");
    }

    private void RUNJOBS(int arrivalTime) {


        while (!requestQueue.empty()) {

            PCB currentQueueProcess = requestQueue.front();
            Scheduler scheduler = requestQueue.scheduler;

            // add the current process' instructions to MEMORY
            loadProgram(currentQueueProcess.id[0]);


            // if burst - quantum time <= 0 then job is executed without taking up other process's time that are arriving
            // and when the job was not already executed in the array
            if (
                    currentQueueProcess.CPUTime - scheduler.TQ <= 0
            ) {

                // RUN THE JOB
                while ( MEMORY[currentQueueProcess.PSIAR] != null && !MEMORY[currentQueueProcess.PSIAR].equalsIgnoreCase("HALT")) {

                    // FETCH
                    String opCode = MEMORY[currentQueueProcess.PSIAR].split(" ")[0];


                    // DECODE
                    switch (opCode) {

                        case "ADD" -> {

                            this.ADD();
                        }
                        case "SUB" -> {

                            this.SUB();
                        }
                        case "LDI" -> {

                            this.LDI();
                        }
                        case "LDA" -> {

                            this.LDA();
                        }
                        case "STR" -> {

                            this.STR();
                        }
                        case "CBR" -> {

                            this.CBR();
                        }
                        case "BRH" -> {

                            this.BRH();
                        }

                        /*default -> {

                            this.HALT();
                        }*/
                    }

                    currentQueueProcess.IR = opCode;
                    scheduler.pcbArrivalTimes.get(arrivalTime).IR = opCode;

                    addRegistersToString();

                    currentQueueProcess.PSIAR++;
                }

                // creates an output file
                createOutput();

                // 1. update the PCB status
                currentQueueProcess.processState = "FINISHED";
                scheduler.pcbArrivalTimes.get(arrivalTime).processState = "FINISHED";

                // 2. put back into arrival time queue
                scheduler.pcbArrivalTimes.put(arrivalTime, currentQueueProcess);

                System.out.println("\n[ RUNNING / FINISHED ] " + currentQueueProcess.id[0]);
//                System.out.println("\t" + requestQueue);
//                System.out.println("\t" + Arrays.toString(MEMORY));
//                System.out.println("");

                END_JOB();

                // 3. dequeue the current process as it is done
                requestQueue.dequeue();

                System.out.println( "\t\t\t SYSTEM QUEUE " + requestQueue.string()  + "\n"  );


            // YIELD INSTRUCTION / INTERRUPT HANDLER TO HANDLE INTERRUPTS
            } else { // if time remain left

                // switches context of the process(es)
                YLD(arrivalTime, currentQueueProcess, scheduler);
            }

        }

    }

    private void YLD(int arrivalTime, PCB currentQueueProcess, Scheduler scheduler){

        // 1. update the burst time to be the remaining time to run the process to PCB
        // where BT - TQ = remaining time
        currentQueueProcess.CPUTime -= scheduler.TQ;
        scheduler.pcbArrivalTimes.get(arrivalTime).processState = "WAITING";

        // 2. update the process status
        currentQueueProcess.processState = "WAITING";

        // 3. put back into arrival time map to update its info / PCB
        scheduler.pcbArrivalTimes.put(arrivalTime, currentQueueProcess);


        System.out.print("[ WAITING ] " + currentQueueProcess.id[0] + " ");
        System.out.println( " BECAUSE IT HAS " + currentQueueProcess.CPUTime + " SECONDS LEFT");


        registersOutput = "";

        // 3.
/*                  * if the current job won't be done before next process come
                        pause it and move the next process to the queue

                ALTERNATIVELY

                    * if the next job(s) arrive before the current one can finish
                        add them to the  REQUEST QUEUE
*/


        for ( Map.Entry<Integer, PCB> processes : requestQueue.scheduler.pcbArrivalTimes.entrySet() ) {

            int id = Integer.parseInt(processes.getValue().id[1]);

            if (
                    !processes.getValue().processState.equalsIgnoreCase("finished") &&
                            !requestQueue.find(id) && processes.getKey() < requestQueue.front().CPUTime)
            {

                System.out.print("[ ADDING TO QUEUE ] " + processes.getValue().id[0] + " ARRIVED AT " + processes.getKey() + " SECONDS");
                System.out.println(" WITH A BURST TIME OF " + processes.getValue().CPUTime + " SECONDS");
                requestQueue.enqueue(processes.getValue());
            }
        }

        // 4. push the current process to the back and after putting/letting the next processes go before it
        requestQueue.dequeue();
        requestQueue.enqueue(currentQueueProcess);


        System.out.println( "\t\t\t SYSTEM QUEUE " + requestQueue.string() + "\n"  );


    }

    private void ADD() {

        int currDataAddress = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt(MEMORY[requestQueue.front().PSIAR].split(" ")[1]);

        requestQueue.front().SAR = currDataAddress;

        requestQueue.front().SDR = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt(MEMORY[currDataAddress]);

        requestQueue.front().TMPR = requestQueue.front().SDR;

        requestQueue.front().ACC += requestQueue.front().SDR;

    }

    private void SUB(){

        int currDataAddress = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt(MEMORY[requestQueue.front().PSIAR].split(" ")[1]);

        requestQueue.front().SAR = currDataAddress;

        // value at that address
        requestQueue.front().SDR = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt(MEMORY[currDataAddress]);

        // TMPR = SDR
        requestQueue.front().TMPR = requestQueue.front().SDR;

        // subtract value by what's in ACC
        requestQueue.front().ACC -= requestQueue.front().SDR;


    }

    public void CBR(){

        int jumpAddress = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt( MEMORY[requestQueue.front().PSIAR].split(" ")[1] );

        if( requestQueue.front().ACC == 0 ){

            // save address of PSIAR before updating it
            int PSIARPrevious = requestQueue.front().PSIAR;

            requestQueue.front().PSIAR = jumpAddress - 1;
            requestQueue.front().SAR = PSIARPrevious;
            requestQueue.front().SDR = jumpAddress;
        }

    }

    public void BRH(){


        // update PSIAR to be the address store in that instruction

        int addressOfInstruction = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt( MEMORY[requestQueue.front().PSIAR].split(" ")[1] );

        // save address of PSIAR before updating it
        int PSIARPrevious = requestQueue.front().PSIAR;

        // update PSIAR
        requestQueue.front().PSIAR = addressOfInstruction - 1;

        // store previous PSIAR to SAR
        requestQueue.front().SAR = PSIARPrevious;

        // SDR = PSIAR
        requestQueue.front().SDR = requestQueue.front().PSIAR;



    }

    private void LDA(){

        // get the address
        int addressOfInstruction = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt(MEMORY[requestQueue.front().PSIAR].split(" ")[1] );

        // get the value of that address
        int valueAtAddress = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt( MEMORY[ addressOfInstruction ] );

// update registers

        // load it to the ACC
        requestQueue.front().ACC = valueAtAddress;

        // save the address to SAR
        requestQueue.front().SAR = addressOfInstruction;

        requestQueue.front().SDR = addressOfInstruction;

        requestQueue.front().TMPR = addressOfInstruction;

    }

    private void LDI() {

        // update registers
        requestQueue.front().ACC = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt(MEMORY[requestQueue.front().PSIAR].split(" ")[1]);
        requestQueue.front().SAR = requestQueue.front().PSIAR;
        requestQueue.front().SDR = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt(MEMORY[requestQueue.front().PSIAR].split(" ")[1]);

    }

    private void STR() {

        // store whats in acc at the current str address
        int currStrAddress = MEMORY[requestQueue.front().PSIAR] == null ? 0 : Integer.parseInt(MEMORY[requestQueue.front().PSIAR].split(" ")[1]);
        this.MEMORY[currStrAddress] = requestQueue.front().ACC + "";

        // update registers
        requestQueue.front().SAR = currStrAddress;

        // value stored at current address
        requestQueue.front().SDR = this.MEMORY[currStrAddress] == null ? 0 : Integer.parseInt(this.MEMORY[currStrAddress]);

        // store the current store address of the value
        requestQueue.front().TMPR = currStrAddress;

    }

    private void HALT() {

        System.out.println("halting");

    }

    private void END_JOB(){

        // reset registers
        requestQueue.front().RESET_REGISTERS();

        // dump contents in memory
        Arrays.fill(MEMORY, null);

        System.out.println("END OF JOB");
    }

    private void addRegistersToString(){

        registersOutput += "INSTRUCTION: " + requestQueue.front().PSIAR + " " + MEMORY[requestQueue.front().PSIAR] + "\n";
        registersOutput += "\tACC " + requestQueue.front().ACC + "\n";
        registersOutput += "\tPSIAR " + (requestQueue.front().PSIAR+1) + "\n";
        registersOutput += "\tSAR " + requestQueue.front().SAR + "\n";
        registersOutput += "\tSDR " + requestQueue.front().SDR + "\n";
        registersOutput += "\tTMPR " + requestQueue.front().TMPR + "\n";
        registersOutput += "\tMEMORY " + Arrays.toString(MEMORY) + "\n";
        registersOutput += "___________________\n";

    }

    private void createOutput(){

        fileHandler.writeFile(requestQueue.front().id[0], registersOutput);

        // reset string for next process
        registersOutput = "";
    }

}

