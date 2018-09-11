package com.eveb.saasops.api.modules.pay.pzpay.service;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.eveb.saasops.api.modules.pay.pzpay.dto.JsonResultUtil;
import com.eveb.saasops.api.modules.pay.pzpay.entity.PzpayPayType;
import com.eveb.saasops.api.modules.user.service.OkHttpService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.modules.pay.pzpay.dto.OnlinePayDto;
import com.eveb.saasops.api.modules.pay.pzpay.entity.PzpayContent;
import com.eveb.saasops.api.modules.pay.pzpay.entity.PzpayPayParams;
import com.eveb.saasops.api.utils.ASCIIUtils;
import com.eveb.saasops.api.utils.HttpsRequestUtil;
import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.api.utils.MD5;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.config.ThreadLocalCache;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.fund.dao.FundDepositMapper;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import com.eveb.saasops.modules.fund.entity.FundDeposit.Mark;
import com.eveb.saasops.modules.fund.entity.FundDeposit.PaymentStatus;
import com.eveb.saasops.modules.fund.entity.FundDeposit.Status;
import com.eveb.saasops.modules.fund.entity.TOpPay;
import com.eveb.saasops.modules.fund.mapper.FundMapper;
import com.eveb.saasops.modules.fund.service.FundDepositService;
import com.eveb.saasops.modules.fund.service.ZhiFuPayService;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrDepositCond;
import com.eveb.saasops.modules.member.service.MbrDepositCondService;
import com.eveb.saasops.modules.system.onlinepay.dao.SetBacicOnlinepayMapper;
import com.eveb.saasops.modules.system.onlinepay.entity.SetBacicOnlinepay;
import com.eveb.saasops.modules.system.onlinepay.mapper.MyOnlinepayMapper;
import com.eveb.saasops.modules.system.onlinepay.mapper.OnlinePayMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by William on 2017/12/6.
 */
@Service
@Slf4j
@Transactional
public class OnlinePayService {

    @Value("${api.onlinePay.Pzpay.returnUrl}")
    private String returnUrl;
    @Value("${api.onlinePay.Pzpay.notifyUrl}")
    private String notifyUrl;
    @Value("${api.onlinePay.Pzpay.pzPayUrl}")
    private String pzPayUrl;
    @Value("${api.onlinePay.Pzpay.pzPayQueryUrl}")
    private String pzPayQueryUrl;

    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private SetBacicOnlinepayMapper setBacicOnlinepayMapper;
    @Autowired
    private MyOnlinepayMapper myOnlinepayMapper;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private ZhiFuPayService zhiFuPayService;
    @Autowired
    private OnlinePayMapper onlinePayMapper;

    public Map<String, Object> optionPayment(String siteCode, PzpayPayParams pzpayPayParams) {
        saveFundDespoit(pzpayPayParams);//保存入款信息、
        // 查询支付信息
        OnlinePayDto onlinePayDto = onlinePayMapper.findPaymentInfo(pzpayPayParams.getPayType());
        if (isNull(onlinePayDto))
            throw new R200Exception("支付类型不存在!");
        BigDecimal fee = pzpayPayParams.getFee().multiply(new BigDecimal(Constants.ONE_HUNDRED));
        if (9 == onlinePayDto.getPaymentId()) {
            pzpayPayParams.setFee(fee);
            return zhiFuPayService.zhiFuPay(pzpayPayParams, onlinePayDto, CommonUtil.getSiteCode());
        }
        Map<String, Object> map = lianLianPay(siteCode + ":" + pzpayPayParams.getAccountId(), fee,
                pzpayPayParams.getBankCode(), pzpayPayParams.getSubject(), pzpayPayParams.getOutTradeNo().toString(),
                pzpayPayParams.getExtra(), pzpayPayParams.getPayType(), onlinePayDto.getPaymentId());
        return map;
    }

