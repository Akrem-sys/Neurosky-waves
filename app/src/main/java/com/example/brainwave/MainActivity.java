package com.example.brainwave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.logging.Logger;
import com.github.pwittchen.neurosky.library.NeuroSky;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.neurosky.rawdata.Raw;
//import com.opencsv.CSVWriter;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter Bluetooth;
    private NeuroSky Sky;
    private Button Connect,Monitor,Save;
    private TextView Attention,Eyeblink,Meditation;
    private LineGraphSeries RAW,DELTA,THETA,LOW_ALPHA,HIGH_ALPHA,LOW_BETA,HIGH_BETA,LOW_GAMMA,MID_GAMMA;
    private double M,X=0;
    private final int div = 100000;
    private Raw RAW_DATA;
    private FileWriter fileWriter;
    private BufferedWriter bw;
    private File file;
  //  CSVWriter csvWriter;
    private boolean running=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String name="data_"+GenerateString()+".csv";
        file = new File(getExternalFilesDir(null),name);
        String row = "DELTA,THETA,LOW_ALPHA,HIGH_ALPHA,LOW_BETA,HIGH_BETA,LOW_GAMMA,MID_GAMMA,RAW_DATA";
        try {
            fileWriter = new FileWriter(file);
            bw = new BufferedWriter(fileWriter);
            bw.write(row);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }


        RAW_DATA= new Raw();
        Save = findViewById(R.id.save);
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Backendless.Files.upload(file, "hello",true, new AsyncCallback<BackendlessFile>() {
                        @Override
                        public void handleResponse(BackendlessFile response) {
                            Toast.makeText(MainActivity.this,"Saved",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(MainActivity.this,fault.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

            }
        });
        Monitor = findViewById(R.id.monitor);
        Monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Sky.enableRawSignal();
                    Sky.startMonitoring();
                    Logger loggers = Backendless.Logging.getLogger("IsEnabled");
                    loggers.info(String.valueOf(Sky.isRawSignalEnabled()));
                    Toast.makeText(MainActivity.this,"Started monitoring",Toast.LENGTH_LONG).show();
                }
                catch (Exception e){
                    Toast.makeText(MainActivity.this,String.valueOf(e),Toast.LENGTH_LONG).show();
                }
            }
        });
        Backendless.UserService.login(Backendapp.Mail, Backendapp.Password, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                Toast.makeText(MainActivity.this,"Logged",Toast.LENGTH_LONG).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(MainActivity.this,fault.getMessage(),Toast.LENGTH_LONG).show();
                Log.d("ERROR BACKEND",fault.getMessage());
                MainActivity.this.finish();
                System.exit(0);
            }
        });
        GraphView graph2 = findViewById(R.id.graph2);
        Viewport viewport2 = graph2.getViewport();
        viewport2.setYAxisBoundsManual(true);
        viewport2.setMinY(-500);
        viewport2.setMaxY(500);
        viewport2.setScrollable(true);
        RAW= new LineGraphSeries<DataPoint>();
        RAW.setColor(Color.BLUE);
        graph2.addSeries(RAW);
        GraphView graph = findViewById(R.id.graph);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(200);
        viewport.setScrollable(true);
        DELTA = new LineGraphSeries<DataPoint>();
        THETA = new LineGraphSeries<DataPoint>();
        LOW_ALPHA = new LineGraphSeries<DataPoint>();
        HIGH_ALPHA = new LineGraphSeries<DataPoint>();
        LOW_BETA = new LineGraphSeries<DataPoint>();
        HIGH_BETA = new LineGraphSeries<DataPoint>();
        LOW_GAMMA = new LineGraphSeries<DataPoint>();
        MID_GAMMA = new LineGraphSeries<DataPoint>();

        DELTA.setColor(Color.BLACK);
        THETA.setColor(Color.WHITE);
        LOW_ALPHA.setColor(Color.BLUE);
        HIGH_ALPHA.setColor(Color.RED);
        LOW_BETA.setColor(Color.GREEN);
        HIGH_BETA.setColor(Color.CYAN);
        LOW_GAMMA.setColor(Color.GRAY);
        MID_GAMMA.setColor(Color.YELLOW);

        graph.addSeries(DELTA);
        graph.addSeries(THETA);
        graph.addSeries(LOW_ALPHA);
        graph.addSeries(HIGH_ALPHA);
        graph.addSeries(LOW_BETA);
        graph.addSeries(HIGH_BETA);
        graph.addSeries(LOW_GAMMA);
        graph.addSeries(MID_GAMMA);






        Attention = findViewById(R.id.Attention);
        Meditation = findViewById(R.id.Meditation);
        Eyeblink = findViewById(R.id.EyeBlink);
        Connect = findViewById(R.id.connect);
        Bluetooth = BluetoothAdapter.getDefaultAdapter();
        Sky = new NeuroSky(new ExtendedDeviceMessageListener() {
            @Override
            public void onStateChange(State state) {
                if (Sky != null && state.equals(State.CONNECTED)) {
                    try{
                        findViewById(R.id.img).setVisibility(View.GONE);
                        findViewById(R.id.graph).setVisibility(View.VISIBLE);
                        findViewById(R.id.Layout1).setVisibility(View.VISIBLE);
                        findViewById(R.id.Layout2).setVisibility(View.VISIBLE);
                        findViewById(R.id.Layout3).setVisibility(View.VISIBLE);
                        findViewById(R.id.graph2).setVisibility(View.VISIBLE);
                        Logger logger = Backendless.Logging.getLogger("Errorsr");
                        logger.info("Started");
                        Sky.enableRawSignal();
                        Sky.startMonitoring();
                        Logger loggers = Backendless.Logging.getLogger("IsEnabled");
                        loggers.info(String.valueOf(Sky.isRawSignalEnabled()));
                    }
                    catch (Exception e){
                        Log.d("ONSTATECHANGE Error",String.valueOf(e));
                        Logger logger = Backendless.Logging.getLogger("State Error");
                        logger.info(String.valueOf(e));
                    }
                }

                Log.d("DEBUG STATE", state.toString());
                Logger logger = Backendless.Logging.getLogger("Debug state error");
                logger.info(String.valueOf(state.toString()));
            }


            @Override
            public void onSignalChange(Signal signal) {
                try{
                    switch (signal) {
                        case ATTENTION:
                            Attention.setText(String.valueOf(signal.getValue()));
                            break;
                        case MEDITATION:
                            Meditation.setText(String.valueOf(signal.getValue()));
                            break;
                        case BLINK:
                            Eyeblink.setText(String.valueOf(signal.getValue()));
                            break;
                        case RAW_DATA:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RAW.appendData(new DataPoint(M, signal.getValue()), false, 40);
                                    M++;
                                    Logger logger = Backendless.Logging.getLogger("RAWDATA");
                                    logger.info(String.valueOf(signal.getValue()));
                                }
                            });
                            break;
                    }
                }
                catch(Exception e){
                    Logger logger = Backendless.Logging.getLogger("Signal change");
                    logger.info(String.valueOf(e));
                }
            }

            @Override
            public void onBrainWavesChange(Set<BrainWave> brainWaves) {
                Logger loggers = Backendless.Logging.getLogger("BrainWaves");
                loggers.info("BrainWave is changing");
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handlewave(brainWaves);
                        }
                    });
                }
                catch (Exception e){
                    Logger logger = Backendless.Logging.getLogger("Errors");
                    logger.info(String.valueOf(e));
                }

            }
        });
        Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Bluetooth.isEnabled()) {
                    Sky.connect();
                    Sky.enableRawSignal();
                    Logger loggers = Backendless.Logging.getLogger("IsEnabled");
                    loggers.info(String.valueOf(Sky.isRawSignalEnabled()));
                } else {
                    try {
                        Bluetooth.enable();
                        Sky.connect();
                        Sky.enableRawSignal();
                        Logger loggers = Backendless.Logging.getLogger("IsEnabled");
                        loggers.info(String.valueOf(Sky.isRawSignalEnabled()));
                    } catch (Exception e) {
                        Log.d("Error", String.valueOf(e));
                        Logger logger = Backendless.Logging.getLogger("Bluetooth error");
                        logger.info(String.valueOf(e));
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            Sky.disconnect();
        }
        catch(Exception e){
            Log.d("ONDESTROY Error",String.valueOf(e));
            Logger logger = Backendless.Logging.getLogger("On Destroy Error");
            logger.info(String.valueOf(e));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            Sky.stopMonitoring();
        }
        catch(Exception e){
            Log.d("ONPAUSE Error",String.valueOf(e));
            Logger logger = Backendless.Logging.getLogger("On Pause error");
            logger.info(String.valueOf(e));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            try{
                Sky.connect();
                Sky.enableRawSignal();
                Logger loggers = Backendless.Logging.getLogger("IsEnabled");
                loggers.info(String.valueOf(Sky.isRawSignalEnabled()));
            }
            catch (Exception e){
                Log.d("ONRESUME Error",String.valueOf(e));
                Logger logger = Backendless.Logging.getLogger("On resume2");
                logger.info(String.valueOf(e));
            }
            Sky.enableRawSignal();
            Sky.startMonitoring();
            Logger loggers = Backendless.Logging.getLogger("IsEnabled");
            loggers.info(String.valueOf(Sky.isRawSignalEnabled()));
        }
        catch (Exception e){
            Log.d("ONRESUME Error",String.valueOf(e));
            Logger logger = Backendless.Logging.getLogger("On Resume error");
            logger.info(String.valueOf(e));
        }

    }
    private void handlewave(Set<BrainWave> brainWaves){
        String nextRow="";
        for (BrainWave i:brainWaves) {
            Logger logger = Backendless.Logging.getLogger("BrainWaves");
            logger.info(String.valueOf(i.getType())+" value "+String.valueOf(i.getValue()));
            switch (i.getType()) {
                case 1:
                    DELTA.appendData(new DataPoint(X, i.getValue()/div), false, 40);
                    nextRow+=i.getValue()+",";
                    break;
                case 2:
                    THETA.appendData(new DataPoint(X, i.getValue()/div), false, 40);
                    nextRow+=i.getValue()+",";
                    break;
                case 3:
                    LOW_ALPHA.appendData(new DataPoint(X, i.getValue()/div), false, 40);
                    nextRow+=i.getValue()+",";
                    break;
                case 4:
                    HIGH_ALPHA.appendData(new DataPoint(X, i.getValue()/div), false, 40);
                    nextRow+=i.getValue()+",";
                    break;
                case 5:
                    LOW_BETA.appendData(new DataPoint(X, i.getValue()/div), false, 40);
                    nextRow+=i.getValue()+",";
                    break;
                case 6:
                    HIGH_BETA.appendData(new DataPoint(X, i.getValue()/div), false, 40);
                    nextRow+=i.getValue()+",";
                    break;
                case 7:
                    LOW_GAMMA.appendData(new DataPoint(X, i.getValue()/div), false, 40);
                    nextRow+=i.getValue()+",";
                    break;
                case 8:
                    MID_GAMMA.appendData(new DataPoint(X, i.getValue()/div), false, 40);
                    nextRow+=i.getValue()+",";
                    break;
            }
        }
        double data=RAW_DATA.Getdata();
        RAW.appendData(new DataPoint(X, data), false, 40);
        nextRow+=data+",";
        X++;
        try {
            bw.write(nextRow);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String GenerateString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        return generatedString;
    }
}