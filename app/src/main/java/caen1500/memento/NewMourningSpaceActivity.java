package caen1500.memento;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.Objects;

public class NewMourningSpaceActivity extends AppCompatActivity {

    private TextInputLayout name;
    private TextInputEditText birthDate, deceasedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_mourning_space);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.new_menu);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getViews();
        switch (item.getItemId()) {
            case R.id.clear:
                clear();
                return true;
            case R.id.save:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getViews() {
        name = findViewById(R.id.editName);
        birthDate = findViewById(R.id.birthDate);
        deceasedDate = findViewById(R.id.deceasedDate);
    }
    private void save() {

      //  validateName();
       // validateLocation();

        if (validateName() && validateBirthDate() && validateDeathDate()) {
            String saved = "Name: " +  name.getEditText().getText().toString() + "\nBorn of date: " + birthDate.getText() + "\nDeceased of date: " +
                    deceasedDate.getText();
            Toast.makeText(getApplicationContext(), saved, Toast.LENGTH_LONG).show();

            //BathingSitesView.setSites(1);
            clear();
        }
    }

    private boolean validateName() {
        if (name.getEditText().getText().toString().equals("")) {
            name.setHelperText("Name required");
            name.setHelperTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),
                    R.color.colorAccent)));
            return false;
        } else {
            name.setHelperText(null);
            return true;
        }
    }

    private boolean validateBirthDate() {
        if (birthDate.getText().toString().equals("")) {
            birthDate.setHint("Date required");
            birthDate.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),
                    R.color.colorAccent)));
            return false;
        } else {
            birthDate.setHint(null);
            return true;
        }
    }

    private boolean validateDeathDate() {
        if (deceasedDate.getText().toString().equals("")) {
            deceasedDate.setHint("Date required");
            deceasedDate.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),
                    R.color.colorAccent)));
            return false;
        } else {
            deceasedDate.setHint(null);
            return true;
        }
    }





    private void clear() {
        name.setHelperText(null);
        if(name.getEditText() != null) {
            name.getEditText().setText(null);
        }
        birthDate.setHint(null);
        if(birthDate.getText() != null) {
            birthDate.setHint(null);
        }
        deceasedDate.setHint(null);
        if(deceasedDate.getText() != null) {
            deceasedDate.setHint(null);
        }

    }
}
