package caen1500.memento;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Client implements Runnable {

    private static Client INSTANCE;
    public static String DEFAULT_ADDRESS = "10.0.2.2";
    public static int DEFAULT_PORT = 10000;
    private String phoneNumber;
    private JsonHelperClass jsonHelperClass;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private static String user, userPath;
    private Context context;

    // Konstruktor
    private Client() {
        jsonHelperClass = new JsonHelperClass();
    }

    public static Client getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Client();
        }
        return(INSTANCE);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setUserPath(String userPath) {
        this.userPath = userPath;
        System.out.println(this.userPath);
    }


    // Metod f�r att sk�ta anslutningar av str�mmar och socket
    public Boolean connect() {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            try {
                socket = new Socket(DEFAULT_ADDRESS, DEFAULT_PORT);
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));


                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void disconnect() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean register(String phoneNumber) {
        this.phoneNumber = phoneNumber;
       try {
            out.writeUTF("REG");
            out.writeUTF(phoneNumber);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verify(String code) {
        String tmp;
        try {
            out.writeUTF(code);
            out.flush();
            tmp = in.readUTF();
            if(tmp.contains("REG")) {
                phoneNumber = tmp.substring(3);
                setUser();
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setUser() {
        File userFile = new File(userPath+"/user.txt");
        File invites = new File(userPath+"/invites.json");
        File lastUpdate = new File(userPath+"/lastUpdate.txt");
        Date date = new Date();
        try {
            userFile.createNewFile();
            invites.createNewFile();
            lastUpdate.createNewFile();
            try {
                FileWriter fileWriter = new FileWriter(userFile);
                fileWriter.write(phoneNumber);
                fileWriter.flush();
                fileWriter = new FileWriter(lastUpdate);
                fileWriter.write(String.valueOf(new Timestamp(date.getTime())));
                fileWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean sendObject(int groupId, String dataType, String wallJsonString, byte[] media, String category) throws IOException {
        getUser();
        try {
            out.writeUTF("SEN");
            out.writeUTF(user);
            out.writeInt(groupId);
            out.writeUTF(dataType);

            if (dataType.equals("wall")) {
                out.writeUTF(wallJsonString);
            } else {
                out.writeInt(media.length);
                out.write(media);
                out.writeUTF(category);
            }
                out.flush();
                return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {

    }

    public String getUser() {
        try (Stream<String> stream = Files.lines(Paths.get(userPath+"/user.txt"))) {
            user = stream.collect(Collectors.joining());
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean inviteUser(String phoneNumber, String deceasedInfoPath, boolean adminRights) throws JSONException, IOException {
        this.connect();
        JSONObject deceasedJson = jsonHelperClass.toJsonObject(deceasedInfoPath);
        try {
            out.writeUTF("INV");
            out.writeUTF(user);
            out.writeUTF(phoneNumber);
            out.writeInt(deceasedJson.getInt("groupId"));
            out.writeBoolean(adminRights);
            if(deceasedJson.getInt("groupId") == 0) {
                out.writeUTF(deceasedJson.getString("name"));
                out.writeUTF(deceasedJson.getString("bornOnDate"));
                out.writeUTF(deceasedJson.getString("deceasedOnDate"));
            }

            out.flush();
                int group= in.readInt();
                deceasedJson.put("groupId", group);
                jsonHelperClass.saveObjectToFile(deceasedJson.toString(), deceasedInfoPath);
                return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String update(String deceasedPath) throws IOException, JSONException {
        Stream<String> getLastUpdate = Files.lines(Paths.get(userPath+"/lastUpdate.txt"));
        return updateMedia(deceasedPath, getLastUpdate.collect(Collectors.joining()));
    }

    private String updateMedia(String deceasedPath, String lastUpdate) {
        String dataType, category, timeStamp;
        int dataSize;
        byte[] data;
        boolean end = false;
        try {
            JSONObject jsonObject = jsonHelperClass.toJsonObject(deceasedPath+"/deceasedInfo.json");
            if(jsonObject.getInt("groupId") != 0) {
                out.writeUTF("UPD");
                out.writeUTF(user);
                out.writeInt(jsonObject.getInt("groupId"));
                out.writeUTF(lastUpdate);
                out.flush();
                String command = null;
                while (!end) {
                    command = in.readUTF();
                    if (!command.equals("END")) {
                        dataType = command;
                        dataSize = in.readInt();
                        data = new byte[dataSize];
                        in.readFully(data);
                        if (dataType.equals("IMG") | dataType.equals("VID")) {
                            category = in.readUTF();
                            addMedia(data, category, deceasedPath, dataType, jsonObject.getString("name"));
                        } else if(dataType.equals("wall")){
                            insertInWall(new String(data), deceasedPath);
                        }
                    } else {
                        end = true;
                    }
                }
                timeStamp = in.readUTF();
                return timeStamp;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();

        }
        return lastUpdate;
    }

    public void updateTimestamp(String timeStamp) throws IOException {
        if(timeStamp != null) {
            File newUpdate = new File(userPath + "/lastUpdate.txt");
            FileWriter fileWriter = new FileWriter(newUpdate);
            fileWriter.write(timeStamp);
            fileWriter.flush();
        }
    }
    private void insertInWall(String data, String deceasedPath) throws JSONException, IOException {
        JSONArray jsonArray = jsonHelperClass.toJsonArray(deceasedPath+"/wall/wall.json");
        JSONObject newWallPost = new JSONObject(data);
        jsonArray.put(newWallPost);
        jsonHelperClass.saveObjectToFile(jsonArray.toString(), deceasedPath+"/wall/wall.json");
    }

    private void addMedia(byte[] data, String category, String deceasedPath, String type, String name) throws JSONException, IOException {
        Uri uri = saveInDevice(data, name, type);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("URI", uri);
        JSONArray jsonArray;
        File file = new File(deceasedPath+"/gallery/"+category);
        file.mkdirs();
        if(file.length() > 0) {
            jsonArray = jsonHelperClass.toJsonArray(file.getPath());
        }
        else
            jsonArray = new JSONArray();
        jsonArray.put(jsonObject);
        jsonHelperClass.saveObjectToFile(jsonArray.toString(), file.getPath());
    }

    private Uri saveInDevice(byte[] data, String name, String type) {
        File dirToSaveMedia;
        File fileToSaveMedia;
        String fileName;
        dirToSaveMedia = new File(Environment.getExternalStorageDirectory().toString()+"/memento");
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (!dirToSaveMedia.mkdir()) {
            System.out.println("failed to create folder");
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String newName = name.replace(' ', '_');
        if(type.equals("VID")) {
            fileName = "/"+newName + timeStamp + ".mp4";
            System.out.println(fileName);
        } else
            fileName = "/"+newName + timeStamp + ".jpg";
        System.out.println(fileName);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            fileToSaveMedia = new File(context.getExternalFilesDir(null), fileName);
        } else {
            fileToSaveMedia = new File(dirToSaveMedia, fileName);
        }
        if (fileToSaveMedia.exists()) fileToSaveMedia.delete();
        try {
            fileToSaveMedia.createNewFile();
            FileOutputStream out = new FileOutputStream(fileToSaveMedia);
            if(type.equals("IMG"))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            else
                out.write(data);
            out.flush();
            out.close();
            return Uri.fromFile(fileToSaveMedia);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }



    public void updateInvites(String invitePath) throws IOException, JSONException {
        out.writeUTF("UPI");
        getUser();
        out.writeUTF(user);
        out.flush();
        boolean end = false;
        JSONArray invites;
        JSONObject newDeceased = new JSONObject();
        if(Files.size(Paths.get(invitePath)) > 0) {
            invites = jsonHelperClass.toJsonArray(invitePath);
        } else
            invites = new JSONArray();
        String command;
        while(!end) {
            command = in.readUTF();
            if (!command.equals("END")) {
                newDeceased.put("name", command);
                newDeceased.put("groupId", in.readInt());
                newDeceased.put("bornOnDate", in.readUTF());
                newDeceased.put("deceasedOnDate", in.readUTF());
                newDeceased.put("admin", in.readBoolean());
                if(!jsonHelperClass.hasValue(invites, "groupId", newDeceased.getInt("groupId"))) {
                    invites.put(newDeceased);
                }
                jsonHelperClass.saveObjectToFile(invites.toString(), invitePath);
            } else {
                end = true;
            }
        }
    }

    public void dismissInvite(JSONObject jsonObject) {
        try {
            out.writeUTF("DEC");
            out.writeUTF(user);
            out.writeInt(jsonObject.getInt("groupId"));
            out.flush();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void acceptInvite(JSONObject jsonObject) {
        try {
            out.writeUTF("ACC");
            out.writeUTF(user);
            out.writeInt(jsonObject.getInt("groupId"));
            out.flush();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


}