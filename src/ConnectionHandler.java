
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

import model.Book;
import model.LibraryRecord;
import model.User;

public class ConnectionHandler extends Thread {

	public static final Set<String> usedIds = new HashSet<>(); // hashset to store usedIds, using a hashset as all items
																// added must be unique
	public static final Map<String, User> users = DataPersistence.loadUsers(); // String used as key, User object containing all
																	// user details as the value
	public static final List<LibraryRecord> records = DataPersistence.loadRecords(); //hold all created library records 
	
	public static final List<Book> books = DataPersistence.loadBooks();

	
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
				case 5:
				    assignBorrowRequest();
				    break;
				case 6:
				    viewAssignedRecords();
				    break;
				case 7:
				    updatePassword();
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
			
			// role
			sendMessage("Select your role:\n1. Student\n2. Librarian");
			String roleChoice = (String) in.readObject();
				
			
			String role;
	        String idLabel; //to use correct wording in prompts
	        
	        if (roleChoice.trim().equals("1")) {
	            role = "Student";
	            idLabel = "Student ID";
	        } else if (roleChoice.trim().equals("2")) {
	            role = "Librarian";
	            idLabel = "Librarian ID";
	        } else {
	            sendMessage("Invalid choice. Registration cancelled.");
	            return;
	        }
			
			

			sendMessage("Enter your " + idLabel + ":");// ask for id and check hashset
			String id = (String) in.readObject();
			if (usedIds.contains(id)) {
				sendMessage("An account with Student ID: " + id + " already exists. Try again");
				return;
			}
			
			sendMessage("Enter your name");// ask for name
			String name = (String) in.readObject();

			// ask for email and check
			sendMessage("Enter Email: ");
			String email = (String) in.readObject();
			
			//validate email format
			if (!email.contains("@") || 
			    !(email.endsWith(".com") || email.endsWith(".ie") || email.endsWith(".co.uk"))) {
			    sendMessage("Invalid email format. Please use a valid email (e.g., user@example.com, user@example.ie, user@example.co.uk).");
			    return;
			}
			
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



			User newUser = new User(name, id, email, password, department, role);
			users.put(email, newUser);
			usedIds.add(id);
			
			DataPersistence.saveUsers(users);//save user


			 sendMessage("Registration successful! Welcome, " + name + " (" + role + ")");
		     System.out.println("New user registered: " + name + " (" + role + ")");

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
	        String recordId;

	        // Step 2:determine record type and status
	        if (recordChoice.trim().equals("1")) {
	            //ask for book details
	            sendMessage("Enter book title:");
	            String bookTitle = (String) in.readObject();

	            sendMessage("Enter author name:");
	            String authorName = (String) in.readObject();
	            
	            Book newBook = new Book(bookTitle, bookTitle, authorName);
	            books.add(newBook);
	            DataPersistence.saveBooks(books);
	            
	            sendMessage("Book added successfully: " + bookTitle + " by " + authorName);

	            //include book details in record type for display
	            recordType = "New Book Entry - " + bookTitle + " by " + authorName;
	            status = "Available";
	            
	            int recordNumber = records.size() + 1; //assign next number to record
		        recordId = String.format("R%03d", recordNumber); //format the id With R + a number up to 3 digits
	        } else if (recordChoice.trim().equals("2")) {
	            recordType = "Borrow Request";
	            status = "Requested";
	            
	            //ask for the book title the user wants to borrow
	            sendMessage("Enter the title of the book you want to borrow:");
	            String requestedTitle = (String) in.readObject();

	            //check if the book exists in the system
	            Book matchingBook = null;
	            for (Book book : books) {
	                if (book.title().equalsIgnoreCase(requestedTitle)) {
	                    matchingBook = book;
	                    break;
	                }
	            }

	            //handle case of missing book
	            if (matchingBook == null) {
	                sendMessage("Book not found in the system. Please register it first.");
	                authenticatedMenu();
	                return;
	            }
	            
	            //create record id
	            int recordNumber = records.size() + 1;
	            recordId = String.format("B%03d", recordNumber);
	            
	            //create and save the borrow request record
	            String date = java.time.LocalDate.now().toString();
	            String studentId = loggedInUser.getStudentId();
	            String librarianId = "";

	            LibraryRecord newRecord = new LibraryRecord(
	                recordId,
	                "Borrow Request for '" + matchingBook.title() + "'",
	                date,
	                studentId,
	                status,
	                librarianId
	            );
	            
	            records.add(newRecord);//add the record
	            DataPersistence.saveRecords(records);//save the record 

	            //contact user on completion
	            sendMessage("Borrow request created successfully!"
	                + "\nRecord ID: " + recordId
	                + "\nBook: " + matchingBook.title()
	                + "\nStatus: " + status);
	        } else {
	            sendMessage("Invalid record type. Record creation cancelled.");
	            return;
	        }


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
	        
