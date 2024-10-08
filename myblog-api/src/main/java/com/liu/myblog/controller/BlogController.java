package com.liu.myblog.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.annotation.ApiIdempotent;
import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.annotation.SkipLoginCheck;
import com.liu.myblog.common.BlogStatus;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.Blog;
import com.liu.myblog.dao.Comment;
import com.liu.myblog.dao.Notification;
import com.liu.myblog.dao.Reply;
import com.liu.myblog.dao.dto.BlogDto;
import com.liu.myblog.dao.dto.CommentDto;
import com.liu.myblog.dao.dto.ReplyDto;
import com.liu.myblog.dao.vo.BlogVo;
import com.liu.myblog.rabbitmq.MessageProducer;
import com.liu.myblog.service.BlogService;
import com.liu.myblog.service.CommentService;
import com.liu.myblog.service.ReplyService;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.util.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @Resource
    private CommentService commentService;

    @Resource
    private ReplyService replyService;

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private MessageProducer messageProducer;

    @Resource
    private RedisUtil redisUtil;

    // BLOG API
    @PostMapping
    @ApiIdempotent
    public ReturnData createOrUpdateBlog(@RequestBody BlogDto blogDto,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token))
            return ReturnData.fail(CodeEnum.AUTH_ERROR.getCode(), "用户未登录");
        Long userId = tokenUtil.getUserId(request);

        if (blogDto.getId() != null && blogDto.getUserId() != userId)
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "你无权修改他人的博客");

        if (blogDto.getId() == null && redisUtil.exist(RedisKeyConstant.MUTE + userId))
            return ReturnData.fail(CodeEnum.MUTE_ERROR.getCode(), "你已被禁言,结束时间为：" +
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()
                            + redisUtil.getExpire(RedisKeyConstant.MUTE + userId) * 1000)));

        blogDto.setUserId(userId);
        Blog blog = blogService.createOrUpdateBlog(blogDto);
        if (blog == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "创建失败");
        return ReturnData.success(blog);
    }

    @PostMapping("/drawback")
    @RequiredAdmin
    public ReturnData drawbackBlog(@RequestBody BlogDto blogDto,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        blogDto.setStatus(BlogStatus.REJECTED.getCode());
        return ReturnData.success(blogService.createOrUpdateBlog(blogDto));
    }

    @PostMapping("/access")
    @RequiredAdmin
    public ReturnData accessBlog(@RequestBody BlogDto blogDto,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        blogDto.setStatus(BlogStatus.APPROVED.getCode());
        return ReturnData.success(blogService.createOrUpdateBlog(blogDto));
    }

    @GetMapping("/page")
    @SkipLoginCheck
    public ReturnData getBlogsByPage(@RequestParam("pageNum") int pageNum,
                                     @RequestParam("pageSize") int pageSize,
                                     @RequestParam(value = "queryText", required = false, defaultValue = "")
                                     String queryText,
                                     @RequestParam(value = "queryTags", required = false, defaultValue = "")
                                     List<Integer> queryTags,
                                     @RequestParam(value = "userId", required = false, defaultValue = "0")
                                     long userId,
                                     @RequestParam(value = "isCollection", required = false, defaultValue = "false")
                                     Boolean isCollection,
                                     @RequestParam(value = "createTimeStart", required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                     Date createTimeStart,
                                     @RequestParam(value = "createTimeEnd", required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                     Date createTimeEnd,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        String token = request.getHeader("token");
        Long currentUserId = null;
        if (token != null && !tokenUtil.isTokenExpired(token)) {
            currentUserId = tokenUtil.getUserId(request);
        }

        if (currentUserId != null && currentUserId == userId) {
            return ReturnData.success(blogService.getBlogsByPage(pageNum, pageSize, queryText, queryTags,
                    userId, isCollection,
                    createTimeStart, createTimeEnd, -1));
        }

        return ReturnData.success(blogService.getBlogsByPage(pageNum, pageSize, queryText, queryTags,
                userId, isCollection,
                createTimeStart, createTimeEnd, BlogStatus.APPROVED.getCode()));
    }

    @GetMapping("/pending/page")
    @SkipLoginCheck
    public ReturnData getPendingBlogsByPage(@RequestParam("pageNum") int pageNum,
                                            @RequestParam("pageSize") int pageSize,
                                            @RequestParam(value = "queryText", required = false, defaultValue = "")
                                            String queryText,
                                            @RequestParam(value = "queryTags", required = false, defaultValue = "")
                                            List<Integer> queryTags,
                                            @RequestParam(value = "userId", required = false, defaultValue = "0")
                                            long userId,
                                            @RequestParam(value = "isCollection", required = false, defaultValue = "false")
                                            Boolean isCollection,
                                            @RequestParam(value = "createTimeStart", required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                            Date createTimeStart,
                                            @RequestParam(value = "createTimeEnd", required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                            Date createTimeEnd,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        String token = request.getHeader("token");
        if (token != null && !tokenUtil.isTokenExpired(token)) {
            tokenUtil.getUserId(request);
        }

        return ReturnData.success(blogService.getBlogsByPage(pageNum, pageSize, queryText, queryTags,
                userId, isCollection,
                createTimeStart, createTimeEnd, BlogStatus.PENDING.getCode()));
    }

    @GetMapping("/follow/page")
    public ReturnData getFollowBlogsByPage(@RequestParam("pageNum") int pageNum,
                                           @RequestParam("pageSize") int pageSize,
                                           @RequestParam(value = "followId", required = false, defaultValue = "0")
                                           long followId,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        String token = request.getHeader("token");
        Long userId = null;
        if (token != null && !tokenUtil.isTokenExpired(token)) {
            userId = tokenUtil.getUserId(request);
        }
        IPage<BlogDto> followBlogsByPage = blogService.getFollowBlogsByPage(pageNum, pageSize, userId, followId);
        return ReturnData.success(followBlogsByPage);
    }

    @GetMapping("/{id}")
    @SkipLoginCheck
    public ReturnData getBlogById(@PathVariable("id") long id,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        String token = request.getHeader("token");
        Long userId = null;
        if (token != null && !tokenUtil.isTokenExpired(token)) {
            userId = tokenUtil.getUserId(request);
        }

        BlogVo blogVo = blogService.getBlogById(id, userId, false);
        if (blogVo == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "博客获取失败");
        return ReturnData.success(blogVo);
    }

//    @PutMapping
//    public ReturnData updateBlog(@RequestBody BlogVo blogVo, HttpServletRequest request, HttpServletResponse response) {
//        String token = request.getHeader("token");
//        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.AUTH_ERROR.getCode(), "用户未登录");
//        Long userId = tokenUtil.getUserId(request);
//
//        Blog blog = new Blog();
//        BeanUtil.copyProperties(blogVo, blog);
//        blogService.updateBlog(blog);
//        return ReturnData.success(blogService.getBlogById(blog.getId(), userId, true));
//    }

    @PostMapping("/like/{id}")
    @ApiIdempotent
    public ReturnData likeBlog(@PathVariable("id") long blogId,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.AUTH_ERROR.getCode(), "用户未登录");
        Long userId = tokenUtil.getUserId(request);

        blogService.likeBlog(blogId, userId);
        return ReturnData.success(blogService.getBlogById(blogId, userId, true));
    }

    @PostMapping("/collect/{id}")
    public ReturnData collectBlog(@PathVariable("id") long blogId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token)) return ReturnData.fail(CodeEnum.AUTH_ERROR.getCode(), "用户未登录");
        Long userId = tokenUtil.getUserId(request);

        blogService.collectBlog(blogId, userId);
        return ReturnData.success(blogService.getBlogById(blogId, userId, true));
    }

    @DeleteMapping("/{id}")
    public ReturnData deleteBlog(@PathVariable("id") long blogId,
                                 HttpServletRequest request) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token))
            return ReturnData.fail(CodeEnum.AUTH_ERROR.getCode(), "用户未登录");
        Long userId = tokenUtil.getIdFromToken(token);

        Boolean deleted = blogService.deleteBlog(blogId, userId);
        if (!deleted) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "删除失败");
        return ReturnData.success();
    }


    // COMMENT API
    @PostMapping("/comment")
    @ApiIdempotent
    public ReturnData createComment(@RequestBody Comment comment,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token))
            return ReturnData.fail(CodeEnum.AUTH_ERROR.getCode(), "用户未登录");
        Long userId = tokenUtil.getUserId(request);

        comment.setUserId(userId);
        CommentDto commentDto = commentService.createComment(comment);
        if (commentDto == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "创建失败");

        Notification notification = new Notification().setSenderId(commentDto.getUserId())
                .setReceiverId(blogService.getUserIdByBlogId(comment.getBlogId()))
                .setBlogId(commentDto.getBlogId())
                .setCommentId(commentDto.getId())
                .setReplyId(null)
                .setContent(commentDto.getContent())
                .setCreateTime(new Date());
        messageProducer.sendNotification(notification);

