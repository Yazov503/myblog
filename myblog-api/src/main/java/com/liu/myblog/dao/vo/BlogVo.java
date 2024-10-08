package com.liu.myblog.dao.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BlogVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private long userId;

    private String username;

    private String avatar;

    private long browseNum;

    private long likeNum;

    private long collectNum;

    private long commentNum;

    private List<Long> tags;

    private List<String> tagNames;

    private Boolean liked;

    private Boolean collected;

}
