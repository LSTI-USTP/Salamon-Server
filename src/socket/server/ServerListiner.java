/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import socket.connect.RequestServer;
import socket.modelo.Transfer;
import socket.modelo.Transfer.Intent;
import socket.modelo.TransferConverter;

import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;


/**
 *
 * @author Servidor
 */
public class ServerListiner 
{
    private static int PORTA = 12345;
    private HashMap<String, ClienteService> map;
    private ClienteService clienteServerSMS;
    
    public void start()
    {
    	    try(ServerSocket server = new ServerSocket(PORTA))
            {
	            this.map = new HashMap<>();
	            Socket socketUser;
	            
	            /**
	             * Enquanto o servidor estiver no ar
	             */
	            while(true)
	            {
	                /**
	                 * Nova requisao recebida
	                 */
	                socketUser = server.accept();
	                if(socketUser!=null)
	                {
	                    System.out.println("NEW SOCKET INTENT :"+socketUser.getInetAddress().getHostAddress());
	                    Runnable request = null;
	                    request = new ClienteService(socketUser);
	                   
	                    if(request != null)
	                    {
	                    	Thread th = new  Thread(request);
	                        th.start();
	                    }
	                }
	                else
	                {
	                	System.out.println(socketUser);
	                }
	            }
	        } catch (IOException e) 
	        {
				e.printStackTrace();
			}
    }

    
    
    
    public  class ClienteService implements Runnable
    {
        private ObjectOutputStream outData;
        private ObjectInputStream inData;
        private String connectionMac;
        private Socket userSocket;
        XStream xstren;
        private Transfer data;
        private ArrayList<Transfer> listTransfer;

        
        private ClienteService(Socket userSocket)
        {
            try 
            {
            	System.out.println(userSocket);
                this.xstren = TransferConverter.createStrem();
                this.listTransfer = new ArrayList<>();
                this.outData = new ObjectOutputStream(userSocket.getOutputStream());
                this.inData = new ObjectInputStream(userSocket.getInputStream());
                this.userSocket = userSocket;
                
            } catch (IOException ex) 
            {
            	ex.printStackTrace();
            }
        } 

        public ArrayList<Transfer> getListTransfer() {
            return listTransfer;
        }
        

        private void lerData(String xml) 
        {
            try 
            {
                this.data = (Transfer) xstren.fromXML(xml);
                try
            	{
            		System.out.println("SERVER-> SENDED XML AT: "+data.getListMaps().get(0).get("TRANSFER_TIMESTAMP"));
            		System.out.println("SERVER-> RECIVED XML AT: "+Server.getCurrentTimeStamp());
            	}
            	catch(Exception ex)
            	{
            		
            	}
                
                System.out.println("Recebeu XML");
                System.out.println(xml);
                this.listTransfer.add(data);
                this.treatRequest();
                this.resposeRequest();
//                this.disconnect();
            } catch (Exception ex)
            {Logger.getLogger(ServerListiner.class.getName()).log(Level.SEVERE, null, ex);}
        }
        
        private void disconnect ()
        {
            try
            {
                this.inData.close();
                this.outData.close();
                this.userSocket.close();
            }
            catch (IOException ex) 
            {Logger.getLogger(ServerListiner.class.getName()).log(Level.SEVERE, null, ex);}
        }
        
        private void resposeRequest()
        {
            try
            {
                String xml = xstren.toXML(data);
                if(xml!=null)
               {
                	System.out.println("XML LENGTH "+xml.length());
                    this.outData.writeObject(xml);
                    System.out.println("ENVIOU XML");
                    System.out.println(xml);
                    
                }
            } catch (IOException ex)
            {Logger.getLogger(ServerListiner.class.getName()).log(Level.SEVERE, null, ex);}
        }

