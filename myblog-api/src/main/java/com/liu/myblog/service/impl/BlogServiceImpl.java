package com.liu.myblog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.myblog.common.BlogStatus;
import com.liu.myblog.common.RedisKeyConstant;
import com.liu.myblog.dao.Blog;
import com.liu.myblog.dao.dto.BlogDto;
import com.liu.myblog.dao.vo.BlogVo;
import com.liu.myblog.mapper.*;
import com.liu.myblog.service.BlogService;
import com.liu.myblog.service.UserService;
import com.liu.myblog.util.RedisUtil;
import com.liu.myblog.util.RichTextExtractUtil;
import com.liu.myblog.util.sensitive.SensitiveWordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ReplyMapper replyMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private UserService userService;

    @Resource
    private SensitiveWordUtil sensitiveWordUtil;

    private static final String CONTENT_REGEX = "REGEXP_REPLACE(" +
            "REPLACE(REGEXP_REPLACE(content, '<[^>]*>', ''), '&nbsp;', '')," +
            " 'http[^\\s]+(\\.jpg|\\.png|\\.gif)', '')";

    @Override
    @Transactional
    public Blog createOrUpdateBlog(BlogDto blogDto) {
        if (blogDto.getTitle() == null || blogDto.getContent().isEmpty()) return null;
        blogDto.setContent(sensitiveWordUtil.replace(blogDto.getContent()));

        Blog blog = new Blog();
        BeanUtil.copyProperties(blogDto, blog);
        if (blog.getId() == null) {
            blogMapper.insert(blog);
        } else {
            blogMapper.deleteImagesByBlogId(blog.getId());
            blogMapper.deleteTagsByBlogId(blog.getId());
            blog.setUpdateTime(new Date());
            blogMapper.updateById(blog);
        }

        List<String> imageUrls = RichTextExtractUtil.getImgStr(blogDto.getContent());
        imageUrls.forEach(url -> blogMapper.insertImage(blog.getId(), url));

        blogDto.getTags().forEach(tagId -> blogMapper.insertTag(blog.getId(), tagId));

        return blog;
    }

    @Override
    public IPage<BlogDto> getBlogsByPage(int pageNum, int pageSize, String queryText, List<Integer> queryTags,
                                         Long userId, Boolean isCollection,
                                         Date createTimeStart, Date createTimeEnd, int status) {
        Page<Blog> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false).orderByDesc("create_time");
        if (status != -1) {
            queryWrapper.and(wrapper -> wrapper.eq("status", status));
        }
        if (StringUtils.isNotEmpty(queryText))
            queryWrapper.and(wrapper -> wrapper.like(CONTENT_REGEX, queryText)
                    .or().like("title", queryText));
        if (userId != null && userId != 0) {
            if (isCollection) {
                queryWrapper.and(wrapper -> wrapper.inSql("id",
                        "select blog_id from t_collection where user_id = " + userId));
            } else {
                queryWrapper.and(wrapper -> wrapper.like("user_id", userId));
            }
        }
        if (!queryTags.isEmpty()) {
            for (Integer tag : queryTags) {
                queryWrapper.and(wrapper -> wrapper.exists(
                        "SELECT 1 FROM t_blog_tag bt WHERE bt.blog_id = t_blog.id AND bt.tag_id = " + tag + " and is_deleted = 0"
                ));
            }
        }

        if (createTimeStart != null && createTimeEnd != null) {
            queryWrapper.and(wrapper -> wrapper.between("create_time", createTimeStart, createTimeEnd));
        }
        return convertToBlogDtoPage(blogMapper.selectPage(page, queryWrapper));
    }

    @Override
    public BlogVo getBlogById(Long id, Long userId, Boolean isRefresh) {
        Blog blog = blogMapper.selectBlogById(id);
        if (blog != null) {
            BlogVo blogVo = new BlogVo();
            BeanUtil.copyProperties(blog, blogVo);
            blogVo.setTags(blogMapper.selectTagsByBlogId(blog.getId()));
            blogVo.setTagNames(Arrays.asList(blogVo.getTags()
                    .stream()
                    .map(tagId -> tagMapper.selectTagById(tagId).getTagName()).toArray(String[]::new)));
            blogVo.setUsername(userMapper.selectUsernameByUserId(blog.getUserId()));
            blogVo.setAvatar(userService.getAvatar(blog.getUserId()));
            blogVo.setLikeNum(redisUtil.getBlogLikeNum(blog.getId()));
            blogVo.setCollectNum(redisUtil.getBlogCollectNum(blog.getId()));
            if (userId != null) {
                blogVo.setLiked(redisUtil.getLikeBlogStatus(blog.getId(), userId));
                blogVo.setCollected(redisUtil.getCollectBlogStatus(blog.getId(), userId));
            }

            if (!isRefresh) {
                blogMapper.updateById(blog.setBrowseNum(blog.getBrowseNum() + 1));
                blogVo.setBrowseNum(blogVo.getBrowseNum() + 1);
            }

            return blogVo;
        }
        return null;
    }

    @Override
    public void updateBlog(Blog blog) {
        blogMapper.updateById(blog);
    }

    @Override
    public void likeBlog(Long blogId, Long userId) {
        Boolean isMember = redisUtil.sIsMember(RedisKeyConstant.LIKE_BLOG + blogId, userId);
        if (isMember != null && !isMember) {
            redisUtil.sAdd(RedisKeyConstant.LIKE_BLOG + blogId, userId);
        } else {
            redisUtil.sRemove(RedisKeyConstant.LIKE_BLOG + blogId, userId);
        }
    }

    @Override
    @Transactional
    public Boolean deleteBlog(long blogId, Long userId) {
        Blog blog = blogMapper.selectBlogById(blogId);
        if (blog.getUserId() != userId) return false;

        replyMapper.deleteRepliesByBlogId(blogId);
        redisUtil.sRemoveAll(RedisKeyConstant.LIKE_REPLY + blogId);

        commentMapper.deleteImagesByBlogId(blogId);
        commentMapper.deleteCommentsByBlogId(blogId);
        redisUtil.sRemoveAll(RedisKeyConstant.LIKE_COMMENT + blogId);

        blogMapper.deleteTagsByBlogId(blogId);
        blogMapper.deleteImagesByBlogId(blogId);
        blogMapper.deleteBlogById(blogId);
        redisUtil.sRemoveAll(RedisKeyConstant.LIKE_BLOG + blogId);
        return blogMapper.selectBlogById(blogId) == null;
    }

    @Override
    public void collectBlog(long blogId, Long userId) {
        String sKey = RedisKeyConstant.COLLECT_BLOG + userId;
        Boolean isMember = redisUtil.sIsMember(sKey, blogId);
        String key = RedisKeyConstant.COLLECT_NUM + blogId;
        if (isMember != null && !isMember) {
            redisUtil.sAdd(sKey, blogId);
            redisUtil.incr(key, 1);
            blogMapper.collectBlog(blogId, userId);
        } else {
            redisUtil.sRemove(sKey, blogId);
            redisUtil.decr(key, 1);
            blogMapper.removeCollectBlog(blogId, userId);
        }
    }

    @Override
    public IPage<BlogDto> getFollowBlogsByPage(int pageNum, int pageSize, Long userId, long followId) {
        if (followId != 0) {
            Page<Blog> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", followId)
                    .eq("status", BlogStatus.APPROVED.getCode())
                    .eq("is_deleted", false)
                    .orderByDesc("create_time");
            return convertToBlogDtoPage(blogMapper.selectPage(page, queryWrapper));
        }

        Set<Object> objects = redisUtil.sMembers(RedisKeyConstant.FOLLOW_USER + userId);
        if (objects != null && !objects.isEmpty()) {
            List<Long> followers = objects.stream().map(obj -> Long.parseLong(obj.toString())).collect(Collectors.toList());
            Page<Blog> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("user_id", followers)
                    .eq("status", BlogStatus.APPROVED.getCode())
                    .eq("is_deleted", false)
                    .orderByDesc("create_time");
            return convertToBlogDtoPage(blogMapper.selectPage(page, queryWrapper));
        }
        return new Page<>();
    }

    @Override
    public Long getUserIdByBlogId(long blogId) {
        return blogMapper.selectBlogById(blogId).getUserId();
    }

    @Override
    public void updateDailyBlogCount(LocalDate date) {
        Long blogCount = blogMapper.countDailyBlogs(date, date.plusDays(1));
        if (blogCount != 0) redisUtil.set(RedisKeyConstant.DAILY_BLOG_COUNT + date, String.valueOf(blogCount));
    }

    @Override
    public Long getDailyBlogCount(LocalDate date) {
        String key = RedisKeyConstant.DAILY_BLOG_COUNT + date;
        if (!redisUtil.exist(key))
            updateDailyBlogCount(date);
        return redisUtil.exist(key) ? Long.parseLong(redisUtil.get(key).toString()) : 0;
    }

    private IPage<BlogDto> convertToBlogDtoPage(IPage<Blog> blogIPage) {
        return blogIPage.convert(blog -> {
            BlogDto blogDto = new BlogDto();
            BeanUtil.copyProperties(blog, blogDto);
            List<Long> tagIds = blogMapper.selectTagsByBlogId(blog.getId());
            blogDto.setTags(blogMapper.selectTagsByBlogId(blog.getId()));
            blogDto.setTagNames(Arrays.asList(tagIds
                    .stream()
                    .map(tagId -> tagMapper.selectTagById(tagId).getTagName()).toArray(String[]::new)));
            blogDto.setImages(blogMapper.selectImagesByBlogId(blog.getId()));
            blogDto.setLikeNum(redisUtil.getBlogLikeNum(blog.getId()));
            blogDto.setCollectNum(redisUtil.getBlogCollectNum(blog.getId()));
            return blogDto;
        });
    }

}
