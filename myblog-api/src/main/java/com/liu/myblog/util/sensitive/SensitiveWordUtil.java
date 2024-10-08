package com.liu.myblog.util.sensitive;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SensitiveWordUtil {

    @Resource
    private SensitiveWordBs sensitiveWordBs;

    // 判断是否含有敏感词
    public boolean contains(String text){
        return sensitiveWordBs.contains(text);
    }

    // 使用默认替换符 * 进行替换敏感词
    public String replace(String text){
        Pattern imgPattern = Pattern.compile("https?://[^\\s]+\\.(jpg|jpeg|png|gif)");
        Matcher imgMatcher = imgPattern.matcher(text);
        List<String> imgUrls = new ArrayList<>();
        while (imgMatcher.find()) {
            imgUrls.add(imgMatcher.group());
            text = text.replace(imgMatcher.group(), "[IMG]");
        }
        text = sensitiveWordBs.replace(text);
        for (String url : imgUrls) {
            text = text.replaceFirst("\\[IMG\\]", url);
        }
        return text;
    }

    // 返回所有敏感词
    public List<String> findAll(String text){
        return sensitiveWordBs.findAll(text);
    }
}
