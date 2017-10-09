import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {

    private ServerSocket server;

    private List<Attendant> attendants;

    private boolean started;
    private boolean running;

    private Thread thread;

    public Server(int port) throws Exception {
        attendants = new ArrayList<>();
        
        started = false;
        running = false;

        open(port);
    }

    private void open(int port) throws Exception {
        server = new ServerSocket(port);
        started = true;
    }

    private void close() {
        for (Attendant a : attendants) {
            try {
                a.stop();
            } catch (Exception e) {
                System.out.println("Error: "+e);
            }

        }

        try {
            server.close();
        } catch (Exception e) {
            System.out.println("Error: "+e);
        }

        server = null;

        started = false;
        running = false;

        thread = null;
    }

    public void sendToAll(Attendant origin, String message) throws Exception{
        Iterator<Attendant> i = attendants.iterator();

        while (i.hasNext()) {
            Attendant attendant = i.next();

            if (attendant != origin) {
                try {
                    attendant.send(message);
                } catch(Exception e){
                    attendant.stop();
                    i.remove();
                }
            }
        }
    }

    public void start() {
        if (!started || running) {
            return;
        }

        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() throws Exception {
        running = false;

        if (thread != null) {
            thread.join();
        }
    }

    @Override
    public void run() {

        System.out.println("Waiting for connection...");

        while (running) {
            try {
                server.setSoTimeout(250000000);

                Socket socket = server.accept();

                System.out.println("Made connection!");

                Attendant attendant = new Attendant(this, socket);
                attendant.start();

                attendants.add(attendant);

            } catch (SocketTimeoutException e) {

            } catch (Exception e) {
                System.out.println("Error: "+e);
                break;
            }
        }
        close();
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Starting server...");

        Server server = new Server(2525);
        server.start();

        System.out.println("Press ENTER for stop server.");
        new Scanner(System.in).nextLine();

        System.out.println("Stopping server...");
        server.stop();
        server.close();
    }

}