import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Attendant implements Runnable {
	
	public boolean verificarAcerto(int [] tabuleiro, int linha, int coluna) {
		int casa = 5*linha;
		casa+=coluna;
		if(tabuleiro[casa]==2) {
			tabuleiro[casa]=3;
			return true;
		}else if(tabuleiro[casa]!=3){
			tabuleiro[casa]=1;
			return false;
		}
		return false;
	}
	
	public void enviarTabuleiro(int [] tabuleiro, PrintStream out) {
		String linha = "1";
		for(int i=0; i<tabuleiro.length; i++) {
			if(tabuleiro[i]==3){
				linha+="\t"+"X";
			}else if(tabuleiro[i]==1) {
				linha+="\t"+"*";
			}else{
				linha+="\t"+"~";
			}
			if((i+1)%5==0) {
				out.println(linha);
				linha = ((i/5)+2)+"";
			}
		}
	}
	
	public int[] inicializarTabuleiro(int[] tabuleiro) {
		
		for(int i=0; i<tabuleiro.length; i++) {
			tabuleiro[i] = 0;
		}		
		return tabuleiro;
	}
	
	public int[] inserirNavios(int[] tabuleiro){
		
		int navio1 = (int) (Math.random() * (24-0));
		int navio2 = (int) (Math.random() * (24-0));
		
		while(navio1 == navio2) {
			navio2 = (int) (Math.random() * (24-0));
		}
		
		int navio3 = (int) (Math.random() * (24-0));
		
		while(navio3 == navio1 && navio3 == navio2) {
			navio3 = (int) (Math.random() * (24-0));
		}
		
		tabuleiro[navio1] = 2;
		tabuleiro[navio2] = 2;
		tabuleiro[navio3] = 2;
		
		return tabuleiro;
	}
	
    private Server server;

    private Socket socket;

    private BufferedReader in;
    private PrintStream out;

    private boolean started;
    private boolean running;

    private Thread thread;

    public Attendant(Server server, Socket socket) throws Exception {
        this.server = server;
        this.socket = socket;

        this.started = false;
        this.running = false;

        open();
    }
    
    public void send(String message){
        out.println(message);
    }
    
    private void open() throws Exception {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());

            started = true;
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    private void close() {
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                System.out.println("Error: "+e);
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                System.out.println("Error: "+e);
            }
        }

        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Error: "+e);
        }

        in = null;
        out = null;
        socket = null;

        started = false;
        running = false;

        thread = null;
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
        
        if(thread != null){
        thread.join();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                socket.setSoTimeout(250000000);
                String message = in.readLine();
                
                Attendant jogo = new Attendant(server, socket);
                int acertos = 0;
                int [] tabuleiro = new int[25];
                jogo.inicializarTabuleiro(tabuleiro);
                jogo.inserirNavios(tabuleiro);
                
                if(message == null){
                    break;
                }

                jogo.enviarTabuleiro(tabuleiro, out);
            	
            	int linha = Integer.parseInt(in.readLine())-1;
            	int coluna = Integer.parseInt(in.readLine())-1;
            	
            	boolean acertou = jogo.verificarAcerto(tabuleiro, linha, coluna);
            	
            	if(acertou==true) {
            		acertos++;
            		if(acertos == 3) {
                		out.println("FIM");
                		break;
                	}
            		out.println("Você Acertou!");
            		
            	}else {
            		out.println("Você errou! Tente novamente");
            	}

                if ("sair".equals(message)) {
                    break;
                }
                
                server.sendToAll(this, message);
                
            } catch(SocketTimeoutException e){
                System.out.println("Error: "+e);
                break;
            } 
            catch(Exception e){
                System.out.println("Error: "+e);
                break;
            }
        }
        System.out.println("Stopping connection...");
        
        close();
    }
}