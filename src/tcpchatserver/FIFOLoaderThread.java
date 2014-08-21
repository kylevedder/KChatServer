/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchatserver;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kyle
 */
public class FIFOLoaderThread extends Thread
{

    private volatile boolean isRunning = true;

    @Override
    public void run()
    {
        while (isRunning)
        {
            if(Main.DEBUG)System.out.println("Fifo loader started");
            try
            {
                Main.stickInLocalFIFOLatch.await();
                if(Main.DEBUG)System.out.println("FIFO unlocked");
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(FIFOLoaderThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (isRunning)
            {
                
                if (!Main.publicBroadcastFIFO.isEmpty())
                {
                    String s = Main.publicBroadcastFIFO.removeFirst();
                    for (ConnectionObject c : Main.connectionPool)
                    {
                        if (c.isInUse())
                        {
                            c.localBroadcastFIFO.add(s);
                            c.localBroadcastFIFOLatch.countDown();//unlock fifo loader

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
                else
                {
                    Main.stickInLocalFIFOLatch = new CountDownLatch(1);//relock latch
                }
            }
        }
    }

    public void kill()
    {
        Main.stickInLocalFIFOLatch.countDown();//break latch
        this.isRunning = false;
    }
}
