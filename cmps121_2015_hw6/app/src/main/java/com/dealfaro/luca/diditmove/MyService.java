package com.dealfaro.luca.diditmove;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.dealfaro.luca.diditmove.MyServiceTask.ResultCallback;

import java.util.Date;

public class MyService extends Service {

    private static final String LOG_TAG = "MyService";

    // Handle to notification manager.
    private NotificationManager notificationManager;
    private int ONGOING_NOTIFICATION_ID = 1; // This cannot be 0. So 1 is a good candidate.

    // Motion detector thread and runnable.
    private Thread myThread;
    private MyServiceTask myTask;

    // Binder given to clients
    private final IBinder myBinder = new MyBinder();

    private PowerManager.WakeLock wakeLock;

    //Variables for the sensor
    private Date T0;
    private Date T1;
    private Date first_accel_time;
    private boolean recorded;

    // Binder class.
    public class MyBinder extends Binder {
        MyService getService() {
            // Returns the underlying service.
            return MyService.this;
        }
    }

    public MyService() {
    }

    @Override
    public void onCreate() {

        Log.i(LOG_TAG, "Service is being created");

        // Display a notification about us starting.  We put an icon in the status bar.
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showMyNotification();

        // Creates the thread running the camera service.
        myTask = new MyServiceTask(getApplicationContext());
        myThread = new Thread(myTask);
        myThread.start();

        //Attempt at sensor code
        boolean shutup =((SensorManager) getSystemService(Context.SENSOR_SERVICE)).registerListener(
                new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        //Check for any x-y movement that isn't just noise.
                        if((event.values[0] < -1 || event.values[0] > 1) || (event.values[1] < -1 || event.values[1] > 1)){
                            T1 = new Date();
                            //If it has been 30 or more seconds since the app was started, note movement
                            if((T1.getTime() - T0.getTime()) / 1000 > 30 && !recorded){
                                first_accel_time = T1;
                                recorded = true;
                                Log.i("LOG_TAG","first_accel changed");
                            }
                        }

                    }
                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {} //ignore
                },
                ((SensorManager)getSystemService(Context.SENSOR_SERVICE))
                        .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);

        T0 = new Date();
        first_accel_time = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "Service is being bound");
        // Returns the binder to this service.
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We start the task thread.
        if (!myThread.isAlive()) {
            Log.i(LOG_TAG, "ALIVE: " + myThread.isAlive());
            myThread.start();
        }

        //WakeLock code
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        /* Cancel the persistent notification.
        notificationManager.cancel(ONGOING_NOTIFICATION_ID);
        Log.i(LOG_TAG, "Stopping.");
        // Stops the motion detector.
        myTask.stopProcessing();
        Log.i(LOG_TAG, "Stopped.");*/

        //Wakelock code
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        if (wakeLock.isHeld()) {
            wakeLock.release(); //Release the WakeLock
        }
    }

    // Interface to be able to subscribe to the bitmaps by the service.

    public void releaseResult(ServiceResult result) {
        myTask.releaseResult(result);
    }

    public void addResultCallback(ResultCallback resultCallback) {
        myTask.addResultCallback(resultCallback);
    }

    public void removeResultCallback(ResultCallback resultCallback) {
        myTask.removeResultCallback(resultCallback);
    }

    // Interface which sets recording on/off.
    public void setTaskState(boolean b) {
        myTask.setTaskState(b);
    }


    /**
     * Show a notification while this service is running.
     */
    @SuppressWarnings("deprecation")
    private void showMyNotification() {

        // Creates a notification.
        Notification notification = new Notification(
                R.mipmap.ic_launcher,
                getString(R.string.my_service_started),
                System.currentTimeMillis());

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getText(R.string.notification_title),
                getText(R.string.my_service_running), pendingIntent);
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    //Service function used to call serviceTask's didItMove
    public boolean didItMove(){
        recorded = false;
        return myTask.didItMove(first_accel_time);
    }

    //Executed by pressing the clear button
    public void clear (){
        Date d = new Date();
        T0 = d;
        first_accel_time = null;
        recorded = false;
    }

}
