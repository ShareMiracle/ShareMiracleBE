package com.sharemiracle.controller;

import com.sharemiracle.entity.Organization;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.OrganService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/organ")
public class OrganController {

    @Resource
    private OrganService organService;

    /**
     * 添加组织
     * @param organ
     * @return
     */
    @PostMapping
    public Result<String> addOrgan(@RequestBody Organization organ){
        return organService.addOrgan(organ);
    }

    /**
     * 当前用户加入组织
     * @param organId
     * @return
     */
    @PutMapping("")
    public Result<String> joinOrgan(Long organId){
        return organService.joinOrgan(organId);
    }

    /**
     * 修改用户在组织中的权限
     * @param organId
     * @param userId
     * @param auth
     * @return
     */
    @PutMapping("/authority/{authority}")
    public Result<String> authority(Long organId, Long userId, @PathVariable("authority") Integer auth){
        return organService.authority(organId, userId, auth);
    }

    /**
     * 启用、禁用用户组织账号
     * @param status
     * @param userId
     * @return
     */
    @PutMapping("/status/{status}")
    public Result<String> status(@PathVariable("status") Integer status, Long userId, Long organId){
        return organService.status(status, userId, organId);
    }

    /**
     * 删除组织中的用户
     * @param organId
     * @param userId
     * @return
     */
    @DeleteMapping("/delete/user")
    public Result<String> deleteOrganUser(Long organId, Long userId){
        return organService.deleteOrganUser(organId, userId);
    }

    /**
     * 删除组织
     * @param organId
     * @return
     */
    @DeleteMapping("/delete")
    public Result<String> deleteOrgan(Long organId){
        return organService.deleteOrgan(organId);
    }

}
