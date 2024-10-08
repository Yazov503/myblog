package com.liu.myblog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.dao.Reply;
import com.liu.myblog.dao.dto.ReplyDto;
import com.liu.myblog.mapper.ReplyMapper;
import com.liu.myblog.mapper.UserMapper;
import com.liu.myblog.service.ReplyService;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.util.sensitive.SensitiveWordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReplyServiceImpl implements ReplyService {

    @Resource
    private ReplyMapper replyMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SensitiveWordUtil sensitiveWordUtil;

    @Override
    @Transactional
    public ReplyDto createReply(Reply reply) {
        if (reply == null || reply.getContent().isEmpty()) return null;
        reply.setContent(sensitiveWordUtil.replace(reply.getContent()));

        replyMapper.insert(reply);
        return getReply(reply.getId(), reply.getUserId());
    }

    @Override
    public void likeReply(long replyId, Long userId) {
        Boolean isMember = redisUtil.sIsMember(RedisKeyConstant.LIKE_REPLY + replyId, userId);
        if (isMember != null && !isMember) {
            redisUtil.sAdd(RedisKeyConstant.LIKE_REPLY + replyId, userId);
        } else {
            redisUtil.sRemove(RedisKeyConstant.LIKE_REPLY + replyId, userId);
        }
    }

    @Override
    public ReplyDto getReplyById(long replyId, long userId) {
        return getReply(replyId, userId);
    }

    @Override
    public Long getUserId(long parentId) {
        return replyMapper.selectUserId(parentId);
    }

    @Override
    public List<ReplyDto> selectByCommentId(Long commentId, Long userId) {
        List<ReplyDto> replyDtos = replyMapper.selectByCommentId(commentId);
        if (replyDtos != null) {
            for (ReplyDto replyDto : replyDtos) {
                replyDto.setLikeNum(redisUtil.getReplyLikeNum(replyDto.getId()));
                if (userId != null) replyDto.setLiked(redisUtil.getLikeReplyStatus(replyDto.getId(), userId));
            }
        }
        return replyDtos;
    }

    @Override
    public Boolean deleteReply(long replyId) {
        return replyMapper.deleteReplyById(replyId);
    }

    @Override
    public void updateDailyReplyCount(LocalDate date) {
        Long replyCount = replyMapper.getDailyReplyCount(date, date.plusDays(1));
        if (replyCount != 0) redisUtil.set(RedisKeyConstant.DAILY_REPLY_COUNT + date, String.valueOf(replyCount));
    }

    @Override
    public Long getDailyReplyCount(LocalDate date) {
        String key = RedisKeyConstant.DAILY_REPLY_COUNT + date;
        if (!redisUtil.exist(key))
            updateDailyReplyCount(date);
        return redisUtil.exist(key) ? Long.parseLong(redisUtil.get(key).toString()) : 0;
    }

    private ReplyDto getReply(Long replyId, Long userId) {
        Reply reply = replyMapper.selectById(replyId);
        ReplyDto replyDto = new ReplyDto();
        BeanUtil.copyProperties(reply, replyDto);
        replyDto.setUsername(userMapper.selectUsernameByUserId(replyDto.getUserId()));
        replyDto.setLikeNum(redisUtil.getReplyLikeNum(replyId));
        if (userId != null) replyDto.setLiked(redisUtil.getLikeReplyStatus(replyId, userId));
        if (replyDto.getParentId() != 0) {
            replyDto.setRepliedUsername(userMapper.selectRepliedUsernameByReplyId(replyDto.getId()));
        }
        return replyDto;
    }
}
