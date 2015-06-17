package com.example.platt.thedrain;

/**
 * Created by Platt on 6/2/2015.
 */
public class Wall {

    public float posX;
    public float posY;
    public float width;
    public float height;

    public Wall (float x, float y, float w, float h) {
        posX = x;
        posY = y;
        width = w;
        height = h;
    }

    public boolean collision (Tomato player){
        if(posX - width/2 <= player.radius + player.posY && posY - height/2 <= player.radius + player.posY &&
                posY + height/2 >= player.radius + player.posY && posX + height/2 >= player.radius + player.posX){
            return true;
        } else {
            return false;
        }
    }

}
