package com.liu.myblog.dao.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@TableName("t_user")
@Accessors(chain = true)
public class Follower {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String avatar;

}
