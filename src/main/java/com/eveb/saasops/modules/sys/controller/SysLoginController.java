package com.eveb.saasops.modules.sys.controller;

import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.api.modules.apisys.service.TCpSiteService;
import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.utils.AESUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.sys.entity.LoggingParam;
import com.eveb.saasops.modules.sys.entity.SysRoleEntity;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import com.eveb.saasops.modules.sys.service.SysRoleService;
import com.eveb.saasops.modules.sys.service.SysUserRoleService;
import com.eveb.saasops.modules.sys.service.SysUserService;
import com.eveb.saasops.modules.sys.service.SysUserTokenService;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 登录相关
 */
@Slf4j
@RestController
@RequestMapping("/bkapi/sys/")
public class SysLoginController extends AbstractController {
    @Autowired
    private Producer producer;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserTokenService sysUserTokenService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private TCpSiteService tCpSiteService;

    @Value("${is.verificationCode.verify}")
    private Boolean codeVerify;

    /**
     * 验证码
     */
    @SysLog(module = "验证码", methodText = "后台系统登录获取验证码")
    @RequestMapping(value = "captcha.jpg", method = RequestMethod.GET)
    public void captcha(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpg");
        //生成文字验证码
        String text = producer.createText();
        //生成图片验证码
        BufferedImage image = producer.createImage(text);
        //保存到shiro session
        //ShiroUtils.setSessionAttribute(Constants.KAPTCHA_SESSION_KEY, text);
        session.setAttribute(Constants.KAPTCHA_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    /**
     * 登录
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Map<String, Object> login(@RequestBody LoggingParam loggingParam, HttpServletRequest request) throws IOException {
        String username = loggingParam.getUsername();
        String password = loggingParam.getPassword();
        String captcha = loggingParam.getCaptcha();
        if (Boolean.TRUE.equals(codeVerify)) {
            Object s = request.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
            if (s == null) {
                return R.error(2000, "验证码无法获取，联系管理员");
            }
            String kaptcha = s.toString();
            if (!captcha.equalsIgnoreCase(kaptcha)) {
                return R.error(2000, "验证码不正确");
            }
        }

        // 用户信息
        SysUserEntity user = sysUserService.queryByUserName(username);
        // 账号不存在、密码错误
        if (user == null || !user.getPassword().equals(new Sha256Hash(password, user.getSalt()).toHex())) {
            return R.error("账号或密码不正确");
        }
        // 账号锁定
        if (user.getStatus() == 0) {
            return R.error("账号已被锁定,请联系管理员");
        }

        // 用户角色判断
        List<Long> roleIds = sysUserRoleService.queryRoleIdList(user.getUserId());
        if (CollectionUtils.isNotEmpty(roleIds)) {
            SysRoleEntity roleEntity = sysRoleService.queryObject(roleIds.get(0));
            if (roleEntity != null && roleEntity.getIsEnable() == 0) {
                return R.error("账号未分配角色,请联系管理员");
            }
        } else {
            return R.error("账号未分配角色,请联系管理员");
        }

        // 生成token，并保存到数据库
        String domainUrl = CommonUtil.requestUrl(request.getRequestURL().toString());
        user.setDomainUrl(domainUrl);
        return sysUserTokenService.createToken(user.getUserId(), domainUrl);
    }

    @GetMapping(value = "/getSiteCode")
    public R getSiteCode(@RequestParam("url") String url) {
        TCpSite tCpSite = tCpSiteService.queryOneCond(CommonUtil.getDomainForUrl(url));
        try {
            return R.ok().put(SystemConstants.STOKEN, AESUtil.encrypt(tCpSite.getSiteCode()));
        } catch (Exception e) {
            log.error("getSiteCode", e);
            return R.ok();
        }

    }


    /**
     * 退出
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public R logout() {
        sysUserTokenService.logout(getUserId());
        return R.ok();
    }

}
