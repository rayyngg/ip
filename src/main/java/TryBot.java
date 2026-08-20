import java.util.Scanner;

/**
 * A simple chatbot that echoes commands until the user says goodbye.
 */
public class TryBot {
    public static void main(String[] args) {
        String separator = "____________________________________________________________";
        String banner = " _____             ____        _\n"
                + "|_   _| _ __ _   _ | __ )  ___ | |_\n"
                + "  | |  | '__| | | ||  _ \\ / _ \\| __|\n"
                + "  | |  | |  | |_| || |_) | (_) | |_\n"
                + "  |_|  |_|   \\__, ||____/ \\___/ \\__|\n"
                + "              |___/\n";

        System.out.println(separator);
        System.out.print(banner);
        System.out.println("Hello! I'm TryBot.");
        System.out.println("What can I do for you?");
        System.out.println(separator);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(separator);

            if (command.equals("bye") || command.equals("BYE") || command.equals("Bye") || command.equals("bye!") || command.equals("Bye!")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(separator);
                break;
            }

            System.out.println(command);
            System.out.println(separator);
        }
    }
}
