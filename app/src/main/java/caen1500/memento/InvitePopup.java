package caen1500.memento;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.santalu.maskedittext.MaskEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class InvitePopup extends AppCompatActivity {
    private MaskEditText inputPhoneNumber;
    private ImageView contactButton;
    private Button sendInviteButton;
    private String phoneNumber, path;
    private static int PICK_CONTACT = 1;
    private JSONObject groupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_popup);
        Bundle b = getIntent().getExtras();
        if (b != null)
            path = b.getString("path");


        inputPhoneNumber = findViewById(R.id.mask_invite_phone);
        contactButton = findViewById(R.id.contacts);
        sendInviteButton = findViewById(R.id.invite_button);

        sendInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInvite();
            }
        });

    }

    private void sendInvite() {
        Runnable runnable = ()->{
            Client client = Client.getInstance();
            client.inviteUser(phoneNumber, getGroupId());
        };
        new Thread(runnable).start();
    }

    private int getGroupId() {
        int noGroup = 0;
        File file = new File(path+"/group_id.json");
        if(file.exists()) {
            fileToObject(file);
            return 1;
        } else
            return noGroup;
    }

    private void fileToObject(File file) {
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
            String objectString = stringBuilder.toString();
            groupData = new JSONObject(objectString);
        } catch (IOException | JSONException e) {
             e.printStackTrace();
        }
    }
    private boolean checkContactPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                (PackageManager.PERMISSION_GRANTED);
    }

    private void requestContactPermission() {
        String[] permission = {Manifest.permission.READ_CONTACTS};

        ActivityCompat.requestPermissions(this, permission, PICK_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PICK_CONTACT) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendInvite();
            }
            else {
                Toast.makeText(this, "Permisson denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == PICK_CONTACT) {
                Uri uri = data.getData();
                Cursor cursor;
                cursor = getContentResolver().query(uri, null, null, null, null);

            }
        } else {

        }
    }
}