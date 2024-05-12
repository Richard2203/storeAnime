/**
 * 
 */
package animeStore;

import java.io.*;
import java.net.*;
import java.sql.*;

public class ServidorCRUD {
    private static final int PUERTO = 12345;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/storeanime";
    private static final String USUARIO = "postgres";
    private static final String CONTRASENA = "base_datos";
 
    public static void main(String[] args) {
        try {
            //Class.forName("org.postgresql.Driver");

            ServerSocket servidorSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor esperando conexiones...");
 
            while (true) {
                Socket socketCliente = servidorSocket.accept();
                System.out.println("Cliente conectado desde: " + socketCliente.getInetAddress().getHostName());
 
                // Manejar la solicitud del cliente en un hilo separado
                Thread hiloCliente = new Thread(new ManejadorCliente(socketCliente));
                hiloCliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    private static class ManejadorCliente implements Runnable {
        private final Socket socketCliente;
 
        public ManejadorCliente(Socket socketCliente) {
            this.socketCliente = socketCliente;
        }
 
        @Override
        public void run() {
            try (
                Connection conn = DriverManager.getConnection(DB_URL, USUARIO, CONTRASENA);
                BufferedReader entradaCliente = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                PrintWriter salidaCliente = new PrintWriter(socketCliente.getOutputStream(), true);
            ) {
                String solicitud = entradaCliente.readLine();
                String[] partes = solicitud.split(" ");
                String operacion = partes[0];
                String respuesta = "";
 
                switch (operacion) {
                    case "CREAR":
                        // Ejemplo: CREAR nombre_producto precio descripcion existencias direccion_root
                        if (partes.length == 6) {
                            String nombre = partes[1];
                            double precio = Double.parseDouble(partes[2]);
                            String descripcion = partes[3];
                            int existencias = Integer.parseInt(partes[4]);
                            String direccionRoot = partes[5];
                            respuesta = crearProducto(conn, nombre, precio, descripcion, existencias, direccionRoot);
                        } else {
                            respuesta = "Formato incorrecto para crear un producto.";
                        }
                        break;
                    case "LEER":
                        // Ejemplo: LEER id_producto
                        if (partes.length == 2) {
                            int idProducto = Integer.parseInt(partes[1]);
                            respuesta = leerProducto(conn, idProducto);
                        } else {
                            respuesta = "Formato incorrecto para leer un producto.";
                        }
                        break;
                    case "ACTUALIZAR":
                        // Ejemplo: ACTUALIZAR id_producto nombre nuevo_nombre
                        if (partes.length == 3) {
                            int idProducto = Integer.parseInt(partes[1]);
                            String nuevoNombre = partes[2];
                            respuesta = actualizarProducto(conn, idProducto, nuevoNombre);
                        } else {
                            respuesta = "Formato incorrecto para actualizar un producto.";
                        }
                        break;
                    case "ELIMINAR":
                        // Ejemplo: ELIMINAR id_producto
                        if (partes.length == 2) {
                            int idProducto = Integer.parseInt(partes[1]);
                            respuesta = eliminarProducto(conn, idProducto);
                        } else {
                            respuesta = "Formato incorrecto para eliminar un producto.";
                        }
                        break;
                    default:
                        respuesta = "Operación no válida.";
                }
 
                salidaCliente.println(respuesta);
 
                // Cierra la conexión con el cliente
                socketCliente.close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
 
        private String crearProducto(Connection conn, String nombre, double precio, String descripcion, int existencias, String direccionRoot) throws SQLException {
            String consulta = "INSERT INTO productos (nombre, precio, descripcion, existencias, direccion_root) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(consulta)) {
                pstmt.setString(1, nombre);
                pstmt.setDouble(2, precio);
                pstmt.setString(3, descripcion);
                pstmt.setInt(4, existencias);
                pstmt.setString(5, direccionRoot);
                int filasAfectadas = pstmt.executeUpdate();
                if (filasAfectadas > 0) {
                    return "Producto creado exitosamente.";
                } else {
                    return "Error al crear el producto.";
                }
            }
        }
 
        private String leerProducto(Connection conn, int idProducto) throws SQLException {
            String consulta = "SELECT * FROM productos WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(consulta)) {
                pstmt.setInt(1, idProducto);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String nombre = rs.getString("nombre");
                        double precio = rs.getDouble("precio");
                        String descripcion = rs.getString("descripcion");
                        int existencias = rs.getInt("existencias");
                        String direccionRoot = rs.getString("direccion_root");
                        return String.format("Nombre: %s, Precio: %.2f, Descripción: %s, Existencias: %d, Dirección Root: %s",
                                nombre, precio, descripcion, existencias, direccionRoot);
                    } else {
                        return "Producto no encontrado.";
                    }
                }
            }
        }
 
        private String actualizarProducto(Connection conn, int idProducto, String nuevoNombre) throws SQLException {
            String consulta = "UPDATE productos SET nombre = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(consulta)) {
                pstmt.setString(1, nuevoNombre);
                pstmt.setInt(2, idProducto);
                int filasAfectadas = pstmt.executeUpdate();
                if (filasAfectadas > 0) {
                    return "Producto actualizado exitosamente.";
                } else {
                    return "Error al actualizar el producto.";
                }
            }
        }
 
        private String eliminarProducto(Connection conn, int idProducto) throws SQLException {
            String consulta = "DELETE FROM productos WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(consulta)) {
                pstmt.setInt(1, idProducto);
                int filasAfectadas = pstmt.executeUpdate();
                if (filasAfectadas > 0) {
                    return "Producto eliminado exitosamente.";
                } else {
                    return "Error al eliminar el producto.";
                }
            }
        }
    }
}