package com.atguigu.gmall.manager.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

public class PmsUploadUtil {
    public static String uploadImage(MultipartFile multipartFile) {
        String ImgUrl="http://192.168.45.131";
        //获取配置文件
        String file = PmsUploadUtil.class.getResource("/tracker.conf").getPath();
        String originalFilename = multipartFile.getOriginalFilename();
        int i = originalFilename.lastIndexOf(".");
        String fileExtName = originalFilename.substring(i + 1);
        try {
            ClientGlobal.init(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            byte[] bytes = multipartFile.getBytes();
            TrackerClient trackerClient = new TrackerClient();
             TrackerServer  trackerServer = trackerClient.getTrackerServer();
            StorageClient storageClient = new StorageClient(trackerServer, null);
            String[]  uploadFile = storageClient.upload_file(bytes, fileExtName, null);
            for (String s:uploadFile){
                System.out.println(s);
                System.out.println(ImgUrl+="/"+s);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ImgUrl;
    }
}
