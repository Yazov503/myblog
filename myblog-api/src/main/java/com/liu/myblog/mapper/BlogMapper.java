package com.liu.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.myblog.dao.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {


    int insertTag(@Param("blogId") long blogId,@Param("tagId") long tagId);

    void insertImage(@Param("blogId") long blogId,@Param("url") String url);

    List<Long> selectTagsByBlogId(@Param("blogId") long blogId);

    List<String> selectImagesByBlogId(@Param("blogId") long blogId);

    Blog selectBlogById(long id);

    void addCommentNum(long blogId);

    void deleteBlogById(long blogId);

    void deleteTagsByBlogId(long blogId);

    void deleteImagesByBlogId(long blogId);

    void deleteTagsByUserId(Long userId);

    void deleteImagesByUserId(Long userId);

    void deleteBlogsByUserId(Long userId);

    void collectBlog(@Param("blogId")long blogId, @Param("userId")Long userId);

    void removeCollectBlog(@Param("blogId")long blogId, @Param("userId")Long userId);

    String selectTitleById(Long blogId);

    Long countDailyBlogs(@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);
}
