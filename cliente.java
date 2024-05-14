import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        final String SERVER_IP = "localhost";
        final int SERVER_PORT = 12345;

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            // Recibir catálogo del servidor
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Catalog catalog = (Catalog) in.readObject();

            // Mostrar catálogo al cliente
            catalog.display();

            // Interacción con el cliente
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\nIngrese el ID del producto a comprar (o 'exit' para salir):");
                String input = reader.readLine();

                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                int productId = Integer.parseInt(input);
                Product product = catalog.getProductById(productId);

                if (product != null && product.getStock() > 0) {
                    System.out.println("Producto seleccionado: " + product.getName());
                    System.out.println("Precio unitario: $" + product.getPrice());
                    System.out.println("¿Desea comprar este producto? (s/n)");

                    String choice = reader.readLine();
                    if (choice.equalsIgnoreCase("s")) {
                        // Facturar la compra
                        double totalPrice = product.getPrice();
                        generateInvoice(productId, product, totalPrice);
                        System.out.println("Factura generada con éxito. Se ha guardado como 'invoice.pdf'");

                        // Actualizar el catálogo
                        catalog.buyProduct(productId);

                        // Enviar catálogo actualizado al servidor
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(catalog);

                        System.out.println("Catálogo actualizado enviado al servidor.");

                        // Mostrar catálogo actualizado
                        System.out.println("\nCatálogo actualizado:");
                        catalog.display();
                    }
                } else {
                    System.out.println("Producto no disponible o no encontrado.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Método para generar la factura en PDF
    private static void generateInvoice(int productId, Product product, double totalPrice) {
        try (PrintWriter writer = new PrintWriter("invoice.pdf")) {
            writer.println("Factura");
            writer.println("ID del Producto: " + productId);
            writer.println("Nombre del Producto: " + product.getName());
            writer.println("Precio Unitario: $" + product.getPrice());
            writer.println("Cantidad: 1");
            writer.println("Total: $" + totalPrice);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
