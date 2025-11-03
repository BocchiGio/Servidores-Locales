package concu_sv;

import concu_sv.Handlers.WriteHandler;
import concu_sv.Handlers.ReadHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    public static Socket conection = null;
    private static boolean reconnect = false;

    // Metodo para que WriteHandler avise al main
    public static void setWantsToReconnect(boolean status) {
        reconnect = status;
    }

    public static void main(String[] args) throws IOException {
        
        // Scanner unico para toda la app
        Scanner scanner = new Scanner(System.in);

        while (true) {
            conection = null;
            // Reiniciar bandera
            reconnect = false;

            System.out.println("Conectando...");
            System.out.println("Conectate usando: start-conection [IP/hostname] [PORT]");
            System.out.println("Ejemplo: start-conection localhost 8080");

            // Esperar el comando de conexión
            while (conection == null) {
                System.out.print("Conectese: ");
                String commands = scanner.nextLine(); // Usar el scanner unico
                String[] parts = commands.trim().split("\\s+");

                if (parts.length == 3 && parts[0].equalsIgnoreCase("start-conection")) {
                    try {
                        String address = parts[1];
                        int port = Integer.parseInt(parts[2]);

                        StartConection(port, address);

                        if (conection == null) {
                            System.err.println("No se pudo conectar. Revise la IP/Puerto e intenta de nuevo.");
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Puerto inválido. Debe ser un número.");
                    }
                } else if (commands.equalsIgnoreCase("/exit")) {
                    System.out.println("Saliendo...");
                    scanner.close(); 
                    return;
                } else {
                    System.err.println("Comando incorrecto. Uso: start-conection [IP] [PORT]");
                }
            }

            System.out.println("Conexión establecida");
            
            // Pasar el scanner unico al WriteHandler
            WriteHandler writer = new WriteHandler(conection, scanner);
            ReadHandler reader = new ReadHandler(conection);

            Thread writeThread = new Thread(writer);
            Thread readThread = new Thread(reader);

            writeThread.start();
            readThread.start();

            // Esperar a que los hilos de red mueran
            try {
                writeThread.join();
                readThread.join();
            } catch (InterruptedException ex) {
                System.err.println("Hilos interrumpidos.");
            }

            // Decidir si reconectar o salir
            if (reconnect) {
                System.out.println("\nReiniciando conexión...");
            } else {
                System.out.println("\nDesconectado");
                break; // Salir del bucle principal
            }
        } 
        
        scanner.close(); 
    }

    public static void StartConection(int port, String address) {
        try {
            conection = new Socket(address, port);
        } catch (IOException ex) {
            System.err.println("Error al conectar: " + ex.getMessage());
        }
    }
}