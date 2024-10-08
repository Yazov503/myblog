package com.liu.myblog.util.sensitive;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.liu.myblog.common.SensitiveWordType;
import com.liu.myblog.mapper.SensitiveWordMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
public class MyWordAllow implements IWordAllow {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Override
    public List<String> allow() {
        return sensitiveWordMapper.selectSensitiveList(SensitiveWordType.WHITELIST.getValue());
    }
}
