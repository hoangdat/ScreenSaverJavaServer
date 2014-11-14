/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmiscreensaver;

/**
 *
 * @author hoangdat
 */
public class Tunnel {
    private String targetID;
    private int width;

    public Tunnel() {
    }

    public Tunnel(String targetID, int width) {
        this.targetID = targetID;
        this.width = width;
    }

    public String getTargetID() {
        return targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }    
}
