package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.SensitiveWord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    @Select("select word from t_sensitive_word where type = #{type} and is_deleted = 0")
    List<String> selectSensitiveList(int type);

    @Select("select * from t_sensitive_word where id = #{id} and is_deleted = 0")
    SensitiveWord selectSensitiveWord(Long id);

    @Update("update t_sensitive_word set is_deleted = 1 where id = #{sensitiveWordId}")
    void deleteSensitiveWord(long sensitiveWordId);
}
