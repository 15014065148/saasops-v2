package com.eveb.saasops.common.constants;

import java.util.HashMap;
import java.util.Map;

public class MerchantPayConstants {

    public static final int HUI_TONG_PAY_ID = 1;
    public static final int PAN_ZI_PAY_ID = 2;


    public static final Map<String, String> htMerchantPayMap = new HashMap<String, String>() {{
        put("中国农业银行", "ABC");
        put("中国银行", "BOC");
        put("交通银行", "BOCOM");
        put("中国建设银行", "CCB");
        put("中国工商银行", "ICBC");
        put("中国邮政储蓄银行", "PSBC");
        put("招商银行", "CMBC");
        put("浦发银行", "SPDB");
        put("中国光大银行", "CEBBANK");
        put("中信银行", "ECITIC");
        put("平安银行", "PINGAN");
        put("中国民生银行", "CMBCS");
        put("华夏银行", "HXB");
        put("广发银行", "CGB");
        put("北京银行", "BCCB");
        put("上海银行", "BOS");
        put("兴业银行", "CIB");
    }};

    public static final Map<String, String> pzMerchantPayMap = new HashMap<String, String>() {{
        put("中国工商银行", "ABC");
        put("中国银行", "BOC");
        put("中国建设银行", "CCB");
        put("中国农业银行", "ABC");
        put("交通银行", "BOCOM");
        put("招商银行", "CMB");
        put("民生银行", "CMBC");
        put("平安银行", "PAB");
        put("华夏银行", "HXBC");
        put("中国邮政储蓄银行", "PSBC");
        put("兴业银行", "CIB");
        put("中国光大银行", "CEB");
        put("中信银行", "CITIC");
        put("浦发银行", "SPDB");
    }};

}
