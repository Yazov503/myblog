package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    void insertImage(@Param("commentId") long commentId, @Param("url") String url);

    Comment selectCommentById(long id);

    void deleteImagesByBlogId(long blogId);

    void deleteCommentsByBlogId(long blogId);

    Long getDailyCommentCount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Boolean deleteCommentById(Long commentId);
}
