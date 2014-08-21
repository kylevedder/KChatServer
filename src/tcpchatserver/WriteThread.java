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
public class WriteThread extends Thread
{

    private volatile boolean isRunning = true;
    DataOutputStream outToClient = null;
    private ConnectionObject conn = null;

    public WriteThread(DataOutputStream outToClient, ConnectionObject conn)
    {
        this.outToClient = outToClient;
        this.conn = conn;
    }

    @Override
    public void run()
    {
        if(Main.DEBUG)System.out.println("WriteThread Started");
        while (isRunning)
        {
            if(Main.DEBUG)System.out.println("getBroadcast");
            try
            {
                conn.localBroadcastFIFOLatch.await();
                if(Main.DEBUG)System.out.println("write unlocked");
            }

            catch (InterruptedException ex)
            {
                Logger.getLogger(WriteThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (isRunning)
            {
                String s = conn.getStringToBroadcast();
                if (!s.equals("") && (s.startsWith("/n") || s.startsWith("/c")))//ensures from a real person
                {
                    try
                    {                        
                        if(Main.DEBUG)System.out.println("Writing: " + s);
                        outToClient.writeBytes(s + '\n');
                    }
                    catch (IOException ex)
                    {
                        Logger.getLogger(WriteThread.class.getName()).log(Level.SEVERE, null, ex);
                        if(Main.DEBUG)System.out.println("Stopping server");
                        Main.stopServer();
                        this.kill();

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
            }
        }
        if(Main.DEBUG)System.out.println("WriteThread stopped");
    }

    public void kill()
    {
        conn.localBroadcastFIFOLatch.countDown();//break latch to continue
        isRunning = false;
    }
}
