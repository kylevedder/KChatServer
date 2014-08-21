/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchatserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kyle
 */
public class NewConnectionsThread extends Thread
{

    private volatile boolean isRunning = true;

    static ServerSocket socketServer = null;
    static Socket socketMain = null;
    static BufferedReader inFromClient = null;
    static DataOutputStream outToClient = null;
    static int port = 0;

    public NewConnectionsThread(int port)
    {
        this.port = port;
    }

    @Override
    public void run()
    {
        try
        {
            if(Main.DEBUG)System.out.println("Connection Acceptor Thread Started");
            //setup socket
            if(Main.DEBUG)System.out.println("Server Sockets starting...");
            socketServer = new ServerSocket(port);
            if(Main.DEBUG)System.out.println("Server Sockets started");
            //recreates conn after every time
            while (isRunning)
            {

                setupSocket();//this is blocking
                if (isRunning)
                {
                    //'\n' is SUPER IMPORTANT
                    ConnectionObject conn = getAvalibleConnectionObj();
                    conn.setSocket(socketMain);
                    if(Main.DEBUG)System.out.println("opening socket");
                    openSocket(conn);
                    if(Main.DEBUG)System.out.println("socket open");
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
        catch (IOException ex)
        {
            Logger.getLogger(NewConnectionsThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Kills the thread
     */
    public void kill()
    {
        isRunning = false;

        try
        {
            if (inFromClient != null)
            {
                inFromClient.close();
            }
            if (outToClient != null)
            {
                outToClient.close();
            }
            if (socketMain != null)
            {
                socketMain.close();
            }
            if (socketServer != null)
            {
                socketServer.close();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(NewConnectionsThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sets up the socket to recieve requests
     */
    private void setupSocket()
    {
        if (socketServer != null)
        {
            try
            {                
                if(Main.DEBUG)System.out.println("Waiting for client to accept connection...");
                try
                {
                    socketMain = socketServer.accept();
                }
                catch (java.net.SocketException ex)
                {
                    if(Main.DEBUG)System.err.println("SOCKET CLOSED UNEXPECTEDLY");
                    if (socketMain != null)
                    {
                        socketMain.close();
                    }                    
                }
                if (isRunning)
                {
                    if(Main.DEBUG)System.out.println("Client connection accepted");
                    inFromClient = new BufferedReader(new InputStreamReader(socketMain.getInputStream()));
                    outToClient = new DataOutputStream(socketMain.getOutputStream());
                    if(Main.DEBUG)System.out.println("In and out setup");
                }

            }
            catch (IOException ex)
            {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Gets an avalible connection object for the client to use
     *
     * @return
     */
    private ConnectionObject getAvalibleConnectionObj()
    {
        for (ConnectionObject p : Main.connectionPool)
        {
            if (!p.isInUse())
            {
                p.setInUse(true);

                return p;
            }
        }
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        //recursive call
        return getAvalibleConnectionObj();
    }

    /**
     * Starts up the threading process for the new client comms
     *
     * @param p
     */
    private void openSocket(ConnectionObject p)
    {

        ReadThread r;
        WriteThread w;
        try
        {
            if(Main.DEBUG)System.out.println("opening read thread");
            r = new ReadThread(new BufferedReader(new InputStreamReader(p.getSocket().getInputStream())), p);
            r.start();
            if(Main.DEBUG)System.out.println("opening write thread");
            w = new WriteThread(new DataOutputStream(p.getSocket().getOutputStream()), p);
            w.start();
            p.setupThreads(r, w);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
