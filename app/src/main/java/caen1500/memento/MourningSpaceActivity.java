package caen1500.memento;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class MourningSpaceActivity extends AppCompatActivity {

    private static String path;
    private boolean admin;
    private String user;
    private int groupId;
    private static Date birthDate, deathDate;
    private JsonHelperClass jsonHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mourning_space);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            path = b.getString("path");
            user = b.getString("user");
        }
        try {
            groupId = getGroupId();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        ImageView wall = findViewById(R.id.wall);
        ImageView gallery = findViewById(R.id.gallery);
        Button inviteButton = findViewById(R.id.invite_button);
        Button disconnectButton = findViewById(R.id.disconnect);
        Button removeButton = findViewById(R.id.remove);
        if(!admin) {
            inviteButton.setVisibility(View.INVISIBLE);
        }
        wall.setOnClickListener(this::wall);
        gallery.setOnClickListener(this::gallery);
        inviteButton.setOnClickListener(this::invite);
        disconnectButton.setOnClickListener(this::disconnect);
        removeButton.setOnClickListener(this::remove);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_space);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void remove(View view) {
        File[] categories;
        File deceasedInfo = new File(path+"/deceasedInfo.json");
        Runnable runnable = () -> {
            Client client = Client.getInstance();
            client.connect();
            try {
                client.dismissInvite(jsonHelper.toJsonObject(deceasedInfo.getPath()));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        };
        new Thread(runnable).start();
        File fileGallery = new File(path+"/gallery");
        File fileWall = new File(path+"/wall/wall.json");
        File fileWallFolder = new File(path+"/wall");

        File deceased = new File(path);
        categories = fileGallery.listFiles(File::isFile);
        if(categories != null) {
            System.out.println(Arrays.toString(fileGallery.list()));
            for(File c : categories) {
                c.delete();
            }
        }

        fileGallery.delete();
        fileWall.delete();
        fileWallFolder.delete();
        deceasedInfo.delete();
        deceased.delete();
    }

    private void disconnect(View view) {
        try {
            JSONObject jsonObject = jsonHelper.toJsonObject(path+"/deceasedInfo.json");
            jsonObject.put("groupId", 0);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private int getGroupId() throws JSONException, IOException {
        jsonHelper = new JsonHelperClass();
        JSONObject jsonObject = jsonHelper.toJsonObject(path+"/deceasedInfo.json");
        admin = jsonObject.getBoolean("admin");
        return jsonObject.getInt("groupId");
    }

    private void invite(View view) {
        Bundle b = new Bundle();
        b.putString("path", path);
        b.putInt("groupId", groupId);
        Intent i1 = new Intent(this, InvitePopup.class);
        i1.putExtras(b);
        startActivity(i1);
    }

    private void wall(View view) {
        Bundle b = new Bundle();
        b.putString("path", path);
        b.putInt("groupId", groupId);
        b.putString("user", user);
        Intent i1 = new Intent(this, WallActivity.class);
        i1.putExtras(b);
        startActivity(i1);
    }

    public void gallery(View view) {
        Bundle b = new Bundle();
        b.putString("path", path);
        b.putInt("groupId", groupId);
        Intent i1 = new Intent(this, GalleryActivity.class);
        i1.putExtras(b);
        startActivity(i1);
    }
}
