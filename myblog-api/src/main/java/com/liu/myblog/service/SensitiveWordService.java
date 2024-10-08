package com.liu.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.myblog.dao.SensitiveWord;

public interface SensitiveWordService {

    void refresh();

    SensitiveWord createOrUpdateSensitiveWord(SensitiveWord sensitiveWord);

    void deleteSensitiveWord(long sensitiveWordId);

    IPage<SensitiveWord> getSensitiveWordByPage(int pageNum, int pageSize, String queryText,int type);
}
