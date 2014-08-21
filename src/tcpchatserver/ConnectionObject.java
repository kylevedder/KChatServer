/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchatserver;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kyle
 */
public class ConnectionObject
{

    Socket socketMain = null;
    private boolean inUse;
    ReadThread r = null;
    WriteThread w = null;

    volatile LinkedList<String> localBroadcastFIFO = new LinkedList<String>();
    
    volatile CountDownLatch localBroadcastFIFOLatch = new CountDownLatch(1);

    public ConnectionObject(boolean inUse)
    {
        this.inUse = inUse;
    }

    public String getStringToBroadcast()
    {
        if (!localBroadcastFIFO.isEmpty())
        {
            localBroadcastFIFOLatch = new CountDownLatch(1);//lock the local latch
            return localBroadcastFIFO.removeFirst();
        }
        else
        {
            return "";
        }
    }

    public void setInUse(boolean inUse)
    {
        this.inUse = inUse;
    }

    public boolean isInUse()
    {
        return inUse;
    }

    /**
     * Opens port for use
     *
     * @param host
     * @param port
     * @return Success
     */
    public void setSocket(Socket socket)
    {
        this.socketMain = socket;

    }

    public Socket getSocket()
    {
        return socketMain;
    }

    public void setupThreads(ReadThread r, WriteThread w)
    {
        this.r = r;
        this.w = w;        
    }

    public void shutdown()
    {
        if (r != null)
        {
            r.kill();
        }
        if (w != null)
        {
            w.kill();
        }
        try
        {
            if (socketMain != null)
            {
                socketMain.close();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(ConnectionObject.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setInUse(false);
        localBroadcastFIFOLatch = new CountDownLatch(1);//lock the local latch        
        localBroadcastFIFO.clear();
    }

}
