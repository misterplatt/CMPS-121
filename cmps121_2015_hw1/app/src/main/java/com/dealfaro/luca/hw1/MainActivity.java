package com.dealfaro.luca.hw1;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clickButton(View v) {
        Button b = (Button) v;
        String t = b.getText().toString();
        TextView tv = (TextView) findViewById(R.id.textView);
        if(t.equals("CALL")){
            tv.setText("");
        } else if (t.equals("DEL")){
            if(tv.length() == 0) return;
            String x = tv.getText().toString();
            t = x.substring(0, tv.length() - 1);
            tv.setText(t);
        } else if (tv.length() >= 11) {
            return;
        } else {
            tv.append(t);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
