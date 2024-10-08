package com.liu.myblog.controller;

import com.liu.myblog.annotation.RequiredAdmin;
import com.liu.myblog.annotation.SkipLoginCheck;
import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.ReturnData;
import com.liu.myblog.dao.Notification;
import com.liu.myblog.service.AdminService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/login")
    @SkipLoginCheck
    public ReturnData login(String username, String password) {
        return adminService.login(username, password);
    }

    @PostMapping("/mute")
    @RequiredAdmin
    public ReturnData mute(long userId, int muteDay) {
        return adminService.mute(userId, muteDay);
    }

    @PostMapping("/cancelMute")
    @RequiredAdmin
    public ReturnData cancel(long userId) {
        return adminService.cancelMute(userId);
    }

    @PostMapping("/bulletin")
    @RequiredAdmin
    public ReturnData createOrUpdateBulletin(@RequestBody Notification bulletin) {
        return adminService.createOrUpdateBulletin(bulletin);
    }

    @GetMapping("/bulletin/page")
    @RequiredAdmin
    public ReturnData getBulletinByPage(@RequestParam("pageNum") int pageNum,
                                        @RequestParam("pageSize") int pageSize,
                                        @RequestParam(value = "queryText", required = false, defaultValue = "")
                                            String queryText) {
        return ReturnData.success(adminService.getBulletinByPage(pageNum,pageSize,queryText));
    }

    @DeleteMapping("/bulletin/{id}")
    @RequiredAdmin
    public ReturnData deleteBulletin(@PathVariable("id") long bulletinId){
        Boolean deleted = adminService.deleteBulletin(bulletinId);
        if(!deleted)return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(), "删除失败");
        return ReturnData.success();
    }

}
