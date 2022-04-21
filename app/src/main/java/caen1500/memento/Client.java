package caen1500.memento;


import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class Client implements Runnable {

    private static Client INSTANCE = null;
    public static String DEFAULT_ADDRESS = "10.0.2.2";
    public static int DEFAULT_PORT = 10000;
    private String address;
    private int port;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private Boolean connected, registered;
    private String user, userPath;

    // Konstruktor
    private Client() {
        address = DEFAULT_ADDRESS;
        port = DEFAULT_PORT;
    }

    public static Client getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Client();
        }
        return(INSTANCE);
    }

    public void setUserPath(String userPath) {
        this.userPath = userPath;
    }


    // Metod f�r att sk�ta anslutningar av str�mmar och socket
    public Boolean connect() {
        if (connected == null || !connected) {
            try {
                socket = new Socket(address, port);
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                if(getUser()) {
                    out.writeUTF(user);
                    out.flush();
                }
                connected = true;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        else
            return false;
    }

    public void disconnect() {
        try {
            in.close();
            out.close();
            socket.close();
            connected = false;
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
    }

    public boolean register(String phoneNumber) {
        try {
            out.writeUTF(phoneNumber);
            out.flush();
            registered = in.readBoolean();
            if(registered) {
                setUser(in.readUTF());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return registered;
        }
        return registered;
    }

    private void setUser(String registeredUser) {
        File file = new File(userPath+"/user.txt");
        try {
            file.createNewFile();
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(registeredUser);
                fileWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendObject(JSONObject jsonObject) {
        System.out.println(jsonObject.toString());
        if(jsonObject != null) {
            try {
                out.writeBytes(jsonObject.toString());
                out.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    @Override
    public void run() {
        connect();
    }

    public boolean getUser() {
        try (Stream<String> stream = Files.lines(Paths.get(userPath+"/user.txt"))) {
            user = stream.toString();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean inviteUser(String phoneNumber, String deceasedInfo) {
        try {
            out.writeUTF("INV"+phoneNumber+"/"+deceasedInfo);

            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
