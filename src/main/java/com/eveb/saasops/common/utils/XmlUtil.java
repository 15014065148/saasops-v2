package com.eveb.saasops.common.utils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eveb.saasops.api.modules.user.dto.*;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XmlUtil {

	public static AgResDto getAginResult(String result)
	{
		Document doc;
		AgResDto agResDto=new AgResDto();
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			agResDto.setInfo(root.attribute("info").getValue());
			agResDto.setMsg(root.attribute("msg").getValue());
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return agResDto;
	}
	public static PtJackPotResDto ptJackPotResDto(String result)
	{
		Document doc;
		PtJackPotResDto ptJackPotResDto=new PtJackPotResDto();
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			ptJackPotResDto.setAmounts(listNodes(root));
			ptJackPotResDto.setInfo("查询奖池金额成功!");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return ptJackPotResDto;
	}
	   //遍历当前节点下的所有节点  
    public static String listNodes(Element node){  
        //System.out.println("当前节点的名称：" + node.getName());  
        //首先获取当前节点的所有属性节点  
   /*     List<Attribute> list = node.attributes();  
        //遍历属性节点  
        for(Attribute attribute : list){  
            //System.out.println("属性"+attribute.getName() +":" + attribute.getValue());  
        }  */
    	//System.out.println(node.getName());
        //如果当前节点内容不为空，则输出  
        if(!(node.getTextTrim().equals(""))){
        	if(node.getName().equals("amount"))
        		return node.getText();
        }
        //同时迭代当前节点下面的所有子节点  
        //使用递归  
        Iterator<Element> iterator = node.elementIterator();  
        while(iterator.hasNext()){  
            Element e = iterator.next();  
            return listNodes(e);  
        } 
        return "0.00";
    }
    public static void balanceResp(GetBalanceResp resp,String result)
    {
    	Document doc;
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			listbalanceNodes(root,resp);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
    public static void pngResp(GetBalanceResp resp,String result)
    {
    	Document doc;
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			listPngNodes(root,resp);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
	public static void opusSbResp(OpusSbResp resp, String result)
	{
		Document doc;
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			listopusSbNodes(root,resp);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

    public static void t188Resp(GetT188Resp resp,String result)
    {
    	Document doc;
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			list188Nodes(root,resp);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
    public static void mgParseRes(Map<String, String> map,String result)
	{
		Document doc;
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			//System.out.println("当前节点的名称：" + root.getName());
			// 首先获取当前节点的所有属性节点
			List<Attribute> list = root.attributes();
			// 遍历属性节点
			for (Attribute attribute : list) {
				map.put(attribute.getName(), attribute.getValue());
				// System.out.println("属性"+attribute.getName() +":" + attribute.getValue());
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
    
    public static void mgParseBanlceRes(Map<String, Object> map,String result)
	{
		Document doc;
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			//System.out.println("当前节点的名称：" + root.getName());
			// 首先获取当前节点的所有属性节点
			List<Attribute> list = root.attributes();
			// 遍历属性节点
			for (Attribute attribute : list) {
				map.put(attribute.getName(), attribute.getValue());
				// System.out.println("属性"+attribute.getName() +":" + attribute.getValue());
			}
			Iterator<Element> iterator = root.elementIterator();
			Element walletNode = iterator.next();
			 iterator = walletNode.elementIterator();
			while (iterator.hasNext()) {
				Element e = iterator.next();
				List<Attribute> listnode = e.attributes();
				Map<String,String> mapnode=new HashMap<String,String>();
				// 遍历属性节点
				for (Attribute attributeNode : listnode) {
					mapnode.put(attributeNode.getName(), attributeNode.getValue());
				}
				map.put(e.getName(), mapnode);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
    
    

    
    public static void transferResp(TransferResp resp,String result)
    {
    	Document doc;
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			listTransferNodes(root,resp);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
    public static void checkTransferResp(GetTransactionResp resp,String result)
    {
    	Document doc;
		try {
			doc = DocumentHelper.parseText(result);
			Element root = doc.getRootElement();
			listCheckTransferNodes(root,resp);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
    
	public static void listCheckTransferNodes(Element node, GetTransactionResp resp) {
		// 如果当前节点内容不为空，则输出
		if (!(node.getTextTrim().equals(""))) {
			if (node.getName().equals("amountPlayableBonus")) {
				resp.setAmountPlayableBonus(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("amountReal")) {
				resp.setAmountReal(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("amountReleasedBonus")) {
				resp.setAmountReleasedBonus(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("balancePlayableBonus")) {
				resp.setBalancePlayableBonus(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("balanceReal")) {
				resp.setBalanceReal(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("balanceReleasedBonus")) {
				resp.setBalanceReleasedBonus(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("dateTime")) {
				resp.setDateTime(new String(node.getText()));
				return;
			} else if (node.getName().equals("gameId")) {
				resp.setGameId(new String(node.getText()));
				return;
			} else if (node.getName().equals("gameTranId")) {
				resp.setGameTranId(new String(node.getText()));
				return;
			} else if (node.getName().equals("platformCode")) {
				resp.setPlatformCode(new String(node.getText()));
				return;
			} else if (node.getName().equals("platformTranId")) {
				resp.setPlatformTranId(new String(node.getText()));
				return;
			} else if (node.getName().equals("tranType")) {
				resp.setTranType(new String(node.getText()));
				return;
			} else if (node.getName().equals("transactionId")) {
				resp.setTransactionId(new String(node.getText()));
				return;
			}
		}
		// 同时迭代当前节点下面的所有子节点
		// 使用递归
		Iterator<Element> iterator = node.elementIterator();
		while (iterator.hasNext()) {
			Element e = iterator.next();
			listCheckTransferNodes(e, resp);
		}
		return;
	}
    
    
	public static void listTransferNodes(Element node, TransferResp resp) {
		// 如果当前节点内容不为空，则输出
		if (!(node.getTextTrim().equals(""))) {
			if (node.getName().equals("balancePlayableBonus")) {
				resp.setBalancePlayableBonus(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("balanceReal")) {
				resp.setBalanceReal(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("balanceReleasedBonus")) {
				resp.setBalanceReleasedBonus(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("balanceSecondary")) {
				resp.setBalanceSecondary(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("balanceTotal")) {
				resp.setBalanceTotal(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("balanceWithdrawable")) {
				resp.setBalanceWithdrawable(new BigDecimal(node.getText()));
				return;
			}
			
			else if (node.getName().equals("amountPlayableBonus")) {
				resp.getResp().setAmountPlayableBonus(new BigDecimal(node.getText()));
				return;
			}
			else if (node.getName().equals("amountReal")) {
				resp.getResp().setAmountReal(new BigDecimal(node.getText()));
				return;
			}
			else if (node.getName().equals("amountReleasedBonus")) {
				resp.getResp().setAmountReleasedBonus(new BigDecimal(node.getText()));
				return;
			}
			else if (node.getName().equals("balancePlayableBonus")) {
				resp.getResp().setBalancePlayableBonus(new BigDecimal(node.getText()));
				return;
			}
			else if (node.getName().equals("balanceReal")) {
				resp.getResp().setBalanceReal(new BigDecimal(node.getText()));
				return;
			}
			else if (node.getName().equals("balanceReleasedBonus")){
				resp.getResp().setBalanceReleasedBonus(new BigDecimal(node.getText()));
				return;
			}
			else if (node.getName().equals("dateTime")) {
				resp.getResp().setDateTime(node.getText());
				return;
			}
			else if (node.getName().equals("platformCode")) {
				resp.getResp().setPlatformCode(node.getText());;
				return;
			}
			else if (node.getName().equals("platformTranId")) {
				resp.getResp().setPlatformTranId(node.getText());
				return;
			}
			else if (node.getName().equals("tranType")) {
				resp.getResp().setTranType(node.getText());
				return;
			}
			else if (node.getName().equals("transactionId")) {
				resp.getResp().setTransactionId(node.getText());
				return;
			}			
		}
		// 同时迭代当前节点下面的所有子节点
		// 使用递归
		Iterator<Element> iterator = node.elementIterator();
		while (iterator.hasNext()) {
			Element e = iterator.next();
			listTransferNodes(e,resp);
		}
		return;
	}
	public static void list188Nodes(Element node, GetT188Resp resp) {
		// 如果当前节点内容不为空，则输出
		if (!(node.getTextTrim().equals(""))) {
			if (node.getName().equals("ReturnCode")) {
				resp.setReturnCode(node.getText());
				return;
			} else if (node.getName().equals("Description")) {
				resp.setDescription(node.getText());
				return;
			}else if (node.getName().equals("CurrencyCode")) {
				resp.setCurrencyCode(node.getText());
				return;
			}else if (node.getName().equals("Balance")) {
				resp.setBalance(node.getText());
				return;
			}
		}
		// 同时迭代当前节点下面的所有子节点
		// 使用递归
		Iterator<Element> iterator = node.elementIterator();
		while (iterator.hasNext()) {
			Element e = iterator.next();
			list188Nodes(e,resp);
		}
		return;
	}
	public static void listopusSbNodes(Element node, OpusSbResp resp) {
		// 如果当前节点内容不为空，则输出
		//<?xml version="1.0" ?>
		//<check_user_balance><status_code>0</status_code><status_text>SUCCESS</status_text><user_balance>0.0000</user_balance><currency>RMB</currency><datetime>2018-03-22 03:09:28</datetime></check_user_balance>
		if (!(node.getTextTrim().equals(""))) {
			if (node.getName().equals("status_code")) {
				resp.setStatusCode(node.getText());
				return;
			} else if (node.getName().equals("status_text")) {
				resp.setStatusText(String.valueOf(node.getText()));
				return;
			}else if (node.getName().equals("user_balance")||node.getName().equals("balance")) {
				resp.setUserBalance(new BigDecimal(node.getText()));
				return;
			}else if (node.getName().equals("currency")) {
				resp.setCurrency(String.valueOf(node.getText()));
				return;
			}else if (node.getName().equals("is_online")) {
				resp.setIsOnline(Boolean.parseBoolean(node.getText()));
				return;
			}else if (node.getName().equals("status")) {
				resp.setStatus(node.getText());
				return;
			}else if (node.getName().equals("member_status")) {
				resp.setMemberStatus(node.getText());
				return;
			}

		}
		// 同时迭代当前节点下面的所有子节点
		// 使用递归
		Iterator<Element> iterator = node.elementIterator();
		while (iterator.hasNext()) {
			Element e = iterator.next();
			listopusSbNodes(e,resp);
		}
		return;
	}

	public static void listPngNodes(Element node, GetBalanceResp resp) {
		// 如果当前节点内容不为空，则输出
		if (!(node.getTextTrim().equals(""))) {
			if (node.getName().equals("Currency")) {
				resp.setCurrency(node.getText());
				return;
			} else if (node.getName().equals("Real")) {
				resp.setReal(new BigDecimal(node.getText()));
				return;
			}else if (node.getName().equals("Ticket")) {
				resp.setTicket(node.getText());
				return;
			}else if (node.getName().equals("TransactionId")) {
				resp.setTransactionId(node.getText());
				return;
			}
			
		}
		// 同时迭代当前节点下面的所有子节点
		// 使用递归
		Iterator<Element> iterator = node.elementIterator();
		while (iterator.hasNext()) {
			Element e = iterator.next();
			listPngNodes(e,resp);
		}
		return;
	}
	public static void listbalanceNodes(Element node, GetBalanceResp resp) {
		// 如果当前节点内容不为空，则输出
		if (!(node.getTextTrim().equals(""))) {
			if (node.getName().equals("real")) {
				resp.setReal(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("releasedBonus")) {
				resp.setReleasedBonus(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("playableBonus")) {
				resp.setPlayableBonus(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("secondaryBalance")) {
				resp.setSecondaryBalance(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("total")) {
				resp.setTotal(new BigDecimal(node.getText()));
				return;
			} else if (node.getName().equals("withdrawable")) {
				resp.setWithdrawable(new BigDecimal(node.getText()));
				return;
			}
		}
		// 同时迭代当前节点下面的所有子节点
		// 使用递归
		Iterator<Element> iterator = node.elementIterator();
		while (iterator.hasNext()) {
			Element e = iterator.next();
			listbalanceNodes(e,resp);
		}
		return;
	}
	/*public static void main(String args[])
	{
		String aa="<mbrapi-login-resp status=\"0\" timestamp=\"2018-02-21 02:24:37.631 UTC\" token=\"gQfp-4_nvndlMXLg6k1-bW1WiMBlJE_H72Ujs2TR-lOh41_BW1hagSkfRmSZf2qpELOiQ-PO3BeUMHSLO_oGPKmv19VJRlvXrxDYVZCke0_F_hhk88bhz0FQXFU1PpXu\" casinoId=\"2301\"/>";
		Map<String, String> map=new HashMap<String, String>(); 
		mgParseRes(map,aa);
		for (Object o : map.keySet()) {  
			   System.out.println("key=" + o + " value=" + map.get(o));  
			  }
		GetBalanceResp resp=new GetBalanceResp();
		balanceResp(resp,aa);
		System.out.println(resp.getReal());
	}*/
}
