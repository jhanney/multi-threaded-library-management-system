import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionHandler extends Thread {
	
	private Socket connection;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	public ConnectionHandler(Socket s)
	{
		connection = s;
	}
	
	public void run()
	{
	
		System.out.println("Connection received from " + connection.getInetAddress().getHostName());
		//3. get Input and Output streams
		
		try
		{
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
		}
		catch(IOException classnot)
		{
			
		}
		
		///Insert the Server Conversation......
	
		//4: Closing connection
		try
		{
			in.close();
			out.close();
			connection.close();
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
		}
	}

	void sendMessage(String msg)
	{
	try{
		out.writeObject(msg);
		out.flush();
		System.out.println("server>" + msg);
	}
	catch(IOException ioException){
		ioException.printStackTrace();
	}
	}

}
