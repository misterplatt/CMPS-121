package com.dealfaro.luca.dialer;

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
        if (t.equals("DEL")) {
            //Remove the last char from t and set textView
            String t2 = tv.getText().toString();
            if (t2.length() > 0)
                tv.setText(t2.substring(0, t2.length()-1));
        }
        else if (t.equals("CALL")) {
            //clear the number
            tv.setText("");
        }

        else {
            //This case means it's a number, simply append.
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
