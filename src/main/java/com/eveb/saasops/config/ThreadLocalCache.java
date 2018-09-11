package com.eveb.saasops.config;

/**
 * Created by William on 2018/3/5.
 */
public class ThreadLocalCache {

    public static ThreadLocal<SiteCodeThreadLocal> siteCodeThreadLocal = new ThreadLocal<>();

    /**
     * 设置SiteCode
     * @param siteCode
     */
    public static void  setSiteCodeAsny(String siteCode){
        SiteCodeThreadLocal siteCodeThreadLocal = new SiteCodeThreadLocal();
        siteCodeThreadLocal.setSiteCode(siteCode);
        ThreadLocalCache.siteCodeThreadLocal.set(siteCodeThreadLocal);
    }
}
