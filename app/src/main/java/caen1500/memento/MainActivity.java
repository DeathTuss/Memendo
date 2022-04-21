package caen1500.memento;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
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
import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static SharedPreferences sharedPreferences;
    private static final int NUM_ROWS = 10;
    private static final int NUM_COLS = 3;
    private static File[] spaces;
    private String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spaces = this.getFilesDir().listFiles();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        path = sharedPreferences.getString("path", "");
        Client client = Client.getInstance();
        client.setUserPath(path);
        new Thread(client).start();
        if(!client.getUser()) {
            Intent i1 = new Intent(this, RegisterActivity.class);
            startActivity(i1);
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
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        Intent i1 = new Intent(this, MourningSpaceActivity.class);
        i1.putExtras(b);
        startActivity(i1);
    }

    public void newSpace(View view) {


        Intent i1 = new Intent(this, NewMourningSpaceActivity.class);
        startActivity(i1);

    }
}