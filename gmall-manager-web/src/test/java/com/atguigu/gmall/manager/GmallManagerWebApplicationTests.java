package com.atguigu.gmall.manager;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerWebApplicationTests {
	@Test
	public void textFileUpload() throws IOException, MyException {
		//获取配置文件
		String file = this.getClass().getResource("/tracker.conf").getFile();
		ClientGlobal.init(file);
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer=trackerClient.getTrackerServer();
		StorageClient storageClient = new StorageClient(trackerServer, null);
		String orginalFileName="F://GuliStore/谷粒商城/测试图片/girl_umbrella.jpg";
		String url="http://192.168.45.131";
		String[] uploadFile = storageClient.upload_file(orginalFileName, "jpg", null);
		for (String s:uploadFile){
			System.out.println(s);
			System.out.println(url+="/"+s);

		}

	}

}
