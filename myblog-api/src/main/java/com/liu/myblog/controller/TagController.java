package com.liu.myblog.controller;

import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.annotation.SkipLoginCheck;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.Tag;
import com.liu.myblog.service.TagService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @GetMapping
    @SkipLoginCheck
    public ReturnData getTags() {
        List<Tag> tags = tagService.getTags();
        if (tags == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "标签获取失败");
        return ReturnData.success(tags);
    }

    @GetMapping("/page")
    @SkipLoginCheck
    public ReturnData getTagsByPage(@RequestParam("pageNum") int pageNum,
                                    @RequestParam("pageSize") int pageSize,
                                    @RequestParam(value = "queryText", required = false, defaultValue = "")
                                    String queryText) {
        return ReturnData.success(tagService.getTagsByPage(pageNum, pageSize, queryText));
    }

    @PostMapping
    @RequiredAdmin
    public ReturnData createOrUpdateTag(@RequestBody Tag tag) {
        return tagService.createOrUpdateTag(tag);
    }

    @DeleteMapping("/{id}")
    @RequiredAdmin
    public ReturnData deleteTag(@PathVariable("id") Long tagId) {
        tagService.deleteTag(tagId);
        return ReturnData.success();
    }
}
