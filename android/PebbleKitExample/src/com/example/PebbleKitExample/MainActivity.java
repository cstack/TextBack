package com.example.PebbleKitExample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;


import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.Random;

/**
 * Sample code demonstrating how Android applications can send+receive data using the 'Sports' app, one of Pebble's
 * built-in watch-apps that supports app messaging.
 */
public class MainActivity extends Activity {

    private static final String TAG = "TextBack";

    private final Random rand = new Random();
    private PebbleKit.PebbleDataReceiver dataHandler = null;
    private boolean useMetric = false;
    private boolean isPaceLabel = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sports);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
        if (dataHandler != null) {
            unregisterReceiver(dataHandler);
            dataHandler = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Handler handler = new Handler();

        // To receive data back from the sports watch-app, Android
        // applications must register a "DataReceiver" to operate on the
        // dictionaries received from the watch.
        //
        // In this example, we're registering a receiver to listen for
        // changes in the activity state sent from the watch, allowing
        // us the pause/resume the activity when the user presses a
        // button in the watch-app.
        dataHandler = new PebbleKit.PebbleDataReceiver(Constants.TEXTBACK_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                final String message = data.getString(Constants.TEXTBACK_KEY);

                PebbleKit.sendAckToPebble(context, transactionId);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUi(message);
                    }
                });
            }
        };
        PebbleKit.registerReceivedDataHandler(this, dataHandler);
    }

    public void updateUi(String message) {
        TextView statusText = (TextView) findViewById(R.id.status);
        statusText.setText(message);
    }

    // Send a broadcast to launch the specified application on the connected Pebble
    public void startWatchApp(View view) {
        PebbleKit.startAppOnPebble(getApplicationContext(), Constants.TEXTBACK_UUID);
    }

    // Send a broadcast to close the specified application on the connected Pebble
    public void stopWatchApp(View view) {
        PebbleKit.closeAppOnPebble(getApplicationContext(), Constants.TEXTBACK_UUID);
    }

    // A custom icon and name can be applied to the sports-app to
    // provide some support for "branding" your Pebble-enabled sports
    // application on the watch.
    //
    // It is recommended that applications customize the sports
    // application before launching it. Only one application may
    // customize the sports application at a time on a first-come,
    // first-serve basis.
    public void customizeWatchApp(View view) {
        final String customAppName = "My Sports App";
        final Bitmap customIcon = BitmapFactory.decodeResource(getResources(), R.drawable.watch);


    }

    // Push (distance, time, pace) data to be displayed on Pebble's Sports app.
    //
    // To simplify formatting, values are transmitted to the watch as strings.
    public void updateWatchApp(View view) {
        String message = "Hey, we're meeting at the Big House.";

        PebbleDictionary data = new PebbleDictionary();
        data.addString(Constants.TEXTBACK_KEY, message);

        PebbleKit.sendDataToPebble(getApplicationContext(), Constants.TEXTBACK_UUID, data);

    }


}
