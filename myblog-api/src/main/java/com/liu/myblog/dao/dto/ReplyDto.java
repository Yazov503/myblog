package com.liu.myblog.dao.dto;

import com.liu.myblog.dao.Reply;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReplyDto extends Reply {

    private long repliedUserId;

    private String username;

    private String repliedUsername;

    private long likeNum;

    private Boolean liked;
}
