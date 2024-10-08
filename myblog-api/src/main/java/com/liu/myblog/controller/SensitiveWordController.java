package com.liu.myblog.controller;

import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.common.SensitiveWordType;
import com.liu.myblog.dao.SensitiveWord;
import com.liu.myblog.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sensitive-word")
public class SensitiveWordController {

    @Resource
    private SensitiveWordService sensitiveWordService;

    @PostMapping("/refresh")
    @RequiredAdmin
    public ReturnData refresh() {
        sensitiveWordService.refresh();
        return ReturnData.success();
    }

    @PostMapping
    @RequiredAdmin
    public ReturnData createOrUpdateBlackWord(@RequestBody SensitiveWord sensitiveWord) {
        return ReturnData.success(sensitiveWordService.createOrUpdateSensitiveWord(sensitiveWord));
    }

    @DeleteMapping("/{id}")
    @RequiredAdmin
    public ReturnData deleteBlackWord(@PathVariable("id") long sensitiveWordId) {
        sensitiveWordService.deleteSensitiveWord(sensitiveWordId);
        return ReturnData.success();
    }

    @GetMapping("/black/page")
    @RequiredAdmin
    public ReturnData getBlackWordByPage(@RequestParam("pageNum") int pageNum,
                                         @RequestParam("pageSize") int pageSize,
                                         @RequestParam(value = "queryText", required = false, defaultValue = "")
                                             String queryText) {
        return ReturnData.success(sensitiveWordService.getSensitiveWordByPage(pageNum,pageSize,queryText,
                SensitiveWordType.BLACKLIST.getValue()));
    }

    @GetMapping("/white/page")
    @RequiredAdmin
    public ReturnData getWhiteWordByPage(@RequestParam("pageNum") int pageNum,
                                         @RequestParam("pageSize") int pageSize,
                                         @RequestParam(value = "queryText", required = false, defaultValue = "")
                                             String queryText) {
        return ReturnData.success(sensitiveWordService.getSensitiveWordByPage(pageNum,pageSize,queryText,
                SensitiveWordType.WHITELIST.getValue()));
    }
}
