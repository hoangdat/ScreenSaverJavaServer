/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmiscreensaver;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 *
 * @author hoangdat
 */
public class CharacterServer {
    private String id;
    private float x,y; //phai la toa do tuong doi so voi clientID
    private float x_1,y_1;//su dung khi server o giua 2 man hinh
    private boolean isUp, isLeft;
    private float speedX, speedY;
    private int characterWidth, characterHeight;
    
    private String clientID; //x o dau thi clientID o do
    private float remainInClient;

    
    private float timeToChange;
    private float timeUpdate;
    private long lastUpdate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public boolean isIsUp() {
        return isUp;
    }

    public void setIsUp(boolean isUp) {
        this.isUp = isUp;
    }

    public boolean isIsLeft() {
        return isLeft;
    }

    public void setIsLeft(boolean isLeft) {
        this.isLeft = isLeft;
    }

    public float getSpeedX() {
        return speedX;
    }

    public void setSpeedX(float speedX) {
        this.speedX = speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setSpeedY(float speedY) {
        this.speedY = speedY;
    }

    public int getCharacterWidth() {
        return characterWidth;
    }

    public void setCharacterWidth(int characterWidth) {
        this.characterWidth = characterWidth;
    }

    public int getCharacterHeight() {
        return characterHeight;
    }

    public void setCharacterHeight(int characterHeight) {
        this.characterHeight = characterHeight;
    }
    
    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public float getRemainInClient() {
        return remainInClient;
    }

    public void setRemainInClient(float remainInClient) {
        this.remainInClient = remainInClient;
    }

    public CharacterServer(String id, float x, float y, 
                            int characterWidth, int characterHeight) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.characterWidth = characterWidth;
        this.characterHeight = characterHeight;
        this.lastUpdate = 0;
        GenerateState();
    }
    
    public CharacterServer() {
        this.lastUpdate = 0;
        GenerateState();
    }
    
    
    public void Update(long totaltime) {
        //truoc khi vao x y la gi
        
        
        long delta = totaltime - lastUpdate;
        lastUpdate = totaltime;
        
        float deltaS = delta * 1.0f / CenterControllerImpl.secInNanosec;   //convert to seconds  
        timeUpdate += deltaS;
        
        int widthSvr = CenterControllerImpl.calculateWidthofServer();
        
        ClientInfo clCenter = CenterControllerImpl.getClient(x);
        int edgeLeftCL = clCenter.getEdgeLeftinSV();
        int edgeRightCL = clCenter.getEdgeRightinSV();
        
        Tunnel[] tunnels = CenterControllerImpl.calculateTuneForClient(clCenter.getId());
        int tunnelWidthLeft = tunnels[1].getWidth();
        int tunnelWidthRight = tunnels[0].getWidth();
        
        if (isLeft) {
            if (x < (widthSvr - characterWidth)) {
                if ((x < (edgeLeftCL - characterWidth)) 
                      && (edgeLeftCL <= widthSvr)) {
                    
                    x += speedX * deltaS;
                    
                } else if ((x < (edgeLeftCL - characterWidth))
                        && (edgeLeftCL > widthSvr)) {
                    System.err.println("Truong hop dac biet, neu vao la sai");
                    x = x;
                } else {
                    if (tunnelWidthLeft == clCenter.getFrameHeight()) {
                        x += speedX * deltaS;
                    } else {
                        //xem y o dau
                        if (y < (clCenter.getFrameHeight() - tunnelWidthLeft)) {
                            speedX = -speedX;
                            isLeft = false;
                        } else {
                            x += speedX * deltaS;
                        }
                    }
                }    
            } else {
                speedX = -speedX;
                isLeft = false;
            }
        } else {
            if (x > 0) {
                if ((x > edgeRightCL)) {
                    x += speedX * deltaS;
                } else {
                    if (tunnelWidthRight == clCenter.getFrameHeight()) {
                        x += speedX * deltaS;
                    } else {
                        if (y < (clCenter.getFrameHeight() - tunnelWidthRight)) {
                            speedX = -speedX;
                            isLeft = true;
                        } else {
                            x += speedX * deltaS;
                        }
                    }
                }
            } else {
                speedX = -speedX;
                isLeft = true;
            }
        }
        
        if (isUp) {
            if (y > 0) {
                y += speedY * deltaS;
            } else {
                speedY = -speedY;
                isUp = false;
            }
        } else {
            if (y < (clCenter.getFrameHeight() - characterHeight)) {
                y += speedY * deltaS;
            } else {
                speedY = -speedY;
                isUp = true;
            }
        }
        
        if (timeUpdate > timeToChange)
            GenerateState();
        
        
        //man hinh chinh phu thuoc x
        ClientInfo clToDraw = CenterControllerImpl.getClient(x);
//        if (clToDraw == null) {
//            System.out.println("clToDraw bi null");
//        } else {
//            System.out.println("Sao lai ko co ID :" + clToDraw.getId()
//                    + " edgrLeft: "+ clToDraw.getEdgeLeftinSV() + " x:" + x);
//        }
        this.clientID = new String();
        this.clientID = clToDraw.getId();
        this.remainInClient = (x + characterWidth) - clToDraw.getEdgeLeftinSV();
        //System.out.println("Remian o update: " + this.remainInClient);
    }
    
    
    private void GenerateState() {
        int state = generateRandom(1, 2);
        
        if (state == 1) {
            speedX = generateRandom(40, 60);
            speedY = generateRandom(20, 40);
        } else {
            speedX = -generateRandom(40, 60);
            speedY = -generateRandom(20, 40);
        }
         
        if (speedX > 0) isLeft = true;
        else            isLeft = false;
        
        if (speedY > 0) isUp = false;
        else            isUp = true;
        
        timeToChange = generateRandom(10, 30);
        timeUpdate = 0;
    }
    
    private int generateRandom(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
    
    private static BufferedImage scale(BufferedImage bI, int destWidth, int destHeight) {
        
        BufferedImage buffImg = new BufferedImage(
                destWidth, destHeight, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = buffImg.createGraphics();
        g2d.drawImage(bI, 0, 0, destWidth, destHeight, null);
        g2d.dispose();
        
        return buffImg;
    }
    
}
