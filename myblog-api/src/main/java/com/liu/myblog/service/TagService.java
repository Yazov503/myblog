package com.liu.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.Tag;

import java.util.List;

public interface TagService {
    List<Tag> getTags();

    IPage<Tag> getTagsByPage(int pageNum, int pageSize, String queryText);

    ReturnData createOrUpdateTag(Tag tag);

    void deleteTag(Long tagId);
}
