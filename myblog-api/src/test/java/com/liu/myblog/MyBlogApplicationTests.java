package com.liu.myblog;

import com.liu.myblog.dao.Message;
import com.liu.myblog.mapper.MessageMapper;
import com.liu.myblog.mapper.ReplyMapper;
import com.liu.myblog.mapper.UserMapper;
import com.liu.myblog.service.BlogService;
import com.liu.myblog.util.sensitive.SensitiveWordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;

@SpringBootTest
class MyBlogApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Resource
    private BlogService blogService;

    @Resource
    private ReplyMapper replyMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private SensitiveWordUtil sensitiveWordUtil;

    @Test
    void contextLoads() {
        System.out.println(replyMapper.selectByCommentId(9));
    }

    @Test
    public void testInsertAndSelect() {
        // 插入测试数据
        Message message = new Message();
        message.setSenderId(1L)
                .setReceiverId(2L)
                .setContent("Test message")
                .setCreateTime(new Date());
        int rowsInserted = messageMapper.insert(message);

        // 查询测试数据
        Message retrievedMessage = messageMapper.selectById(message.getId());
        System.out.println(retrievedMessage);
    }

    @Test
    public void testStatistics(){
        for (int i = 1; i < 20; i++) {
            blogService.updateDailyBlogCount(LocalDate.now().minusDays(i));
        }

    }

    @Test
    public void testSensitiveWordFilter(){
        String replaced = sensitiveWordUtil.replace("你他妈的是不是傻比");
        System.out.println(replaced);
    }

}
