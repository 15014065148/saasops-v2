package com.eveb.saasops.modules.member.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.modules.member.dto.IpResultDto;
import com.google.gson.Gson;

@Service
public class IpService {
	private static String ipUrl = "http://ip.taobao.com/service/getIpInfo.php?ip=";

	public String getIpArea(String ip) {
		// Jsoup.connect("http://ip.taobao.com/service/getIpInfo.php?ip=161.202.230.40")
		String inputLine = new String();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new URL(ipUrl + ip).openStream()));
			String str;
			while ((str = in.readLine()) != null)
				inputLine += str;
			in.close();
			if (!StringUtils.isEmpty(inputLine)) {
				IpResultDto resultDto = new Gson().fromJson(inputLine, IpResultDto.class);
				inputLine = resultDto.getData().toString();
				inputLine=inputLine.replaceAll("XX ","");
			}
		} catch (Exception e) {}

		return inputLine;
	}
}
