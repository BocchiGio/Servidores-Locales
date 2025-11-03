package concu_sv.Handlers;

import concu_sv.Handlers.Handler;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author sergi
 */
public class ReadHandler extends Handler implements Runnable {

    public ReadHandler(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        String messageRx;

        try {
            // Bucle de lectura, se bloquea hasta recibir algo
            while ((messageRx = in.readLine()) != null) {

                System.out.println("sevidor: " + messageRx);

                if (messageRx.equalsIgnoreCase("/exit")) {
                    break;
                }
            }

        } catch (IOException ex) {
            // Salta aqui si se cierra el socket 
            System.out.println("Conexión cerrada.");

        } finally {
            // Limpiar y cerrar hilo
            dismiss();
        }
    }
}