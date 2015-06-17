package com.example.platt.thedrain;

import java.lang.Math;

/**
 * Created by Platt on 6/2/2015.
 */
public class Tomato {

    public float radius = 40;

    //Spawn location
    public float posX = 180;
    public float posY = 180;

    //Velocity vector
    private double vx;
    private double vy;

    //Friction vector
    private double fx;
    private double fy;

    //Gravity vector
    private double gx;
    private double gy;

    //Multiplier factors
    private double alpha = 0.2;
    private double beta = 1.2;
    private double gamma = 4;

    //Called every 20ms
    void update(float ix, float iy, Drain goal, float maxWidth, float maxHeight){
        //Calculate friction force
        fx = -alpha * vx;
        fy = -alpha * vy;

        //Calculate gravitational force
        gx = beta * ix;
        gy = beta * iy;

        //Sum for velocities
        vx = fx + gx;
        vy = fy + gy;

        //Check for border collision
        if (posX - radius < 30 || posY - radius < 30 || posX + radius > maxWidth - 30 || posY + radius > maxHeight - 30){
            this.posX -= gamma * vx;
            this.posY -= gamma * vy;
        }
        //Check for drain collision
        if (Math.abs(goal.posX - posX) < Math.abs(goal.radius - radius) && Math.abs(goal.posY - posY) < Math.abs(goal.radius - radius)) {
            //Return to spawn and stop all velocity
            posX = 180;
            posY = 180;
            vx = 0;
            vy = 0;
        } else {    //Execute normal movement
            this.posX += vx;
            this.posY += vy;
        }

    }
}
