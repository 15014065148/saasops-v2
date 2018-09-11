package com.eveb.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

import com.eveb.saasops.api.utils.MD5;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IbcDto {

	private String opCode;
	private String playerName;
	private String securityToken;
	private String oddsType;
	private BigDecimal maxTransfer;
	private BigDecimal minTransfer;
	private String firstName;
	private String lastName;
	private String opTransId;
	private BigDecimal amount;
	private Integer direction;
	private Integer currency;
	private String sportType;
	private Integer minBet;
	private Integer maxBet;
	private Integer maxBetPerMatch;
	

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.isEmpty(securityToken))
			buffer.append("&SecurityToken=").append(securityToken);
		if (!StringUtils.isEmpty(opCode))
			buffer.append("&OpCode=").append(opCode);
		if (!StringUtils.isEmpty(playerName))
			buffer.append("&PlayerName=").append(playerName);
		if (!StringUtils.isEmpty(oddsType))
			buffer.append("&OddsType=").append(oddsType);
		if (!StringUtils.isEmpty(firstName))
			buffer.append("&FirstName=").append(firstName);
		if (!StringUtils.isEmpty(firstName))
			buffer.append("&LastName=").append(lastName);
		if (!StringUtils.isEmpty(maxTransfer))
			buffer.append("&MaxTransfer=").append(String.valueOf(maxTransfer));
		if (!StringUtils.isEmpty(minTransfer))
			buffer.append("&MinTransfer=").append(String.valueOf(minTransfer));
		if (!StringUtils.isEmpty(opTransId))
			buffer.append("&OpTransId=").append(opTransId);
		if (!StringUtils.isEmpty(amount))
			buffer.append("&Amount=").append(String.valueOf(amount));
		if (!StringUtils.isEmpty(direction))
			buffer.append("&Direction=").append(String.valueOf(direction));
		if (!StringUtils.isEmpty(currency))
			buffer.append("&Currency=").append(String.valueOf(currency));
		
		if (!StringUtils.isEmpty(sportType))
			buffer.append("&sportType=").append(String.valueOf(sportType));
		if (!StringUtils.isEmpty(minBet))
			buffer.append("&minBet=").append(String.valueOf(minBet));
		if (!StringUtils.isEmpty(maxBet))
			buffer.append("&maxBet=").append(String.valueOf(maxBet));
		if (!StringUtils.isEmpty(maxBetPerMatch))
			buffer.append("&maxBetPerMatch=").append(String.valueOf(maxBetPerMatch));
		
		if (!StringUtils.isEmpty(buffer))
			buffer.delete(0, 1);
		//buffer.insert(0, );
		return buffer.toString();
	}

	public String getParams(String md5Key,String mod) {
		String prams = MD5.getMD5(md5Key +mod+ toString());
		//System.out.println(md5Key +mod+ toString());
		//System.out.println(prams);
		this.setSecurityToken(prams);
		return mod+toString();
	}
}
