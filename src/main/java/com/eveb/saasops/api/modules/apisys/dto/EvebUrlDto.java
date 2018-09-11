package com.eveb.saasops.api.modules.apisys.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@Getter
public class EvebUrlDto {

    private String siteCode;
    private String url;
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (!StringUtils.isEmpty(siteCode))
            buffer.append("&eveb_id=").append(siteCode);
        if (!StringUtils.isEmpty(url))
            buffer.append("&url=").append(url);
        if (!StringUtils.isEmpty(buffer))
            buffer.delete(0, 1);
        return buffer.toString();
    }
}
