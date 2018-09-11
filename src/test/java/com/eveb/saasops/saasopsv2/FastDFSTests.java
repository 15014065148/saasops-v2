package com.eveb.saasops.saasopsv2;

import com.eveb.saasops.common.utils.FastDFSUtil;

import org.csource.common.MyException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FastDFSTests {

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Test
    public void uploadFile() throws IOException, MyException {
    	 String list_name="d:\\pics.txt";
    	 String dir_name="C:\\Users\\William\\Pictures\\mg";
        Vector<String> ver = new Vector<String>();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
		FileOutputStream(list_name, true))); // 以追加的方式写入到指定的文件
		ver.add(dir_name);
		while (ver.size() > 0) {
			File[] files = new File(ver.get(0).toString()).listFiles(); // 获取该文件夹下所有的文件(夹)名
			ver.remove(0);
			int len = files.length;
			for (int i = 0; i < len; i++) {
				String tmp = files[i].getAbsolutePath();
				if (files[i].isDirectory()) // 如果是目录，则加入队列。以便进行后续处理
					ver.add(tmp);
				else
				{
					File file = new File(tmp);
					//System.out.println(tmp.substring(tmp.indexOf(".")));
					//System.out.println(tmp.substring(tmp.lastIndexOf("\\")+1));
					
					//String fileName = UUID.randomUUID().toString() + tmp.substring(tmp.indexOf("."));
					String fileName=tmp.substring(tmp.lastIndexOf("\\")+1);
			       fastDFSUtil.initByProperties();
			        String[] files1 = fastDFSUtil.uploadFile(file, fileName);
			        //byte[] fileBuff = IOUtils.toByteArray(fileInputStream);
			        //System.out.println(fastDFSUtil.getJointFileName(files));
			        out.write( "insert into bb(b1,b2) values (\'"+fastDFSUtil.getJointFileName(files1) +"\',\'"+fileName+"\');"+"\r\n"); // 如果是文件，则直接输出文件名到指定的文件。
			        
				}
				
					//System.out.println(tmp);
			}
		}
		out.close();
        
    }

/*    @Test
    public void downloadFile() throws IOException, MyException {
        String groupName = "group1";
        String remoteFileName = "M00/00/00/yj1WlVpxiBKAVII5AAE_4W7ABOM711.jpg";

        fastDFSUtil.initByProperties();

        InputStream inputStream = fastDFSUtil.downloadFile(groupName, remoteFileName);
        fastDFSUtil.saveInputStream(inputStream, "33333333.jpg", "D:\\images\\test");
    }

    @Test
    public void deleteFile() throws IOException, MyException {
        String groupName = "";
        String remoteFileName = "";

        fastDFSUtil.initByProperties();

        fastDFSUtil.deleteFile(groupName, remoteFileName);
    }*/

	//@Autowired
	//private MbrDepotWalletService mbrDepotWalletService;

/*	*//**
	 * 测试异步方法调用顺序
	 *//*
	@Test
	public void getEntityById() throws Exception {

		long start = System.currentTimeMillis();

		Future<String> task1 = mbrDepotWalletService.doTaskOne();
		Future<String> task2 = mbrDepotWalletService.doTaskTwo();
		Future<String> task3 = mbrDepotWalletService.doTaskThree();

		while(true) {
			if(task1.isDone() && task2.isDone() && task3.isDone()) {
				// 三个任务都调用完成，退出循环等待
				break;
			}
			Thread.sleep(1000);
		}

		long end = System.currentTimeMillis();

		System.out.println("任务全部完成，总耗时：" + (end - start) + "毫秒");

	}*/
}
