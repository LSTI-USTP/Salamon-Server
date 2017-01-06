/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Timer;

import socket.connect.RequestServer;
import dbutil.dba.DUCallPostgres;

/**
 *
 * @author Servidor
 */
public class Server 
{
    public static void main(String[] args)
    {
    	System.out.println("SERVER-> START DATE: "+getCurrentTimeStamp());
        DUCallPostgres.newStaticInstance("dba", "agente", "1234", "policia");
        System.err.println(DUCallPostgres.getStaticInstace().testNewConnection());   
        RequestServer.EXEC = DUCallPostgres.getStaticInstace();
        
        ServerListiner listener = new ServerListiner();
        //final ServerBackground backgraoud = new ServerBackground(listener);
        
        Timer timer = new Timer(5000, new ActionListener() 
        {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				//backgraoud.next();
			}
		}) ;
      
        timer.start();
        System.out.println("SERVER-> INIT: "+getCurrentTimeStamp());
        listener.start(); // inicia a conexão com servidor
    }
    
    public static String getCurrentTimeStamp()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateandTime = sdf.format(new Date());
		return currentDateandTime;
	}
    
   
    
}
