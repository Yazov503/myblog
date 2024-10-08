package com.liu.myblog.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.liu.myblog.service.CommonService;
import com.liu.myblog.util.QiniuCloudUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Service
public class CommonServiceImpl implements CommonService {

    @Resource
    private QiniuCloudUtil qiniuCloudUtil;

    private static final Snowflake snowflake = IdUtil.getSnowflake(1, 1);
    @Override
    public String uploadImg(MultipartFile image) {
        String fileName = image.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) return null;

        String suffix = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        return qiniuCloudUtil.upload(image, snowflake.nextIdStr() + suffix);
    }
}
