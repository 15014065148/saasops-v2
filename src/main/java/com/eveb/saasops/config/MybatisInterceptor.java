package com.eveb.saasops.config;

import com.eveb.saasops.api.modules.apisys.service.TCpSiteService;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.AESUtil;
import com.eveb.saasops.common.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.util.Properties;

/**
 * mybatis 全局sql过滤
 * mybatis 的insert 和 delete 也是调用了 update方法
 * Created by William on 2018/1/20.
 */
@Slf4j
@Intercepts({@Signature(type = StatementHandler.class,method = "prepare",args = {Connection.class,Integer.class})})
public class MybatisInterceptor implements Interceptor {

    private static final String PRE_SCHEMA ="saasops_";

    private static final String MYBATIS_SQL_ID = "delegate.boundSql.sql";

    private static final String MYBATIS_SQL_STATEMENT ="delegate.mappedStatement";

    private static final String NO_MANAGE_SQL_ID ="com.eveb.saasops.api.modules.apisys.mapper.ApiSysMapper.findCpSiteOne";

    private static final String MANAGE = "manage";

    private static final String MYCAT_SQL = "/*!mycat:schema = ";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler ) invocation.getTarget();
        //获取statementHandler包装类
        MetaObject MetaObjectHandler = SystemMetaObject.forObject(statementHandler);
        //获取查询接口映射的相关信息
        MappedStatement mappedStatement = (MappedStatement) MetaObjectHandler.getValue(MYBATIS_SQL_STATEMENT);
        //获取进行数据库操作时管理参数的handler
        //ParameterHandler parameterHandler = (ParameterHandler) MetaObjectHandler.getValue("delegate.parameterHandler");
        //Mapper 对应的id ,暂时用不到
        String mapId = mappedStatement.getId();
        //获取sql
        String sql = (String) MetaObjectHandler.getValue(MYBATIS_SQL_ID);
        String schema = MANAGE;
        if(!mapId.equals(NO_MANAGE_SQL_ID) ){
            HttpServletRequest request =getSchemaName();
            if(request != null){
                //同步的处理方式
                schema =request.getAttribute(SystemConstants.SCHEMA_NAME).toString();
            }else if(ThreadLocalCache.siteCodeThreadLocal.get() !=null && ThreadLocalCache.siteCodeThreadLocal.get().getSiteCode() != null){
                //异步处理方式
                schema = TCpSiteService.siteCode.get(ThreadLocalCache.siteCodeThreadLocal.get().getSiteCode());
            }
        }
        //获取站点前缀
        MetaObjectHandler.setValue(MYBATIS_SQL_ID, MYCAT_SQL +PRE_SCHEMA+schema.toLowerCase()+" */"+sql);
        return invocation.proceed();
    }

    private HttpServletRequest getSchemaName(){
        RequestAttributes requestAttributes =RequestContextHolder.getRequestAttributes();
        if( requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if (null == request.getAttribute(SystemConstants.SCHEMA_NAME) || "".equals(request.getAttribute(SystemConstants.SCHEMA_NAME))) {
                String schemaName = TCpSiteService.siteCode.get(AESUtil.decrypt(request.getHeader(SystemConstants.STOKEN)));
                if (TCpSiteService.schemaName.get(schemaName) == null){
                    //刷新内存
                    SpringContextUtils.getBean(SystemConstants.T_CP_SITE_SERVICE,TCpSiteService.class).initSchemaName();
                    schemaName = TCpSiteService.schemaName.get(AESUtil.decrypt(request.getHeader(SystemConstants.STOKEN)));
                    if (TCpSiteService.schemaName.get(schemaName) == null) {
                        log.error("SToken wrong,SToken =" + request.getHeader(SystemConstants.STOKEN) , "schemaName = " + schemaName);
                        throw new RRException("SToken wrong ,schemaName  中不存在次siteCode对应");
                    }
                }
                request.setAttribute(SystemConstants.SCHEMA_NAME, schemaName);
                return request;
            }else {
                return request;
            }
        }
        return null;
    }



    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        log.info(properties.toString());
    }

}
