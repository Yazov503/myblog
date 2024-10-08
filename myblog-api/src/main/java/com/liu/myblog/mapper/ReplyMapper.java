package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.Reply;
import com.liu.myblog.dao.dto.ReplyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {
    List<ReplyDto> selectByCommentId(long commentId);

    void deleteRepliesByBlogId(long blogId);

    Long selectUserId(long userId);

    Boolean deleteReplyById(long replyId);

    void deleteReplyByCommentId(Long commentId);

    Long getDailyReplyCount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
