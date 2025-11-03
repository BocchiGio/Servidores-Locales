package concu_sv.Handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Handler {

    protected Socket socket;
    protected BufferedReader in;
    protected PrintWriter out;

    public Handler(Socket socket, String IP) {

    }

    public Handler(Socket socket) {
        this.socket = socket;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException ex) {
            System.getLogger(Handler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }

    public void dismiss() {
        try {
            // Asegurarse de cerrar el socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        } catch (IOException ex) {
            System.getLogger(Handler.class.getName()).log(System.Logger.Level.ERROR, "Error al cerrar socket", ex);
        }
    }
}