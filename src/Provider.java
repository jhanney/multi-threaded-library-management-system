

import java.io.*;
import java.net.*;
public class Provider
{
	private ServerSocket providerSocket;
	private Socket connection = null;
	
	public Provider()
	{
		try 
		{
			this.providerSocket = new ServerSocket(2004, 10);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ServerSocket getProvider()
	{
		return providerSocket;
	}

	public static void main(String args[])
	{

		Provider p = new Provider();
		
		//2. Wait for connection
		while(true)
		{
			System.out.println("Waiting for connection");
			try 
			{
			
					p.connection = p.providerSocket.accept();
				
				ConnectionHandler temp = new ConnectionHandler(p.connection);
				temp.start();
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Create the handler Thread and send the socket....
		}
		
	}
}
			