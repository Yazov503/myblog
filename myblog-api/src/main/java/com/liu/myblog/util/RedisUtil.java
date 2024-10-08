package com.liu.myblog.util;

import com.liu.myblog.common.RedisKeyConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component("redisUtil")
public class RedisUtil {
    Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 写入redis缓存（不设置expire存活时间）
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, String value) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            logger.error("写入redis缓存失败！错误信息为: " + e.getMessage());
        }
        return result;
    }

    /**
     * 将String写入redis缓存，设置expire存活时间(以秒为单位)
     *
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public boolean set(final String key, String value, Long expire) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(key, value);
            stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("写入redis缓存（设置expire存活时间）失败！错误信息为：" + e.getMessage());
        }
        return result;
    }

    /**
     * 写入redis缓存，设置expire存活时间(以小时为单位)
     *
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public boolean setByHours(final String key, String value, Long expire) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(key, value);
            stringRedisTemplate.expire(key, expire, TimeUnit.HOURS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("写入redis缓存（设置expire存活时间）失败！错误信息为：" + e.getMessage());
        }
        return result;
    }


    /**
     * 获取Object
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String stringObject = stringRedisTemplate.opsForValue().get(key);
        return JackSonUtil.parse(clazz, stringObject);
    }

    /**
     * 读取Redis缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object result = null;
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            result = operations.get(key);
        } catch (Exception e) {
            logger.error("读取redis缓存失败！错误信息为：" + e.getMessage());
        }
        return result;
    }

    /**
     * 判断redis缓存中是否有对应的key
     *
     * @param key
     * @return
     */
    public boolean exist(final String key) {
        boolean result = false;
        try {
            result = Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * redis根据key删除对应的value
     *
     * @param key
     * @return
     */
    public boolean remove(final String key) {
        boolean result = false;
        try {
            if (exist(key)) {
                stringRedisTemplate.delete(key);
            }
            result = true;
        } catch (Exception e) {
            logger.error("redis根据key删除对应的value失败！错误信息为：" + e.getMessage());
        }
        return result;
    }

    /**
     * Redis根据keys批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }


    public long incr(final String key, final long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    public long decr(final String key, final long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }

    public Long getExpire(String key) {
        return redisTemplate.opsForValue().getOperations().getExpire(key);
    }


    public boolean setObject(String key, Object value, long expire) {
        try {
            redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            logger.error("----------------->redis 写入object失败" + e.getMessage());
            return false;
        }
    }

    public boolean setObject(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("----------------->redis 写入object失败" + e.getMessage());
            return false;
        }
    }

    public Object getObject(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("redis 获取object异常" + e.getMessage());
            return null;
        }
    }

    public Boolean sIsMember(String s, Object o) {
        return redisTemplate.opsForSet().isMember(s, o);
    }

    public Set<Object> sMembers(String s) {
        Set<Object> result;
        try {
            result = redisTemplate.opsForSet().members(s);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public void sAdd(String s, Object obj) {
        redisTemplate.opsForSet().add(s, obj);
    }

    public void sRemove(String s, Object obj) {
        redisTemplate.opsForSet().remove(s, obj);
    }

    public void sRemoveAll(String s) {
        redisTemplate.opsForSet().remove(s, redisTemplate.opsForSet().members(s));
    }

    public Boolean getCollectBlogStatus(Long blogId, Long userId) {
        return sIsMember(RedisKeyConstant.COLLECT_BLOG + userId, blogId);
    }

    public void zsAdd(String key, Object value, double seqNo) {
        redisTemplate.opsForZSet().add(key, value, seqNo);
    }

    public Set<Object> zsGetTop(String key, int size) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = zSetOps.reverseRangeWithScores(key, 0, size - 1);
        return typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toSet());
    }

    public Boolean getLikeBlogStatus(Long blogId, Long userId) {
        return sIsMember(RedisKeyConstant.LIKE_BLOG + blogId, userId);
    }

    public Boolean getLikeCommentStatus(Long blogId, Long userId) {
        return sIsMember(RedisKeyConstant.LIKE_COMMENT + blogId, userId);
    }

    public Boolean getLikeReplyStatus(Long blogId, Long userId) {
        return sIsMember(RedisKeyConstant.LIKE_REPLY + blogId, userId);
    }

    public long getBlogLikeNum(Long blogId) {
        return redisTemplate.opsForSet().size(RedisKeyConstant.LIKE_BLOG + blogId);
    }

    public long getCommentLikeNum(Long commentId) {
        return redisTemplate.opsForSet().size(RedisKeyConstant.LIKE_COMMENT + commentId);
    }

    public long getReplyLikeNum(Long replyId) {
        return redisTemplate.opsForSet().size(RedisKeyConstant.LIKE_REPLY + replyId);
    }

    public long getBlogCollectNum(long blogId) {
        Object o = get(RedisKeyConstant.COLLECT_NUM + blogId);
        return o == null ? 0 : Long.parseLong(o.toString());
    }


}
