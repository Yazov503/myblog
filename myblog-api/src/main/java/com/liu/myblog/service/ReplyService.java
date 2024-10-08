package com.liu.myblog.service;

import com.liu.myblog.dao.Reply;
import com.liu.myblog.dao.dto.ReplyDto;

import java.time.LocalDate;
import java.util.List;

public interface ReplyService {
    ReplyDto createReply(Reply reply);

    void likeReply(long replyId, Long userId);

    ReplyDto getReplyById(long replyId,long userId);

    Long getUserId(long parentId);

    List<ReplyDto> selectByCommentId(Long commentId,Long userId);

    Boolean deleteReply(long replyId);

    void updateDailyReplyCount(LocalDate date);

    Long getDailyReplyCount(LocalDate date);
}
