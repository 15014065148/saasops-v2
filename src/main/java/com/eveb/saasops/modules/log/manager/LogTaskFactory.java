package com.eveb.saasops.modules.log.manager;

import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.config.SiteCodeThreadLocal;
import com.eveb.saasops.config.ThreadLocalCache;
import com.eveb.saasops.modules.log.entity.OperationLog;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.TimerTask;

@Component
public class LogTaskFactory {

	private static LogTaskFactory factory = new LogTaskFactory();

    @Transactional
	public MyTimeTask operationLog(OperationLog logger,TransportClient client,Map<String, Object> map,String siteCode) throws Throwable  {
    		
    	return new MyTimeTask() {
            @Override
            public void run() {
                SiteCodeThreadLocal siteCodeThreadLocal =new SiteCodeThreadLocal();
                siteCodeThreadLocal.setSiteCode(siteCode);
                ThreadLocalCache.siteCodeThreadLocal.set(siteCodeThreadLocal);
            	String randomNum = CommonUtil.getRandomNum();
            	client.prepareIndex("operation", "log_operation",randomNum).setSource(map).execute();
            }
        };
    }
    
    public static LogTaskFactory me() {
    	return factory;
    }

    @Component
    class MyTimeTask extends TimerTask{

	    @Override
        public void run() {

        }

    }
}
