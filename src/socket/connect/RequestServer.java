/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket.connect;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import socket.modelo.Transfer;
import socket.modelo.Transfer.Intent;
import socket.server.ServerListiner;
import socket.server.ServerListiner.ClienteService;
import dbutil.dba.DUCallPostgres;
import dbutil.dba.DUResultSet;

/**
 *
 * @author AhmedJorge
 */
public class RequestServer
{
    public static DUCallPostgres EXEC;
    static String MARCA = "Marca".toUpperCase(), MODELO = "Modelo".toUpperCase(), VERSAO = "Versao".toUpperCase(), POLEGADA = "POLEGADA".toUpperCase(), MAC = "MAC".toUpperCase();
    
    public static HashMap<String, String> loadCarta(String message)
    {
        HashMap<String, String> map = new HashMap<>();
        ResultSet rs = DUCallPostgres.getStaticInstace().select("VER_CARTA WHERE ID_CARTA = ?", "*", message);
        ArrayList<HashMap<String, Object>> list = DUResultSet.toArrayMap(rs);
        
        for(Map.Entry<String, Object> s: list.get(0).entrySet() )
        {
            map.put(s.getKey(), (s.getValue() != null)? s.getValue().toString(): "");
        }
        return map;
    }
    
    public static void regDispositivo(Transfer t) 
    {
        HashMap<String, String> map = new HashMap<>();
        if(t.getListMaps().size()>0)
            map = t.getListMaps().get(0);
        else return;
        ResultSet rs = EXEC.selectFromFunction("adm.func_reg_req_despositivo", "*",
                Integer.parseInt(t.getSender()),
                map.get(MARCA),
                map.get(MODELO),
                map.get(VERSAO),
                Float.valueOf(map.get(POLEGADA)),
                map.get(MAC));
        HashMap<String, String> mapRead = resultListString(rs).get(0);
        t.setMessage(mapRead.remove("DIVICE.RESULT"));
        t.getListMaps().set(0, mapRead);
        t.redirect();
    }
    /**
     * Carrega todos os menus do utilizador
     * @param data
     */
    public static void loadMenu(Transfer data) 
    {
		// TODO Auto-generated method stub
    	int valido = 0;
    	int idUser = Integer.valueOf(data.getListMaps().get(0).get("ID UTILIZADOR"));
    	ResultSet rs = EXEC.selectFromFunction("adm.funct_load_menu_user", "*",idUser);
    	 ArrayList<HashMap<String, String>> menus = resultListString(rs);
    	 if(menus.size() > 0)
         {
    		 for(HashMap<String, String> hashMap : menus)
    		 {
    			 if(hashMap.get("ID").equals("10301"))
    			 {
    				 valido = 1;
    				 break;
    			 }
    		 }
         }
    	 
    	if(valido==1)
    		data.setMessage("true");
    	else data.setMessage("false");
    	
         data.redirect();	
	}
    
    /**
     * Login de um administrador no despositivo
     * @param transfer
     */
    public static void loginAdmAgente(Transfer transfer)
    {
        String accessName = transfer.getListMaps().get(0).get("ACCESSNAME");
        String pwd = transfer.getListMaps().get(0).get("PWD");
        transfer.getListMaps().clear();
        ResultSet rs = EXEC.selectFromFunction("fisca.func_login_adm", "*", accessName, pwd);
        ArrayList<HashMap<String, String>> list = resultListString(rs);
        if(list.size() > 0)
        {
            transfer.getListMaps().add(list.get(0));
            transfer.setMessage("true");
        }
        else transfer.setMessage("false");
        transfer.redirect();
    }
    
    private static HashMap<String, String> mapObjectoToString(HashMap<String, Object> map)
    {
        HashMap<String, String> mapString = new  HashMap<>();
        for (Map.Entry<String, Object> iten: map.entrySet())
        	 mapString.put(iten.getKey(), (iten.getValue() != null)?iten.getValue().toString():"");
       return  mapString;
    }
    
    private static ArrayList<HashMap<String, String>> listMapObjectToString (ArrayList<HashMap<String, Object>> listMapObjct)
    {
        ArrayList<HashMap<String, String>> listStringMap = new ArrayList<>();
        for(HashMap<String, Object> map: listMapObjct)
            listStringMap.add(mapObjectoToString(map));
        return listStringMap;
    }
    