    /**
     * 连连支付，借记卡
     *
     * @param fee
     * @param bankCode
     * @param subject
     * @param out_trade_no
     * @param extra
     * @param payType      1连连借记卡 2连连信用卡 3微信 4支付宝
     * @return
     */
    public Map<String, Object> lianLianPay(String userId, BigDecimal fee, String bankCode, String subject,
                                           String out_trade_no, String extra, int payType, int paymentId) {
        Map<String, Object> map = getSign(out_trade_no, payType, fee, bankCode, paymentId);
        String res = pzPayUrl + "?subject=" + subject + "&extra=" + extra + "&user_id=" + userId + "&" + map.get("url");
        if (map.get("urlMethod").equals(1)) {
            String jsonMessage = okHttpService.postForm(okHttpService.getPayHttpClient(), res, null);
            try {
                Gson gson = new Gson();
                JsonResultUtil jsonResult = gson.fromJson(jsonMessage, JsonResultUtil.class);
                if (jsonResult.isSuccess()) {
                    map.put("url", jsonMessage);
                } else {
                    throw new R200Exception("该支付维护中,请使用其它支付");
                }
            } catch (Exception e) {
                throw new R200Exception("该支付维护中,请使用其它支付");
            }
        } else {
            map.put("url", res);
        }
        return map;
    }

    /**
     * 判断支付是否可进行,比较单笔存款的最大值或最小值 并且用户非正常操作判断
     *
     * @param fee     单位是一块
     * @param payType
     * @return
     */
    public boolean judgeSaveConditions(BigDecimal fee, int payType) {
        // 根据支付方式获取支付最大值
        SetBacicOnlinepay setBacicOnlinepay = new SetBacicOnlinepay();
        setBacicOnlinepay.setId(payType);
        setBacicOnlinepay = setBacicOnlinepayMapper.selectOne(setBacicOnlinepay);
        return !(fee.compareTo(new BigDecimal(setBacicOnlinepay.getMinLimit())) == -1
                || fee.compareTo(new BigDecimal(setBacicOnlinepay.getMaxLimit())) == 1);
    }

    /**
     * 获取单次充值的手续费
     *
     * @param fee
     * @param accountId
     * @return
     */
    public BigDecimal getFeeScale(BigDecimal fee, Integer accountId) {
        // 根据该会员组,会员的存款设置,获取线上支付玩家单笔存款手续费及相关设置信息
        MbrDepositCond mbrDeposit = mbrDepositCondService.getMbrDeposit(accountId);
        if (mbrDeposit.getFeeAvailable() == 1) {
            // 判断限免,从支付流水中取出该用户支付数据,
            FundDeposit fundDeposit = new FundDeposit();
            fundDeposit.setAccountId(accountId);
            Date d = new Date();
            // 实际这段时间的充值次数
            Map<String, Object> mbrFreeTimes = fundMapper.querySumFeeFreeTimes(accountId,
                    DateUtil.getBrforeTime(d, mbrDeposit.getFeeHours()),
                    DateUtil.format(d, DateUtil.FORMAT_18_DATE_TIME));
            BigDecimal freeTime = mbrFreeTimes != null ? (BigDecimal) mbrFreeTimes.get("freeTimes") : new BigDecimal(0);
            if (freeTime.compareTo(new BigDecimal(mbrDeposit.getFeeTimes())) == -1) {
                // 手续费为0
                return BigDecimal.ZERO;
            }
            BigDecimal feeScale = fee.multiply(mbrDeposit.getFeeScale().divide(new BigDecimal(100))); // 手续费 按比例收费
            return feeScale.compareTo(mbrDeposit.getFeeTop()) == 1 ? mbrDeposit.getFeeTop() : feeScale; // 手续费
        }
        return BigDecimal.ZERO;
    }

    private Map<String, Object> getSign(String out_trade_no, int payType, BigDecimal fee, String bankCode,
                                        int paymentId) {
        Map<String, Object> map = new HashMap<>();
        // map.put("isQR",false);
        // SetBacicOnlinepay setBacicOnlinepay = new SetBacicOnlinepay();
        // setBacicOnlinepay.setId(payType);
        // setBacicOnlinepay = setBacicOnlinepayMapper.selectOne(setBacicOnlinepay);
        OnlinePayDto onlinePayDto = onlinePayMapper.findPaymentInfo(payType);
        if (!onlinePayDto.getPassword().contains("||")) {
            throw new R200Exception("该支付维护中,请使用其它支付");
        }
        String[] pwd = onlinePayDto.getPassword().split("\\|\\|");
        Long bankId = new Long(pwd[1]);
        String key = pwd[0];
        if (4 != paymentId && 5 != paymentId) {
            bankCode = "";
        }

        // 取消枚举，自己组装需要的map参数
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("partner_id", onlinePayDto.getMerNo());
        params.put("bank_id", bankId);
        params.put("pay_type", onlinePayDto.getPayCode());
        params.put("notify_url", notifyUrl);
        params.put("return_url", returnUrl);
        params.put("out_trade_no", out_trade_no);
        params.put("total_fee", fee);
        params.put("bank_code", bankCode);

        // 调用获取加密签证url
        String signUrl = getSignParams(params, key);
        /*
         * if(setBacicOnlinepay.getIsQR() == 1 ){ // map.put("isQR",true); }
         */
        map.put("urlMethod", onlinePayDto.getUrlMethod());
        map.put("url", signUrl);
        return map;
    }

