package com.example.lagerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lagerapp.integration.android.IntentIntegrator;
import com.example.lagerapp.integration.android.IntentResult;

import java.sql.SQLException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scanBtn, devicescanbtn;
    private TextView contentTxt, countTxt;
    private ListView listView;
    private RadioButton addbtn, deletebtn, deleteallbtn, newlocationbtn;
    private boolean addC, deleteC, alldC, newlocC, scanF, scanS;
    private String loadstring = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand");
            dbController.createTable();
            dbController.closeConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        scanBtn = (Button)findViewById(R.id.scanbtn);
        contentTxt = (TextView)findViewById(R.id.scannedean);
        listView = findViewById(R.id.listview);
        addbtn = findViewById(R.id.addbtn);
        deletebtn = findViewById(R.id.deletebtn);
        deleteallbtn = findViewById(R.id.deleteallbtn);
        newlocationbtn = findViewById(R.id.newlocbtn);
        devicescanbtn = findViewById(R.id.devicescanbtn);
        countTxt = findViewById(R.id.countField);

        contentTxt.setTag(contentTxt.getKeyListener()); //making it uneditable
        contentTxt.setKeyListener(null); //making it uneditable
        countTxt.setTag(countTxt.getKeyListener());
        countTxt.setKeyListener(null);


        scanBtn.setOnClickListener(this);
        devicescanbtn.setOnClickListener(this);

        final Context context = this;

        //TODO: TESTS FÜLLEN DER RESOLVE TABLE



    }

    public void onRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()){
            case R.id.addbtn:
                if(checked){
                    addC = true;
                    deleteC=false;
                    alldC=false;
                    newlocC=false;
                }
                break;
            case R.id.deletebtn:
                if(checked){
                    addC = true;
                    deleteC=true;
                    alldC=false;
                    newlocC=false;
                }
                break;
            case R.id.deleteallbtn:
                if(checked){
                    addC = true;
                    deleteC=false;
                    alldC=true;
                    newlocC=false;
                }
                break;
            case R.id.newlocbtn:
                if(checked){
                    addC = true;
                    deleteC=false;
                    alldC=false;
                    newlocC=true;
                }
                break;
        }
    }


    public void onClick(View v){
        if(v.getId() == R.id.scanbtn){
            scanF = true;
            scanS = false;
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        if(v.getId() == R.id.devicescanbtn){
            scanS = true;
            scanF = false;
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    @SuppressLint("SetTextI18n")
    public void updateList(String scanContent){
        DBController dbController = null;
        try {

            dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand");

            ArrayList<LEntity> list = dbController.getEntities(scanContent);

            ArrayList<String> stringArrayList = new ArrayList<>();

            int am=0;

            for (int i = 0; i < list.size(); i++) {

                stringArrayList.add("Name: "+list.get(i).getName()+" | ArticelNr: "+list.get(i).getArticle()+" | Menge: "+list.get(i).getAmount());
                am+=list.get(i).getAmount();

            }

            ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringArrayList);
            listView.setAdapter(adapter);

            if(list.size()>0){
                countTxt.setText(" Stk: "+am);
            }

            dbController.closeConnection();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, Intent intent) { //TODO RESOLVE BEFÜLLEN UND TESTEN
        super.onActivityResult(requestCode, resultCode, intent);

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {

            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            if(scanF){
                loadstring = scanContent;
                try {

                    DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand");

                    if(newlocC){
                        if(dbController.insertLocation(loadstring)){
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("Erfolgreich den Bereich erstellt!");
                            builder.setCancelable(true);
                            AlertDialog alert = builder.create();
                            alert.show();
                            dbController.closeConnection();
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("Diesen Bereich gibt es schon!");
                            builder.setCancelable(true);
                            AlertDialog alert = builder.create();
                            alert.show();
                            dbController.closeConnection();
                        }
                    }
                    if(alldC){

                        dbController.deleteEntities(loadstring);

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Erfolgreich gelöscht!");
                        builder.setCancelable(true);
                        AlertDialog alert = builder.create();
                        alert.show();

                        countTxt.setText("");
                    }

                    updateList(loadstring);

                    dbController.closeConnection();

                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if(scanS && !loadstring.equalsIgnoreCase("")){

                DBController dbController = null;

                try {

                    dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand");

                    if(addC){

                        String name = dbController.resolveName(scanContent);
                        String article = dbController.resolveEAN(scanContent);

                        dbController.insertEntity(new LEntity(scanContent, article, name, 1, loadstring), loadstring);

                    }
                    if(deleteC){

                        String name = dbController.resolveName(scanContent);
                        String article = dbController.resolveEAN(scanContent);

                        dbController.deleteEntity(new LEntity(scanContent, article, name, 1, loadstring), loadstring);

                    }

                    dbController.closeConnection();

                    updateList(loadstring);

                    contentTxt.setText(scanFormat+": "+scanContent);


                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
            if(scanS && loadstring.equalsIgnoreCase("")){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Zuerst den Bereich einscannen!");
                builder.setCancelable(true);
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Wurde nichts gescannt!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}