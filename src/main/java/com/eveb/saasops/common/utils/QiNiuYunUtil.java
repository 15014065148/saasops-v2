package com.eveb.saasops.common.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 七牛云操作相关API:
 */
@Slf4j
@Component
public class QiNiuYunUtil {

	@Value("${qiniuyun.accessKey}")
	private String accessKey;
	@Value("${qiniuyun.secretKey}")
	private String secretKey;
	@Value("${qiniuyun.bucket}")
	private String bucket;
	@Value("${qiniuyun.url}")
	private String domainOfBucket;

	public String uploadFile(File file, String uploadFileName) {
		try {
			InputStream is = new FileInputStream(file);
			return uploadFile(IOUtils.toByteArray(is), uploadFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取七牛云上的所有文件列表
	 * 
	 * @return 文件名列表
	 */
	public List<String> bucketFileList() {
		// 构造一个带指定Zone对象的配置类
		Configuration cfg = new Configuration(Zone.zone0());
		// ...其他参数参考类注释
		Auth auth = Auth.create(accessKey, secretKey);
		BucketManager bucketManager = new BucketManager(auth, cfg);
		List<String> list = new ArrayList<>();
		// 文件名前缀
		String prefix = "";
		// 每次迭代的长度限制，最大1000，推荐值 1000
		int limit = 1000;
		// 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
		String delimiter = "";
		// 列举空间文件列表
		BucketManager.FileListIterator fileListIterator = bucketManager.createFileListIterator(bucket, prefix, limit,
				delimiter);
		while (fileListIterator.hasNext()) {
			// 处理获取的file list结果
			FileInfo[] items = fileListIterator.next();
			for (FileInfo item : items) {
				list.add(item.key);
			}
		}
		return list;
	}

	/**
	 * 上传 通过字节数组与文件名的形式上传
	 * 
	 * @param fileBuff:上传的字节数组
	 * @param uploadFileName："111.png"
	 * @return fileName:"FhAUqPq3FZjRSDYZKK4TOKfnac3T"无后缀名
	 */
	public String uploadFile(byte[] fileBuff, String uploadFileName) {
		String fileName = null;
		String key = null;
//		String fileExtName = FilenameUtils.getExtension(uploadFileName);
//		if (isEmpty(fileExtName)) {
//			log.info("Fail to upload file, because the format of filename is illegal.");
//		}
		try {
			Configuration cfg = new Configuration(Zone.zone0());
			UploadManager uploadManager = new UploadManager(cfg);
			Auth auth = Auth.create(accessKey, secretKey);
			String upToken = auth.uploadToken(bucket);
			Response response = uploadManager.put(fileBuff, key, upToken);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			fileName = domainOfBucket+putRet.key;
			return fileName;
		} catch (QiniuException ex) {
			log.error("Upload file [" + uploadFileName + "] fails", ex);
			Response r = ex.response;
			ex.printStackTrace();
			try {
				System.err.println(r.bodyString());
			} catch (QiniuException ex2) {
				// ignore
			}
		}
		return fileName;
	}

	/**
	 * 上传 通过本地路径直接上传
	 * 
	 * @param filePath：本地图片路径：F:\\****.png
	 * @return fileName: "FhAUqPq3FZjRSDYZKK4TOKfnac3T.png"
	 */
	public String uploadFile(String filePath) {
		if (null == filePath) {
			return null;
		}
		try {
			// 构造一个带指定Zone对象的配置类
			Configuration cfg = new Configuration(Zone.zone0());
			UploadManager uploadManager = new UploadManager(cfg);
			Auth auth = Auth.create(accessKey, secretKey);
			String upToken = auth.uploadToken(bucket);
			// 默认不指定key的情况下，以文件内容的hash值作为文件名
			String key = null;
			Response response = uploadManager.put(filePath, key, upToken);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			String fileName = domainOfBucket+putRet.key;
			return fileName;
		} catch (QiniuException ex) {
			Response r = ex.response;
			log.error("Upload file [" + filePath + "] fails", ex);
			ex.printStackTrace();
			try {
				System.err.println(r.bodyString());
			} catch (QiniuException ex2) {
			}
		}
		return null;
	}

	/**
	 * 下载 通过文件名
	 * 
	 * @param fileName:"FhAUqPq3FZjRSDYZKK4TOKfnac3T"
	 * @param savePath:"d:/resource/images/diaodiao/country/"
	 * @throws Exception
	 */
	public void downLoadFile(String fileName, String savePath) {
		URL url;
		// 得到输入流
		InputStream inputStream = null;
		FileOutputStream fos = null;
		try {
			String encodedFileName = URLEncoder.encode(fileName, "utf-8");
			String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
			Configuration cfg = new Configuration(Zone.zone0());
			Auth auth = Auth.create(accessKey, secretKey);
			BucketManager bucketManager = new BucketManager(auth, cfg);
			FileInfo fileInfo = bucketManager.stat(bucket, fileName);
			String QiniuSuffixName=fileInfo.mimeType;//image/jpeg
			String suffixName=QiniuSuffixName.substring(QiniuSuffixName.indexOf("/")+1);
			long expireInSeconds = 3600;// 1小时，可以自定义链接过期时间
			String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
			log.info("dowload:" + finalUrl);
			url = new URL(finalUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 设置超时间为3秒
			conn.setConnectTimeout(3 * 1000);
			// 防止屏蔽程序抓取而返回403错误
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			inputStream = conn.getInputStream();
			// 获取自己数组
			byte[] getData = readInputStreamAsByteArray(inputStream);
			// 文件保存位置
			File saveDir = new File(savePath);
			if (!saveDir.exists()) {
				saveDir.mkdir();
			}
			File file = new File(saveDir + File.separator + fileName+"."+suffixName);
			fos = new FileOutputStream(file);
			fos.write(getData);
			log.info("info:" + fileName + "download success");
		} catch (Exception ex) {
			log.error("DownLoad file [" + fileName + "] fails", ex);
			ex.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 删除 通过文件名直接删除七牛云上的图片
	 * 
	 * @param fileName:"FhAUqPq3FZjRSDYZKK4TOKfnac3T"无后缀名
	 */
	public String deleteFile(String fileName) {
		Configuration cfg = new Configuration(Zone.zone0());
		Auth auth = Auth.create(accessKey, secretKey);
		BucketManager bucketManager = new BucketManager(auth, cfg);
		fileName=fileName.substring(fileName.lastIndexOf("/")+1);
		try {
			Response response = bucketManager.delete(bucket, fileName);
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			return domainOfBucket+putRet.key;
		} catch (QiniuException ex) {
			// 如果遇到异常，说明删除失败
			log.error("Delete file [" + fileName + "] fails", ex);
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 流转换成数组
	 * 
	 * @param inputStream
	 * @return 字节数组
	 * @throws Exception
	 */
	public byte[] readInputStreamAsByteArray(InputStream inputStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}

	/**
	 * 截掉文件后缀
	 * 
	 * @param fileName:FhAUqPq3FZjRSDYZKK4TOKfnac3T.png
	 * @return prefixName:FhAUqPq3FZjRSDYZKK4TOKfnac3T
	 */
	public String cutSuffixName(String fileName) {
		if (fileName.contains(".")) {
			String prefixName = fileName.substring(0, fileName.lastIndexOf("."));
			return prefixName;
		}
		return fileName;
	}

	/**
	 * 替换表中的路径
	 * @param mySqlUrl
	 * @param username
	 * @param password
	 * @param tableName
	 * @param column1
	 * @param column2
	 * @param savePath
	 */
	public void replaceTablePath(String mySqlUrl, String username, String password, String tableName, String column1,
			String column2, String savePath) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(mySqlUrl, username, password);
			String sql = "select * from " + tableName;
			File file = new File(savePath);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bfw = new BufferedWriter(fw);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			//遍历表中查询出来的数据
			while (resultSet.next()) {
				int id = resultSet.getInt(column1);
				String picPath = resultSet.getString(column2);
				String fileUrl=picPath;
				if(null==picPath||picPath.equals("")) {
					continue;
				}
				if(picPath.startsWith("M00")) {
					fileUrl="group1/"+picPath;
				}
				if(!picPath.contains("http")) {
					fileUrl="http://pic.evebcomp.com/"+fileUrl;
				}else {
					fileUrl=picPath;
				}
				//通过URL来下载图片并上传到七牛云
				URL url = new URL(fileUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				// 设置超时间为3秒
				conn.setConnectTimeout(3 * 1000);
				// 防止屏蔽程序抓取而返回403错误
				conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
				InputStream inputStream = conn.getInputStream();
				byte[] data = readInputStreamAsByteArray(inputStream);
				String uploadFile = uploadFile(data, picPath);
				bfw.write("update " + tableName + " set " + column2 + "= '" + uploadFile + "' where " + column1 + "=" + id
						+ "; -- "+"["+picPath+"]");
				bfw.newLine();
				bfw.flush();
			}
			bfw.close();
			connection.close();
		} catch (Exception e) {
			System.out.println("----------------------------");
		}

	}

}
