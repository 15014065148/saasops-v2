package com.eveb.saasops.api.modules.user.service;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_8_DATE;


import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.BbinConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BbinResponseDto;
import com.eveb.saasops.api.modules.user.dto.UserBbinDto;
import com.eveb.saasops.api.utils.MD5;
import com.eveb.saasops.api.utils.OkHttpUtils;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.StringUtil;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BbinService {

    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private TGmDepotService tGmDepotService;
	@Autowired
	private AginService aginService;
	@Autowired
	private  OkHttpService okHttpService;

	/**
	 * 建立用户
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),loginName);
		if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
			try {
				UserBbinDto userBbinDto=getMemberDto(gmApi, mbrWallet);
				String result = okHttpService.postJson(gmApi.getPcUrl() + BbinConstants.BBIN_API_CREATEMEMBER,userBbinDto);
				log.debug(" BWIN—>会员注册 [会员账号{}|提交参数{}返回结果{}]",loginName,new Gson().toJson(userBbinDto),result);
				if (StringUtil.isNotEmpty(result)) {
					BbinResponseDto responseDto = new Gson().fromJson(result, BbinResponseDto.class);
					switch (responseDto.getData().getCode()) {
					case BbinConstants.BBIN_RSCODE_ACCADDREPEATED:
					case BbinConstants.BBIN_RSCODE_ACCADDSUCC:
						TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
						mbrWallet.setDepotName(tGmDepot.getDepotName());
						mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
						mbrWallet = mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
						break;
					case BbinConstants.BBIN_SYS_MAINTENANCE:
					case BbinConstants.BBIN_GM_MAINTENANCE:
						throw new RRException("BBIN系统维护中,暂停服务!");
					case BbinConstants.BBIN_KEY_ERROR:
						throw new RRException("BBIN加密解密服务出错,此次服务中止!");
					default:
						throw new RRException(messagesConfig.getValue("bbwin.register.fail"));
					}
				}
			} catch (Exception e) {
				throw new RRException("BBIN创建第三方账号失败!");
			}
		}
		return mbrWallet;
	}

	private UserBbinDto getMemberDto(TGmApi gmApi, MbrDepotWallet mbrWallet) {
        String[] keys=gmApi.getSecureCodes().get(BbinConstants.BBIN_API_CREATEMEMBER).split(",");
		UserBbinDto bbinEntity = new UserBbinDto();
		bbinEntity.setUsername(gmApi.getPrefix() + mbrWallet.getLoginName());
		bbinEntity.setPassword(mbrWallet.getPwd());
		bbinEntity.setWebsite(gmApi.getWebName());
		bbinEntity.setUppername(gmApi.getAgyAcc());
		StringBuffer sf = new StringBuffer();
		sf.append(keys[0])
				.append(makeMd5(bbinEntity, keys[1]))
				.append(keys[2]);
		bbinEntity.setKey(sf.toString());
		return bbinEntity;
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public boolean login(String url) {
		try {
			String result = okHttpService.postJson(url, null);
			log.debug(" BWIN—>会员登陆 [提交参数{}返回结果{}]",url,result);
			if (StringUtil.isNotEmpty(result)) {
				BbinResponseDto responseDto = new Gson().fromJson(result, BbinResponseDto.class);
                return responseDto.getResult() == Boolean.TRUE;
			}
			return false;
		} catch (Exception ex) {
			throw new RRException("BBIN每三方账号登陆失败!");
		}
	}


	private String getLoginDto(TGmApi gmApi, MbrDepotWallet mbrWallet, TGmGame tgmGame) {
        String[] keys=gmApi.getSecureCodes().get(BbinConstants.BBIN_API_LOGIN).split(",");
		UserBbinDto bbinEntity = new UserBbinDto();
		bbinEntity.setUsername(gmApi.getPrefix() + mbrWallet.getLoginName());
		bbinEntity.setWebsite(gmApi.getWebName());
		bbinEntity.setUppername(gmApi.getAgyAcc());
		bbinEntity.setKey(keys[0]
				+ makeMd5(bbinEntity, keys[1])
				+ keys[2]);
		String logUrl = gmApi.getPcUrl2()
				+ (tgmGame.getCatId() == 5 ? BbinConstants.BBIN_API_LOGIN2 : BbinConstants.BBIN_API_LOGIN) + "?"
				+ bbinEntity.toString();
		if (!StringUtils.isEmpty(tgmGame.getGameParam()))
			logUrl += "&" + tgmGame.getGameParam();
		return logUrl;
	}

	/**
	 * 登出
	 * @param loginName
	 * @param gmApi
	 * @return
	 */
    public boolean logOut(TGmApi gmApi, String loginName) {
        boolean rsCode = false;
		String[] keys=gmApi.getSecureCodes().get(BbinConstants.BBIN_API_LOGOUT).split(",");
		UserBbinDto bbinEntity = new UserBbinDto();
		bbinEntity.setUsername(gmApi.getPrefix() + loginName);
		bbinEntity.setWebsite(gmApi.getWebName());
		bbinEntity.setUppername(gmApi.getAgyAcc());
		bbinEntity.setKey(keys[0]
				+ makeMd5(bbinEntity, keys[1])
				+ keys[2]);
        String result = OkHttpUtils.get(gmApi.getPcUrl() + "?" + bbinEntity.toString());
        log.debug(" BWIN—>会员登出 [提交参数{}返回结果{}]",gmApi.getPcUrl()+BbinConstants.BBIN_API_LOGOUT + "?" + bbinEntity.toString(),result);
        if (StringUtil.isNotEmpty(result)) {
            BbinResponseDto responseDto = new Gson().fromJson(result, BbinResponseDto.class);
            if (responseDto.getResult()) {
                switch (responseDto.getData().getCode()) {
                    case BbinConstants.BBIN_RSCODE_LOGOUTSCC:
                        rsCode = true;
                        break;
					case BbinConstants.BBIN_RSCODE_ACCADDFAIL:
						rsCode = true;
						break;
                    default:
                        rsCode = false;
                        break;
                }
            }
        }
        return rsCode;
    }


	/**
	 *
	 * @param gmApi
	 * @param tgmGame
	 * @param accountId
	 * @param loginName
	 * @param terminal
	 * @return
	 */
	public String generateUrl(TGmApi gmApi, TGmGame tgmGame,Integer accountId, String loginName, Byte terminal) {
		MbrDepotWallet mbrWallet = createMember(gmApi, accountId, loginName);
		String logUrl = getLoginDto(gmApi, mbrWallet, tgmGame);
		if (tgmGame.getCatId() != 5) {
			return logUrl;
		} else {
			if (login(logUrl)) {
				return getLaunchGmDto(gmApi, mbrWallet, tgmGame, terminal);
			} else {
				throw new RRException("BBIN,账号登陆失败!");
			}
		}
	}

	/**
	 *
	 * @param gmApi
	 * @param mbrWallet
	 * @param tgmGame
	 * @param terminal
	 * @return
	 */
    private String getLaunchGmDto(TGmApi gmApi, MbrDepotWallet mbrWallet, TGmGame tgmGame,Byte terminal)
	{
		String[] keys=gmApi.getSecureCodes().get(BbinConstants.BBIN_API_PLAYGAME).split(",");
		UserBbinDto bbinEntity = new UserBbinDto();
		bbinEntity.setUsername(gmApi.getPrefix() + mbrWallet.getLoginName());
		bbinEntity.setWebsite(gmApi.getWebName());
		bbinEntity.setUppername(gmApi.getAgyAcc());
		bbinEntity.setKey(keys[0]
				+ makeMd5(bbinEntity, keys[1])
				+ keys[2]);
		String palyGameUrl = gmApi.getPcUrl2()
				+ (terminal == ApiConstants.Terminal.mobile ? BbinConstants.BBIN_API_PLAYGAMEBYH5
						: BbinConstants.BBIN_API_PLAYGAME)
				+ "?" + bbinEntity.toString();
		palyGameUrl += "&gamekind=5&lang=zh-cn&gametype=" + tgmGame.getGameCode();
		log.debug(" BWIN—>游戏跳转 [会员账号为{}|提交参数{}]",mbrWallet.getLoginName(),palyGameUrl);
		return palyGameUrl;
	}

    private String makeMd5(UserBbinDto bbinEntity, String keyB) {
        StringBuffer sf = new StringBuffer();
        sf.append(bbinEntity.getWebsite()).append(bbinEntity.getUsername()).append(keyB)
                .append(DateUtil.getAmericaDate(FORMAT_8_DATE, new Date()));
        return MD5.getMD5(sf.toString());
    }
}
