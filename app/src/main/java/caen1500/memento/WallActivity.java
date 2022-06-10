package caen1500.memento;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

public class WallActivity extends AppCompatActivity {
    private static String path;
    private String user;
    private EditText writText;
    private TextView displayText;
    private Button postButton;
    private JSONArray posts;
    private JsonHelperClass helper;
    private SwitchCompat share;
    private Client client;
    private int groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);
        Toolbar toolbar = findViewById(R.id.toolbar);
        helper = new JsonHelperClass();
        Bundle b = getIntent().getExtras();
        if (b != null) {
            path = b.getString("path") + "/wall/";
            groupId = b.getInt("groupId");
            user = b.getString("user");
        }
        writText = findViewById(R.id.write_text);
        displayText = findViewById(R.id.display_text);
        postButton = findViewById(R.id.post_button);
        share = findViewById(R.id.share2);
        if (groupId == 0) {
            share.setVisibility(View.INVISIBLE);
        }
        posts = new JSONArray();
        loadText();
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndPost();
            }
        });
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.wall_menu);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wall_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void loadText() {
        try {
            fileToArray();
            if (posts != null) {
                for (int i = 0; i < posts.length(); i++) {
                    JSONObject object = posts.getJSONObject(i);
                    displayText.append((String) object.get("Name") + "\n");
                    displayText.append((String) object.get("Text") + "\n\n");
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }

    private void fileToArray() throws JSONException, IOException {
        posts = helper.toJsonArray(path + "/wall.json");
    }

    private void saveAndPost() {
        String theText = writText.getText().toString();
        writText.setText("");
        displayText.append("Me\n" + theText + "\n\n");
        helper.saveObjectToFile(posts.toString(), path + "wall.json");
        sharePost(makeItJson(theText));
    }

    private JSONObject makeItJson(String theText) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Name", user);
            jsonObject.put("TimeStamp", System.currentTimeMillis() / 1000);
            jsonObject.put("Text", theText);
            posts.put(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private void sharePost(JSONObject thePost) {
        Runnable runnable = () -> {
            client = Client.getInstance();
            client.connect();
            try {
                client.sendObject(groupId, "wall", thePost.toString(), null, null);
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(runnable).start();
    }
}


