package com.example.textback;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.R;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static String SENT = "SMS_SENT";
    private static String DELIVERED = "SMS_DELIVERED";
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    //private static final String ACTION_SMS_RECEIVED = "android.intent.action.HEADSET_PLUG";
    private static int MAX_SMS_MESSAGE_LENGTH = 160;
    private SMSReceiver smsReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SMS_RECEIVED);
        smsReceiver = new SMSReceiver();
        registerReceiver(smsReceiver, filter);

    }

    public void enterPhrases(View view){
        Intent intent = new Intent(this, SetPhrases.class);
        startActivity(intent);
    }

    // ---sends an SMS message to another device---
    public void sendSMS(View view){

        EditText phoneNumberField = (EditText)findViewById(R.id.phone_number);
        EditText messageField = (EditText)findViewById(R.id.message_text);

        String phoneNumber = phoneNumberField.getText().toString();
        String message = messageField.getText().toString();
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

    public void SMSReceived(String displayString){
        Log.d("BBDEBUG", String.format("Received sms:\n%s", displayString));
        TextView displayView = (TextView)findViewById(R.id.display_received);
        displayView.setText(displayString);
    }
}
