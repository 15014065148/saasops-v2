package com.eveb.saasops.sysapi.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eveb.saasops.common.utils.QiNiuYunUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sysapi")
@Api(value = "大后台项目", description = "提供给大后台项目的服务")
public class BizCommonController {

	@Value("${qiniuyun.url}")
	private String qiNiuYunUrl;

	@Autowired
	private QiNiuYunUtil qiNiuYunUtil;

	@RequestMapping("/uploadpic")
	@ApiOperation(value = "上传图片", notes = "上传图片")
	public R depotBalance(@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile) {
		Assert.isNull(uploadFile, "上传文件不能为空");
		byte[] fileBuff = null;
		try {
			fileBuff = IOUtils.toByteArray(uploadFile.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String uploadFileName = uploadFile.getOriginalFilename();
		String fileName = qiNiuYunUtil.uploadFile(fileBuff, uploadFileName);
		Map<String, String> map = new HashMap<>();
		map.put("url", qiNiuYunUrl);
		map.put("fileName", fileName);
		return R.ok().put(map);
	}
}
