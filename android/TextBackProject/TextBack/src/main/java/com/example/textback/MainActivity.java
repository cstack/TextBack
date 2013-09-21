package com.example.textback;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.R;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends Activity {
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private SMSReceiver smsReceiver;
    private ArrayList<String> phrases;
    private ListView listView;
    private ArrayAdapter<String> listAdapter;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_phrases);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SMS_RECEIVED);
        smsReceiver = new SMSReceiver();
        registerReceiver(smsReceiver, filter);

        loadPhrasesFromPreferences();

        listView = (ListView)findViewById(R.id.phrase_list);
        listAdapter = new ArrayAdapter<String>(this, R.layout.phrase_row, phrases);


        View footerView =  ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.phrase_listview_footer, null, false);
        listView.addFooterView(footerView);
        listView.setAdapter(listAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                phrases.remove(i);
                updatePhrases();
            }
        });


    }

    public void saveNewPhrase(View view){
        EditText newPhraseView = (EditText)findViewById(R.id.insert_phrase_field);
        String newPhrase = newPhraseView.getText().toString();
        newPhraseView.setText("");
        phrases.add(newPhrase);
        updatePhrases();

    }
    public void updatePhrases(){

        Button saveButton = (Button)findViewById(R.id.insert_phrase_button);
        saveButton.setEnabled(phrases.size()<5);

        listAdapter.notifyDataSetChanged();
        savePhrasesInPreferences();
    }
    public void savePhrasesInPreferences(){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        StringBuilder sb = new StringBuilder();
        for (String s : phrases)
        {
            sb.append(s);
            sb.append((char)29);
        }
        prefsEditor.putString("Phrases", sb.toString());
        prefsEditor.commit();
    }
    public void loadPhrasesFromPreferences(){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        String phrasesString = appSharedPrefs.getString("Phrases", "");
        if(phrasesString!=null){
            phrases=new ArrayList<String>(Arrays.asList(phrasesString.split("" + (char) 29)));
        }
        else{
            phrases=new ArrayList<String>();
        }
    }
    public ArrayList<String> getPhrases(){
        return phrases;
    }



}
