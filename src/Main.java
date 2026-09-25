import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Database.init();
        Register_login auth = new Register_login();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Register  2. Login  3. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> {
                    System.out.print("Username: ");
                    String u = sc.nextLine();
                    System.out.print("Password: ");
                    String p = sc.nextLine();
                    System.out.println(auth.register(u, p));
                }
                case "2" -> {
                    System.out.print("Username: ");
                    String u = sc.nextLine();
                    System.out.print("Password: ");
                    String p = sc.nextLine();
                    System.out.println(auth.login(u, p)
                            ? "Login successful!"
                            : "Invalid username or password.");
                }
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}