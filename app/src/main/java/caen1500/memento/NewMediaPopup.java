package caen1500.memento;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillValue;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;

public class NewMediaPopup extends Activity {
    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 2;
    private Spinner chosenCategory;
    private TextInputLayout newCategory;
    private static String path;
    private Uri imagePath;
    private JSONArray mediaArray;
    private ImageView preview;
    private Button save, cancel;
    private ImageButton addImage, addVideo;
    private static SwitchCompat share;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_media);

        Bundle b = getIntent().getExtras();
        if(b != null)
            path = b.getString("path");

        chosenCategory = (Spinner) findViewById(R.id.categoriesDrop);
        newCategory = findViewById(R.id.add_category);
        share = findViewById(R.id.share);
        preview = findViewById(R.id.imageView);
        addImage = findViewById(R.id.add_media_button);
        addVideo = findViewById(R.id.addVideoButton);
        cancel = findViewById(R.id.cancel_button);
        save = findViewById(R.id.save_button);

        share.setVisibility(View.INVISIBLE);
        fillCategoryList();
        if(true) {
            share.setVisibility(View.VISIBLE);
          //  shareContent();
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*0.75), (int)(height*0.6));

        cancel.setOnClickListener(view -> finish());
        save.setOnClickListener(view -> saveAndPost());
        addImage.setOnClickListener(view -> { Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                                                                    startActivityForResult(intent, 3); });
        addVideo.setOnClickListener(view -> { Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, 3); });
    }

    private void saveAndPost() {
        File file = new File(getCategoryPath());
        if(file.length() == 0) {
            mediaArray = new JSONArray();
        } else {
            fileToArray(file);
        }
        JSONObject object = makeItJson();
        mediaArray.put(object);
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(mediaArray.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(share.isChecked()) {
            send();
        }
        finish();
    }

    private void send() {
        String imageData;
        JSONObject jsonObject = new JSONObject();
        try {
            imageData = toBitmapString();
            jsonObject.put("Format", "User1");
            jsonObject.put("TimeStamp", System.currentTimeMillis()/1000);
            jsonObject.put("data", imageData);
        } catch (JSONException | IOException e) {
            Toast.makeText(this, "Failed to share media", Toast.LENGTH_LONG);
        }
        Runnable runnable = ()->{
            Client client = Client.getInstance();
            client.sendObject(jsonObject);
        };
        new Thread(runnable).start();

    }

    private String toBitmapString() throws IOException {
        ContentResolver contentResolver = getContentResolver();
            Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imagePath));
            ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                    byteArrayBitmapStream);
            byte[] b = byteArrayBitmapStream.toByteArray();
            return Base64.encodeToString(b, Base64.DEFAULT);

    }

    private JSONObject makeItJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Format", "User1");
            jsonObject.put("TimeStamp", System.currentTimeMillis()/1000);
            jsonObject.put("URI", imagePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private void fileToArray(File file) {
        FileReader fileReader = null;
        try {
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
            mediaArray = new JSONArray(arrayString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private String getCategoryPath() {
         if(!newCategory.getEditText().getText().toString().equals("")) {
            File file = new File(path + "/"+newCategory.getEditText().getText().toString()+".json");

            try {
                file.createNewFile();
                return file.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(chosenCategory.getSelectedItem() != null) {
            // Toast.makeText(this, chosenCategory.getSelectedItem(), Toast.LENGTH_LONG).show();
             return path + "/"+chosenCategory.getSelectedItem();
         }
        return path + "/Category.json";
    }

    private void fillCategoryList() {
        File directoryPath = new File(path);
        String[] categoryList = directoryPath.list();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        chosenCategory.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null) {
            Uri selectedMedia = data.getData();

            if(selectedMedia.getPath().contains("VID_")) {
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(selectedMedia.getPath(),
                        MediaStore.Video.Thumbnails.MINI_KIND);
                preview.setImageBitmap(thumbnail);
            }
            else if (selectedMedia.getPath().contains("IMG_")) {
                preview.setImageURI(selectedMedia);
            }
            this.getContentResolver().takePersistableUriPermission(selectedMedia, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePath = selectedMedia;
        }
    }
}
