package com.liu.myblog.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.liu.myblog.util.sensitive.MyWordAllow;
import com.liu.myblog.util.sensitive.MyWordDeny;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class SensitiveWordConfig {

  @Resource
  private MyWordAllow myWordAllow;

  @Resource
  private MyWordDeny myWordDeny;

  /**
     * 初始化引导类
     *
     * @return 初始化引导类
     * @since 1.0.0
     */
  @Bean
  public SensitiveWordBs sensitiveWordBs() {
    // 可根据数据库数据判断 动态增加配置
    return SensitiveWordBs.newInstance()
      .wordDeny(WordDenys.chains(WordDenys.defaults(), myWordDeny)) // 设置黑名单
      .wordAllow(WordAllows.chains(WordAllows.defaults(), myWordAllow))
      .ignoreCase(true)
      .ignoreWidth(true)
      .ignoreNumStyle(true)
      .ignoreChineseStyle(true)
      .ignoreEnglishStyle(true)
      .ignoreRepeat(true)
      .enableEmailCheck(true)
      .enableUrlCheck(true)
      // 各种其他配置
      .init();
  }

}
