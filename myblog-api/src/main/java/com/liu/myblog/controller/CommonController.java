package com.liu.myblog.controller;


import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.service.BlogService;
import com.liu.myblog.service.CommentService;
import com.liu.myblog.service.CommonService;
import com.liu.myblog.service.ReplyService;
import com.liu.myblog.util.TokenUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class CommonController {

    @Resource
    private CommonService commonService;

    @Resource
    private BlogService blogService;

    @Resource
    private CommentService commentService;

    @Resource
    private ReplyService replyService;

    @Resource
    private TokenUtil tokenUtil;

    @PostMapping("/upload")
    public ReturnData uploadImg(MultipartFile image,
                                HttpServletRequest request) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");

        String url = commonService.uploadImg(image);
        if (url == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "图片上传失败");
        return ReturnData.success(url);
    }

    @GetMapping("/statistics/blog-count")
    @RequiredAdmin
    public ReturnData getBlogCount(@RequestParam("startDate")
                                   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                   @RequestParam("endDate")
                                   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Long> blogCounts = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        if (endDate.isAfter(today)) {
            endDate = today;
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            blogService.updateDailyBlogCount(date);
            Long blogCount = blogService.getDailyBlogCount(date);
            blogCounts.put(date.toString(), blogCount);
        }

        return ReturnData.success(blogCounts);
    }

    @GetMapping("/statistics/comment-count")
    @RequiredAdmin
    public ReturnData getCommentAndReplyCount(@RequestParam("startDate")
                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam("endDate")
                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Long> counts = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        if (endDate.isAfter(today)) {
            endDate = today;
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            commentService.updateDailyCommentCount(date);
            Long blogCount = commentService.getDailyCommentCount(date);
            replyService.updateDailyReplyCount(date);
            Long replyCount = replyService.getDailyReplyCount(date);
            counts.put(date.toString(), blogCount+replyCount);
        }

        return ReturnData.success(counts);
    }

}
