package com.example.platt.thedrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.View;

import java.sql.Time;

/**
 * Created by Platt on 6/2/2015.
 */



public class GameSpace extends View {

    private Time T0;
    private Time T1;

    public float accelX;
    public float accelY;

    Handler handler;
    Tomato player;
    Drain goal;

    Wall left;
    Wall top;
    Wall right;
    Wall bottom;

    Wall p1;
    Wall p2;

    public GameSpace(Context context) {
        super(context);

        player = new Tomato();
        goal = new Drain(550, 1200, 65);

        left = new Wall(0,0,0,0);
        top = new Wall(0,0,0,0);
        right = new Wall(0,0,0,0);
        bottom = new Wall(0,0,0,0);

        p1 = new Wall(0, getHeight() / 4, getWidth() - 400, getHeight() / 4);
        p2 = new Wall(0,0,0,0);

        boolean shutup =((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).registerListener(
                new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        //Check for any x-y movement that isn't just noise.
                        if((event.values[0] < -1 || event.values[0] > 1) || (event.values[1] < -1 || event.values[1] > 1)){
                            accelX = -event.values[0];
                            accelY = event.values[1];
                        }

                    }
                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {} //ignore
                },
                ((SensorManager)context.getSystemService(Context.SENSOR_SERVICE))
                        .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);

        handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            //Causes onDraw to be re-called every 20ms
            public void run(){
                invalidate();
                handler.postDelayed(this,20);
            }
        },20);
    }

    public void onDraw(Canvas canvas){
        //Declare a paint and draw a background
        Paint p = new Paint();
        canvas.drawColor(Color.LTGRAY);

        //Draw drain
        p.setColor(Color.DKGRAY);
        canvas.drawCircle(goal.posX, goal.posY, goal.radius, p);

        //Draw border
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(60);
        canvas.drawRect(0, 0, getWidth(), getHeight(), p);

        //Draw partitions
        p.setStrokeWidth(40);
        canvas.drawLine(0, getHeight() / 4, getWidth() - 400, getHeight() / 4, p);
        canvas.drawLine(getWidth(), getHeight() - 600, getWidth() - 800, getHeight() - 600, p);

        //Draw/update tomato
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.FILL);

        player.update(accelX, accelY, goal, getWidth(), getHeight());
        canvas.drawCircle(player.posX, player.posY, player.radius, p);
    }
}
