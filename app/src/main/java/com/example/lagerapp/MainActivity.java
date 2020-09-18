package com.example.lagerapp;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.lagerapp.integration.android.IntentIntegrator;
import com.example.lagerapp.integration.android.IntentResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Nino Rinnerberger
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scanBtn, devicescanbtn, articlesearchbtn;
    private TextView contentTxt, countTxt, scannedean;
    private ListView listView;
    private RadioButton addbtn, deletebtn, deleteallbtn, newlocationbtn, deletelocbtn, reloadbtn;
    private boolean addC, deleteC, alldC, newlocC, scanF, scanS, deletelocC, reloadC, scanT;
    private String loadstring = "";
    Context context;
    static Context con;
    private String articlesearchtxt;
    AlertDialog.Builder builder;
//JUST ADD FROM ADD AND RESOLVE FROM THERE FFS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand", this);
            dbController.createTable();
            dbController.closeConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        scanBtn = (Button) findViewById(R.id.scanbtn);
        //contentTxt = (TextView) findViewById(R.id.scannedean);
        listView = findViewById(R.id.listview);
        addbtn = findViewById(R.id.addbtn);
        deletebtn = findViewById(R.id.deletebtn);
        deleteallbtn = findViewById(R.id.deleteallbtn);
        newlocationbtn = findViewById(R.id.newlocbtn);
        devicescanbtn = findViewById(R.id.devicescanbtn);
        countTxt = findViewById(R.id.countField);
        articlesearchbtn = findViewById(R.id.articlesearchbtn);
        deletelocbtn = findViewById(R.id.deletelocbtn);
        reloadbtn = findViewById(R.id.reloadbtn);

        //contentTxt.setTag(contentTxt.getKeyListener()); //making it uneditable //TODO: maybe remove
        //contentTxt.setKeyListener(null); //making it uneditable

        countTxt.setTag(countTxt.getKeyListener());
        countTxt.setKeyListener(null);


        scanBtn.setOnClickListener(this);
        devicescanbtn.setOnClickListener(this);
        articlesearchbtn.setOnClickListener(this);

        context = this;
        con=this;

        builder = new AlertDialog.Builder(this);

        //TODO: TESTS FÜLLEN DER RESOLVE TABLE


    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.addbtn:
                if (checked) {
                    addC = true;
                    deleteC = false;
                    alldC = false;
                    newlocC = false;
                    deletelocC = false;
                    reloadC = false;
                }
                break;
            case R.id.deletebtn:
                if (checked) {
                    addC = false;
                    deleteC = true;
                    alldC = false;
                    newlocC = false;
                    deletelocC = false;
                    reloadC = false;
                }
                break;
            case R.id.deleteallbtn:
                if (checked) {
                    addC = false;
                    deleteC = false;
                    alldC = true;
                    newlocC = false;
                    deletelocC = false;
                    reloadC = false;
                }
                break;
            case R.id.newlocbtn:
                if (checked) {
                    addC = false;
                    deleteC = false;
                    alldC = false;
                    newlocC = true;
                    deletelocC = false;
                    reloadC = false;
                }
                break;
            case R.id.deletelocbtn:
                if(checked){
                    deletelocC = true;
                    reloadC = false;
                    addC = false;
                    deleteC = false;
                    alldC = false;
                    newlocC = false;
                }
                break;
            case R.id.reloadbtn:
                if(checked){
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                }
                break;
        }
    }


    public void onClick(View v) {
        if (v.getId() == R.id.scanbtn) {
            scanF = true;
            scanS = false;
            scanT = false;
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        if (v.getId() == R.id.devicescanbtn) {
            scanS = true;
            scanF = false;
            scanT = false;
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        if(v.getId() == R.id.articlesearchbtn){
            //DO STUFF //OPEN TEXT FOR INPUT SEARCH AND GIVE LOCATIONS AND AMOUNT PER LOCATION TODO: DO IT
            scanS = false;
            scanF = false;
            scanT = true;
            builder.setTitle("Artikelnummer: ");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("Los gehts", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    articlesearchtxt = input.getText().toString();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            giveEntityLocations(articlesearchtxt);
        }
    }

    @SuppressLint("SetTextI18n")
    public void updateList(String scanContent) {
        DBController dbController = null;
        try {

            dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand", this);

            ArrayList<LEntity> list = dbController.getEntities(scanContent);

            ArrayList<String> stringArrayList = new ArrayList<>();

            int am = 0;

            for (int i = 0; i < list.size(); i++) {
                if(list.get(i).getAmount()>0){
                    stringArrayList.add("Artikel: " + list.get(i).getArticle() + " | Menge: " + list.get(i).getAmount());
                    am += list.get(i).getAmount();
                }
            }

            ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringArrayList);
            listView.setAdapter(adapter);

            if (list.size() > 0) {
                countTxt.setText(" Stk: " + am);
            }

            dbController.closeConnection();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    @SuppressLint("SetTextI18n")
    public void giveEntityLocations(String entity){
        try {
            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand", this);
            ArrayList<LEntity> list = dbController.getEntities();
            ArrayList<String> listES = new ArrayList<>();
            int am = 0;
            for (int i = 0; i < list.size(); i++) {
                if(Objects.equals(list.get(i).getArticle(), entity)){
                    if(list.get(i).getAmount()>0){
                        listES.add("Artikel: "+list.get(i).getArticle()+" | Bereich: "+list.get(i).getLocation()+" | Menge: "+list.get(i).getAmount());
                        am += list.get(i).getAmount();
                    }
                }
            }
            ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listES);
            listView.setAdapter(adapter);
            if (list.size() > 0) {
                countTxt.setText(" Stk: " + am);
            }
            else{
                builder.setMessage("Dieser Artikel wurde nicht gefunden!");
                builder.setCancelable(true);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            dbController.closeConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void newLoc() {
        try {
            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand", this);
            if (dbController.insertLocation(loadstring)) {
                builder.setMessage("Erfolgreich den Bereich erstellt!");
                builder.setCancelable(true);
                AlertDialog alert = builder.create();
                alert.show();
                dbController.closeConnection();
            } else {
                builder.setMessage("Diesen Bereich gibt es schon!");
                builder.setCancelable(true);
                AlertDialog alert = builder.create();
                alert.show();
                dbController.closeConnection();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void alldelete() {
        try {
            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand", this);

            dbController.deleteEntities(loadstring);

            builder.setMessage("Erfolgreich gelöscht!");
            builder.setCancelable(true);
            AlertDialog alert = builder.create();
            alert.show();

            countTxt.setText("");
            updateList(loadstring);
            dbController.closeConnection();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addEnt(String scanContent) {
        try {

            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand",this);

            dbController.insertEntity(new LEntity(scanContent, "", "", 1, loadstring), loadstring);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateList(loadstring);
                }
            });

            dbController.closeConnection();

            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void deleteEnt(String scanContent) {

        try {
            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand", this);

            dbController.deleteEntity(new LEntity(scanContent, "", "", 1, loadstring), loadstring);

            updateList(loadstring);

            dbController.closeConnection();

            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    public boolean checkExistLoc(String location){
        try {
            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand", this);
            ArrayList<String> list = dbController.getLocations();
            if(list.contains(location)){
                dbController.closeConnection();
                return true;
            }
            else{
                dbController.closeConnection();
                return false;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean deleteLocation(String location){
        try {
            DBController dbController = new DBController("lagerbestand", "nwKHYVhSakav9eVO", "49.12.124.252", 3306, "lagerbestand", this);
            dbController.deleteLoc(location);
            dbController.closeConnection();
            updateList(loadstring);
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, Intent intent) { //TODO RESOLVE BEFÜLLEN UND TESTEN
        super.onActivityResult(requestCode, resultCode, intent);

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {

            final String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            if(scanContent != null){
                if (scanF) {

                    if(scanFormat.equalsIgnoreCase("CODE_128")){

                        loadstring = scanContent;

                        if(checkExistLoc(loadstring)){
                            updateList(loadstring);
                            if (alldC) {
                                alldelete();
                            }
                            if(deletelocC){
                                deleteLocation(loadstring);
                            }
                        }
                        else {
                            if (newlocC) {
                                newLoc();
                            }
                            else {
                                builder.setMessage("Diesen Bereich gibt es noch nicht!");
                                builder.setCancelable(true);
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        }

                    }
                    else {
                        builder.setMessage("Bitte Format = CODE_128 scannen!");
                        builder.setCancelable(true);
                        AlertDialog alert = builder.create();
                        alert.show();
                    }


                }
                if (scanS && !loadstring.equalsIgnoreCase("")) {
                    if(scanFormat.equalsIgnoreCase("EAN_13")){

                        if (addC) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    addEnt(scanContent);
                                }
                            }).start();
                        }
                        if (deleteC) {
                            deleteEnt(scanContent);
                        }

                    }
                    else{
                        builder.setMessage("Bitte Format = EAN_13 scannen!"+scanFormat);
                        builder.setCancelable(true);
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                }
                if (scanS && loadstring.equalsIgnoreCase("")) {
                    builder.setMessage("Zuerst den Bereich einscannen!");
                    builder.setCancelable(true);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Wurde nichts gescannt!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}