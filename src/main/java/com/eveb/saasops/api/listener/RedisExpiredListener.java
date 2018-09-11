package com.eveb.saasops.api.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.user.dto.RedisKey;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.config.ThreadLocalCache;
import com.eveb.saasops.modules.member.service.MbrAccountService;

@Component
public class RedisExpiredListener implements MessageListener {
	//listener 加载比Service更早 不能便 用注入的方式
	//@Autowired
	//ApiUserService apiUserService;
/*	@Autowired
	MbrAccountService mbrAccountService;*/
    //public final static String LISTENER_PATTERN = "__key*__:*";
	public final static String LISTENER_PATTERN = "__keyspace@0__:"+Constants.PROJECT_NAME+Constants.REDIS_SPACE_SPACING+ApiConstants.REIDS_LOGIN_TOKEN_KEY+":*";
	

    /**
     * 客户端监听订阅的topic，当有消息的时候，会触发该方法;
     * <br/>并不能得到value, 只能得到key。
     * <br/>姑且理解为: redis服务在key失效时(或失效后)通知到java服务某个key失效了, 那么在java中不可能得到这个redis-key对应的redis-value。
     * <p>解决方案:
     *  <br/>创建copy/shadow key, 例如 set vkey "vergilyn"; 对应copykey: set copykey:vkey "" ex 10;
     *  <br/>真正的key是"vkey"(业务中使用), 失效触发key是"copykey:vkey"(其value为空字符为了减少内存空间消耗)。
     *  <br/>当"copykey:vkey"触发失效时, 从"vkey"得到失效时的值, 并在逻辑处理完后"del vkey"
     * </p>
     * <p>缺陷:
     *  <br/>1: 存在多余的key; (copykey/shadowkey)
     *  <br/>2: 不严谨, 假设copykey在 12:00:00失效, 通知在12:10:00收到, 这间隔的10min内程序修改了key, 得到的并不是 失效时的value.
     *  (第1点影响不大; 第2点貌似redis本身的Pub/Sub就不是严谨的, 失效后还存在value的修改, 应该在设计/逻辑上杜绝)
     *  <br/>当"copykey:vkey"触发失效时, 从"vkey"得到失效时的值, 并在逻辑处理完后"del vkey"
     * </p>
     * @param message
     * @param bytes
     */
    @Override
	public void onMessage(Message message, byte[] bytes) {
		RedisKey keys = getLoingName(message);
		if (!StringUtils.isEmpty(keys)) {
			MbrAccountService mbrAccountService = SpringContextHolder.getBean("mbrAccountService");
			ThreadLocalCache.setSiteCodeAsny(keys.getSiteCode());
			mbrAccountService.updateOffline(keys.getLoginName());
		}
	}
   
    private RedisKey getLoingName(Message message)
	{
		String body = new String(message.getBody());// 建议使用: valueSerializer
		if (!StringUtils.isEmpty(body) && body.equals(Constants.REDIS_CMD_EXPIRED)) {
			String channel = new String(message.getChannel());
			String key = channel.substring(channel.lastIndexOf(Constants.REDIS_SPACE_SPACING) + 1);
			String keyArr[] = key.split("_");
			if (keyArr.length == 2) {
				RedisKey keyContent = new RedisKey();
				keyContent.setSiteCode(keyArr[0]);
				keyContent.setLoginName(keyArr[1]);
				return keyContent;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
