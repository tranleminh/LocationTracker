package com.functionality.locationtracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ParametersCollection {

    /**Location permission value, used in MainActivity**/
    public static final int REQUEST_LOCATION_PERMISSION = 200;

    /**Tag assimilated to the periodic worker defined in MainActivity**/
    public static final String LOCATION_TRACKER_TAG = "LOCATION UPDATE";

    /**Shared preferences file name**/
    public static final String sharedPrefFile = "LocationTrackerPreferences";

    /**Repeat interval used to set up location worker's period. Used in MainActivity.java. Unit is minutes**/
    public static final int APPLICATION_REPEAT_INTERVAL = 15;

    /**Day time interval when app is allowed to ask for location. Used in DailyLocationTracker.java**/
    public static final String DEFAULT_START_TIME = "06:00";
    public static final String DEFAULT_END_TIME = "24:00";

    /**Simple date formats used in DailyLocationTracker for database update and address fetching**/
    public static final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    public static final DateFormat dayTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

    /**Tag assimilated to the application's log in order to check Worker's status**/
    public static final String LOCATION_SERVICE_TAG = "DAILY TRACKER";

    /**Location request parameters, the higher LOCATION_PERIOD is, the more accurate the location will be.
     * FASTEST_INTERVAL defines the maximal delay if there is another app that asks for location.
     * Both are used in DailyLocationTracker.
     */
    public static final int LOCATION_PERIOD = 10000;
    public static final int FASTEST_INTERVAL = LOCATION_PERIOD/2;

    /**Broadcast tag name used to define a broadcast in DailyLocationTracker and a listener in MainActivity**/
    public static final String BROADCAST_TAG = BuildConfig.APPLICATION_ID + ".ACTION_BROADCAST_ADDRESS";

    /**Titles and Messages displayed in MainActivity's dialog**/
    public static final String TOOLTIPS_TITLE = "Welcome to Location Tracker !";
    public static final String TOOLTIPS_MESSAGE = "In order to use this app, " +
            "please first fill correctly the ID field with your given ID " +
            "before pressing the Start button.\n\n" +
            "This app works well on Background task, and the Service will be relaunched " +
            "if app is manually turned off, or your device is rebooted.\n\n" +
            "Thanks for your patience ! Click anywhere outside this box to use Location Tracker.";

    public static final String TITLE_LEGAL = "Term of Use";
    public static final String INFO_LEGAL = "You can read about term of use here";

}
