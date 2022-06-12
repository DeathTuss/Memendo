package caen1500.memento;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static SharedPreferences sharedPreferences;
    private static final int NUM_ROWS = 10;
    private static final int NUM_COLS = 3;
    private static File[] spaces;
    private JsonHelperClass jsonHelperClass;
    private String path, user;
    private Client client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spaces = this.getFilesDir().listFiles(File::isDirectory);
        jsonHelperClass = new JsonHelperClass();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        path = sharedPreferences.getString("path", "");
        client = Client.getInstance();
        client.setUserPath(getFilesDir().getPath());
        user = client.getUser();
        if(user == null) {
            Intent i1 = new Intent(this, RegisterActivity.class);
            startActivity(i1);
        } else {
            try {
                verifyStoragePermissions(this);
                update();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
        if(spaces.length >0) {
            displayMourningSpaces();
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newSpace(view);
            }
        });
        Button invitesButton = findViewById(R.id.pending_invites);
        File inviteFile = new File(this.getFilesDir()+"/invites.json");
        if(inviteFile.length() < 3) {
            invitesButton.setVisibility(View.INVISIBLE);
        }
        invitesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInvites(view, inviteFile);
            }
        });
    }

    private void showInvites(View view, File inviteFile) {
        Bundle b = new Bundle();
        b.putString("pathToInvites", String.valueOf(inviteFile));
        b.putString("path", String.valueOf(this.getFilesDir()));
        Intent i1 = new Intent(this, InvitesPopup.class);
        i1.putExtras(b);
        startActivity(i1);
    }

    public void update() throws JSONException, IOException {
            client.setContext(this);
            Runnable runnable = () -> {
                String newLastUpdate = null;
                client.connect();
                try {
                    client.updateInvites(this.getFilesDir()+"/invites.json");
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                for (File f : spaces) {
                    try {
                        newLastUpdate = client.update(f.getPath());
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    client.updateTimestamp(newLastUpdate);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            new Thread(runnable).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayMourningSpaces() {

        TableLayout table = (TableLayout) findViewById(R.id.tableForSpaceIcons);
        assert spaces != null;
        int numbSpaces = spaces.length;
        int placedSpaces = 0;
        for(int row = 0; row < NUM_ROWS; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(10,10,10,10);
            table.addView(tableRow);
            for(int col = 0; col < NUM_COLS; col++) {
                if(placedSpaces < numbSpaces) {
                    Button spaceButton = new Button(this);
                    spaceButton.setText(spaces[placedSpaces].getName());
                    int finalPlacedSpaces = placedSpaces;
                    spaceButton.setOnClickListener((v) -> { enterMourningSpace(spaces[finalPlacedSpaces].toString()); } );
                    placedSpaces++;
                    tableRow.addView(spaceButton);
                }
            }
        }
    }

    private void enterMourningSpace(String path) {
        Bundle b = new Bundle();
        b.putString("path", path);
        System.out.println(user);
        b.putString("user", user);
        Intent i1 = new Intent(this, MourningSpaceActivity.class);
        i1.putExtras(b);
        startActivity(i1);
    }

    public void newSpace(View view) {
        Intent i1 = new Intent(this, NewMourningSpaceActivity.class);
        startActivity(i1);
    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}