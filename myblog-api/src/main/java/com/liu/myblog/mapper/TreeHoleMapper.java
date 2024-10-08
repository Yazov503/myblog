package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.TreeHole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TreeHoleMapper extends BaseMapper<TreeHole> {

    List<TreeHole> getTreeHoles(int pageSize);
}
