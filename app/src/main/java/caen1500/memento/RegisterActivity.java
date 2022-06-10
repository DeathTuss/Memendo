package caen1500.memento;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.santalu.maskedittext.MaskEditText;

import java.io.File;

public class RegisterActivity extends AppCompatActivity {
    private MaskEditText inputPhoneNumber;
    private Button registerButton;
    private Client client;
    private String phoneNumber;
    private boolean registered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        inputPhoneNumber = findViewById(R.id.editTextPhone);
        registerButton = findViewById(R.id.registerButton);
        Runnable runnable = ()->{
            client = Client.getInstance();
        };
        new Thread(runnable).start();
        registered = false;
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = inputPhoneNumber.getRawText();
                Runnable runnable = ()->{
                    client = Client.getInstance();
                    client.connect();
                    client.register(phoneNumber);
                };
                new Thread(runnable).start();
                   verificationCode();
            }
        });
    }

    public void verificationCode() {
        inputPhoneNumber.setMask("######");
        inputPhoneNumber.setText("");
        inputPhoneNumber.setHint(R.string.enter_code);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable runnable = ()->{
                    client = Client.getInstance();
                    client.connect();
                    registered = client.verify(inputPhoneNumber.getRawText());
                };
                new Thread(runnable).start();
                if(registered) {
                    finish();
                }
            }
        });
    }
}