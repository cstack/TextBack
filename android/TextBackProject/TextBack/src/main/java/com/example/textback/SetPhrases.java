package com.example.textback;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SetPhrases extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_phrases);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.set_phrases, menu);
        return true;
    }
    
}