    /**
     * 盘子支付
     *
     * @param deposits
     * @return
     */
    @Async("getPayResultExecutor")
    public String getPayResult(List<FundDeposit> deposits, String siteCode) {
        deposits.stream().forEach(deposit -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            if (Objects.isNull(deposit.getOnlinePayId())) return;
            SetBacicOnlinepay setBacicOnlinepay = new SetBacicOnlinepay();
            setBacicOnlinepay.setId(deposit.getOnlinePayId());
            setBacicOnlinepay = setBacicOnlinepayMapper.selectOne(setBacicOnlinepay);

            //TODO 判断是否是直付支付宝
            if (setBacicOnlinepay.getPaymentId() == PzpayPayType.ZhiFu.getPayType()) {
                deposit.setStatus(Constants.EVNumber.zero);
                fundMapper.updatePayStatus(deposit);
            } else {
                Map<String, Object> params = new HashMap<>();
                params.put("out_trade_no", deposit.getOrderNo());
                params.put("trade_no", "");
                params.put("partner_id", setBacicOnlinepay.getMerNo());
                String urlParams = ASCIIUtils.formatUrlMap(params, false, false);
                String sign = MD5.getMD5((urlParams + "&key=" + setBacicOnlinepay.getPassword().split("\\|\\|")[0]));
                String payUrl = pzPayQueryUrl + "?" + urlParams + "&sign=" + sign;
                String payRS = okHttpService.get(okHttpService.getPayHttpClient(), payUrl);
                log.info("请求支付的请求参数password{} out_trade_no{}  partner_id{}",
                        Arrays.toString(setBacicOnlinepay.getPassword().split("\\|\\|")), deposit.getOrderNo(),
                        setBacicOnlinepay.getMerNo());
                log.info("请求支付的返回参数{}   " + payRS);
                updateWalltes(payRS, deposit.getOrderNo(), siteCode);
            }
        });
        return "finish";
    }

    /**
     * 更新简报信息，同时插入流水表
     *
     * @param payRS
     * @param orderNo
     */
    @Transactional
    public void updateWalltes(String payRS, String orderNo, String siteCode) {
        Map<String, Object> rs = jsonUtil.toMap(payRS);
        // 根据订单号查询数据库
        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo(orderNo);
        List<FundDeposit> deposits = fundMapper.selectForUpdate(deposit);
        if (0 == deposits.size()) return;
        deposit = deposits.get(0);
        if (deposit.getStatus() != 2) return;
        if (rs.get("success").toString().equals("true")) {
            PzpayContent pzpayContent = jsonUtil.fromJson(jsonUtil.toJson(rs.get("content")), PzpayContent.class);
            succsess(pzpayContent, deposit, siteCode);
        } else if (rs.get("success").toString().equals("false")) {
            log.info("支付失败，错误，盘子支付");
            MbrAccount mbrAccount = getMbr(deposit.getAccountId());
            deposit.setModifyUser(mbrAccount.getLoginName());
            deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            deposit.setStatus(Constants.EVNumber.zero);
            fundMapper.updatePayStatus(deposit);
            return;
        }
        MbrAccount mbrAccount = getMbr(deposit.getAccountId());
        deposit.setModifyUser(mbrAccount.getLoginName());
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundMapper.updatePayStatus(deposit);
    }

    private void succsess(PzpayContent pzpayContent, FundDeposit deposit, String siteCode) {
        // 支付成功
        deposit.setActualArrival(new BigDecimal(pzpayContent.getTotal_fee()).divide(new BigDecimal(100)));
        if (pzpayContent.getTrade_status().equals("SUCCESS")) {
            deposit.setIsPayment(true);
            deposit.setMemo("支付成功，盘子支付");
            fundDepositService.updateDepositSucceed(deposit);
            applicationEventPublisher
                    .publishEvent(new BizEvent(this, siteCode, deposit.getAccountId(), BizEventType.ONLINE_PAY_SUCCESS,
                            deposit.getActualArrival(), deposit.getOrderPrefix() + deposit.getOrderNo()));
        } else if (pzpayContent.getTrade_status().equals("FAIL")) {
            // 支付失败
            deposit.setStatus(0);
            deposit.setIsPayment(false);
            deposit.setMemo("支付失败，未支付，盘子支付");
        }
    }

    /**
     * 保存支付线上支付信息 TODO: 手续费，优惠金额暂时写死，后期要动态根据配置计算
     *
     * @param pzpayPayParams
     */
    @Transactional
    public void saveFundDespoit(PzpayPayParams pzpayPayParams) {
        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo(pzpayPayParams.getOutTradeNo().toString());
        deposit.setMark(Mark.onlinePay);
        deposit.setStatus(Status.apply);
        deposit.setIsPayment(PaymentStatus.unPay);
        deposit.setOnlinePayId(pzpayPayParams.getPayType());
        deposit.setActivityId(pzpayPayParams.getActivityId());
        deposit.setDepositAmount(pzpayPayParams.getFee());
        BigDecimal feeScale = getFeeScale(pzpayPayParams.getFee(), pzpayPayParams.getAccountId());
        deposit.setHandlingCharge(feeScale);
        deposit.setActualArrival(deposit.getDepositAmount());
        deposit.setIp(pzpayPayParams.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_ONLINEDEPOSIT);
        MbrAccount mbrAccount = getMbr(pzpayPayParams.getAccountId());
        deposit.setDepositUser(mbrAccount.getRealName());
        deposit.setCreateUser(mbrAccount.getLoginName());
        deposit.setAccountId(pzpayPayParams.getAccountId());
        deposit.setCreateTime(DateUtil.format(new Date(), DateUtil.FORMAT_25_DATE_TIME));
        deposit.setModifyTime(DateUtil.format(new Date(), DateUtil.FORMAT_25_DATE_TIME));
        deposit.setHandingback(Constants.Available.enable);
        deposit.setFundSource(pzpayPayParams.getFundSource());
        fundDepositMapper.insert(deposit);
        pzpayPayParams.setDepositId(deposit.getId());
    }

    private MbrAccount getMbr(Integer accountId) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(accountId);
        return mbrAccountMapper.selectOne(mbrAccount);
    }

    /**
     * 获取必须要签名的参数，并进行MD5计算并拼接参数
     *
     * @param params
     * @param key
     * @return
     */
    private String getSignParams(Map<String, Object> params, String key) {
        // Map<String, Object> params = jsonUtil.Entity2Map(entity);
        String urlParams = ASCIIUtils.formatUrlMap(params, false, false);
        String sign = MD5.getMD5((urlParams + "&key=" + key));
        return urlParams + "&sign=" + sign;
    }

    public Object getPzpayPictureUrl(TOpPay tOpPay, HttpServletRequest request) {
        String onlinePays = Arrays.toString(
                myOnlinepayMapper.queryPayType(DateUtil.format(new Date(), DateUtil.FORMAT_10_DATE)).toArray());
        //获取设备源
        String dev = request.getHeader("dev");
        Byte devSource = HttpsRequestUtil.getHeaderOfDev(dev);
        tOpPay.setDevSource(devSource);
        // 判断前端传参terminal=0为PC端 PC+移動，=1为移动端 PC+移動，不传或者其他为PC+移動
        if (!StringUtils.isEmpty(tOpPay.getTerminal())) {
            switch (tOpPay.getTerminal()) {
                case "0": {
                    tOpPay.setPayType((byte) 1);
                    break;
                }
                case "1": {
                    tOpPay.setPayType((byte) 2);
                    break;
                }
                default: {
                    tOpPay.setPayType((byte) 3);
                    break;
                }
            }
        } else {
            tOpPay.setPayType((byte) 3);
        }
        return myOnlinepayMapper.getPzpayPictureUrl(tOpPay, onlinePays.substring(1, onlinePays.length() - 1));
    }

    public static void main(String[] args) {
        System.out.println("158ead8309274816ba2356e1d7c10e62||8151254867699080".split(" /||/")[0]);
    }
}
