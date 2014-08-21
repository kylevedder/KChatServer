/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchatserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kyle
 */
public class ReadThread extends Thread
{

    private volatile boolean isRunning = true;
    ConnectionObject conn;

    BufferedReader inFromClient = null;

    public ReadThread(BufferedReader inFromClient, ConnectionObject conn)
    {
        this.inFromClient = inFromClient;
        this.conn = conn;
    }

    @Override
    public void run()
    {
        if(Main.DEBUG)System.out.println("ReadThread Started");
        while (isRunning)
        {
            String s = "";
            try
            {
                s = inFromClient.readLine();//this is blocking...
                if(Main.DEBUG) System.out.println("Raw readLine " + s);
            }
            catch (IOException ex)
            {
                Logger.getLogger(ReadThread.class.getName()).log(Level.SEVERE, null, ex);
                this.kill();
            }
            //print in anything interesting is recieved
            if (!s.equals(""))
            {
                if (s.equals("/kill"))
                {
                    if(Main.DEBUG)System.out.println("SHUTTING DOWN CONNECTION");
                    conn.shutdown();
                }
                else
                {
                    if(Main.DEBUG)System.out.println(s);
                    Main.addStringToBroadcast(s);
                }
            }
            //<editor-fold defaultstate="collapsed" desc="Sleep 200 milis">
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(WriteThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            //</editor-fold>
        }
//        System.out.println("ReadThread Closing");
    }

    public void kill()
    {
        isRunning = false;        
    }
}
