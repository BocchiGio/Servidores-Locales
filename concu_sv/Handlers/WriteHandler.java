package concu_sv.Handlers;

import concu_sv.Handlers.Handler;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import concu_sv.Cliente;

public class WriteHandler extends Handler implements Runnable {

    // Scanner compartido
    private final Scanner messageScanner;

    // Recibir el scanner de Cliente
    public WriteHandler(Socket socket, Scanner scanner) {
        super(socket);
        this.messageScanner = scanner;
    }

    @Override
    public void run() {
        String messageTx;

        // Asegurar que se cierre la conexion
        try {
            while (out != null && !socket.isClosed()) {

                messageTx = this.messageScanner.nextLine();

                if (messageTx.equalsIgnoreCase("/reconnect")) {
                    System.out.println("...reconectando...");
                    // Avisar al main que queremos reconectar
                    Cliente.setWantsToReconnect(true);
                    // Avisar al servidor que se va
                    out.print("/exit\r\n");
                    out.flush();
                    break;

                // Interceptar comando de salida
                } else if (messageTx.equalsIgnoreCase("/exit")) {
                    // Avisar al main que no queremos reconectar
                    Cliente.setWantsToReconnect(false);
                    // Avisar al servidor
                    out.print("/exit\r\n");
                    out.flush();
                    break;

                } else {
                    // Mensaje normal
                    out.print(messageTx + "\r\n");
                    System.out.println("me: " + messageTx);
                    out.flush();
                }
            }
        } catch (Exception e) {
            // Error si se cae el servidor
            System.err.println("Conexión perdida: " + e.getMessage());
            Cliente.setWantsToReconnect(false);
        } finally {
            System.out.println("Cerrando WriteHandler...");
            // Cierra el socket y detiene ambos hilos
            dismiss();
        }
    }
}