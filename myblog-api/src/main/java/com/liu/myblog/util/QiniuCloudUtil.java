package com.liu.myblog.util;

import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.google.gson.Gson;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@ConfigurationProperties(prefix = "oss")
@Data
public class QiniuCloudUtil {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String url;

    public String upload(MultipartFile image, String fileName) {
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);

        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            // 获取图片的输入流
            byte[] uploadBytes = image.getBytes();
            Response response = uploadManager.put(uploadBytes, fileName, upToken);
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            // 返回文件的URL
            return url + "/" + putRet.key;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
