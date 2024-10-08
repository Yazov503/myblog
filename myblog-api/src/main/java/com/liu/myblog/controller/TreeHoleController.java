package com.liu.myblog.controller;

import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.TreeHole;
import com.liu.myblog.service.TreeHoleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/treehole")
public class TreeHoleController {

    @Resource
    private TreeHoleService treeHoleService;

    @PostMapping
    public ReturnData addTreeHole(@RequestBody TreeHole treeHole) {
        return ReturnData.success(treeHoleService.addTreeHole(treeHole));
    }

    @GetMapping
    public ReturnData getTreeHoles(@RequestParam("pageSize") int pageSize) {
        return ReturnData.success(treeHoleService.getTreeHoles(pageSize));
    }
}
