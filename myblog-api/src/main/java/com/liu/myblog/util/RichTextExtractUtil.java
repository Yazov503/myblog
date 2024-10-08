package com.liu.myblog.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichTextExtractUtil {

    private static final Pattern p_image=Pattern.compile("<img.*src\\s*=\\s*(.*?)[^>]*?>",Pattern.CASE_INSENSITIVE);
    private static final Pattern r_image=Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)");

    public static List<String> getImgStr(String htmlStr) {
        List<String> list = new ArrayList<>();
        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        Matcher m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            String img = m_image.group();
            Matcher m = r_image.matcher(img);
            while (m.find()) {
                if(!m.group(1).matches("rId\\d+"))list.add(m.group(1));
            }
        }
        return list;
    }

    public static String getText(String richText) {
        String regx = "(<.+?>)|(</.+?>)";
        Matcher matcher = Pattern.compile(regx).matcher(richText);
        while (matcher.find()) {
            // 替换图片
            richText = matcher.replaceAll("").replace(" ", "");
        }
        return richText;
    }

    public static void main(String[] args) {
        List<String> list=getImgStr("<p>体验冬日的<img src=\"https://www.xxx.cn/images/1a7169d805d04031b442ba7ff3d65f26.jpeg\">事实上事实上<img src=\"https://www.xxx.cn/images/bf2f79a47aa140b499101e29270b1c08.jpeg\">身上</p>");
        for (Object a:list){
            System.out.println(a.toString());
        };
        System.out.println(list);
    }
}

