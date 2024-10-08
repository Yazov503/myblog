package com.liu.myblog.service.impl;

import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.dao.TreeHole;
import com.liu.myblog.mapper.TreeHoleMapper;
import com.liu.myblog.service.TreeHoleService;
import com.liu.myblog.util.RedisUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TreeHoleServiceImpl implements TreeHoleService {

    @Resource
    private TreeHoleMapper treeHoleMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public TreeHole addTreeHole(TreeHole treeHole) {
        treeHole.setCreateTime(new Date());
        treeHoleMapper.insert(treeHole);
        redisUtil.zsAdd(RedisKeyConstant.TREE_HOLE, treeHole, treeHole.getCreateTime().getTime());
        return treeHole;
    }

    @Override
    public List<TreeHole> getTreeHoles(int pageSize) {
        Set<Object> objects = redisUtil.zsGetTop(RedisKeyConstant.TREE_HOLE, pageSize);
        if (objects != null) {
            return objects.stream()
                    .filter(obj -> obj instanceof TreeHole)
                    .map(obj -> (TreeHole) obj)
                    .collect(Collectors.toList());
        }
        List<TreeHole> treeHoles = treeHoleMapper.getTreeHoles(pageSize);
        if (treeHoles != null) {
            treeHoles.forEach(treeHole -> redisUtil.zsAdd(RedisKeyConstant.TREE_HOLE, treeHole, treeHole.getCreateTime().getTime()));
            return treeHoles;
        }
        return new ArrayList<>();
    }
}
