package com.eveb.saasops.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.csource.fastdfs.*;
import org.springframework.stereotype.Component;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.NameValuePair;

import java.io.*;
import java.util.Objects;

import static com.eveb.saasops.common.utils.StringUtil.isEmpty;

@Slf4j
@Component
public class FastDFSUtil {

    private static final String SPRIT = "/";

    public void initByProperties(){
        try {
            ClientGlobal.initByProperties("application-env.properties");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getJointFileName(String[] files) {
        if (Objects.nonNull(files)) {
            return files[0] + SPRIT + files[1];
        }
        return null;
    }

    public String[] uploadFile(File file, String uploadFileName) {
        try {
            InputStream is = new FileInputStream(file);
            return uploadFile(IOUtils.toByteArray(is), uploadFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] uploadFile(byte[] fileBuff, String uploadFileName) {
        String[] files = null;
        String fileExtName = FilenameUtils.getExtension(uploadFileName);
        if (isEmpty(fileExtName)) {
            log.info("Fail to upload file, because the format of filename is illegal.");
        }
        try {
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient client = new StorageClient(trackerServer, storageServer);

            NameValuePair[] metaList = new NameValuePair[3];
            metaList[0] = new NameValuePair("fileName", uploadFileName);
            metaList[1] = new NameValuePair("fileExtName", fileExtName);
            metaList[2] = new NameValuePair("fileLength", String.valueOf(fileBuff.length));

            files = client.upload_file(fileBuff, fileExtName, metaList);
            trackerServer.close();
        } catch (Exception e) {
            log.error("Upload file \"" + uploadFileName + "\"fails", e);
        }
        return files;
    }

    public InputStream downloadFile(String groupName, String remoteFileName) {
        try {
            log.info("download file GROUP:[{}] NAME:[{}]", groupName, remoteFileName);
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient client = new StorageClient(trackerServer, storageServer);
            byte[] bytes = null;

            try {
                bytes = client.download_file(groupName, remoteFileName);
            } catch (Exception e) {
                log.error("fail to download file from fastDFS CODE:[{}]", e);
            }
            trackerServer.close();
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int deleteFile(String groupName, String remoteFileName) {
        try {
            log.info("delete file GROUP:[{}] NAME:[{}]", groupName, remoteFileName);

            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient client = new StorageClient(trackerServer, storageServer);

            int ex = client.delete_file(groupName, remoteFileName);
            if (ex != 0) {
                log.error("fail to delete file from fastDFS CODE:[{}]", ex);
            }
            trackerServer.close();
            return ex;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Integer.parseInt(null);
    }

    public static void saveInputStream(InputStream inputStream, String fileName, String path) {
        OutputStream os = null;
        try {
            byte[] bs = new byte[1024];
            int len;
            File tempFile = new File(path);
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            os = new FileOutputStream(tempFile.getPath() + File.separator + fileName);
            while ((len = inputStream.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
        } catch (Exception e) {
            log.error("Upload file savePic", e);
        } finally {
            try {
                os.close();
                inputStream.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }
}
