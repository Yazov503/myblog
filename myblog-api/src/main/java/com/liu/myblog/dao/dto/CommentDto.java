package com.liu.myblog.dao.dto;

import com.liu.myblog.dao.Comment;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CommentDto extends Comment {

    private String username;

    private String avatar;

    private List<ReplyDto> replies;

    private long likeNum;

    private Boolean liked;

}
