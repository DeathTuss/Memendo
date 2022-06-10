package caen1500.memento;

import static android.os.Environment.DIRECTORY_DCIM;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import android.os.CancellationSignal;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

public class NewMediaPopup extends Activity {
    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 2;
    private Spinner chosenCategory;
    private TextInputLayout newCategory;
    private static String path;
    private int groupId;
    private Uri imagePath;
    private JSONArray mediaArray;
    private ImageView preview;
    private Button save, cancel;
    private ImageButton addImage;
    private static SwitchCompat share;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_media);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            path = b.getString("path");
            groupId = b.getInt("groupId");
        }

        chosenCategory = (Spinner) findViewById(R.id.categoriesDrop);
        newCategory = findViewById(R.id.add_category);
        share = findViewById(R.id.share);
        preview = findViewById(R.id.imageView);
        addImage = findViewById(R.id.add_media_button);
        cancel = findViewById(R.id.cancel_button);
        save = findViewById(R.id.save_button);

        share.setVisibility(View.INVISIBLE);
        fillCategoryList();
        if(groupId != 0) {
            share.setVisibility(View.VISIBLE);
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*0.75), (int)(height*0.6));

        cancel.setOnClickListener(view -> finish());
        save.setOnClickListener(view -> saveAndPost());
        addImage.setOnClickListener(view -> { Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
        String dataType = "IMG";
        if(imagePath.toString().toLowerCase().contains("video") || imagePath.getPath().toLowerCase().contains(".mp4")) {
            dataType = "VID";
        }
        String finalDataType = dataType;
        Runnable runnable = ()->{
            Client client = Client.getInstance();
            client.connect();
            try {
                client.sendObject(groupId, finalDataType, null, toBitArray(), chosenCategory.getSelectedItem().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(runnable).start();
    }

    private byte[] toBitArray() throws IOException {
        ContentResolver contentResolver = getContentResolver();
        if(imagePath.toString().toLowerCase().contains("vid")) {
            InputStream inputStream;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            String[] split = imagePath.getPath().split(":");

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                inputStream = contentResolver.openInputStream(imagePath);
            } else {
                inputStream = contentResolver.openInputStream(imagePath);
            }

                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = inputStream.read(buf)))
                    byteArrayOutputStream.write(buf, 0, n);
                return byteArrayOutputStream.toByteArray();

        } else {

            Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imagePath));
            ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                    byteArrayBitmapStream);
            return byteArrayBitmapStream.toByteArray();
        }
    }

    private JSONObject makeItJson() {
        JSONObject jsonObject = new JSONObject();
        try {
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

            File file = new File(selectedMedia.getPath());//create path from uri
            final String[] split = file.getPath().split(":");//split the path.
            String filePath = "/"+split[1];//assign it to a string(your choice)

            if(selectedMedia.getPath().toLowerCase().contains(".mp4") || selectedMedia.getPath().toLowerCase().contains("video")) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    Size thumbSize = new Size(200,200);
                    CancellationSignal ca = new CancellationSignal();
                    try {
                        Bitmap thumbnail = getContentResolver().loadThumbnail(selectedMedia, thumbSize, ca);
                        preview.setImageBitmap(thumbnail);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(Environment.getExternalStorageDirectory().getPath() + filePath,
                            MediaStore.Video.Thumbnails.MINI_KIND);
                    preview.setImageBitmap(thumbnail);
                }
            }
            else if (selectedMedia.getPath().toLowerCase().contains(".jpg") || selectedMedia.getPath().toLowerCase().contains("image")) {
                preview.setImageURI(selectedMedia);
            }
            this.getContentResolver().takePersistableUriPermission(selectedMedia, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePath = selectedMedia;
        }
    }
}