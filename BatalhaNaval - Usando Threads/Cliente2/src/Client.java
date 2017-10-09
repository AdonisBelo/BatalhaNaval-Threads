import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class Client implements Runnable {

    private Socket socket;

    private BufferedReader in;
    private PrintStream out;

    private boolean started;
    private boolean running;

    private Thread thread;

    public Client(String address, int port) throws Exception {
        started = false;
        running = false;

        open(address, port);
    }

    private void open(String address, int port) throws Exception {
        try {
            socket = new Socket(address, port);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());

            started = true;
        } catch (Exception e) {
            System.out.println("Error: "+e);
            close();
            throw e;
        }
    }

    private void close() {
        if (in != null) {
            try{
            in.close();
        }catch(Exception e){
            System.out.println("Error: "+e);
        }
      }
        
        if (out != null) {
            try{
            out.close();
        }catch(Exception e){
            System.out.println("Error: "+e);
        }
      }
        
        if (socket != null) {
            try{
            socket.close();
        }catch(Exception e){
            System.out.println("Error: "+e);
        }
      }
        
        in = null;
        out = null;
        socket = null;
        
        started = false;
        running = false;
        
        thread = null;
    }
    
    public void start(){
        if(!started || running){
            return;
        }
        
        running = true;
        thread = new Thread(this);
        thread.start();
    }
    
    public void stop() throws Exception{
        running = false;
        
        if(thread != null){
            thread.join();
        }
    }
    
    public boolean isRunning(){
        return running;
    }
    
    public void send(String message){
        out.println(message);
    }
    
    @Override
    public void run() {
        while(running){
            try{
                socket.setSoTimeout(250000000);
                
                String message = in.readLine();
                
                if(message==null){
                    break;
                }
                
                System.out.println("\t1 \t2 \t3 \t4 \t5");
    			System.out.println();
    			System.out.println(in.readLine());
    			System.out.println(in.readLine());
    			System.out.println(in.readLine());
    			System.out.println(in.readLine());
    			System.out.println(in.readLine());
    			
            } catch(SocketTimeoutException e){
                
            } catch(Exception e){
                System.out.println("Error: "+e);
                break;
            }
        }
        close();
    }

    public static void main(String[] args) throws Exception {
    	
        System.out.println("Starting client...");
        
        System.out.println("Starting connection with the server...");
        
        Client client = new Client("localhost", 2525);
        
        System.out.println("Connection made successfully!");
        
        client.start();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            
        	System.out.println("Digite a Linha: ");
			String linha = scanner.next();
			System.out.println("Digite a Coluna: ");
			String coluna = scanner.next();

            client.send(linha);
            client.send(coluna);

            if ("sair".equals(linha) || "sair".equals(coluna)) {
                break;
            }
        }
        
        System.out.println("Stopping client...");

        client.stop();
    }
}