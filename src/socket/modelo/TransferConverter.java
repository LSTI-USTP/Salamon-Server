/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


/**
 *
 * @author AhmedJorge
 */
public class TransferConverter implements Converter{

    private static final String OBJ_NAME = "Content", OBJ_SENDER= "Sender",
            OBJ_RECIVER="Reciver", OBJ_MESSAGE="Message", OBJ_DATAS="Datas", OBJ_ITEM="Item", OBJ_TYPE="type",
            OBJ_INTENT="intent",OBJ_KEY="key", OBJ_SIZE="size", OBJ_GRUPODATA="Grupo-Datas",
            OBJ_ESPERA="Wait";

    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext mc) {
    
       Transfer transfer = (Transfer) value;
        
       writer.addAttribute(OBJ_TYPE, transfer.getType()+"");
       if(transfer.getIntent() == null)
	   {
	       throw new RuntimeException("Invalid Transfer - Falta Intent");
	   }
       
       writer.addAttribute(OBJ_INTENT, transfer.getIntent().name());
       writer.startNode(OBJ_SENDER);
       writer.setValue(transfer.getSender());
       writer.endNode();
       
       writer.startNode(OBJ_RECIVER);
       writer.setValue(transfer.getReciver());
       writer.endNode();
       
       writer.startNode(OBJ_ESPERA);
       writer.setValue(transfer.getEspera());
       writer.endNode();
       
       writer.startNode(OBJ_MESSAGE);
       writer.setValue(transfer.getMessage());
       writer.endNode();
        
        ArrayList<HashMap<String, String>> map = transfer.getListMaps();
        
        writer.startNode(OBJ_GRUPODATA);
        writer.addAttribute(OBJ_SIZE, map.size()+"");
        for (HashMap<String, String> datas : map) 
        {
            writer.startNode(OBJ_DATAS);
            writer.addAttribute(OBJ_SIZE, datas.size()+"");
            
            for (Map.Entry<String, String> item :datas.entrySet())
            {
               writer.startNode(OBJ_ITEM);
               writer.addAttribute(OBJ_KEY, item.getKey()+"");
               writer.setValue(item.getValue()+"");
               writer.endNode();
            }
            
            writer.endNode();
        }
        
        writer.endNode();
        writer.close();
    }


    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        Transfer transfer = new  Transfer();
        
        String tag,value,keyName, size , type, intent;
        int i=0;
        
        //Pegar os atributos geral do XML
        tag = reader.getNodeName();
        type = reader.getAttribute(OBJ_TYPE);
        intent = reader.getAttribute(OBJ_INTENT);
        if(tag.equals(OBJ_NAME)&&type!=null&&intent!=null)
        {
            transfer.setType(Integer.valueOf(type));
            transfer.setIntent(Transfer.Intent.valueOf(intent));
        }
        else throw new RuntimeException("XML invalido!");
        
        //Ler O sender
        reader.moveDown();
        tag = reader.getNodeName();
        value= reader.getValue();
        if(tag.equals(OBJ_SENDER))
            transfer.setSender(value);
        else throw new RuntimeException("XML invalido!");
        reader.moveUp();
        
        //Pegar o sender
        reader.moveDown();
        tag = reader.getNodeName();
        value= reader.getValue();
        if(tag.equals(OBJ_RECIVER))
            transfer.setReciver(value);
        else throw new RuntimeException("XML invalido!");
        reader.moveUp();
        
        reader.moveDown();
        tag = reader.getNodeName();
        value= reader.getValue();
        if(tag.equals(OBJ_ESPERA)) transfer.setEspera(value);
        	else throw new RuntimeException("XML invalido!");
     reader.moveUp();
        
        //Lendo a mensagem...
        reader.moveDown();
        tag = reader.getNodeName();
        value= reader.getValue();
        if(tag.equals(OBJ_MESSAGE)) transfer.setMessage(value);
        else throw new RuntimeException("XML invalido!");
        reader.moveUp();
        
        //Lendo o grupo geral e os seus atribtos
        reader.moveDown();
        tag = reader.getNodeName();
        size = reader.getAttribute(OBJ_SIZE);
        if(tag!=null&&tag.equals(OBJ_GRUPODATA))
        {
        	//Lendo os dados dos HasMahp do grupo
            while (reader.hasMoreChildren()) 
            {
            		//Lendo as datas
                    reader.moveDown();
                    tag = reader.getNodeName();
                    if (tag != null)
                    {
                        if(tag.equals(OBJ_DATAS))
                        {
                        	//Lendo os datos internos da data
                            transfer.getListMaps().add(readerMaps(reader));
                            i++;
                        }
                        else
	                	{
        
	                	    throw new RuntimeException("XML Invalido! Tag não esperada no momento. Taga esperada = "+OBJ_DATAS+" | Tag recebida = "+tag);
	                	}
                        
                    }
                    reader.moveUp();
            }
         }
         else
            throw new RuntimeException("XML invalido!");
        
        if(i!=Integer.valueOf(size))
                throw new RuntimeException("XML invalido!");
        
        reader.moveUp();
        return transfer;
    }


    public boolean canConvert(Class type) {
        return type.equals(Transfer.class);
    }
    
    public static XStream createStrem()
    {
        XStream stream = new XStream();
        stream.registerConverter(new TransferConverter());
        stream.alias(OBJ_NAME, Transfer.class);
        return stream;
    }
    
   private HashMap<String,String> readerMaps(HierarchicalStreamReader reader)
   {
       boolean validator;
       String tag,value,keyName, size;
       int i=0;
       HashMap<String,String> map = new HashMap();
       size = reader.getAttribute(OBJ_SIZE);
       while (reader.hasMoreChildren()) {
            validator = false;
            reader.moveDown();
            tag = reader.getNodeName();
            keyName = reader.getAttribute(OBJ_KEY);
            value = reader.getValue();
            if (tag != null && keyName != null) 
            {
                if (tag.equals(OBJ_ITEM)) 
                {
                    validator = true;
                    i++;
                }
            }            
            if(validator)
                map.put(keyName, value);
            reader.moveUp();
            
        }
        
        if(i!=Integer.valueOf(size))
                throw new RuntimeException("XML invalido!");
        
        return map;
   }
}