    private static ArrayList<HashMap<String, String>> resultListString (ResultSet rs)
    {
        ArrayList<HashMap<String, Object>> listMapObjct = DUResultSet.toArrayMap(rs);
        return listMapObjectToString(listMapObjct);
    }

    public static void revalidaSateRegistrationDivece(Transfer transfer) 
    {
        int idUser = Integer.parseInt(transfer.getListMaps().get(0).get("USER"));
        String mac = transfer.getListMaps().get(0).get("MAC");
        ResultSet rs = EXEC.selectFromFunction("adm.func_load_state_divice", "*",
                idUser,
                mac
                );
        ArrayList<HashMap<String, String>> list = resultListString(rs);
        HashMap<String, String> map = list.get(0);
        transfer.setMessage(map.remove("DIVICE.RESULT"));
        transfer.getListMaps().set(0, map);
        transfer.redirect();
    }

    /**
     * Forma de carregar todas as infrações ativas
     * @param data
     */
	public static void loadListInfracao(Transfer data) 
	{
		ResultSet rs = EXEC.select("fisca.ver_infracao_ativa","*");
        ArrayList<HashMap<String, String>> list = resultListString(rs);
        data.getListMaps().clear();
        for(HashMap<String, String> mapa: list)
        	data.getListMaps().add(mapa);
        
        data.redirect();
        data.setMessage("Enviando lista de infrações");
		
	}


	public static void loadEstadoCondutor(Transfer data) 
	{
		// TODO Auto-generated method stub
		ResultSet rs = EXEC.select("fisca.ver_esta_condutor", "*");
		ArrayList<HashMap<String, String>> list = resultListString(rs);
		data.getListMaps().clear();
		for(HashMap<String,String> mapa:list)
			data.getListMaps().add(mapa);
		
		data.redirect();
		data.setMessage("Enviando estado de condutor");
	}

	public static void loadCategoriaVeiculo(Transfer data)
	{
		// TODO Auto-generated method stub
		ResultSet rs = EXEC.select("fisca.ver_categoria_veiculo", "*");
		ArrayList<HashMap<String, String>> list = resultListString(rs);
		data.getListMaps().clear();
		for(HashMap<String,String> mapa:list)
			data.getListMaps().add(mapa);
		
		data.redirect();
		data.setMessage("Enviando categórias de veiculo");
		
	}

	public static void logar(Transfer transfer) 
	{
		// TODO Auto-generated method stub
		String accessName = transfer.getListMaps().get(0).get("ACCESSNAME");
        String pwd = transfer.getListMaps().get(0).get("PWD");
        String deviceName = transfer.getListMaps().get(0).get("DEVICE NAME");
        String mac = transfer.getListMaps().get(0).get("MAC");
        transfer.getListMaps().clear();
        ResultSet rs = EXEC.selectFromFunction("fisca.funct_login", "*", accessName, pwd,deviceName,mac);
        ArrayList<HashMap<String, String>> list = resultListString(rs);
        if(list.size() > 0)
        {
            transfer.getListMaps().add(list.get(0));
            transfer.setMessage("true");
        }
        else
        {
        	transfer.setMessage("false");
        }
 	    transfer.redirect();
	}


