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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class GalleryActivity extends AppCompatActivity {

    private static final int NUM_COLS = 2;
    private static String path;
    private static Stream<Path> media;
    private static Stream<Path> categories;
    private String[] categoryList;
    private Spinner chosenCategory;
    private ArrayList<String> listOfFiles;
    private JSONArray mediaArray;
    private TableLayout table;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();

        if (b != null)
            path = b.getString("path") + "/gallery";
        File folder = new File(path + "/Category.json");

        listOfFiles = new ArrayList<>();
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
                // your code here
                displayMedia();
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // getViews();
        switch (item.getItemId()) {
         //   case R.id.category_spinner:
           //     categories();
           //     return true;
            case R.id.add_media:
                addMedia();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void categories() {
    }

    private void displayMedia() {

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
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(500, 500);
        int placed = 0;
        int NUM_ROWS = listOfFiles.size() / NUM_COLS;

        table = (TableLayout) findViewById(R.id.tableForSpaceIcons);
        table.removeAllViewsInLayout();
        for (int row = 0; row < NUM_ROWS; row++) {
            TableRow tableRow = new TableRow(this);
            table.addView(tableRow);
            for (int col = 0; col < NUM_COLS; col++) {
                if (placed < listOfFiles.size()) {
                    ImageView mediaView = new ImageView(this);
                    mediaView.setPadding(5, 5, 5, 5);
                    mediaView.setScaleType(ImageView.ScaleType.FIT_XY);
                    mediaView.setImageURI(Uri.parse(listOfFiles.get(placed)));
                    mediaView.setLayoutParams(layoutParams);
                    placed++;
                    tableRow.addView(mediaView);
                }
            }

        }
     //
    }

    private void loadMediaList(String fileName) {
        String line;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder;
        FileReader fileReader = null;

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
        Intent i1 = new Intent(this, NewMediaPopup.class);
        i1.putExtras(b);
        startActivity(i1);
    }






}