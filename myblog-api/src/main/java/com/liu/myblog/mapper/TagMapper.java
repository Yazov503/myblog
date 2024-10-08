package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    Tag selectTagById(Long tagId);

    void deleteTagById(Long tagId);

    Tag selectTagByName(String tagName);
}
