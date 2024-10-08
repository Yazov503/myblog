package com.liu.myblog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.dao.Comment;
import com.liu.myblog.dao.dto.CommentDto;
import com.liu.myblog.mapper.BlogMapper;
import com.liu.myblog.mapper.CommentMapper;
import com.liu.myblog.mapper.ReplyMapper;
import com.liu.myblog.mapper.UserMapper;
import com.liu.myblog.service.CommentService;
import com.liu.myblog.service.ReplyService;
import com.liu.myblog.service.UserService;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.util.RichTextExtractUtil;
import com.liu.myblog.util.sensitive.SensitiveWordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ReplyMapper replyMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private UserService userService;

    @Resource
    private ReplyService replyService;

    @Resource
    private SensitiveWordUtil sensitiveWordUtil;


    @Override
    @Transactional
    public CommentDto createComment(Comment comment) {
        if (comment.getContent().isEmpty()) return null;
        comment.setContent(sensitiveWordUtil.replace(comment.getContent()));

        commentMapper.insert(comment);
        comment = commentMapper.selectById(comment.getId());
        List<String> imageUrls = RichTextExtractUtil.getImgStr(comment.getContent());
        Comment finalComment = comment;
        imageUrls.forEach(url -> commentMapper.insertImage(finalComment.getId(), url));
        blogMapper.addCommentNum(comment.getBlogId());

        CommentDto commentDto = new CommentDto();
        BeanUtil.copyProperties(comment, commentDto);
        commentDto.setUsername(userMapper.selectUsernameByUserId(commentDto.getUserId()));
        commentDto.setAvatar(userService.getAvatar(commentDto.getUserId()));
        commentDto.setReplies(replyMapper.selectByCommentId(commentDto.getId()));
        return commentDto;
    }

    @Override
    public IPage<CommentDto> getCommentsByPage(Long blogId, int pageNum, int pageSize, Long userId) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false)
                .eq("blog_id", blogId)
                .orderByDesc("create_time");
        IPage<Comment> commentIPage = commentMapper.selectPage(page, queryWrapper);

        IPage<CommentDto> commentDtoIPage = commentIPage.convert(comment -> {
            CommentDto commentDto = new CommentDto();
            BeanUtil.copyProperties(comment, commentDto);
            commentDto.setUsername(userMapper.selectUsernameByUserId(commentDto.getUserId()));
            commentDto.setAvatar(userService.getAvatar(commentDto.getUserId()));
//            commentDto.setReplies(replyMapper.selectByCommentId(commentDto.getId()));
            commentDto.setReplies(replyService.selectByCommentId(commentDto.getId(),userId));
            commentDto.setLikeNum(redisUtil.getCommentLikeNum(commentDto.getId()));
            if (userId != null) commentDto.setLiked(redisUtil.getLikeCommentStatus(commentDto.getId(), userId));
            return commentDto;
        });

        return commentDtoIPage;
    }

    @Override
    public void likeComment(Long commentId, Long userId) {
        Boolean isMember = redisUtil.sIsMember(RedisKeyConstant.LIKE_COMMENT + commentId, userId);
        if (isMember != null && !isMember) {
            redisUtil.sAdd(RedisKeyConstant.LIKE_COMMENT + commentId, userId);
        } else {
            redisUtil.sRemove(RedisKeyConstant.LIKE_COMMENT + commentId, userId);
        }
    }

    @Override
    public CommentDto getCommentById(Long commentId, Long userId) {
        Comment comment = commentMapper.selectCommentById(commentId);
        if (comment != null) {
            CommentDto commentDto = new CommentDto();
            BeanUtil.copyProperties(comment, commentDto);
            String username = userMapper.selectUsernameByUserId(commentDto.getUserId());
            commentDto.setUsername(username == null ? "default_username" : username);
            commentDto.setAvatar(userService.getAvatar(commentDto.getUserId()));
            commentDto.setReplies(replyMapper.selectByCommentId(commentDto.getId()));
            if (userId != null) commentDto.setLiked(redisUtil.getLikeCommentStatus(commentDto.getId(), userId));
            commentDto.setLikeNum(redisUtil.getCommentLikeNum(commentId));
            return commentDto;
        }
        return null;
    }

    @Override
    public Long getBlogId(long commentId) {
        return commentMapper.selectById(commentId).getBlogId();
    }

    @Override
    public Long getUserId(long commentId) {
        return commentMapper.selectCommentById(commentId).getUserId();
    }

    @Override
    public void updateDailyCommentCount(LocalDate date) {
        Long commentCount = commentMapper.getDailyCommentCount(date, date.plusDays(1));
        if (commentCount != 0) redisUtil.set(RedisKeyConstant.DAILY_COMMENT_COUNT + date, String.valueOf(commentCount));
    }

    @Override
    public Long getDailyCommentCount(LocalDate date) {
        String key = RedisKeyConstant.DAILY_COMMENT_COUNT + date;
        if (!redisUtil.exist(key))
            updateDailyCommentCount(date);
        return redisUtil.exist(key) ? Long.parseLong(redisUtil.get(key).toString()) : 0;
    }

    @Override
    public Boolean deleteComment(Long commentId) {
        replyMapper.deleteReplyByCommentId(commentId);
        return commentMapper.deleteCommentById(commentId);
    }

}
