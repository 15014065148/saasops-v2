package com.eveb.saasops.modules.system.onlinepay.mapper;

import java.util.List;
import java.util.Map;

import com.eveb.saasops.modules.fund.entity.TOpPay;
import com.eveb.saasops.modules.system.onlinepay.entity.OnlinePayRelations;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.eveb.saasops.api.modules.pay.pzpay.entity.OnlinePayPicture;
import com.eveb.saasops.modules.system.onlinepay.entity.SetBacicOnlinepay;

/**
 * Created by William on 2017/11/10.
 */
@Mapper
@Component
public interface MyOnlinepayMapper {

    void deleteBatch(Integer[] ids);
    List<SetBacicOnlinepay> queryConditions(SetBacicOnlinepay setBacicOnlinepay);

    /**
     *查询支付平台及相关信息，下拉列表使用
     * @return
     */
    List<Map<String,Object>> queryPayment(@Param("siteCode") String siteCode);

    /**
     * 查询支付平台相关的银行
     * @param map
     * @return
     */
    List<Map<String,Object>> queryPayBanks(Map<String,Object> map);

    List<OnlinePayPicture> getPzpayPictureUrl(@Param("tOpPay") TOpPay tOpPay,@Param("onlinePayId") String onlinePayId);

    List<String> queryPayType(@Param("createTime") String createTime);

    List<Map<String,Object>> queryMbrGroupById(String id);

    List<OnlinePayRelations> selectBankAndGroup(@Param("onlinePayId") Integer onlinePayId,@Param("mbrGroupType") Integer mbrGroupType);

    List<SetBacicOnlinepay> getAllBacicOnlinepay();
}
