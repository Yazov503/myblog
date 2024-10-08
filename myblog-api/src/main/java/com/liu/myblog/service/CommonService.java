package com.liu.myblog.service;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    String uploadImg(MultipartFile image);

}
