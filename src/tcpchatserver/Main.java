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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kyle
 */
public class Main
{

    static final boolean DEBUG = false;

    static String stringToBroadcast = "";

    static volatile ConnectionObject[] connectionPool = new ConnectionObject[100];
    static volatile LinkedList<String> publicBroadcastFIFO = new LinkedList<String>();

    static volatile CountDownLatch stickInLocalFIFOLatch = new CountDownLatch(1);

    static NewConnectionsThread newConnThread = null;
    static FIFOLoaderThread fifoLoader = null;
    static UI ui = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length == 1)
        {
            if(Main.DEBUG)System.out.println("Spawing in headless mode with port " + args[0]);
            startUpServer(Integer.parseInt(args[0]));
        }
        else
        {
            if(Main.DEBUG)System.out.println("Spawing GUI");
            ui = new UI();
            ui.setVisible(true);
        }

    }

    public static void startUpServer(int port)
    {
        if(Main.DEBUG)System.out.println("SERVER STARTING");
        //sets up the array of connections avalible
        setupConnectionPool();

        //start the new connection handler thread
        newConnThread = new NewConnectionsThread(port);
        newConnThread.start();
        fifoLoader = new FIFOLoaderThread();
        stickInLocalFIFOLatch = new CountDownLatch(1);
        fifoLoader.start();

    }

    public static void stopServer()
    {
        if(Main.DEBUG)System.out.println("COMPLETE HALT");

        if (fifoLoader != null)
        {
            fifoLoader.kill();
        }
        if (newConnThread != null)
        {
            newConnThread.kill();
        }

        for (ConnectionObject conn : Main.connectionPool)
        {
            if (conn != null)
            {
                conn.shutdown();
            }
        }
        ui.lockInput(false);
        publicBroadcastFIFO.clear();

        if(Main.DEBUG)System.out.println("HALT COMPLETED");
    }

    public synchronized static void addStringToBroadcast(String s)
    {
        publicBroadcastFIFO.add(s);
        stickInLocalFIFOLatch.countDown();//unlock fifo for reading
    }

    private static void setupConnectionPool()
    {
        for (int i = 0; i < connectionPool.length; i++)
        {
            connectionPool[i] = new ConnectionObject(false);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Basic Test Server">
    //private static void basicServer()     {
    //        String clientSentence;
    //        String capitalizedSentence;
    //        try
    //        {
    //            socketServer = new ServerSocket(6789);
    //
    //        }
    //        catch (IOException ex)
    //        {
    //            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    //        }
    //
    //        //infinite loop to restart quit connections
    //        while (true)
    //        {
    //            setupSocket();
    //
    //            innerLoop:
    //            while (socketServer != null)
    //            {
    //                try
    //                {
    //                    clientSentence = inFromClient.readLine();
    //                    System.out.println("Received: " + clientSentence);
    //                    capitalizedSentence = clientSentence + '\n';
    //                    outToClient.writeBytes(capitalizedSentence);
    //                }
    //                catch (IOException ex)
    //                {
    //                    System.out.println("IO Exception");
    //                    break innerLoop;
    //                }
    //            }
    //        }
    //    }
    //</editor-fold>
}
