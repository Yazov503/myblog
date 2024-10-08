package com.liu.myblog.service;

import com.liu.myblog.dao.TreeHole;

import java.util.List;

public interface TreeHoleService {

    TreeHole addTreeHole(TreeHole treeHole);

    List<TreeHole> getTreeHoles(int pageSize);
}
