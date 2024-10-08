package com.liu.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.dao.Blog;
import com.liu.myblog.dao.dto.BlogDto;
import com.liu.myblog.dao.vo.BlogVo;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface BlogService {

    Blog createOrUpdateBlog(BlogDto blogDto);

    IPage<BlogDto> getBlogsByPage(int pageNum, int pageSize,
                                  String queryText, List<Integer> queryTags, Long userId,
                                  Boolean isCollection, Date createTimeStart,Date createTimeEnd,int status);

    BlogVo getBlogById(Long id,Long userId,Boolean isRefresh);

    void updateBlog(Blog blog);

    void likeBlog(Long blogId, Long userId);

    Boolean deleteBlog(long blogId, Long userId);

    void collectBlog(long blogId, Long userId);

    IPage<BlogDto> getFollowBlogsByPage(int pageNum, int pageSize, Long userId, long followId);

    Long getUserIdByBlogId(long blogId);

    void updateDailyBlogCount(LocalDate date);

    Long getDailyBlogCount(LocalDate date);
}
