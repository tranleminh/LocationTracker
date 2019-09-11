package com.functionality.locationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    /*******************Attributes and global variables are declared here*********************/

    /***Shared Preferences containing user ID and app's working status***/
    private SharedPreferences mPreferences;

    /***Main UI features***/
    private EditText ID;
    private Button btnTracker;
    private Button btnInfo;
    private TextView Address;
    private String adr = "";
    private String id = "NOT_INITIALIZED";

    /***Broadcast Receiver that get fetched address from LocationTracker service***/
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adr = intent.getStringExtra("Address");
            Address.setText("Current address :" + adr);
        }
    };

    /*****************************************************************************************/

    /**
     * Check if user already gives permission to access to location to the application
     * @return true if permission granted, false otherwise
     */
    private boolean checkLocationPermission() {
        int result3 = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);
        int result4 = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        return result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Overidden method that ask user for permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ParametersCollection.REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0) {
                boolean coarseLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean fineLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (coarseLocation && fineLocation)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * A method that display a dialog containing tooltips
     */
    private void displayTooltips() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(ParametersCollection.TOOLTIPS_TITLE);
        builder.setMessage(ParametersCollection.TOOLTIPS_MESSAGE);
        builder.show();
    }

    /**
     * A method that displays a dialog containing term of use
     */
    private void displayTermOfUse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(ParametersCollection.TITLE_LEGAL);
        builder.setMessage(ParametersCollection.INFO_LEGAL);
        builder.show();
    }

    /**
     * Main method revoked on application's launch
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**Initialize UI with xml file**/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /***Check whether this app has access to the location permission***/
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, ParametersCollection.REQUEST_LOCATION_PERMISSION);
        }

        /**Get the shared preferences file by name, declared on ParametersCollection.java, then get saved user ID from it**/
        mPreferences = getSharedPreferences(ParametersCollection.sharedPrefFile, MODE_PRIVATE);
        id = mPreferences.getString("ID", id);

        /**UI instantiated here**/
        Address = findViewById(R.id.address);
        ID = findViewById(R.id.id);
        btnTracker = findViewById(R.id.btn_daily);
        btnInfo = findViewById(R.id.btn_info);

        if (!id.equals("NOT_INITIALIZED"))
            ID.setText(id);

        /**Display tooltips before anything else**/
        displayTooltips();

        /***A text changed listener with afterTextChanged() method implemented in order to automatically save ID right after the ID field is modified***/
        ID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                id = ID.getText().toString();
                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                preferencesEditor.putString("ID",id);
                preferencesEditor.apply();
            }
        });

        /***The Broadcast Receiver is launched here***/
        registerReceiver(broadcastReceiver, new IntentFilter(ParametersCollection.BROADCAST_TAG));

        /**Start button implemented here**/
        btnTracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**On click, a periodic worker will be created, which asks for location every 15 minutes and fetches it into an address**/
                PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(DailyLocationTracker.class, ParametersCollection.APPLICATION_REPEAT_INTERVAL, TimeUnit.MINUTES)
                        .addTag(ParametersCollection.LOCATION_TRACKER_TAG)
                        .build();

                /**Enqueue unique work to make sure only an instance of worker is running at a time**/
                WorkManager.getInstance(MainActivity.this).enqueueUniquePeriodicWork("Location Tracker", ExistingPeriodicWorkPolicy.REPLACE,periodicWork);
                Toast.makeText(MainActivity.this, "Location Tracker is now started.", Toast.LENGTH_SHORT).show();

            }
        });

        /***Term of Use button implemented here***/
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayTermOfUse();
            }
        });
    }
}

