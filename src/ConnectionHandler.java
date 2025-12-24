import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.LibraryRecord;
import model.User;

public class ConnectionHandler extends Thread {

	public static final Set<String> usedIds = new HashSet<>(); // hashset to store usedIds, using a hashset as all items
																// added must be unique
	public static final Map<String, User> users = new HashMap<>(); // String used as key, User object containing all
																	// user details as the value
	public static final List<LibraryRecord> records = new ArrayList<>(); //hold all created library records 
	
	
	private Socket connection;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	
 	boolean isAutheticated; //verify if users authenticated
 	User loggedInUser = null; 

	public ConnectionHandler(Socket s) {
		connection = s;
	}

	public void run() {

		System.out.println("Connection received from " + connection.getInetAddress().getHostName());
		// 3. get Input and Output streams

		try {
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
		} catch (IOException classnot) {

		}
		try {
			String choice; 
			int menuChoice = 0; 
			/// Insert the Server Conversation......
				displayMenu();
			
			
			do {
				if(isAutheticated) {
					authenticatedMenu();
				}
				
				choice = (String) in.readObject();
				menuChoice = Integer.parseInt(choice);
				
				switch(menuChoice) {
				case 1:
					registerUser();
					displayMenu();
				break; 
				case 2:
					loginUser();
					authenticatedMenu();
				break; 
				case 8:
					exit();
					return;
				default:
					sendMessage("Invalid choice. Please select a choice displayed on the menu");
					displayMenu();
				}
				
			}while(menuChoice != 8); 

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("There has been an error in the Library Managment System");
		}

		// 4: Closing connection
		try {
			in.close();
			out.close();
			connection.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("server>" + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	/**
	 * Handles user registration. Prompts the client for user details (Name, Student
	 * ID, Email, Password, Department, and Role), validates that the Student ID and
	 * Email are unique, and creates a new User object. Adds the new user to the
	 * server's user list if registration is successful.
	 **/

	public void registerUser() {

		try {
			sendMessage("Enter your name");// ask for name
			String name = (String) in.readObject();

			sendMessage("Enter Student ID:");// ask for id and check hashset
			String id = (String) in.readObject();
			if (usedIds.contains(id)) {
				sendMessage("An account with Student ID: " + id + " already exists. Try again");
				return;
			}

			// ask for email and check
			sendMessage("Enter Email: ");
			String email = (String) in.readObject();
			if (users.containsKey(email)) {
				sendMessage("An account with the email: " + email + " already exists. Try again");
				return;
			}

			// get password
			sendMessage("Enter Password:");
			String password = (String) in.readObject();

			// department
			sendMessage("Enter Department:");
			String department = (String) in.readObject();

			// role
			sendMessage("Enter Role (Student / Librarian):");
			String role = (String) in.readObject();

			User newUser = new User(name, id, email, password, department, role);
			users.put(email, newUser);
			usedIds.add(id);

			sendMessage("Registration has been successful!!");

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void loginUser() {
		try {
			//ask for user email, cross reference with users map
			sendMessage("Please enter your email:");
			String email = (String) in.readObject();
			if(!users.containsKey(email)) {
				sendMessage("This email has not been registered, login unsuccessful");
				return; 
			}
			

			//check the user and the matching password
			User user = users.get(email);
			String password; 
			
			//loop to ask for password until correct
			do {
				sendMessage("Please enter your password");
				password = (String) in.readObject();
				
				if(!user.getPassword().equals(password)) {
					sendMessage("Password incorrect please try again");
				}

			
			}while(!user.getPassword().equals(password));
			
			isAutheticated = true;
			loggedInUser = user; 
			
			sendMessage("Login successful! Welcome " + user.getName() );
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	//exit method used to send message to client, allowing system to see exit has been chosen 
	public void exit() {
		String exitMessage = "Goodbye and have a good day.";
		sendMessage(exitMessage);
	}
	
	public void displayMenu() {
		sendMessage("WELCOME TO THE LIBRARY MANAGEMENT SYSTEM"
				+ "\nPlease choose one of the following options"
				+ "\n1.Register"
				+ "\n2.Login"
				+ "\n8.Exit");
	}
	
	public void authenticatedMenu() {
		if(isAutheticated) {
		sendMessage("USER AUTHENTICATED"
				+ "\nPlease choose one of the following options"
				+ "\n3.Create a Library Record"
				+ "\n4.Retrieve all registered book records"
				+ "\n5.Assign a borrowing request"
				+ "\n6.View all library records assigned to you"
				+ "\n7.Update your password"
				+ "\n8.Exit");
		}else {
			   sendMessage("You must log in to access these options.");
		}
	}

}
