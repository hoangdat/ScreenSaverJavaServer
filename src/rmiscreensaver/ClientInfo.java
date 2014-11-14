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
public class ClientInfo {
    private CenterController client;
    
    private String id;
    
    private int frameWidth;
    
    private int frameHeight;
    
    private int edgeLeftinSV;
    
    private int edgeRightinSV;
    
    private boolean isAdmin;
    
    private boolean isReady;

    public ClientInfo(CenterController client, int frameWidth, int frameHeight) {
        this.client = client;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public ClientInfo() {
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CenterController getClient() {
        return client;
    }

    public void setClient(CenterController client) {
        this.client = client;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public boolean isIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public int getEdgeLeftinSV() {
        return edgeLeftinSV;
    }

    public void setEdgeLeftinSV(int edgeLeftinSV) {
        this.edgeLeftinSV = edgeLeftinSV;
    }

    public int getEdgeRightinSV() {
        return edgeRightinSV;
    }

    public void setEdgeRightinSV(int edgeRightinSV) {
        this.edgeRightinSV = edgeRightinSV;
    }

    public boolean isIsReady() {
        return isReady;
    }

    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }
}
