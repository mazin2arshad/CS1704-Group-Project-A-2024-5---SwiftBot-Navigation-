import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import swiftbot.Button;
import swiftbot.SwiftBotAPI;
import java.text.SimpleDateFormat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class SwiftBot_Navigation {
	// List to store the history of commands executed
	private static List<String> commandHistory = new ArrayList<>();

	public static void main(String[] args) {
		WelcomePage();// Display the welcome message
		SwiftBotAPI swiftbot = getAPIInstance(); // Get API instance

		// Enable X button to exit program
		swiftbot.enableButton(Button.X, () -> {
			System.out.println("Exit button (X) pressed. Shutting down...");
			System.exit(0);// Exit the program
		});

		try (Scanner sc = new Scanner(System.in)) {
			while (true) { // Continuously ask for user input until they decide to exit
				System.out.println("Are you ready to scan your QR Code?");
				System.out.println("Please type 'y' to scan your QR Code");
				System.out.println("Or press X on your SwiftBot to exit the program");
				
				if(!sc.hasNextLine()) {// Check if user input is detected
					System.out.println("No input detected, please try scan again");
					continue;// Repeat the prompt
				}

				String answer = sc.nextLine().trim().toLowerCase();// Read the user input
				if (answer.equals("y")) {
					String qrData = scanQRcode(sc);// Scan the QR code for instructions
					if (qrData == null || qrData.isEmpty()) {
						System.out.println("No valid QR code scanned. Please try again.");
						continue; // Ask for QR code again if no valid data was found
					}
					processInput(qrData); // Process the commands from the scanned QR code

					System.out.println("Command(s) executed successfully!");  
					
				} 
				else {
					System.out.println("Invalid input. Please enter 'y' to scan qr code again");
					System.out.println("Or if you want to exit, please press X on the SwiftBot");
				}
			}
		}
	}

	private static void WelcomePage() {

		// Display a welcome page with instructions for the user
		System.out.println("*************************************************************************************************************");
		System.out.println("*************************************************************************************************************");
		System.out.println("                                      SwiftBot Navigation");
		System.out.println("*************************************************************************************************************");
		System.out.println("*************************************************************************************************************");
		System.out.println("Select a Movement Command, Retrace Command, Logfile Command : ");
		System.out.println("");
		System.out.println("For Movement commands, type in your speed(1-100) and time(1-6) splitting each command with commas,");
		System.out.println("e.g. F,92,4 (NO SPACES)");
		System.out.println("For Retrace Commands, type in the total amount of previous commands you want to repeat after and");
		System.out.println("splitting it with a comma, e.g. T,3 (NO SPACES)(WARNING - DON’T TYPE A TOTAL AMOUNT THAT EXCEEDS THE");
		System.out.println("TOTAL AMOUNT OF PREVIOUSLY EXECUTED COMMANDS)");
		System.out.println("");
		System.out.println("Movement :");
		System.out.println("Type 'F' for the SwiftBot to move Forward");
		System.out.println("Type 'B' for the SwiftBot to move Backwards");
		System.out.println("Type 'R' for the SwiftBot to move Right");
		System.out.println("Type 'L' for the SwiftBot to move Left");
		System.out.println("Retrace :");
		System.out.println("Type 'T' for SwiftBot to repeat previous movements");
		System.out.println("Logfile :");
		System.out.println("Type 'W' for the SwiftBot to create a logfile of all executed commands with total time and");
		System.out.println("current time of creation of logfile");
		System.out.println("");
		System.out.println("After you've entered you're commands, please use your SwiftBot to scan the Qr Code for movement instructions");
		System.out.println("To exit the program press the X button on your SwiftBot");

	}

	// Initialise SwiftBotAPI if not already created
	private static SwiftBotAPI API = null;
	private static SwiftBotAPI getAPIInstance() {
		if (API == null) {
			API = new SwiftBotAPI();// Create a new instance of SwiftBotAPI
		}
		return API;
	}

	// Method to scan the QR code and decode it into commands
	private static String scanQRcode(Scanner sc) {
		while (true) {	
			SwiftBotAPI API = getAPIInstance(); 
			for(int i=0;i<10;++i) // Try up to 10 times to scan a QR code
			{ 
				try 
				{ 
					BufferedImage img = API.getQRImage(); // Get the QR code image from the bot
					String decodedText = API.decodeQRImage(img); // Decode the QR code
					if(!decodedText.isEmpty()) 
					{ 
						System.out.println(decodedText); // Display the decoded text (commands)
						return decodedText; // Return the decoded QR code data
					} 
					else 
					{ 
						System.out.println("Error: No valid Qr Code Scanned!"); 
					} 
				} 
				catch(IllegalArgumentException e) 
				{ 
					e.printStackTrace(); 
				} 
			} 
			System.out.println("Sorry, unable to scan a valid QR Code, can you please type y again");
			System.out.println("Or if you want to exit the program please press X on the SwiftBot");
			
			// If no QR code is scanned, ask the user if they want to retry
			if (!sc.hasNextLine()) { 
	            System.out.println("No input detected. Press X to exit the program");
	            return null;
	        }

	        String retryAnswer = sc.nextLine().trim().toLowerCase();
	        if (!retryAnswer.equals("y")) {
	            System.out.println("Invalid input. Please type 'y' to retry.");
	        }
		}
	}

	// Method to process the input commands from the QR code
	private static void processInput(String input) {
		String[]commands = input.split(";"); // Split the input into individual commands
		if (commands.length > 10) {
			System.out.println("Error: Maximum of 10 commands allowed"); // Limit the number of commands
		}
		for (String cmd : commands) {
			if (!validateAndExecute(cmd.trim())) { // Validate and execute each command
				return; // Stop execution if an error occurs
			}
		}
	}

	// Method to validate and execute individual commands
	private static boolean validateAndExecute(String command) {
		String[] parts = command.split(","); // Split command by commas
		if (parts.length < 1) {
			System.out.println("Error: Invalid command format");
			return false; // Invalid command format
		}
		switch (parts[0].toUpperCase()) {
		case "F": case "B": case "L": case "R": // Movement commands
			if (parts.length != 3) {
				System.out.println("Error: Movement commands require 2 parameters (speed and time) ");
				return false;
			}
			try {
				int speed = Integer.parseInt(parts[1]);
				int time = Integer.parseInt(parts[2]);
				if (speed < 1 || speed > 100 || time < 1 || time > 6) {
					throw new NumberFormatException(); // Validate speed and time range
				}
				executeMovementCommand(parts[0], speed, time); // Execute the movement command
				return true; 
			} catch (NumberFormatException e) {
				System.out.println("Error: Invalid speed or time values.");
				return false;
			}
		case "T": // Retrace command
			if (parts.length != 2) {
				System.out.println("Error: Retrace command requires one integer parameter.");
				return false;
			}
			try {
				int steps = Integer.parseInt(parts[1]);
				executeRetraceCommand(steps); // Execute retrace command
				return true;
			} catch (NumberFormatException e) {
				System.out.println("Error: Invalid retrace value.");
				return false;
			}
		case "W": // Logfile command
			executeLogfileCommand(); // Execute the log file command
			return true;
		default:
			System.out.println("Error: Unknown command.");
			return false;
		}

	}

	 // Method to execute movement commands (Forward, Backward, Left, Right)
	private static void executeMovementCommand(String direction, int speed, int time) {
		SwiftBotAPI API = getAPIInstance(); 
		try {
			switch (direction) {
			case "F":
				API.move(speed, speed, time * 1000);// Move forward   
				break;
			case "B":
				API.move(-speed, -speed, time * 1000);// Move backward
				break;
			case "L":
				API.move(-speed, speed, time * 1000);// Move left
				break;
			case "R":
				API.move(speed, -speed, time * 1000);// Move right
				break;
			default:
				System.out.println("Error: Unknown direction.");
				return;
			}
			System.out.println("Executed " + direction + " at speed " + speed + " for " + time + "seconds");
			commandHistory.add(direction + "," + speed + "," + time); // Add command to history

		} catch (Exception e) {
			System.out.println("Error executing movement command: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Method to execute retracing commands
	private static void executeRetraceCommand(int steps) {
		if (steps>commandHistory.size()) {
			System.out.println("Error: Cannot retrace more steps then executed");
			return;
		}
		List<String> commandsToRetrace = new ArrayList<>(commandHistory.subList(commandHistory.size() - steps, commandHistory.size()));
		for (String cmd : commandsToRetrace) {
			validateAndExecute(cmd); // Re-execute the previous commands
		}
	}
	
	// Method to generate and save a logfile of executed commands
	private static void executeLogfileCommand() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("swiftbot_log.txt", true))) {
			String logTime = new SimpleDateFormat("HH:mm:ss").format(new Date()); // Get the current time

			writer.write("Command History: " + commandHistory + "\n");
			writer.write("Log Time: " + logTime + "\n\n"); // Write command history and time to the log file

			System.out.println("Log file saved at: " + new File("swiftbot_log.txt").getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Error writing log file: " + e.getMessage());
		}
	}
}

