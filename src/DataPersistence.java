import java.io.*;
import java.util.*;

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
}
