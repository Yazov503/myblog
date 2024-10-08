package com.liu.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.dao.Comment;
import com.liu.myblog.dao.dto.CommentDto;

import java.time.LocalDate;

public interface CommentService {

    CommentDto createComment(Comment comment);

    IPage<CommentDto> getCommentsByPage(Long blogId, int pageNum, int pageSize,Long userId);

    void likeComment(Long commentId, Long userId);

    CommentDto getCommentById(Long commentId,Long userId);

    Long getBlogId(long commentId);

    Long getUserId(long commentId);

    void updateDailyCommentCount(LocalDate date);

    Long getDailyCommentCount(LocalDate date);

    Boolean deleteComment(Long commentId);
}
