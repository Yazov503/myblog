package com.liu.myblog.dao.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
public class MessageDto implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long senderId;

    private String senderName;

    private String senderAvatar;

    private Long receiverId;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    private Boolean isRead;

}
