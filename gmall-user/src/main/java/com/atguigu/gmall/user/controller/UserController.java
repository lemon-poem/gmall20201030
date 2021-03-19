package com.atguigu.gmall.user.controller;


import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
   @Autowired
   UserService userService;
    @ResponseBody
    @RequestMapping("index")
    public String index(){
        return "hello gmall-user";
    }
    @ResponseBody
    @RequestMapping("getAllUser")
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMembers=userService.getAllUser();
        return umsMembers;
    }
    @ResponseBody
    @RequestMapping("getUmsMemberReceiveAddress")
    public List<UmsMemberReceiveAddress> getUmsMemberReceiveAddress(String memberId){
        List<UmsMemberReceiveAddress> umsMemberReceiveAddress=userService.getUmsMemberReceiveAddress(memberId);
        return umsMemberReceiveAddress;
    }
}
