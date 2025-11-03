package concu_sv.Handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<ClientHandler> clients;
    private BufferedReader in;
    private PrintWriter out;

    String username = "";

    public ClientHandler(Socket clientSocket, Set<ClientHandler> clients) {
        this.socket = clientSocket;
        this.clients = clients;
    }

    //Enviar mensaje a los que esten conectados menos a el mismo.
    private int notifyAll(String message, ClientHandler excludeClient) {
        int count = 0;
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != excludeClient) {
                    client.out.println(message);
                    client.out.flush();
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void run() {

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Pide el nombre de usuario
            out.println("Introduce tu nombre de usuario: ");
            out.flush();

            // Obtener nombre de usuario
            this.username = in.readLine();
            if (this.username == null || this.username.isBlank()) {
                this.username = "user-" + socket.getPort();
            }

            System.err.println(username + " se ha unido al chat");
            notifyAll("SERVER: " + username + " se ha unido.", this); //Notificar a todos

            out.println("Bienvenido " + username);
            out.flush();

            out.println("Escribe /help para ver los comandos.");
            out.flush();

            // Comandos
            String inputMessage;
            while ((inputMessage = in.readLine()) != null) {

                if (inputMessage.equalsIgnoreCase("/exit")) {
                    break; // Salir del bucle si el cliente se desconecta
                }

                if (inputMessage.equalsIgnoreCase("/help")) {
                    showHelp();

                } else if (inputMessage.startsWith("/change-userName ")) {
                    String[] parts = inputMessage.split(" ", 2);
                    if (parts.length == 2 && !parts[1].isEmpty()) {
                        ChangeUserName(parts[1]);
                    } else {
                        out.println("Uso: /change-userName [Nombre]");
                        out.flush();
                    }

                } else if (inputMessage.startsWith("/send-msg ")) {
                    String[] parts = inputMessage.split(" ", 3);
                    if (parts.length == 3 && !parts[1].isEmpty() && !parts[2].isEmpty()) {
                        privateMessage(parts[1], parts[2]);
                    } else {
                        out.println("Uso: /send-msg [usuario] [mensaje]");
                        out.flush();
                    }

                } else if (inputMessage.startsWith("/global-msg")) {
                    String[] parts = inputMessage.split(" ", 2);
                    if (parts.length == 2 && !parts[1].isEmpty()) {
                        globalMessage(parts[1]);
                    } else {
                        out.println("Uso: /global-msg [mensaje]");
                        out.flush();
                    }

                } else {
                    // Comando no valido
                    out.println("Comando no reconocido. Escribe /help para ver la lista.");
                    out.flush();
                }
            }

        } catch (IOException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, "Cliente desconectado: " + username, ex);
        } finally {
            //Remover cliente y notificar 
            synchronized (clients) {
                clients.remove(this);
            }
            System.err.println(username + " se ha desconectado.");
            notifyAll("SERVER: " + username + " ha salido del chat.", this);
            try {
                if (socket != null) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, "Error al cerrar socket", ex);
            }
        }
    }

    // Mostrar comandos
    private void showHelp() {
        out.println("--- Lista de Comandos ---");
        out.println(" * /global-msg [mensaje]: Envia un mensaje global.");
        out.println(" * /send-msg [usuario] [mensaje]: Envia un mensaje privado.");
        out.println(" * /change-userName [nuevoNombre]: Cambia tu nombre.");
        out.println(" * /reconnect: Vuelve a la pantalla de conexión.");
        out.println(" * /exit: Cierra el cliente.");
        out.println("-------------------------");
        out.flush(); 
    }

    //Cambiar nombre y notificar 
    public void ChangeUserName(String newName) {
        String oldName = this.username;
        this.username = newName;
        out.println("Tu nuevo nombre es: " + newName);
        out.flush();
        notifyAll(oldName + " ahora es " + newName, this);
    }

    public void privateMessage(String targetUsername, String message) {

        boolean userFound = false;

        synchronized (clients) {
            for (ClientHandler client : clients) {
                // Buscar al usuario ignorando mayusculas y minusculas
                if (client.username.equalsIgnoreCase(targetUsername)) {

                    client.out.println("[" + this.username + " -> tu]: " + message);
                    client.out.flush();
                    out.println("Mensaje enviado a " + targetUsername);
                    out.flush();
                    userFound = true;
                    break;
                }
            }
        }

        //Mandar una alerta si el usuario no esta en linea o no existe
        if (!userFound) {
            out.println("Usuario '" + targetUsername + "' no encontrado o no está en línea.");
            out.flush();
        }
    }

    public void globalMessage(String message) {
        String formattedMessage = "[" + username + " -> todos]: " + message;

        //  Enviar a todos menos a quien envia
        int count = notifyAll(formattedMessage, this);

        //Notificar no. de usuarios que recibieron el mensaje. 
        out.println("Mensaje global enviado a " + count + " usuarios.");
        out.flush();
    }
}