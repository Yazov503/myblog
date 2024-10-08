package com.liu.myblog.util.sensitive;

import com.github.houbb.sensitive.word.api.IWordDeny;
import com.liu.myblog.common.SensitiveWordType;
import com.liu.myblog.mapper.SensitiveWordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MyWordDeny implements IWordDeny {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Override
    public List<String> deny() {
        return sensitiveWordMapper.selectSensitiveList(SensitiveWordType.BLACKLIST.getValue());
    }
}
