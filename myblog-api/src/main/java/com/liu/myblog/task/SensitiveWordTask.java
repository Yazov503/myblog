package com.liu.myblog.task;


import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.service.SensitiveWordService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;

@Component
public class SensitiveWordTask {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordTask.class);

    @Resource
    private SensitiveWordService sensitiveWordService;

    @XxlJob("sensitiveWordJobHandler")
    public void sensitiveWordJobHandler() {
        sensitiveWordService.refresh();
        logger.info("敏感词刷新成功，时间：{}", LocalDate.now());
    }
}
