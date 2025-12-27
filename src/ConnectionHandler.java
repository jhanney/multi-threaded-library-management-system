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
//				if(isAutheticated) {
//					authenticatedMenu();
//				}
				
				choice = (String) in.readObject();
				menuChoice = Integer.parseInt(choice);
				
				switch(menuChoice) {
				case 1:
					registerUser();
					displayMenu();
				break; 
				case 2:
					loginUser();
				break; 
				case 3:
					createLibraryRecord();
				break;
				case 4:
				    retrieveAllRecords();
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
				displayMenu();
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
			
			authenticatedMenu();
			
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
	
	public void createLibraryRecord() {
		try {
	        //is authenticated before allowing record creation
	        if (!isAutheticated) {
	            sendMessage("Please log in to create a library record.");
	            return;
	        }

	        // Step 1:ask for record type
	        sendMessage("Enter record type:"
	                + "\n1. New Book Entry"
	                + "\n2. Borrow Request");
	        String recordChoice = (String) in.readObject();

	        String recordType;
	        String status;

	        // Step 2:determine record type and status
	        if (recordChoice.trim().equals("1")) {
	            recordType = "New Book Entry";
	            status = "Available";
	        } else if (recordChoice.trim().equals("2")) {
	            recordType = "Borrow Request";
	            status = "Requested";
	        } else {
	            sendMessage("Invalid record type. Record creation cancelled.");
	            return;
	        }

	        // Step 3:generate unique record ID
	        int recordNumber = records.size() + 1; //assign next number to record
	        String recordId = String.format("R%03d", recordNumber); //format the id With R + a number up to 3 digits

	        // Step 4:gather other details
	        String date = java.time.LocalDate.now().toString();
	        String studentId = loggedInUser.getStudentId();
	        String librarianId = "";

	        // Step 5:create and store record
	        LibraryRecord newRecord = new LibraryRecord(
	                recordId,
	                recordType,
	                date,
	                studentId,
	                status,
	                librarianId);

	        records.add(newRecord);//add the new record

	        // Step 6:confirm creation
	        sendMessage("Record created successfully!"
	                + "\nRecord ID: " + recordId
	                + "\nType: " + recordType
	                + "\nStatus: " + status);

	        System.out.println("New record created: " + newRecord);
	        
	        authenticatedMenu();

	    } catch (Exception e) {
	        e.printStackTrace();
	        sendMessage("An error occurred while creating the record. Please try again.");
	    }
	}
	/**
	 * displays all library records currently stored in the system.
	 * 
	 * This method allows authenticated users to view every record that has been 
	 * created by all clients connected to the server. Each record includes 
	 * details such as ID, type, date, status, student ID, and assigned librarian.
	 * 
	 * Step:
	 * 1.verify that the user is authenticated.
	 * 2.If no records exist, notify the client.
	 * 3.If records exist, loop through the list and send each record’s details.
	 * 4.show the authenticated menu again when finished.
	 */
	public void retrieveAllRecords() {
	    try {
	        //verify user authentication
	        if (!isAutheticated) {
	            sendMessage("Please log in to view library records.");
	            return;
	        }

	        //check if there are any records stored in the system
	        if (records.isEmpty()) {
	            sendMessage("No library records found.");
	            authenticatedMenu();
	            return;
	        }

	        //header message before listing all records
	        sendMessage("Displaying All Registered Library Records:");

	        //lLoop through each record and display its details
	        for (LibraryRecord record : records) {
	            String recordDetails = "\nRecord ID: " + record.recordId()
	                    + "\nType: " + record.recordType()
	                    + "\nDate: " + record.date()
	                    + "\nStatus: " + record.status()
	                    + "\nStudent ID: " + record.studentId()
	                    + "\nAssigned Librarian: "
	                    + (record.librarianId().isEmpty() ? "None" : record.librarianId())
	                    + "\n-----------------------------";

	            //send each record’s details to the client
	            sendMessage(recordDetails);
	        }
	     
	        //show the authenticated menu again after listing records
	        authenticatedMenu();

	    } catch (Exception e) {
	        
	        e.printStackTrace();
	        sendMessage("An error occurred while retrieving records. Please try again.");
	    }
	}


}
