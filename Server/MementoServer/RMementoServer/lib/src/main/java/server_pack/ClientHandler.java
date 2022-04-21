package server_pack;

/**
 * ClientHandler
 * �rver fr�n Thread. Har hand om kommunikation mellan 
 * klient och server. 
 *  
 *  @author Caroline Engqvist (caen1500)
 *  @version 1.0
 *  @since 2020-01-11
 */
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.io.*;

public class ClientHandler extends Thread{

	private Socket socket;
	private final String SERVER_DIR = new String ("\\xml");

	// Konstruktor
	public ClientHandler(Socket s)
	{
		socket = s;
	}
	public void run()
	{
		// Deklarerar instr�m och utstr�m
		DataInputStream in = null;
		DataOutputStream out = null;
	
		try
		{
			// Skapar buffrade in- och utstr�mmar f�r kommunikation
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		}
		catch (IOException ioe)
		{
			System.out.println("Failed to create streams");
			return;
		}
		try
		{
			// Tar emot val fr�n klienten
			String command = in.readUTF();
			System.out.println("Command '"+ command+ "' receved from " + socket.getRemoteSocketAddress());
			
			// Registration command
			if (command.substring(0, 2).equals("REG")) {	
				String number = command.substring(3);
				String code = getRandomCode();
				boolean registered;
				registered = sendCode(code,number);
				if(!registered) {
					out.writeUTF("ERR");	// Error sending SMS
					out.flush();
				} else if(registered) {
					if(in.readUTF().equals(code)) {
						out.writeUTF("REG"+number);	// If user entered the code correctly
						out.flush();
					} else {
						out.writeUTF("COD"); 	// If user failed to enter the right code
						out.flush();
					}
				}
			} else if(command.substring(0, 2).equals("INV")) { // add others to group
				// go through registerd users 
				// send a push notis to that user
				// accept or recline
				// some notification to user
			} else if(command.substring(0, 2).equals("SEN")) {
				// look up group members
				// store content in database
				// send information forward to group members
			}
			
			
			try
			{
				// St�nger ner streams och socket
				in.close();
				out.close();
				socket.close();
				System.out.println("Client from "+ socket.getRemoteSocketAddress()+" has disconnected");
			}
			catch(IOException ioe)
			{
				System.out.println("Could not disconnect.");
			}
		}
	
		catch (IOException ioe)
		{
			System.out.println("No valid command received");
			//return;
		}
	}
	public boolean sendCode(String code, String phoneNumber) {
		SMSVerificationClass smsVerificationClass = new SMSVerificationClass();
		return smsVerificationClass.sendCode(code, phoneNumber);		
	}
	
	public static String getRandomCode() {
		Random random = new Random();
		int code = random.nextInt(999999);
		
		return String.format("%06d", code);
	}
	
	
}

	


