package com.dealfaro.luca.newspapr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    static final public String MYPREFS = "myprefs";
    static final public String PREF_URL = "restore_url";
    static final public String WEBPAGE_NOTHING = "about:blank";
    static final public String MY_WEBPAGE = "http://users.soe.ucsc.edu/~luca/android.html";
    static final public String LOG_TAG = "webview_example";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clicksf(View v) {
        //Switch to Reader Activity
        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
        intent.putExtra("URL", "http://m.sfgate.com");
        startActivity(intent);
    }

    public void clicksj(View v) {
        //Switch to Reader Activity
        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
        intent.putExtra("URL", "http://www.mercurynews.com");
        startActivity(intent);
    }

    public void clicksc(View v) {
        //Switch to Reader Activity
        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
        intent.putExtra("URL", "http://www.santacruzsentinel.com");
        startActivity(intent);
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
