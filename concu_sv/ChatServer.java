package concu_sv;

import concu_sv.Handlers.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author sergi
 */
public class ChatServer {
    private static final int PORT = 8080;
    
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    
    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        
        try (ServerSocket server = new ServerSocket(PORT)){
            
            System.err.println("Server active in localhost:"+ PORT);
            
            while (true) {
                System.out.println("Esperanddo conexión...");                
                Socket clientSocket = server.accept();
                System.err.println("New client IP:" + clientSocket.getInetAddress());
                
                ClientHandler client = new ClientHandler (clientSocket, clients);
                clients.add(client);
                
                pool.execute(client);
            }
            
            
        } catch (IOException ex) {
            System.getLogger(ChatServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}