//        messageProducer.sendNotification(commentDto.getUserId(),
//                blogService.getUserIdByBlogId(comment.getBlogId()),
//                commentDto.getBlogId(),
//                commentDto.getId(),
//                null,
//                commentDto.getContent());
        return ReturnData.success(commentDto);
    }

    @GetMapping("/comment/page")
    @SkipLoginCheck
    public ReturnData getCommentsByPage(@RequestParam("blogId") long blogId,
                                        @RequestParam("pageNum") int pageNum,
                                        @RequestParam("pageSize") int pageSize,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        String token = request.getHeader("token");
        Long userId = null;
        if (token != null && !tokenUtil.isTokenExpired(token)) {
            userId = tokenUtil.getUserId(request);
        }

        return ReturnData.success(commentService.getCommentsByPage(blogId, pageNum, pageSize, userId));
    }

    @PostMapping("/comment/like/{id}")
    @ApiIdempotent
    public ReturnData likeComment(@PathVariable("id") long commentId, HttpServletRequest request) {
        Long userId = tokenUtil.getIdFromToken(request.getHeader("token"));
        if (userId == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        commentService.likeComment(commentId, userId);
        return ReturnData.success(commentService.getCommentById(commentId, userId));
    }

    @DeleteMapping("/comment/{id}")
    @RequiredAdmin
    public ReturnData deleteComment(@PathVariable("id") long commentId,
                                    HttpServletRequest request) {
        Boolean deleted = commentService.deleteComment(commentId);
        if (!deleted) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "删除失败");
        return ReturnData.success();
    }


    // REPLY API
    @PostMapping("/comment/reply")
    @ApiIdempotent
    public ReturnData createReply(@RequestBody Reply reply,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        String token = request.getHeader("token");
        if (tokenUtil.isTokenExpired(token))
            return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        Long userId = tokenUtil.getUserId(request);

        reply.setUserId(userId);
        ReplyDto replyDto = replyService.createReply(reply);
        if (replyDto == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "回复失败");
        Long receiverId = replyDto.getParentId() == 0 ? commentService.getUserId(replyDto.getCommentId()) : replyService.getUserId(replyDto.getParentId());

        Notification notification = new Notification().setSenderId(replyDto.getUserId())
                .setReceiverId(receiverId)
                .setBlogId(commentService.getBlogId(replyDto.getCommentId()))
                .setCommentId(replyDto.getCommentId())
                .setReplyId(replyDto.getId())
                .setContent(replyDto.getContent())
                .setCreateTime(new Date());
        messageProducer.sendNotification(notification);

//        messageProducer.sendNotification(replyDto.getUserId(),
//                receiverId,
//                commentService.getBlogId(replyDto.getCommentId()),
//                replyDto.getCommentId(),
//                replyDto.getId(),
//                replyDto.getContent());
        return ReturnData.success(replyDto);
    }

    @PostMapping("/comment/reply/like/{id}")
    @ApiIdempotent
    public ReturnData likeReply(@PathVariable("id") long replyId, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = tokenUtil.getIdFromToken(token);
        if (userId == null) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "用户未登录");
        replyService.likeReply(replyId, userId);
        return ReturnData.success(replyService.getReplyById(replyId, userId));
    }

    @DeleteMapping("/comment/reply/{id}")
    @RequiredAdmin
    public ReturnData deleteReply(@PathVariable("id") long replyId,
                                  HttpServletRequest request) {
        Boolean deleted = replyService.deleteReply(replyId);
        if (!deleted) return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "删除失败");
        return ReturnData.success();
    }

}
