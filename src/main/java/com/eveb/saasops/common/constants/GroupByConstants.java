package com.eveb.saasops.common.constants;

import java.util.ArrayList;

import org.springframework.util.StringUtils;

public class GroupByConstants {
	public static ArrayList<String> accountMod = new ArrayList<String>();
	public static ArrayList<String> groupMod = new ArrayList<String>();
	public static ArrayList<String> onlineAccountMod = new ArrayList<String>();
	public static ArrayList<String> noticeMod=new ArrayList<String>();
	public static ArrayList<String> mbrBillMod=new ArrayList<String>();
	static {
		// 会员排序增加
		accountMod.add(0, "registertime desc");// 第一个为默认

		accountMod.add(1, "loginname asc");
		accountMod.add(2, "loginname desc");

		accountMod.add(3, "totalbalance asc");
		accountMod.add(4, "totalbalance desc");

		accountMod.add(5, "totaldeposit asc");
		accountMod.add(6, "totaldeposit desc");

		accountMod.add(7, "totalwithdrawal asc");
		accountMod.add(8, "totalwithdrawal desc");

		accountMod.add(9, "registertime asc");
		accountMod.add(10, "registertime desc");

		// 会员组排序
		groupMod.add(0, "id asc");
		groupMod.add(1, "groupname asc");
		groupMod.add(2, "groupname desc");
		// 在线会员
		onlineAccountMod.add(0, "logintime desc");
		accountMod.add(1, "loginname asc");
		accountMod.add(2, "loginname desc");
		//公告  startTime
		
		noticeMod.add(0,"createtime desc");
		noticeMod.add(1, "createtime asc");
		noticeMod.add(2, "createtime desc");
		
		//详单排序
		
		mbrBillMod.add(0,"ordertime desc");
		mbrBillMod.add(1, "ordertime asc");
		mbrBillMod.add(2, "ordertime desc");
	}

	public static String getOrderBy(ArrayList<String> pipeline, String key) {
		if (!StringUtils.isEmpty(key)) {
			String temp = key.replaceAll(" +", " ").trim().toLowerCase();
			String retVal = "";
			for (String item : pipeline) {
				if (temp.equals(item)) {
					retVal = item;
				}
			}
			// 叠加方式暂不开放
			/*
			 * if (StringUtils.isEmpty(retVal)) { String keys[] = key.split(","); if (keys
			 * != null && keys.length > 0) try { for (int i = 0; i < keys.length; i++) { if
			 * (i == 0) { retVal = pipeline.get(Integer.parseInt(keys[i])); } else { retVal
			 * += "," + pipeline.get(Integer.parseInt(keys[i])); } } } catch (Exception ex)
			 * {
			 * 
			 * } }
			 */
			if (StringUtils.isEmpty(retVal))
				retVal = pipeline.get(0);
			return retVal;
		} else {
			return pipeline.get(0);
		}
	}

/*	public static void main(String args[]) {
		String orderBy = GroupByConstants.getOrderBy(GroupByConstants.accountMod, "  totalbalance   asc   ");
		System.out.println("###" + orderBy + "#####");

	}*/
}
