package com.example.textback;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;

import com.example.R;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends Activity {
    private static String SENT = "SMS_SENT";
    private static String DELIVERED = "SMS_DELIVERED";
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    //private static final String ACTION_SMS_RECEIVED = "android.intent.action.HEADSET_PLUG";
    private static int MAX_SMS_MESSAGE_LENGTH = 160;
    private static int TEXTBACK_MESSAGE = 0;
    private static int PEBBLE_MESSAGE = 7;
    private static final UUID TEXTBACK_UUID = UUID.fromString("E8AE10A2-2E91-473E-B2FA-6DD382BACD52");
    private static int PEBBLE_READY = 6;
    private static int TEXTBACK_PHRASE_1=1;
    private static int TEXTBACK_PHRASE_2=2;
    private static int TEXTBACK_PHRASE_3=3;
    private static int TEXTBACK_PHRASE_4=4;
    private static int TEXTBACK_PHRASE_5=5;
    private SMSReceiver smsReceiver;
    private PebbleKit.PebbleDataReceiver readyDataHandler = null;
    private boolean pebbleIsReady = false;
    private String messageToSend=null;
    private String lastPhoneNumber="";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SMS_RECEIVED);
        smsReceiver = new SMSReceiver();
        registerReceiver(smsReceiver, filter);

        readyDataHandler = new PebbleKit.PebbleDataReceiver(TEXTBACK_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {

                final int readyData = data.getInteger(PEBBLE_READY).intValue();
                final String messageData = data.getString(PEBBLE_MESSAGE);
                PebbleKit.sendAckToPebble(context, transactionId);
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(readyData==1){
                            Log.d("BBDEBUG", "Received ready from pebble");
                            allowSends();
                        }

                        if(messageData!=null){
                            Log.d("BBDEBUG", "Received message to send from Pebble");
                            sendSMS(messageData);
                        }
                    }
                });
            }
        };
        PebbleKit.registerReceivedDataHandler(this, readyDataHandler);
    }
    private void allowSends(){
        pebbleIsReady=true;
        if(messageToSend!=null){
            sendPebbleMessage(messageToSend);
            messageToSend="";
        }
    }
    public void enterPhrases(View view){
        Intent intent = new Intent(this, SetPhrases.class);
        startActivity(intent);
    }


    public void sendSMS(View view){
        lastPhoneNumber="+12409949867";
        sendSMS("Test message");
    }
    // ---sends an SMS message to another device---
    public void sendSMS(String message){

        String phoneNumber = lastPhoneNumber;
        Log.d("BBDEBUG", String.format("Sending sms to %s\nMessage: %s", phoneNumber, message));
        Context mContext = this;

        PendingIntent piSent = PendingIntent.getBroadcast(mContext, 0, new Intent(SENT), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(mContext, 0,new Intent(DELIVERED), 0);
        SmsManager smsManager = SmsManager.getDefault();

        int length = message.length();
        if(length > MAX_SMS_MESSAGE_LENGTH) {
            ArrayList<String> messagelist = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, messagelist, null, null);
        }
        else
            smsManager.sendTextMessage(phoneNumber, null, message, piSent, piDelivered);
    }
    public void sendPebbleMessage(String displayString){

        Log.d("BBDEBUG", "Sending message to pebble");
        PebbleDictionary data = new PebbleDictionary();
        String[] phrases = {"No", "Stop", "Bad"};

        for(int i=0; i<phrases.length; i++){
            data.addString(TEXTBACK_PHRASE_1+i, phrases[i]);
        }
        data.addString(TEXTBACK_MESSAGE, displayString);


          PebbleKit.sendDataToPebble(getApplicationContext(), TEXTBACK_UUID, data);

    }
    public void sendPebblePhrase(String phrase){
        Log.d("BBDEBUG", "Sending phrase to pebble");
        PebbleDictionary data = new PebbleDictionary();
        //data.addString(TEXTBACK_PHRASE, phrase);
        PebbleKit.sendDataToPebble(getApplicationContext(), TEXTBACK_UUID, data);
    }
    public void SMSReceived(String from, String displayString){
        Log.d("BBDEBUG", String.format("Received sms:\n%s", displayString));
        pebbleIsReady=false;
        lastPhoneNumber = from;
        PebbleKit.startAppOnPebble(this, TEXTBACK_UUID);

        if(pebbleIsReady){
            sendPebbleMessage(displayString);
        }
        else{
            messageToSend = new String(displayString);
        }

    }
}
