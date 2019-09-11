package com.functionality.locationtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class DailyLocationTracker extends Worker {

    /****************Attributes and global variables declared here*****************/

    /**Location API's necessary attributes' declaration**/
    private Context mContext;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;

    /**Shared Preferences and user ID declaration**/
    private SharedPreferences prefs;
    private String ID;

    /**Intent used for broadcasting addresses**/
    private Intent intent;

    /******************************************************************************/

    /**
     * Constructor
     * @param context
     * @param workerParams
     */
    public DailyLocationTracker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    /**
     * Update the Firebase Database with a fetched address
     * @param adr
     */
    private void updateDB(String adr) {
        Date date = Calendar.getInstance().getTime();
        Toast.makeText(getApplicationContext(), "You are now at " + adr, Toast.LENGTH_LONG).show();

        /**Saved address will be arranged by date, then by day time**/
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ID + "/" + ParametersCollection.df.format(date) + "/" + ParametersCollection.dayTime.format(date));
        ref.setValue(adr);
    }

    /**
     * Overridden method evoked by the periodic worker
     * @return
     */
    @NonNull
    @Override
    public Result doWork() {

        /**Intent instantiated for broadcasting addresses**/
        intent = new Intent(ParametersCollection.BROADCAST_TAG);

        /**Write app's working status to log**/
        Log.d(ParametersCollection.LOCATION_SERVICE_TAG, "doWork: Done");

        Log.d(ParametersCollection.LOCATION_SERVICE_TAG, "onStartJob: STARTING JOB..");

        /***Instantiate the Shared Preference and get the user ID from it***/
        prefs = getApplicationContext().getSharedPreferences(ParametersCollection.sharedPrefFile, MODE_PRIVATE);
        ID = prefs.getString("ID", ID);

        /**Getting the current day time**/
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        String formattedDate = ParametersCollection.dayTime.format(date);

        try {

            /**This app will only get location from 6 AM to 0 AM**/
            Date currentDate = ParametersCollection.dayTime.parse(formattedDate);
            Date startDate = ParametersCollection.dayTime.parse(ParametersCollection.DEFAULT_START_TIME);
            Date endDate = ParametersCollection.dayTime.parse(ParametersCollection.DEFAULT_END_TIME);

            if (currentDate.before(startDate) && currentDate.after(endDate)) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                    }
                };

                /**Create a Location Request with given LOCATION_PERIOD and FASTEST_INTERVAL**/
                final LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(ParametersCollection.LOCATION_PERIOD);
                mLocationRequest.setFastestInterval(ParametersCollection.FASTEST_INTERVAL);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                try {
                    mFusedLocationClient
                            .getLastLocation()
                            .addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        mLocation = task.getResult();
                                        Log.d(ParametersCollection.LOCATION_SERVICE_TAG, "Location : " + mLocation);
                                        String adr = getCompleteAddressString(mLocation.getLatitude(), mLocation.getLongitude());
                                        intent.putExtra("Address", adr);
                                        getApplicationContext().sendBroadcast(intent);
                                        /**If an address is found (not null), update it into the database**/
                                        if (!adr.equals("")) {
                                            updateDB(adr);
                                        }
                                        else {
                                            updateDB("[ERROR] Address not found ! Probably there was no internet connection !");
                                        }
                                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                                    } else {
                                        Log.w(ParametersCollection.LOCATION_SERVICE_TAG, "Failed to get location.");
                                    }
                                }
                            });
                } catch (SecurityException unlikely) {
                    Log.e(ParametersCollection.LOCATION_SERVICE_TAG, "Lost location permission." + unlikely);
                }

                try {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, null);
                } catch (SecurityException unlikely) {
                    //Utils.setRequestingLocationUpdates(this, false);
                    Log.e(ParametersCollection.LOCATION_SERVICE_TAG, "Lost location permission. Could not request updates. " + unlikely);
                }
            } else {
                /**If it is from 0 AM to 6 AM, a message will be written to the log. No location is asked during that time**/
                Log.d(ParametersCollection.LOCATION_SERVICE_TAG, "Time up to get location. Your time is : " + ParametersCollection.DEFAULT_START_TIME + " to " + ParametersCollection.DEFAULT_END_TIME);
            }
        } catch (ParseException ignored) {

        }

        return Result.success();
    }

    /**
     * A method that fetch a given location (shown in longitude and latitude) into address
     * @param LATITUDE
     * @param LONGITUDE
     * @return a String containing the fetched address
     */
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }
}