        private void treatRequest() 
        {
            if (data.getIntent() == Transfer.Intent.REG ) 
            {
                switch(data.getType())
                {
                    case 1001:
                        System.out.println("REQUISICAO DE NOVO DESPOSITIVO PARA O SISTEMA");
                        RequestServer.regDispositivo(data);
                        break;
                    case 1002: 
                    	 System.out.println("\nAUTENTICAR AGENTE NO SISTEMA");
                    	 RequestServer.logar(this.data);
                    	break;
                    case 1003:
                    	Transfer transfer = RequestServer.regFiscalizacao (this.data);
                    	if(transfer != null)
                    		ServerListiner.this.sendSms(transfer);
                    	break;
                    case 1004:
                    	break;
                    	
                }
            }
            else if(data.getIntent() == Transfer.Intent.VALIDATE)
            {
                switch(data.getType())
                {
                    case 7001:
                        System.out.println("\nLOGIN ADM IN DEVICE");
                        RequestServer.loginAdmAgente(data);
                        break;
                    case 7002:
                        System.out.println("\nREVALIDATION STATE");
                        RequestServer.revalidaSateRegistrationDivece(this.data);
                        break;
                    case 7003:
                    	RequestServer.ativarAgente(this.data);
                    	break;
                    case 7004:
                    	RequestServer.alterSenha(this.data);
                    	break;
                    case 7005:
                    	RequestServer.verEquipasTerminadas(null);
                    	break;
                    
                }
                
            }
            else if(this.data.getIntent() == Transfer.Intent.GET)
            {
            	switch(data.getType())
                {
            		case 5001:
            			System.out.println("\nCARREGANDO ESTADO DE CONDUTOR");
            			RequestServer.loadEstadoCondutor(this.data);
            			break;	
            		case 5002:
             			System.out.println("\nCARREGAMENTO DE CATEG�RIAS DE VEICULO");
            			RequestServer.loadCategoriaVeiculo(this.data);
            			break;
            		case 5003:
            			System.out.println("\nCARREGAMENTO DAS LISTAS DE INFRA��O");
            			RequestServer.loadListInfracao(this.data);
            			break;
            		case 5004:
                    	RequestServer.loadMenu(this.data);
                    	break;
            		case 5005:
            			RequestServer.getEstadoCarta(this.data);
            			break;
            		case 5006:
            			RequestServer.getEstadoMatricula(this.data);
            			break;
                }
            }
            else if(this.data.getIntent() == Intent.CONNECT)
            {
            	if(this.data.getType() ==9001)
            	{
            		System.out.println("NEW CONNEXAO");
                	if(map.containsKey(data.getSender()))
                		map.put(data.getSender(), this);
                	else map.put(data.getSender(), this);
                	this.data.setMessage("true");
            	}
            	else if(this.data.getType() == 9002)
            	{
            		clienteServerSMS = this;
            		System.out.println("CLIENTE SERVER SMS CONNECTD");
            	}
            }
            if(this.data.getIntent() != Intent.CONNECT)
            	this.data.setIntent(Transfer.Intent.RESPONSE);
        }
      

        @Override
        public void run() 
        {
            try 
            {
                String xml="";
                while ((xml= (String) this.inData.readObject()) != null)
                {
                	
                	this.lerData(xml);
                }
            } catch (Exception ex) 
            {
            	System.out.println("ERROR IN CONNECTION");
                try 
                {
                	System.out.println("AUTO DISCONNECTING SERVICE");
                    ServerListiner.this.map.remove(connectionMac);
                    disconnect();
                } catch (Exception ex1) 
                {
                }
            }
        }

		public void sendTransfer(Transfer transfer) 
		{

            try
            {
                String xml = xstren.toXML(transfer);
                if(xml!=null)
               {
                	System.out.println("XML LENGTH "+xml.length());
                    this.outData.writeObject(xml);
                    System.out.println("ENVIOU XML");
                    System.out.println(xml);
                    
                }
            } catch (IOException ex)
            {Logger.getLogger(ServerListiner.class.getName()).log(Level.SEVERE, null, ex);}
        
			
		}
    }

    public HashMap<String, ClienteService> getConnectClients() 
	{
    	HashMap<String, ClienteService> map = new HashMap<String, ServerListiner.ClienteService>();
    	
		return map;
	}
	
	public void sendSms(Transfer transfer2)
	{
		System.out.println("Enviando sms...");
		if(clienteServerSMS != null && clienteServerSMS.userSocket != null && clienteServerSMS.userSocket.isConnected())
		{
			try {
				transfer2.setEspera("------");
				transfer2.setMessage("SMS");
				clienteServerSMS.data = transfer2;
				clienteServerSMS.resposeRequest();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Impossivel enviar sms "+e.getMessage());
				e.printStackTrace();
			}
		}
		else System.out.println("SMS NOT SENT - SERVER SMS NOT FOUND");
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		
	}
    
}
