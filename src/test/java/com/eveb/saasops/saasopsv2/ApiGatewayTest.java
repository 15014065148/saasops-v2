package com.eveb.saasops.saasopsv2;

import com.eveb.saasops.api.modules.unity.dto.LoginModel;
import com.eveb.saasops.api.modules.unity.dto.PlayGameModel;
import com.eveb.saasops.api.modules.unity.dto.RegisterModel;
import com.eveb.saasops.api.modules.unity.dto.TransferModel;
import com.eveb.saasops.api.modules.unity.service.GameDepotService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiGatewayTest {

	@Autowired
	private GameDepotService service;

	@Test
	public void testCreateMember() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		RegisterModel registerModel = new RegisterModel();
		registerModel.setDepotId(29);
		registerModel.setDepotName("GD");
		registerModel.setSiteCode("ybh");
		registerModel.setUserName("lebron");
		String resultStr = service.createMember(registerModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void testLogin() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		LoginModel loginModel = new LoginModel();
		loginModel.setDepotId(29);
		loginModel.setDepotName("GD");
		loginModel.setSiteCode("ybh");
		loginModel.setUserName("lebron");
		String resultStr = service.login(loginModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	@Test
	public void testLogout() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		LoginModel loginModel = new LoginModel();
		loginModel.setDepotId(29);
		loginModel.setDepotName("GD");
		loginModel.setSiteCode("ybh");
		loginModel.setUserName("lebron");
		String resultStr = service.logout(loginModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	@Test
	public void testDeposit() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		TransferModel transferModel = new TransferModel();
		transferModel.setDepotId(29);
		transferModel.setDepotName("GD");
		transferModel.setSiteCode("ybh");
		transferModel.setUserName("lebron");
		transferModel.setAmount(200);
		transferModel.setOrderNo("D180826134512Dn2D8");
		String resultStr = service.deposit(transferModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	@Test
	public void testWithdrawal() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		TransferModel transferModel = new TransferModel();
		transferModel.setDepotId(29);
		transferModel.setDepotName("GD");
		transferModel.setSiteCode("ybh");
		transferModel.setUserName("lebron");
		transferModel.setAmount(50);
		transferModel.setOrderNo("W180826134512Dn225");
		String resultStr = service.withdrawal(transferModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	@Test
	public void testQueryBalance() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		LoginModel loginModel = new LoginModel();
		loginModel.setDepotId(29);
		loginModel.setDepotName("GD");
		loginModel.setSiteCode("ybh");
		loginModel.setUserName("lebron");
		String resultStr = service.queryBalance(loginModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void testCheckTransfer() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		TransferModel transferModel = new TransferModel();
		transferModel.setDepotId(29);
		transferModel.setDepotName("GD");
		transferModel.setSiteCode("ybh");
		transferModel.setUserName("lebron");
		transferModel.setAmount(200);
		transferModel.setOrderNo("D180826134512Dn2D8");
		String resultStr = service.checkTransfer(transferModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}



	@Test
	public void testPlayGame() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		PlayGameModel playGameModel = new PlayGameModel();
		playGameModel.setDepotId(29);
		playGameModel.setDepotName("GD");
		playGameModel.setSiteCode("ybh");
		playGameModel.setUserName("lebron");
		playGameModel.setOrigin("PC");
		String resultStr = service.playGame(playGameModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	@Test
	public void testOpenHall() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		PlayGameModel playGameModel = new PlayGameModel();
		playGameModel.setDepotId(29);
		playGameModel.setDepotName("GD");
		playGameModel.setSiteCode("ybh");
		playGameModel.setUserName("lebron");
		String resultStr = service.openHall(playGameModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	@Test
	public void testTryPlayGame() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		PlayGameModel playGameModel = new PlayGameModel();
		playGameModel.setDepotId(29);
		playGameModel.setDepotName("GD");
		playGameModel.setSiteCode("ybh");
		playGameModel.setGameType("hunter"); // live //真人 hunter //捕鱼  slot //电子
		String resultStr = service.tryPlayGame(playGameModel);
		System.out.println("返回值======"+resultStr);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
}
