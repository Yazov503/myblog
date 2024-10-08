package com.liu.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.liu.myblog.dao.Blog;
import com.liu.myblog.dao.SensitiveWord;
import com.liu.myblog.mapper.SensitiveWordMapper;
import com.liu.myblog.service.SensitiveWordService;
import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    @Resource
    private SensitiveWordBs sensitiveWordBs;

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Override
    public void refresh() {
        sensitiveWordBs.init();
    }

    @Override
    public SensitiveWord createOrUpdateSensitiveWord(SensitiveWord sensitiveWord) {
        sensitiveWord.setUpdateTime(new Date());
        if (sensitiveWord.getId() != null) {
            sensitiveWordMapper.updateById(sensitiveWord);
        } else {
            sensitiveWordMapper.insert(sensitiveWord);
        }
        return sensitiveWordMapper.selectSensitiveWord(sensitiveWord.getId());
    }

    @Override
    public void deleteSensitiveWord(long sensitiveWordId) {
        sensitiveWordMapper.deleteSensitiveWord(sensitiveWordId);
    }

    @Override
    public IPage<SensitiveWord> getSensitiveWordByPage(int pageNum, int pageSize, String queryText, int type) {
        Page<SensitiveWord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SensitiveWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false).eq("type", type).orderByDesc("id");
        if (StringUtils.isNotEmpty(queryText))
            queryWrapper.and(wrapper -> wrapper.like("word", queryText));
        return sensitiveWordMapper.selectPage(page, queryWrapper);
    }

}
