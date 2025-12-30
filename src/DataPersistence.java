import java.io.*;
import java.util.*;

import model.Book;
import model.LibraryRecord;
import model.User;


public class DataPersistence {
	
	private static final String USER_FILE = "users.dat"; //file where user data save
    private static final String RECORD_FILE = "records.dat";//file for record data
    
    /**
     * saves the current list of registered users to a file for persistence.
     *
     * method serializes the users Map, containing all User objects
     * and writes it to the file defined by USER_FILE ("users.dat").
     * it ensures that user data is retained between server restarts.
     */
    public static void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            out.writeObject(users);
            System.out.println("Users saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the list of registered users from the persistence file.
     *
     * This method attempts to read the serialized users Map from USER_FILE ("users.dat").
     * If the file exists, it deserializes the stored user data and returns it.
     * If the file does not exist or cannot be read, it prints a message and
     * returns a new, empty HashMap to start fresh.
     */
    @SuppressWarnings("unchecked") // prevent a compiler warning about unchecked type casting
    public static Map<String, User> loadUsers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            return (Map<String, User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing user data found. Starting fresh.");
            return new HashMap<>();
        }
    }
    
    /**
     * saves the list of library records to a file for persistence
     *
     * the method serializes the List of LibraryRecord objects and writes it
     * to the file defined by RECORD_FILE ("records.dat"). It ensures that all
     * created or updated library records are retained after the server restarts
     */
    
    public static void saveRecords(List<LibraryRecord> records) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(RECORD_FILE))) {
            out.writeObject(records);
            System.out.println("Records saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static List<LibraryRecord> loadRecords() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(RECORD_FILE))) {
            return (List<LibraryRecord>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing record data found. Starting fresh.");
            return new ArrayList<>();
        }
    }

	public static List<Book> loadBooks() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void saveBooks(List<Book> books) {
		// TODO Auto-generated method stub
		
	} 
    
}
