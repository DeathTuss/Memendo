package caen1500.memento;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class InvitesPopup extends AppCompatActivity implements InvitesListAdapter.ItemClickListener {
    private String pathToInvites, mainpath;
    private JsonHelperClass jsonHelperClass;
    private Client client;
    private ArrayList<String> invites;
    private JSONArray jsonArray;
    private InvitesListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites_popup);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            pathToInvites = b.getString("pathToInvites");
            mainpath = b.getString("path");
        }

        jsonHelperClass = new JsonHelperClass();
        jsonArray = null;
        invites = new ArrayList<>();
        try {
            jsonArray = jsonHelperClass.toJsonArray(pathToInvites);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                invites.add(jsonArray.getJSONObject(i).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        RecyclerView recyclerView = findViewById(R.id.invites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvitesListAdapter(this, invites);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(invites.get(position));


        builder.setNeutralButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    client.dismissInvite(jsonArray.getJSONObject(position));
                    jsonArray.remove(position);
                    jsonHelperClass.saveObjectToFile(jsonArray.toString(), pathToInvites);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(InvitesPopup.this, "Invite dismissed", Toast.LENGTH_SHORT).show();

            }
        });


        builder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    System.out.println(position);
                    if(createSpace(jsonArray.getJSONObject(position))) {
                        Runnable runnable = ()->{
                            Client client = Client.getInstance();
                            client.connect();
                            try {
                                client.acceptInvite(jsonArray.getJSONObject(position));
                                jsonArray.remove(position);
                                jsonHelperClass.saveObjectToFile(jsonArray.toString(), pathToInvites);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        };
                        new Thread(runnable).start();

                        Toast.makeText(InvitesPopup.this, "Invite accepted", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getWindow().setGravity(Gravity.CENTER);
    }

    public boolean createSpace(JSONObject jsonObject) {
        File myDir;
        try {
            myDir = new File(mainpath, "/" + jsonObject.getString("name"));
            myDir.mkdir();
            new File(myDir.getPath() + "/", "gallery").mkdir();
            File file = new File(myDir.getPath() + "/gallery/", "Category.json");
            file.createNewFile();
            new File(myDir.getPath() + "/", "wall").mkdir();
            File fileWall = new File(myDir.getPath() + "/wall/", "wall.json");
            fileWall.createNewFile();
            File deceasedFile = new File(myDir.getPath() + "/deceasedInfo.json");
            deceasedFile.createNewFile();
            jsonHelperClass.saveObjectToFile(jsonObject.toString(), deceasedFile.toString());
            return true;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}