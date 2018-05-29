public class CC_01_Threads extends Thread{
    final static int N= 10;
    final static int T= 5000;
    private int i;

    private CC_01_Threads (int i){
        this.i= i;
    }

    public void run (){
        System.out.println("Thread nº "+ i+ " iniciado.");
        try {
            Thread.sleep(T);
        }catch(InterruptedException ie){
            System.out.println("Thread nº "+ i+ " despertado.");
        }
        System.out.println("Thread nº "+ i+ " finalizado.");
    }

    public static void main (String [] args){
        System.out.println("Inicio del programa:");
        Thread[] th_arr= new Thread [N];
        for (int i=0; i< th_arr.length; i++){
            th_arr[i]= new CC_01_Threads(i);
            th_arr[i].start();
        }
    }
}