	        DataPersistence.saveRecords(records); //save record

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
	
	/**
	 * assigns the borrowing request to a librarian and updates its status
	 * 
	 * the method allows a user to assign an existing Borrow Request record
	 * to a librarian by providing the Record ID and the Librarian ID.
	 * 
	 * Steps:
	 * 1.verify user authentication.
	 * 2.ask for the Record ID and locate the corresponding record.
	 * 3.validate that it is a Borrow Request and not already processed.
	 * 4.ask for the Librarian ID to assign to
	 * 5.update the records status to 'Borrowed' and set the librarian ID.
	 * 6.confirm success and display the updated menu
	 */
	public void assignBorrowRequest() {
	    try {
	        //verify that user is logged in
	        if (!isAutheticated) {
	            sendMessage("Please log in before assigning borrowing requests.");
	            return;
	        }

	        //ask for the record ID
	        sendMessage("Enter the Record ID of the borrowing request to assign:");
	        String requestedId = ((String) in.readObject()).trim();

	        //find the record
	        LibraryRecord recordToAssign = null;
	        for (LibraryRecord r : records) {
	            if (r.recordId().equalsIgnoreCase(requestedId)) {
	                recordToAssign = r;
	                break;
	            }
	        }

	        //check if the record exists
	        if (recordToAssign == null) {
	            sendMessage("No record found with ID: " + requestedId);
	            authenticatedMenu();
	            return;
	        }

	        //make sure it is a borrow request
	        if (!recordToAssign.recordType().equalsIgnoreCase("Borrow Request")) {
	            sendMessage("This record is not a borrowing request and cannot be assigned.");
	            authenticatedMenu();
	            return;
	        }

	        //make sure it hasnt been processed already
	        if (recordToAssign.status().equalsIgnoreCase("Borrowed")
	                || recordToAssign.status().equalsIgnoreCase("Returned")) {
	            sendMessage("This borrowing request has already been processed.");
	            authenticatedMenu();
	            return;
	        }

	        //ask for librarian ID to assign this request to
	        sendMessage("Enter the Librarian ID to assign this request to:");
	        String librarianId = ((String) in.readObject()).trim();

	        //update record with librarian ID and new status
	        String updatedStatus = "Borrowed";

	        LibraryRecord updated = new LibraryRecord(
	                recordToAssign.recordId(),
	                recordToAssign.recordType(),
	                recordToAssign.date(),
	                recordToAssign.studentId(),
	                updatedStatus,
	                librarianId
	        );

	        //replace the old record with the updated one
	        int index = records.indexOf(recordToAssign);
	        records.set(index, updated);
	        
	        DataPersistence.saveRecords(records);//save record

	        //confirm to user
	        sendMessage("Borrow request " + requestedId + " has been successfully assigned!"
	                + "\nStatus updated to: " + updatedStatus
	                + "\nAssigned Librarian ID: " + librarianId);

	        System.out.println("Record " + requestedId + " assigned to librarian " + librarianId);
	        authenticatedMenu();

	    } catch (Exception e) {
	        e.printStackTrace();
	        sendMessage("An error occurred while assigning the borrowing request. Please try again.");
	    }
	}

