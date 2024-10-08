package com.liu.myblog.task;

import com.liu.myblog.service.BlogService;
import com.liu.myblog.service.CommentService;
import com.liu.myblog.service.ReplyService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Component
public class DataStatisticsTask {

    private static final Logger logger = LoggerFactory.getLogger(DataStatisticsTask.class);

    @Resource
    private BlogService blogService;

    @Resource
    private CommentService commentService;

    @Resource
    private ReplyService replyService;

    //    // 定时任务 - 每小时整点执行一次
//    // 每日博客数
//    @Scheduled(cron = "0 0 * * * ?")
//    public void updateTodayBlogCount() {
//        blogService.updateDailyBlogCount(LocalDate.now());
//    }
//
//    // 每日评论数
//    @Scheduled(cron = "0 0 * * * ?")
//    public void updateTodayCommentCount() {
//        commentService.updateDailyCommentCount(LocalDate.now());
//    }
//
//    // 每日回复数
//    @Scheduled(cron = "0 0 * * * ?")
//    public void updateTodayReplyCount() {
//        replyService.updateDailyReplyCount(LocalDate.now());
//    }

    @XxlJob("dataStatisticsJobHandler")
    public void dataStatisticsJobHandler() {
        LocalDate today = LocalDate.now();

        try {
            blogService.updateDailyBlogCount(today);
            logger.info("Updated daily blog count for date: {}", today);
        } catch (Exception e) {
            logger.error("Failed to update daily blog count", e);
        }

        try {
            commentService.updateDailyCommentCount(today);
            logger.info("Updated daily comment count for date: {}", today);
        } catch (Exception e) {
            logger.error("Failed to update daily comment count", e);
        }

        try {
            replyService.updateDailyReplyCount(today);
            logger.info("Updated daily reply count for date: {}", today);
        } catch (Exception e) {
            logger.error("Failed to update daily reply count", e);
        }
    }
}