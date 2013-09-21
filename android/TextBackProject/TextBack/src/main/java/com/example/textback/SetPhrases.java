package com.example.textback;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.R;

import java.util.ArrayList;

public class SetPhrases extends Activity {


    private ArrayList<String> phrases;
    private ListView listView;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_phrases);
        phrases = new ArrayList<String>();
        phrases.add("Test phrase");

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
                listAdapter.notifyDataSetChanged();
            }
        });

    }

    public void saveNewPhrase(View view){
        EditText newPhraseView = (EditText)findViewById(R.id.insert_phrase_field);
        String newPhrase = newPhraseView.getText().toString();
        newPhraseView.setText("");
        phrases.add(newPhrase);
        listAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.set_phrases, menu);
        return true;
    }
    
}
