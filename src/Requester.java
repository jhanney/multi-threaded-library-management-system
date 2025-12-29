

import java.io.*;
import java.net.*;
import java.util.Scanner;

import model.User;
public class Requester{
	Socket requestSocket;
	ObjectOutputStream out;
 	ObjectInputStream in;
 	String message;
 	Scanner input;
	Requester(){
		
		input = new Scanner(System.in);
	}
	void run()
	{
		try{
			//1. creating a socket to connect to the server
			
			requestSocket = new Socket("127.0.0.1", 2004);
			System.out.println("Connected to localhost in port 2004");
			System.out.println("Test Commit");
			//2. get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			
			///Client Conversation......
			while(true) {
				try {
					// Read message from server
					message = (String) in.readObject();
					System.out.println("Server> " + message);
					
					//continue, to keep messaging loop in sync
					if(message.contains("successful") || message.contains("unsuccessful") || message.contains("Try again")|| message.contains("Invalid")|| message.contains("Displaying")|| message.contains("Record ID: ")|| message.contains("No record found with ID: ")|| message.contains("No record found with ID: ")
							|| message.contains("This record is not a borrowing request and cannot be assigned.")|| message.contains("This borrowing request has already been processed.")|| message.contains("Access denied. Only librarians can view assigned records.")|| message.contains("No records have been assigned to you.")|| message.contains("Incorrect password. Password update cancelled.")|| message.contains("Invalid email format. Please use a valid email ")) {
						continue; 
					}
					
					//check if server is saying goodbye
					if (message.contains("Goodbye")|| message.contains("unsuccessful")) {
						break;
					}
					
					System.out.print("You> ");
					message = input.nextLine();
					sendMessage(message);//send message to the server 
					
					

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//4: Closing connection
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("client>" + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	public static void main(String args[])
	{
		Requester client = new Requester();
		client.run();
	}
}