import java.util.Scanner;

public class testMain {
	GPIO gpio;
	static Scanner scan;
	static String command;
	
	public static void main(String[] args) {
		
		System.out.println("Laserlink cable test");
		System.out.println(
				"\nReceived messages will be printed automatically. To send information type \"send your message\". To exit the program type \"exit\"");
		System.out.println(args[0]);
		int waitTime = Integer.parseInt(args[0]);
		GPIO gpio = new GPIO(waitTime);
		scan = new Scanner(System.in);
		while (true) {
			System.out.println("Type commands here:");
			command = scan.nextLine();
			if (command.contains("send")) {
				System.out.println("Will now send this text: " + command.substring(command.indexOf("send ") + 5));
				gpio.sendByteArray(command.substring(command.indexOf("send ") + 5).getBytes());
			}
			if (command.contains("exit") && !command.contains("send")) {
				System.out.println("Software is shutting down...");
				gpio.shutdown();
				System.exit(0);
			}
		}
	}
}