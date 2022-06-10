package caen1500.memento;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class JsonHelperClass {

   public JsonHelperClass() {}

   public JSONObject toJsonObject(String filePath) throws IOException, JSONException {
      File file = new File(filePath);
      FileReader fileReader = null;
      fileReader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      StringBuilder stringBuilder = new StringBuilder();
      String line = bufferedReader.readLine();
      while (line != null){
         stringBuilder.append(line).append("\n");
         line = bufferedReader.readLine();
      }
      bufferedReader.close();
      String objectString = stringBuilder.toString();
      return new JSONObject(objectString);
   }

   public JSONArray toJsonArray(String filePath) throws JSONException, IOException {
      File file = new File(filePath);
      FileReader fileReader = null;
         fileReader = new FileReader(file);
         BufferedReader bufferedReader = new BufferedReader(fileReader);
         StringBuilder stringBuilder = new StringBuilder();
         String line = bufferedReader.readLine();
         while (line != null){
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
         }
         bufferedReader.close();
         String arrayString = stringBuilder.toString();
         if(arrayString.length() > 0)
            return new JSONArray(arrayString);
         else
            return new JSONArray();
   }

   public boolean saveObjectToFile(String json, String filePath) {
      File file = new File(filePath);
      FileWriter fileWriter;
      try {
         fileWriter = new FileWriter(file);
         BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
         bufferedWriter.write(json.toString());
         bufferedWriter.close();
         return true;
      } catch (IOException e) {
         return false;
      }
   }

   public boolean hasValue(JSONArray json, String key, int value) throws JSONException {
      JSONObject jsonObject;
      for(int i = 0; i < json.length(); i++) {
         jsonObject = json.getJSONObject(i);
         if(jsonObject.getInt(key) == value) {
            return true;
         }
      }
      return false;
   }


}