	public static Transfer regFiscalizacao(Transfer data)
	{
		HashMap<String, String> map = data.getListMaps().get(0);
		int idUser = Integer.parseInt(map.get("USER.ID"));
		int idAlocacao = Integer.parseInt(map.get("USER.ALOCACAO"));
		
		String matricula = map.get("VEICULO.MATRICULA");
		String numCarta = map.get("CONDUTOR.CARTA");
		String nomeCondutor = map.get("CONDUTOR.NOME").toString();
	    String dataRegistro = map.get("CLIENTE.TIME.SEND");
		int condutorState = Integer.parseInt(map.get("CONDUTOR.STATE"));
		int condutorIncompatil = Integer.parseInt(map.get("CONDUTOR.COMPATIL"));
		int veiculoCategoria = Integer.parseInt(map.get("VEICULO.CATEGORIA"));
			
		double localx = Double.parseDouble(map.get("LOCAL.LATITUDE"));
		double localy = Double.parseDouble(map.get("LOCAL.LONGITUDE"));
		String localLocal = (map.get("LOCAL.LOCAL")==null || map.get("LOCAL.LOCAL").equals("") || map.get("LOCAL.LOCAL").equals("NULL"))? "" : map.get("LOCAL.LOCAL");
		
		int aprensaoVeiculo = Integer.parseInt(map.get("PRENDE.VEICULO"));
		int aprensaoCarta = Integer.parseInt(map.get("PRENDE.CARTA"));
		int aprensaoLivrete = Integer.parseInt(map.get("PRENDE.LIVRETE"));
		int aprensaoCondutor = Integer.parseInt(map.get("PRENDE.CONDUTOR"));
		int existCarta = Integer.parseInt(map.get("EXIST.CARTA"));
		int existLivrete = Integer.parseInt(map.get("EXIST.LIVRETE"));
		
		
		int idMatricula = funcRegGetVeiculo (idUser, matricula);
		Integer idCarta = funcRegGetCarta(idUser, numCarta);
		
		/* fisca.func_reg_fiscalizacao ->
		 * id_matricula integer,
		    id_carta integer,
		    aloca_id integer,
		    localx character varying,
		    localy character varying,
		    zona character varying,
		    prendveiculo integer,
		    prendlivrete integer,
		    prendcarta integer,
		    existecarta integer,
		    existelivrete integer,
		    estadocondutor integer,
		    categoriaveiculo integer,
		    incompatiblidadecarta integer)
		 */
		Object fiscalizacao = EXEC.callFunction("fisca.func_reg_fiscalizacao", Types.VARCHAR,
				idMatricula,
				idCarta,
				idAlocacao,
				localx,
				localy,
				localLocal,
				aprensaoVeiculo,
				aprensaoLivrete,
				aprensaoCarta,
				existCarta,
				existLivrete,
				condutorState,
				veiculoCategoria,
				condutorIncompatil,
				nomeCondutor,
				aprensaoCondutor);
	    String[] campos = fiscalizacao.toString().split(";");
	    
	    if(campos[0].equals("true"))
	    {
	    	int idInfracao;
	    	int idFiscalizacao = Integer.parseInt(campos[1]);
	    	Integer nivelGravidade;
	    	int countInfracao = 0;
	    	for (int i = 1; i < data.getListMaps().size(); i++)
	    	{
	    		map = data.getListMaps().get(i);
	    		countInfracao++;
	    		idInfracao = Integer.parseInt(map.get("INFRACAO.ID"));
	    		if(map.get("INFRACAO.GRAVIDADE") != null && map.get("INFRACAO.GRAVIDADE").length()>0 && !map.get("INFRACAO.GRAVIDADE").equals("null"))
	    			nivelGravidade = Integer.parseInt(map.get("INFRACAO.GRAVIDADE"));
	    		else nivelGravidade = null;
	    		
	    		/** fisca.func_reg_detecta ->
				    id_ficalizacao integer,
				    id_infracao integer,
				    id_gravidade integer,
				    id_user integer)
	    		 */
	    		Object dectect = EXEC.callFunction("fisca.func_reg_detecta", Types.VARCHAR,
	    				idFiscalizacao,
	    				idInfracao,
	    				nivelGravidade,
	    				idUser);
	    	}
	    	/**
	    	 * fisca.funct_end_fiscalizacao
				   idUser INTEGER,
				   idFiscalizacao INTEGER
	    	 */
	    	
	    	ResultSet rs = EXEC.selectFromFunction("fisca.funct_end_fiscalizacao", "*", 
	    			idUser,
	    			idFiscalizacao);
	    	data.getListMaps().clear();
	    	data.redirect();
	    	data.setMessage("true");
	    	HashMap<String, String> mapSumary = resultListString(rs).get(0);
	    	data.getListMaps().add(mapSumary);
	    	Object contacto = EXEC.callFunction("func_get_contacto", Types.VARCHAR, matricula);
	    	System.out.println("Contacto retornado "+contacto);
	    	if(contacto != null)
	    	{
	    		HashMap<String, String> hashMap = new HashMap<String, String>();
	    		Transfer sms = new Transfer("SERVER.APP", "SERVER.SMS", Intent.SEND_SMS);
	    		
	    		hashMap.put("MATRICULA", matricula);
	    		if(countInfracao == 0)
	    			hashMap.put("COUNT.INFRACAO", "Nenhuma");
	    		else hashMap.put("COUNT.INFRACAO", countInfracao+"");
	    			
	    		hashMap.put("MULTA", mapSumary.get("VALOR"));
	    		hashMap.put("LOCAL", localLocal);
	    		hashMap.put("COD.FISCALIZACACO", mapSumary.get("CODIGO"));
	    		hashMap.put("CONTACTO", contacto.toString());
	    		
	    		sms.getListMaps().add(hashMap);
	    		return sms;		
	    	}
	    }
	    return null;
	}

