package animeStore;

import java.io.*;
import java.net.*;

public class ClienteCRUD {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        try (
            Socket socketCliente = new Socket(HOST, PUERTO);
            BufferedReader entradaServidor = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salidaServidor = new PrintWriter(socketCliente.getOutputStream(), true);
            BufferedReader entradaTeclado = new BufferedReader(new InputStreamReader(System.in));
        ) {
            System.out.println("Cliente conectado al servidor en " + HOST + ":" + PUERTO);

            while (true) {
                // Leer la solicitud del usuario desde la consola
                System.out.print("Ingrese su solicitud (CREAR, LEER, ACTUALIZAR, ELIMINAR): ");
                String solicitud = entradaTeclado.readLine();

                // Enviar la solicitud al servidor
                salidaServidor.println(solicitud);

                // Leer la respuesta del servidor
                String respuestaServidor = entradaServidor.readLine();
                System.out.println("Respuesta del servidor: " + respuestaServidor);

                // Preguntar al usuario si desea realizar otra operación
                System.out.print("¿Desea realizar otra operación? (s/n): ");
                String opcion = entradaTeclado.readLine();
                if (!opcion.equalsIgnoreCase("s")) {
                    break; // Salir del bucle si el usuario no desea realizar más operaciones
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
