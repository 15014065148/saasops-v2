package com.eveb.saasops.api.modules.user.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.api.constants.ApiConstants;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user/captcha")
@Api(value = "api", description = "前端会员 验证码")
@Slf4j
public class CaptchaController {
    @Autowired
    private Producer producer;

    /**
     * 注册验证码(图片)
     */
    @GetMapping("/reg.jpg")
    @ApiOperation(value = "会员注册验证码")
    public void captchaReg(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        // 生成文字验证码
        String text = producer.createText();
        // 生成图片验证码
        BufferedImage image = producer.createImage(text);
        session.setAttribute(ApiConstants.KAPTCHA_REG_SESSION_KEY, text);
        log.info("验证码【" + session.getAttribute(ApiConstants.KAPTCHA_REG_SESSION_KEY) + "】====================" + session.getId());
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    /**
     * 登陆验证码(图片)
     */
    @GetMapping("/login.jpg")
    @ApiOperation(value = "会员登陆验证码")
    public void captchaLogin(HttpServletResponse response, HttpSession session) throws ServletException, IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        try {
            response.setHeader("Cache-Control", "no-store, no-cache");
            response.setContentType("image/jpeg");
            // 生成文字验证码
            String text = producer.createText();
            // 生成图片验证码
            BufferedImage image = producer.createImage(text);
            // 保存到shiro session
            session.setAttribute(ApiConstants.KAPTCHA_LOGIN_SESSION_KEY, text);
            ServletOutputStream out = response.getOutputStream();
            ImageIO.write(image, "jpg", out);
            IOUtils.closeQuietly(out);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("----------Captcha Create Error!!!!!!");
        } finally {
            String endDate = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
            log.debug("==============================start time>:" + endDate + "=============================================");
        }
    }

    /**
     * 找回密码验证码(图片)
     */
    @GetMapping("/retrvpwd.jpg")
    @ApiOperation(value = "会员找回密码验证码")
    public void captchaFindPwd(HttpServletResponse response, HttpSession session) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        // 生成文字验证码
        String text = producer.createText();
        // 生成图片验证码
        BufferedImage image = producer.createImage(text);
        session.setAttribute(ApiConstants.KAPTCHA_RETRVPWD_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }
}
