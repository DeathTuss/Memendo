package caen1500.memento;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
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
    private JsonHelperClass jsonHelper;
    private static int PICK_CONTACT = 1;
    private JSONObject groupData;
    private Handler handler;
    private static SwitchCompat adminRights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_popup);
        Bundle b = getIntent().getExtras();
        if (b != null)
            path = b.getString("path");

        jsonHelper = new JsonHelperClass();
        inputPhoneNumber = findViewById(R.id.mask_invite_phone);
        contactButton = findViewById(R.id.contacts);
        sendInviteButton = findViewById(R.id.send_invite_button);
        adminRights = findViewById(R.id.invite_other_switch);

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkContactPermission()) {
                    pickContact();
                } else
                    requestContactPermission();
            }
        });
        sendInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = inputPhoneNumber.getRawText();
                sendInvite();

            }

        });


    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    private void sendInvite() {
        Activity activity = this;
        Runnable runnable = ()->{
            Client client = Client.getInstance();
            client.connect();
            try {
                if(!client.inviteUser(phoneNumber, path+"/deceasedInfo.json", adminRights.isChecked())) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.invite_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                client.disconnect();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(runnable).start();
        finish();
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