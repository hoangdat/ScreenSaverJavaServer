/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmiscreensaver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hoangdat
 */
public class CenterControllerImpl extends UnicastRemoteObject implements CenterController{
    
    public static final int   SESSION_CONFIGURING = 0;
    public static final int   SESSION_RUNNING     = 1;
    public static final long secInNanosec        = 1000000000L;
    public static final long milisecInNanosec    = 1000000L;

    private static final int  FPS = 40;
    private static final long UPDATE_PERIOD = secInNanosec / FPS;
    
    private String bgNameChoosed;
    private String chNameChoosed;
    private int chCount;
    //for calculating elapsed time
    private long loopTime;
    private long lastTime;
    
    
    private ArrayList<CharacterServer> allCharacter;
    
    public static HashMap<String, ClientInfo> clients = new HashMap<String, ClientInfo>();
    public static LinkedList<String> orderIDClient = new LinkedList<String>();
    public static HashMap<String, ClientInfo> clientsRegister = new HashMap<String, ClientInfo>();
    public static LinkedList<String> orderIDClientRegister = new LinkedList<String>();
    
    
    public HashMap<String, ArrayList<CharacterMessage>> queueForClient;
    private int state;
    
    public CenterControllerImpl () throws RemoteException {
        this.state = SESSION_CONFIGURING;
        bgNameChoosed = new String();
        chNameChoosed = new String();
    }

