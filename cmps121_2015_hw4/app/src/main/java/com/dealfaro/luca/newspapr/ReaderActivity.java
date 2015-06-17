package com.dealfaro.luca.newspapr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ReaderActivity extends ActionBarActivity {

    static final public String MYPREFS = "myprefs";
    static final public String PREF_URL = "restore_url";
    static final public String WEBPAGE_NOTHING = "about:blank";
    static final public String LOG_TAG = "webview_example";

    private String webpage;

    WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        myWebView = (WebView) findViewById(R.id.webView1);
        myWebView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //Obtain the URL
        Intent intent = getIntent();
        webpage = intent.getStringExtra("URL");

        // Binds the Javascript interface
        myWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        myWebView.loadUrl(webpage);
        myWebView.loadUrl("javascript:alert(\"Hello\")");

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals(webpage) || Uri.parse(url).getHost().equals("blog.sfgate.com") || Uri.parse(url).getHost().equals("m.sfgate.com")) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site,
            // so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }


    public class JavaScriptInterface {
        Context mContext; // Having the context is useful for lots of things,
        // like accessing preferences.

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void myFunction(String args) {
            final String myArgs = args;
            Log.i(LOG_TAG, "I am in the javascript call.");
            runOnUiThread(new Runnable() {
                public void run() {
                    Button v = (Button) findViewById(R.id.button1);
                    v.setText(myArgs);
                }
            });

        }

    }

    @Override
    public void onPause() {

        Method pause = null;
        try {
            pause = WebView.class.getMethod("onPause");
        } catch (SecurityException e) {
            // Nothing
        } catch (NoSuchMethodException e) {
            // Nothing
        }
        if (pause != null) {
            try {
                pause.invoke(myWebView);
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            }
        } else {
            // No such method.  Stores the current URL.
            String suspendUrl = myWebView.getUrl();
            SharedPreferences settings = getSharedPreferences(ReaderActivity.MYPREFS, 0);
            SharedPreferences.Editor ed = settings.edit();
            ed.putString(PREF_URL, suspendUrl);
            ed.commit();
            // And loads a URL without any processing.
            myWebView.clearView();
            myWebView.loadUrl(WEBPAGE_NOTHING);
        }
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();

        Method resume = null;
        // Resumes the webview.
        try {
            resume = WebView.class.getMethod("onResume");
        } catch (SecurityException e) {
            // Nothing
        } catch (NoSuchMethodException e) {
            // Nothing
        }
        if (resume != null) {
            try {
                resume.invoke(myWebView);
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            }
        } else {
            // No such method.  Restores the suspended URL.
            if (PREF_URL == null) {
                myWebView.loadUrl(webpage);
            } else {
                myWebView.loadUrl(PREF_URL);
            }
        }
    }

    public void clickShare(View v) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, myWebView.getUrl());
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    //If there is web history, go back through that. Else, return to MainActivity
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            // If it wasn't the Back key or there's no web page history,
            // bubble up to the default
            // system behavior (probably exit the activity)
            super.onBackPressed();
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
