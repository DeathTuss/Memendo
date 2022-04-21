package caen1500.memento;

import android.content.Context;
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
import com.santalu.maskedittext.MaskEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class NewMourningSpaceActivity extends AppCompatActivity {

    private TextInputLayout name;
    private JsonHelperClass jsonHelper;
    private MaskEditText birthDate, deceasedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_mourning_space);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.new_menu);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        jsonHelper = new JsonHelperClass();

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
        birthDate = findViewById(R.id.bornDate);
        deceasedDate = findViewById(R.id.deceasedDate);
    }
    private void save() {

        validateName();
        validateBirthDate();
        validateDeathDate();

        if (validateName() && validateBirthDate() && validateDeathDate()) {
            String saved = "Name: " +  name.getEditText().getText().toString() + "\nBorn of date: " + birthDate.getText().toString() + "\nDeceased of date: " +
                    deceasedDate.getText().toString();
            Toast.makeText(getApplicationContext(), saved, Toast.LENGTH_LONG).show();

        }
        String dirName = name.getEditText().getText().toString();//.replace(" ","_");
        File myDir = new File(getFilesDir(), dirName);
        myDir.mkdir();
        new File(myDir.getPath()+"/","gallery").mkdir();
        File file = new File(myDir.getPath()+"/gallery/","Category.json");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new File(myDir.getPath()+"/","wall").mkdir();
        File fileWall = new File(myDir.getPath()+"/wall/","wall.json");
        try {
            fileWall.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File deceasedFile = new File(myDir.getPath()+"/deceasedInfo.json");
        try {
            deceasedFile.createNewFile();
            jsonHelper.saveObjectToFile(toJsonString(), deceasedFile.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        clear();
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
            birthDate.setText("Date required");
            birthDate.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),
                    R.color.colorAccent)));
            return false;
        } else {
           // birthDate.setText(null);
            return true;
        }
    }

    private boolean validateDeathDate() {
        if (deceasedDate.getText().toString().equals("")) {
            deceasedDate.setText("Date required");
            deceasedDate.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),
                    R.color.colorAccent)));
            return false;
        } else {
          //  deceasedDate.setText(null);
            return true;
        }
    }

    private String toJsonString() throws JSONException {
        JSONObject theDeceasedInfo = new JSONObject();
        theDeceasedInfo.put("name", name.getEditText().getText().toString());
        theDeceasedInfo.put("bornOnDate", birthDate.getRawText());
        theDeceasedInfo.put("deceasedOnDate", deceasedDate.getRawText());
        theDeceasedInfo.put("groupNumber", 0);
        return theDeceasedInfo.toString();
    }

    private void clear() {
        name.setHelperText(null);
        if(name.getEditText() != null) {
            name.getEditText().setText(null);
        }
        birthDate.setText(null);
        if(birthDate.getText() != null) {
            birthDate.setText(null);
        }
        deceasedDate.setText(null);
        if(deceasedDate.getText() != null) {
            deceasedDate.setText(null);
        }

    }
}
