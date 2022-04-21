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

import java.util.Date;
import java.util.Objects;

public class MourningSpaceActivity extends AppCompatActivity {

    private static String path;
    private int groupId;
    private static Date birthDate, deathDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mourning_space);
        Bundle b = getIntent().getExtras();
        if(b != null)
            path = b.getString("path");

        ImageView wall = findViewById(R.id.wall);
        ImageView gallery = findViewById(R.id.gallery);
        Button inviteButton = findViewById(R.id.invite_button);
        wall.setOnClickListener(this::wall);
        gallery.setOnClickListener(this::gallery);
        inviteButton.setOnClickListener(this::invite);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_space);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void invite(View view) {
        Bundle b = new Bundle();
        b.putString("path", path);
        Intent i1 = new Intent(this, InvitePopup.class);
        i1.putExtras(b);
        startActivity(i1);
    }

    private void wall(View view) {
        Bundle b = new Bundle();
        b.putString("path", path);
        Intent i1 = new Intent(this, WallActivity.class);
        i1.putExtras(b);
        startActivity(i1);
    }

    public void gallery(View view) {
        Bundle b = new Bundle();
        b.putString("path", path);
        Intent i1 = new Intent(this, GalleryActivity.class);
        i1.putExtras(b);
        startActivity(i1);
    }

}
