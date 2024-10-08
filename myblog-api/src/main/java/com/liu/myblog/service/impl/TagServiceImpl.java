package com.liu.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.Tag;
import com.liu.myblog.mapper.TagMapper;
import com.liu.myblog.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Resource
    private TagMapper tagMapper;

    @Override
    public List<Tag> getTags() {
        return tagMapper.selectList(new QueryWrapper<Tag>()
                .eq("is_deleted", false));
    }

    @Override
    public IPage<Tag> getTagsByPage(int pageNum, int pageSize, String queryText) {
        Page<Tag> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false).orderByDesc("create_time");
        if (StringUtils.isNotEmpty(queryText))
            queryWrapper.and(wrapper -> wrapper.like("tag_name", queryText));
        return tagMapper.selectPage(page, queryWrapper);
    }

    @Override
    public ReturnData createOrUpdateTag(Tag tag) {
        if (tag.getId() != null) {
            tagMapper.updateById(tag.setUpdateTime(new Date()));
        } else {
            if(tagMapper.selectTagByName(tag.getTagName())!=null)
                return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "标签名已存在");
            tagMapper.insert(tag);
        }
        return ReturnData.success();
    }

    @Override
    public void deleteTag(Long tagId) {
        tagMapper.deleteTagById(tagId);
    }
}
