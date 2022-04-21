package server_pack;

/**
 * Server
 * Serverklass som kan ha fler klienter kopplad till sig.
 *  
 *  @author Caroline Engqvist (caen1500)
 *  @version 1.0
 *  @since 2020-01-11
 */
import java.net.*;
import java.io.*;

public class Server {
	
	public static void main(String[] args)
	{
		int port = 10000;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		ServerSocket ss = null;
		
			try
			{
				// Skapar ServerSocket
				
				ss = new ServerSocket(port);
				System.out.println("Server started on port: " + port);
				
			}
			catch (IOException ioe)
			{
				System.out.println("Kunde inte lyssna p� port " + port);
				
				return;
			}
		
		while (true)
		{
			try
			{
				// v�ntar p� att n�gon ska ansluta
				Socket s = ss.accept();

				// Vid anslutning
				System.out.println("New client connected from: " + s.getRemoteSocketAddress());
				new ClientHandler(s).start();
			}
			catch (IOException ioe)
			{
				System.out.println("No connection with client:\n");
			}	
		}
    }
}