/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import rmiscreensaver.CenterControllerImpl;
import rmiscreensaver.RegisterMessage;

/**
 *
 * @author hoangdat
 */
public class Main {
    private void startServer() {
        try {
            
            Registry res = LocateRegistry.createRegistry(1099);
            res.rebind("ScreenSaver_Service", new CenterControllerImpl());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Session start");
    }
    
    public static void main(String[] args) {
        Main main_ = new Main();
        main_.startServer();
    }
}
