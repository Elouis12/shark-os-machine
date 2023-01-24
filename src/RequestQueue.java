
/*

    ACTS AS THE INTERRUPT HANDLER
    THIS SERVES THE PURPOSE OF HANDLING INTERRUPTS THAT OCCURS

*/

public class RequestQueue {


    // round robin scheduler
    Scheduler scheduler;

    private Node head;
    private Node tail;

    public RequestQueue(){

        head = null;
        tail = null;

        scheduler = new Scheduler();
    }

    // pcb node
    public class Node{

        public PCB process;
        public Node next;
    }


    public void enqueue(PCB process){

        Node node = new Node();
        node.process = process;
        node.next = null;


        if( head == null && tail == null ){

            head = tail = node;
            return;
        }

        tail.next = node;
        tail = node;
    }


    public void dequeue(){


        if( head == null && tail == null ){

            return;

        }

        if( head == tail ){
            head = tail = null;

        }else{

            assert head != null;
            head = head.next;

        }

    }

    boolean find(int id){

        Node holdHead = head;

        while( holdHead != null ){

            if( Integer.parseInt(holdHead.process.id[1]) == id ){

                return true;
            }

            holdHead = holdHead.next;

        }

        return false;

    }

    boolean find(String id){

        Node holdHead = head;

        while( holdHead != null ){

            if( holdHead.process.id[0].equalsIgnoreCase(id)){

                return true;
            }

            holdHead = holdHead.next;

        }

        return false;
    }


    PCB getProcessById(int id){

        Node holdHead = head;

        while( holdHead != null ){

            if( Integer.parseInt(holdHead.process.id[1]) == id ){

                return holdHead.process;
            }

            holdHead = holdHead.next;

        }

        return null;
    }



    boolean empty(){

        return this.head == null;
    }


 /*   public Node front(){

        return this.head;
    }*/

    public PCB front(){

        return this.head.process;
    }



    @Override
    public String toString() {

        Node holdHead = head;
        StringBuilder processString = new StringBuilder();

        while( holdHead != null ){

//            System.out.print(holdHead.process + " ");

            processString.append(holdHead.process);
            holdHead = holdHead.next;

        }

        return processString.toString();
    }

    void print(){

        Node holdHead = head;

        System.out.print("[ ");
        while( holdHead != null ){

            if( holdHead.next == null ){
                System.out.print(holdHead.process.id[0] + " ");

            }else{
                System.out.print(holdHead.process.id[0] + " | ");

            }

            holdHead = holdHead.next;

        }

        System.out.println("]");
    }

    String string(){

        Node holdHead = head;

        String queue = "[ ";

        while( holdHead != null ){

            if( holdHead.next == null ){
                queue += holdHead.process.id[0] + " ";

            }else{
                queue += holdHead.process.id[0] + " | ";

            }

            holdHead = holdHead.next;

        }

        queue += "]";

        return queue;

    }

}

