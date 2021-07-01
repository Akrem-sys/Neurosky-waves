package com.example.brainwave;

import android.app.Application;

import com.backendless.Backendless;

public class Backendapp extends Application {
    public static final String APPLICATION_ID = "D10CD6E9-52CB-7899-FFB3-6F098FB1E300";
    public static final String API_KEY = "E774C0EB-8146-4BCB-920A-AC6714739522";
    public static final String SERVER_URL = "https://eu-api.backendless.com";
    public static final String Mail="akramalouair@gmail.com";
    public static final String Password="123456789";
    @Override
    public void onCreate() {
        super.onCreate();
        Backendless.setUrl( SERVER_URL );
        Backendless.initApp( getApplicationContext(),
                APPLICATION_ID, API_KEY );
    }
}
