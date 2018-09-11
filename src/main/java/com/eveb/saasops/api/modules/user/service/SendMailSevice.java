package com.eveb.saasops.api.modules.user.service;

import com.eveb.saasops.api.config.ApiConfig;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.modules.system.systemsetting.dto.MailSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


@Service
@Slf4j
public class SendMailSevice{

/*	@Autowired
	private JavaMailSender javaMailSender;*/
	//@Autowired
	//private JavaMailSenderImpl senderImpl;

	//@Value("${spring.mail.username}")
	//private String serverMail;
    @Autowired
    private ApiConfig apiConfig;

	/*public void sendMail(List<String> toUser, String subject, String text) {
		MimeMessage message = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(serverMail);
			helper.setTo(toUser.toArray(new String[toUser.size()]));
			helper.setSubject(subject);
			helper.setText(text, true);

			javaMailSender.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}*/

	/*public void sendMail(String toUser, String subject, String text) throws MessagingException{
		MimeMessage message = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		
		helper.setFrom(serverMail);
		helper.setTo(toUser);
		helper.setSubject(subject);
		helper.setText(text, true);
		
		javaMailSender.send(message);
	}*/

	/**
	 * 邮件发送
	 * @param mailSet 必要参数
	 * @param toUser 发送给谁? 目标邮箱地址
	 * @param subject 邮件主题
	 * @param text 内容
	 * @return
	 */
	public boolean sendMail(MailSet mailSet,String toUser, String subject, String text)
	{
		try {
			JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();
			// 设定mail server
			senderImpl.setHost(mailSet.getMailSendServer());
			// 建立邮件消息,发送简单邮件和html邮件的区别
			MimeMessage mailMessage = senderImpl.createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "utf-8");
			// MimeMessageHelper messageHelper = new
			// MimeMessageHelper(mailMessage,true,"utf-8");
			// 设置收件人，寄件人
			messageHelper.setTo(toUser);
			messageHelper.setFrom(mailSet.getMailSendAccount());
			messageHelper.setSubject(subject);
			// true 表示启动HTML格式的邮件
			messageHelper.setText(text, true);
			senderImpl.setUsername(mailSet.getMailSendAccount());
			senderImpl.setPassword(mailSet.getMailPassword());
			senderImpl.setDefaultEncoding(mailSet.getCharacterSet());
			senderImpl.setPort(Integer.parseInt(mailSet.getMailSendPort()));
			Properties prop = new Properties();
			// 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
			prop.put(ApiConstants.MAIL_DEBUG, Boolean.TRUE);// 调试
			prop.put(ApiConstants.MAIL_SMTP_AUTH, apiConfig.getMailAuth());
			prop.put(ApiConstants.MAIL_SMTP_TIMEOUT, apiConfig.getMailTimeout());
			prop.put(ApiConstants.MAIL_SMTP_SSL_ENABLE, mailSet.getWetherSsl());
			senderImpl.setJavaMailProperties(prop);
			// 发送邮件
			senderImpl.send(mailMessage);
			return Boolean.TRUE;
		} catch (MessagingException e) {
			log.error("发送邮件错误消息：",e);
			return Boolean.FALSE;
		}
	}
	  

}