    public CenterControllerImpl(int state) throws RemoteException{
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public RegisterMessage register(RegisterMessage resMes) throws RemoteException {
        boolean isAdmin = false;
        
        // chu y dieu kien kiem tra 1 may ko dc truy cap 2 lan
        
        String id = generateClientUniqueID();
        ClientInfo clientInfo = new ClientInfo(resMes.getClient(), 
                    resMes.getFrameWidth(), resMes.getFrameHeight());
        clientInfo.setId(id);
        synchronized(clients) {
            if (clients.isEmpty()) {
                isAdmin = true;
                clientInfo.setIsAdmin(isAdmin);
            } else {
                clientInfo.setIsAdmin(false);
            }
            
            synchronized(orderIDClient) {
                orderIDClient.add(id);
            }
            System.out.println("Client " + id + " vua dang ki" + "Info: " 
                    + clientInfo.getFrameWidth() + " ," + clientInfo.getFrameHeight());
            clients.put(id, clientInfo);
        }
        
        RegisterMessage regResult = new RegisterMessage(id, isAdmin, getState());
        return regResult;
    }

    @Override
    public void changeBackground(String Imgname) throws RemoteException {
        synchronized(clients) {
            for (String key : clients.keySet()) {
                if (clients.get(key).isIsAdmin()) {
                    continue;
                } else {
                    CenterController client = clients.get(key).getClient();
                    client.bcchangeBackground(Imgname);
                }
            }
        }
    }

    @Override
    public void bcchangeBackground(String Imgname) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public ContentMessage loadContent() throws RemoteException {
        ContentMessage content = new ContentMessage();
        
        if (bgNameChoosed.isEmpty() || chNameChoosed.isEmpty() || chCount == 0) {
            return content;
        }
        
        content.setBackgroundName(bgNameChoosed);
        content.setCharacterName(chNameChoosed);
        content.setCharacterCount(chCount);
        
        return content;
    }
    
    private String generateClientUniqueID() {
        String strID = new String();
        
        UUID uid = UUID.randomUUID();
        strID += uid.toString();
        
        return strID;
    }

    @Override
    public void configureContent(String backgroundName, String characterName, 
                                        int numberCha) throws RemoteException {
        bgNameChoosed = backgroundName;
        chNameChoosed = characterName;
        chCount = numberCha;
        
        System.out.println(backgroundName + "\n" 
                         + characterName);
        
        synchronized(clients) {
            for (String key : clients.keySet()) {
                if (clients.get(key).isIsAdmin()) {
                    continue;
                } else {
                    CenterController client = clients.get(key).getClient();

                    try {
                                
                        client.bcconfigureContent(backgroundName, 
                                        characterName, numberCha);
                                
                    } catch (RemoteException ex) {
                        Logger.getLogger(CenterControllerImpl.class.getName())
                                        .log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
        InitalizeContent();
        for (int i = 0; i < chCount; i++) {
            String chID = genCharaterID(i);
            CharacterServer ch = new CharacterServer(chID, i * 10, i * 5, 60, 80);
            allCharacter.add(ch);//debug doan nay xem sao
        }
    }
    
    private void InitalizeContent() {
        allCharacter = new ArrayList<CharacterServer>();
    }

    @Override
    public void bcconfigureContent(String backgroundName, String characterName, 
                                        int numberCha) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean start() throws RemoteException {
        System.out.println("Start loop");
        synchronized(clients) {
            for (String key : clients.keySet()) {
                if (clients.get(key).isIsAdmin()) {
                    continue;
                } else {
                    CenterController client = clients.get(key).getClient();
                    System.out.println("sent to: " + key); 
                    try {
                        if (!client.bcstart()) {
                            clients.remove(key);
                            orderIDClient.remove(key);
                            continue;
                        }  
                    } catch (RemoteException e) {
                        clients.remove(key);
                        orderIDClient.remove(key);
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        this.state = SESSION_RUNNING;
        
        Thread tRunLoop = new Thread() {

            @Override
            public void run() {
                RunLoop();
            }
            
        };
        tRunLoop.start();
        
        return true;
    }

    @Override
    public boolean bcstart() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void RunLoop() {
        long beginTime, timeTaken, timeLeft;
        loopTime = 0;
        lastTime = System.nanoTime();
        
        while (true) {
            loopTime += System.nanoTime() - lastTime;
            lastTime = System.nanoTime();
            
            beginTime = System.nanoTime();
            //code something here
            queueForClient = new HashMap<String, ArrayList<CharacterMessage>>();
            
            for (int i = 0; i < allCharacter.size(); i++) {
                allCharacter.get(i).Update(loopTime);
            }

            //chuyen vao queue
            queueForClient = distributeCharater(allCharacter);
            synchronized(clients) {
                //chi check dieu kien de nhan du lieu o thoi diem nay thoi
                for (String key : clients.keySet()) {
                    System.out.println("Queue for " + key);
                    try {
                        ArrayList<CharacterMessage> chsToqueue = queueForClient.get(key);
                        //co the null o day                       
                        if (chsToqueue.size() > 0) {
                            for (int i = 0; i < chsToqueue.size(); i++) {
                                System.out.println("la " + chsToqueue.get(i).getId() + 
                                        " ClientID la: " + chsToqueue.get(i).getClientID());
                            }
                        }
                        clients.get(key).getClient().putClientQueue(chsToqueue);
                    } catch (RemoteException ex) {
                        Logger.getLogger(CenterControllerImpl.class.getName())
                                    .log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            
            timeTaken = System.nanoTime() - beginTime;
            timeLeft  = (UPDATE_PERIOD - timeTaken) / milisecInNanosec;
            if (timeLeft < 10)
                timeLeft = 10;
            try {
                Thread.sleep(timeLeft);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static String genCharaterID(int i) {
        long time = System.currentTimeMillis();
        
        String s = "char-";
        
        s = s.concat(Objects.toString(time, null));
        
        s = s.concat(Integer.toString(i));
        
        return s;
    }

    //kiem tra ham nay
    @Override
    public ArrayList<CharacterMessage> getCharacter() throws RemoteException {
        ArrayList<CharacterMessage> chars = new ArrayList<CharacterMessage>();
        
        for (CharacterServer chS : allCharacter) {
            CharacterMessage chM = new CharacterMessage();
            chM.setId(chS.getId());
            chM.setCharacterWidth(chS.getCharacterWidth());
            chM.setCharacterHeight(chS.getCharacterHeight());
            chars.add(chM);
        }
        
        return chars;
    }

    @Override
    public void putClientQueue(ArrayList<CharacterMessage> arrChars) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static int calculateWidthofServer() {
        synchronized (clients) {
            int totalWidth = 0;
        
            for (String key : clients.keySet()) {
                ClientInfo clInfo = clients.get(key);
                totalWidth += clInfo.getFrameWidth();
            }
        
            return totalWidth;
        }
    }
    
    public static ClientInfo getClient(String clientID) {
        ClientInfo clInfo = new ClientInfo();
        synchronized(clients) {
            clInfo = clients.get(clientID);
        }
        
        return clInfo;
    }
    
    //test
    public static ClientInfo getClient(float x) {
        ClientInfo clInfoResult = new ClientInfo();
        int increseWidth = 0;
        synchronized (clients) {
            for (int i = 0; i < orderIDClient.size(); i++) {
                String clID = orderIDClient.get(i);
                ClientInfo clInfo = clients.get(clID);
                increseWidth += clInfo.getFrameWidth();
                int remain = increseWidth - (int)x;
                
                if (remain >= 0) {
                    
                    clInfoResult = clients.get(clID);
                    //System.out.println("ID o getClient la: " + clInfoResult.getId());
                    clInfoResult.setEdgeLeftinSV(increseWidth);//check ki 
                    clInfoResult.setEdgeRightinSV(increseWidth - clInfoResult.getFrameWidth());
                    break;
                    
                } else {
                    continue;
                }
                
            }
        }
        return clInfoResult;
    }
    
    //dag loi
    public static String getClientID(float x) {
        String clientID = new String();
        int increseWidth = 0;
        synchronized (clients) {
            for (int i = 0; i < orderIDClient.size(); i++) {
                String clID = orderIDClient.get(i);
                ClientInfo clInfo = clients.get(clID);
                int remain = increseWidth - (int)x;
                increseWidth += clInfo.getFrameWidth();
                if (remain < 0) {
                    continue;
                } else {
                    if (i == 0) return null;
                    clientID = orderIDClient.get(i-1);
                    break;
                }
                
            }
        }
        return clientID;
    }
    
    
    //[0] => tuneWdithRight
    //[1] => tuneWidthLeft
    public static Tunnel[] calculateTuneForClient(String clientID) {
        Tunnel[] arr =  new Tunnel[2];
        arr[0] = new Tunnel();
        arr[1] = new Tunnel();
        
        synchronized(orderIDClient) {
            String idRight = new String();
            String idLeft = new String();
            int i = orderIDClient.indexOf(clientID);
            if (i == 0) {
                if (orderIDClient.size() > 1) {
                    idLeft = orderIDClient.get(i);
                }
            } else if (i == (orderIDClient.size() - 1) && (i != 0)) {
                idRight = orderIDClient.get(i-1);
            } else {
                idRight = orderIDClient.get(i-1);
                idLeft = orderIDClient.get(i+1);
            }
            
            synchronized (clients) {
                ClientInfo clCenter = clients.get(clientID);
                if (!idLeft.isEmpty()) {
                    ClientInfo clLeft = clients.get(idLeft);
                    arr[1].setTargetID(idLeft);
                    if (clCenter.getFrameHeight()>= clLeft.getFrameHeight()) {
                        
                        arr[1].setWidth(clLeft.getFrameHeight());
                    } else {
                        
                        arr[1].setWidth(clCenter.getFrameHeight());
                    }
                
                } else if (!idRight.isEmpty()){
                    ClientInfo clRight = clients.get(idRight);
                    arr[0].setTargetID(idRight);
                    if (clCenter.getFrameHeight() >= clRight.getFrameHeight()) {
                        arr[0].setWidth(clRight.getFrameHeight());
                    } else {
                        arr[0].setWidth(clCenter.getFrameHeight());
                    }
                    
                } else {
                    arr[0].setTargetID(null);
                    arr[0].setWidth(0);
                    arr[1].setTargetID(null);
                    arr[1].setWidth(0);
                }
            }
        }
        return arr;
    }
    
    public HashMap<String, ArrayList<CharacterMessage>> 
                        distributeCharater(ArrayList<CharacterServer> arr) {
                            
        HashMap<String, ArrayList<CharacterMessage>> hResult = 
                new HashMap<String, ArrayList<CharacterMessage>>();
        
        synchronized(clients) {
            for (String key : clients.keySet()) {      
                ArrayList<CharacterMessage> lst = new ArrayList<CharacterMessage>();
                hResult.put(key, lst);
            }
        }
        
        for (int i = 0; i < arr.size(); i++) {
            CharacterServer chTemp = arr.get(i);
            float xRelative = convertXrelative(chTemp.getX());
            ArrayList<CharacterMessage> clLst = hResult.get(chTemp.getClientID());
//            //khong ton tai cai clientID do == clLst null
//            if (clLst == null) {
//                continue;
//            }
            //chuyen nhan vat voi toa do = tuong doi
            
            CharacterMessage chMes = new CharacterMessage(chTemp.getId(), 
                                                          chTemp.getClientID(), 
                                                          xRelative, 
                                                          chTemp.getY(), 
                                                          chTemp.isIsUp(), 
                                                          chTemp.isIsLeft());
            
            ArrayList<CharacterMessage> lstToQueue = hResult.get(chTemp.getClientID());
            lstToQueue.add(chMes);
            
            if ((chTemp.getRemainInClient() < chTemp.getCharacterWidth()) && 
                    (chTemp.getRemainInClient() > 0)) {
                //phai dua sang man hinh ben canh
//                System.out.println("Vao truong hop phan phoi " + 
//                        "Con lai o client chinh: " + chTemp.getRemainInClient());
                String neighborID = new String();
                try {
                    neighborID = getLeftNeighbour(chTemp.getClientID());
                } catch (NullPointerException nullE) {
                    continue;
                }
                
                float x_neighbor = calculateXrelativeNext(chTemp.getRemainInClient());//day
                System.out.println("x o neighbor la: " + x_neighbor);
                
                ArrayList<CharacterMessage> lstNeighbor = hResult.get(neighborID);
                CharacterMessage chMesNExt = new CharacterMessage(chTemp.getId(), 
                                                                  neighborID, 
                                                                  x_neighbor, 
                                                                  chTemp.getY(), 
                                                                  chTemp.isIsUp(), 
                                                                  chTemp.isIsLeft());
                lstNeighbor.add(chMesNExt);
            }
        }
        
        
        return hResult;
    }
                        
    private static float convertXrelative(float x) {
        ClientInfo clIn = getClient(x);
        
        return x - clIn.getEdgeRightinSV();
    }
    
    private static String getLeftNeighbour(String clientID) {
        String neighborID = new String();
        
        int indexOf = orderIDClient.indexOf(clientID);
        
        if (indexOf < 0 || (indexOf == (orderIDClient.size() - 1))) 
            throw new NullPointerException();
        
        neighborID = orderIDClient.get(indexOf + 1);
        
        return neighborID;
    }
    
    private static float calculateXrelativeNext(float remain) {
        float xResult;
        
        xResult = 0 - 60 + remain;
        
        return xResult;
    }

    @Override
    public void sendReady(boolean isReady) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void signOut(String id) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