	/**
	 * shows all library records assigned to the currently logged-in librarian.
	 * 
	 * This method filters the list of all records and shows only those
	 * where the librarianId matches the logged-in user's student ID.
	 * 
	 * Steps:
	 * 1 verify that the user is authenticated
	 * 2 ensure the logged-in user has the 'Librarian' role
	 * 3 filter records assigned to this librarian
	 * 4. Display the matching records, or a message if none exist
	 * 5 display the authenticated menu afterward
	 */
	public void viewAssignedRecords() {
	    try {
	        //verify authentication
	        if (!isAutheticated) {
	            sendMessage("Please log in before viewing assigned records.");
	            return;
	        }

	        //ensure user is a librarian
	        if (!loggedInUser.getRole().equalsIgnoreCase("Librarian")) {
	            sendMessage("Access denied. Only librarians can view assigned records.");
	            authenticatedMenu();
	            return;
	        }

	        //find all records assigned to this librarian
	        List<LibraryRecord> assignedRecords = new ArrayList<>();
	        for (LibraryRecord record : records) {
	            if (record.librarianId().equals(loggedInUser.getStudentId())) {
	                assignedRecords.add(record);
	            }
	        }

	        //display results
	        if (assignedRecords.isEmpty()) {
	            sendMessage("No records have been assigned to you.");
	        } else {
	            sendMessage("Displaying all records assigned to you:");
	            for (LibraryRecord record : assignedRecords) {
	                String recordDetails = "\nRecord ID: " + record.recordId()
	                        + "\nType: " + record.recordType()
	                        + "\nDate: " + record.date()
	                        + "\nStatus: " + record.status()
	                        + "\nStudent ID: " + record.studentId()
	                        + "\n-----------------------------";
	                sendMessage(recordDetails);
	            }
	        }

	        //return to the authenticated menu
	        authenticatedMenu();

	    } catch (Exception e) {
	        e.printStackTrace();
	        sendMessage("An error occurred while viewing assigned records. Please try again.");
	        authenticatedMenu();
	    }
	}
	
	/**
	 * lets a logged-in user to update their password
	 * 
	 * Steps:
	 * 1 verify that the user is authenticated
	 * 2. ask for the current password and validate it
	 * 3. prompt for a new password twice for confirmation
	 * 4. if confirmed, update the password in the system
	 * 5.redisplay the authenticated menu
	 */
	public void updatePassword() {
	    try {
	        //ensure user is logged in
	        if (!isAutheticated) {
	            sendMessage("Please log in before updating your password.");
	            return;
	        }

	        //ask for current password
	        sendMessage("Enter your current password:");
	        String currentPassword = (String) in.readObject();

	        //verify current password
	        if (!loggedInUser.getPassword().equals(currentPassword)) {
	            sendMessage("Incorrect password. Password update cancelled.");
	            authenticatedMenu();
	            return;
	        }

	        //ask for new password and confirmation
	        sendMessage("Enter your new password:");
	        String newPassword = (String) in.readObject();

	        sendMessage("Confirm your new password:");
	        String confirmPassword = (String) in.readObject();

	        //compare the two new passwords
	        if (!newPassword.equals(confirmPassword)) {
	            sendMessage("Passwords do not match. Please try again.");
	            authenticatedMenu();
	            return;
	        }

	        //update password in the users map
	        loggedInUser.setPassword(newPassword);  //update using setter
	        users.put(loggedInUser.getEmail(), loggedInUser);
	        
	        DataPersistence.saveUsers(users);//save password to users

	        sendMessage("Password updated successfully!");
	        System.out.println("User " + loggedInUser.getName() + " updated their password.");

	        //return to menu
	        authenticatedMenu();

	    } catch (Exception e) {
	        e.printStackTrace();
	        sendMessage("An error occurred while updating your password. Please try again.");
	        authenticatedMenu();
	    }
	}
}