	private static int funcRegGetVeiculo(int idUser, String matricula) 
	{
		Object idVeiculo = EXEC.callFunction("fisca.func_reg_get_veiculo", Types.VARCHAR, idUser, matricula);
		return Integer.parseInt(idVeiculo.toString());
	}
	
	private static Integer funcRegGetCarta(int idUser, String numCrata) 
	{
		String bi = null;
		if(numCrata == null || numCrata.length() == 0) return null;
		Object idCarta = EXEC.callFunction("fisca.func_reg_get_carta", Types.VARCHAR, idUser, numCrata, bi);
		return Integer.parseInt(idCarta.toString());
	}

	public static void ativarUtilizador(Transfer data) {
		// TODO Auto-generated method stub
		
	}

	public static void ativarAgente(Transfer transfer)
	{
		// TODO Auto-generated method stub
		int idUser = Integer.valueOf(transfer.getListMaps().get(0).get("ID USER"));
        String pwd = transfer.getListMaps().get(0).get("PWD");
        transfer.getListMaps().clear();
        Object result = EXEC.callFunction("adm.func_agente_activar_pwd",Types.VARCHAR,
        		idUser,
        		pwd
         );
        HashMap<String, String> map = new HashMap<String, String>();
        if(result != null)
        {
	    	  if(result.toString().split(";")[0].equals("true"))
	    		  map.put("RESULTADO", "ativado");
	    	  else
	    		  map.put("RESULTADO", result.toString().split(";")[1]);
        }
        transfer.getListMaps().add(map);
 	    transfer.redirect();
	}
	
	public static void getEstadoCarta(Transfer transfer)
	{
		int idUser = Integer.valueOf(transfer.getListMaps().get(0).get("ID USER"));
		String numCarta = transfer.getListMaps().get(0).get("CARTA");
		transfer.getListMaps().clear();
		ResultSet rs = EXEC.selectFromFunction("fisca.funct_load_state_carta", "*",idUser,numCarta);
		ArrayList<HashMap<String, String>> arrayList = resultListString(rs);
   	 	if(arrayList.size() > 0)
        {
   	 		transfer.setMessage("true");
   	 		transfer.getListMaps().add(arrayList.get(0));
        }
   	 	else transfer.setMessage("false");
   	 	
   	 	transfer.redirect();
	}

	public static void getEstadoMatricula(Transfer transfer) 
	{
		int idUser = Integer.valueOf(transfer.getListMaps().get(0).get("ID USER"));
		String numMatricula = transfer.getListMaps().get(0).get("MATRICULA");
		transfer.getListMaps().clear();
		ResultSet rs = EXEC.selectFromFunction("fisca.funct_load_state_matricula", "*",idUser,numMatricula);
		ArrayList<HashMap<String, String>> arrayList = resultListString(rs);

   	 	if(arrayList.size() > 0)
        {
   	 		transfer.setMessage("true");
   	 		transfer.getListMaps().add(arrayList.get(0));
        }
   	 	else transfer.setMessage("false");
   	 	
   	 	transfer.redirect();	
	}

	public static void alterSenha(Transfer data) 
	{
		
		
		
	}

	public static void verEquipasTerminadas(ServerListiner listiner)
	{
		// TODO Auto-generated method stub
		String mac;
		ResultSet rs = EXEC.select("adm.ver_equipamentos_terminado", "*");
		ArrayList<HashMap<String, String>> list = resultListString(rs);
		
		for(HashMap<String,String> mapa:list)
		{
		    mac = mapa.get("DISPOSITIVO");
		   HashMap<String, ClienteService> clients = listiner.getConnectClients();
		   
		   
		}
			
		

		
		
	}	
}
