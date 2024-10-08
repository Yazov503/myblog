package com.liu.myblog.dao.dto;

import com.liu.myblog.dao.User;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDto extends User {
    private Boolean isFollowed;

    private Boolean isMuted;
}
