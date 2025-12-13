import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.User;

public class ConnectionHandler extends Thread {

	public static final Set<String> usedIds = new HashSet<>(); // hashset to store usedIds, using a hashset as all items
																// added must be unique
	public static final Map<String, User> users = new HashMap<>(); // String used as key, User object containing all
																	// user details as the value

	private Socket connection;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;

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
			sendMessage("WELCOME TO THE LIBRARY MANAGMENT SYSTEM"
					+ "\nPlease choose one of the following options"
					+ "\n1.Register"
					+ "\n2.Login"
					+ "\n8.Exit");
			
			do {
				
				choice = (String) in.readObject();
				menuChoice = Integer.parseInt(choice);
				
				switch(menuChoice) {
				case 1:
					registerUser();
				break; 
				case 2:
					loginUser();
				break; 
				case 8:
					return;
				default:
					sendMessage("Invalid choice. Please select a choice displayed on the menu");
				}
				
			}while(menuChoice != 3); 

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
				sendMessage("This email has not been registered");
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
			
			sendMessage("Login successful! Welcome " + user.getName() );
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
