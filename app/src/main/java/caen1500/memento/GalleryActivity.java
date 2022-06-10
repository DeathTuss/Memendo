package caen1500.memento;

import static android.graphics.Matrix.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

public class GalleryActivity extends AppCompatActivity {

    private static final int NUM_COLS = 2;
    private static String path;
    private String[] categoryList;
    private Spinner chosenCategory;
    private ArrayList<String> listOfFiles = new ArrayList<String>();
    private JSONArray mediaArray;
    private TableLayout table;
    private int groupId;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();

        if (b != null) {
            path = b.getString("path") + "/gallery";
            groupId = b.getInt("groupId");
        }
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.gallery_menu);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        chosenCategory = findViewById(R.id.category_spinner);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, STORAGE_PERMISSION_CODE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallery_menu, menu);
        MenuItem item = menu.findItem(R.id.category_spinner);

        chosenCategory = (Spinner) item.getActionView();
        fillCategoryList();
        chosenCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                listOfFiles.clear();
                displayMedia();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                displayMedia();
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_media) {
            addMedia();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void displayMedia() {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(500, 500);
        String fileName;

        if (chosenCategory.getSelectedItem() != null && !chosenCategory.getSelectedItem().equals("Category")) {
            fileName = path + "/" + chosenCategory.getSelectedItem().toString() + ".json";
            loadMediaList(fileName);
        }
        else {
            for (String category : categoryList) {
                fileName = path + "/" + category + ".json";
                loadMediaList(fileName);
            }
        }

        int placed = 0;
        int NUM_ROWS = (listOfFiles.size() / NUM_COLS)+1;

        table = (TableLayout) findViewById(R.id.tableForSpaceIcons);
        table.removeAllViewsInLayout();
        for (int row = 0; row < NUM_ROWS; row++) {
            TableRow tableRow = new TableRow(this);
            table.addView(tableRow);
            for (int col = 0; col < NUM_COLS; col++) {
                if (placed < listOfFiles.size()) {
                    tableRow.addView(displayPicture(listOfFiles.get(placed), layoutParams));

                    placed++;
                }
            }
        }
    }


    private ImageView displayPicture(String uriToPicture, TableRow.LayoutParams layoutParams) {
        Uri uri = Uri.parse(uriToPicture);
        String filePath = null;
        File file = new File(uri.getPath());
        ImageView mediaView = new ImageView(this);
        mediaView.setPadding(5, 5, 5, 5);
        mediaView.setScaleType(ImageView.ScaleType.FIT_XY);
        Bitmap thumbnail = null;
        final String[] split = file.getPath().split(":");
        if(uri.getPath().toLowerCase().contains(".mp4") || uri.getPath().toLowerCase().contains("video")) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                Size thumbSize = new Size(200, 200);
                CancellationSignal ca = new CancellationSignal();
                if(split.length < 2) {
                    try {   // Video received
                        thumbnail = ThumbnailUtils.createVideoThumbnail(file, thumbSize, ca);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaView.setImageBitmap(thumbnail);
                } else { // Video from own device
                    try {
                        thumbnail = getContentResolver().loadThumbnail(uri, thumbSize, ca);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mediaView.setImageBitmap(thumbnail);
            } else {
                if(split.length != 2) { // Video received
                    filePath = file.getPath();
                } else  // Video from own device
                    filePath = Environment.getExternalStorageDirectory().getPath() + "/" + split[1];
                thumbnail = ThumbnailUtils.createVideoThumbnail(filePath,
                        MediaStore.Video.Thumbnails.MINI_KIND);
                mediaView.setImageBitmap(thumbnail);
            }

        } else {
            mediaView.setImageURI(Uri.parse(uriToPicture));
        }
        mediaView.setLayoutParams(layoutParams);
        String finalFilePath = filePath;
        mediaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(uriToPicture.toLowerCase().contains(".mp4") || uriToPicture.toLowerCase().contains("video")) {

                   Bundle b = new Bundle();
                   b.putString("uri", String.valueOf(uri));
                   Intent i1 = new Intent(GalleryActivity.this, ViewVideo.class);
                   i1.putExtras(b);
                   startActivity(i1);
               } else {
                   Bundle b = new Bundle();
                   b.putString("uri", String.valueOf(uri));
                   Intent i1 = new Intent(GalleryActivity.this, ViewPicture.class);
                   i1.putExtras(b);
                   startActivity(i1);
               }
            }
        });
        return mediaView;
    }

    public static class ViewVideo extends AppCompatActivity {
        private Uri videoUri;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle b = getIntent().getExtras();
            if (b != null) {
                videoUri = Uri.parse(b.getString("uri"));
            }
            setContentView(R.layout.activity_show_media);
            VideoView videoView = findViewById(R.id.videoView);
            videoView.setVideoURI(videoUri);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.start();
        }
    }

    public static class ViewPicture extends AppCompatActivity {
        private Uri imageUri;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle b = getIntent().getExtras();
            if (b != null) {
                imageUri = Uri.parse(b.getString("uri"));
            }
            setContentView(R.layout.activity_show_media);
            ImageView imageView = findViewById(R.id.imageViewBig);
            imageView.setImageURI(imageUri);
        }
    }

    private void loadMediaList(String fileName) {
        String line;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder;
        FileReader fileReader;

        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
            stringBuilder = new StringBuilder();
            line = bufferedReader.readLine();
            while (line != null){
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            String arrayString = stringBuilder.toString();
            if (arrayString.length() > 0) {
                mediaArray = new JSONArray(arrayString);
                for (int i = 0; i < mediaArray.length(); i++) {
                    try {
                        JSONObject tmp = mediaArray.getJSONObject(i);
                        listOfFiles.add(tmp.getString("URI"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillCategoryList() {
        File directoryPath = new File(path);
        categoryList = Arrays.stream(directoryPath.list()).map(s -> s.replace(".json", "")).toArray(String[]::new);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        chosenCategory.setAdapter(adapter);
    }

    private void addMedia() {
        Bundle b = new Bundle();
        b.putString("path", path);
        b.putInt("groupId", groupId);
        Intent i1 = new Intent(this, NewMediaPopup.class);
        i1.putExtras(b);
        startActivity(i1);
    }
}