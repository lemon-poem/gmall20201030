package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.HttpclientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PassPortController {
    @Reference
    UserService userService;
    @RequestMapping("loginSuccess")
    public String loginSuccess(String code,HttpServletRequest request){
        //使用授权码交换access_token
        Map<String,String> map1=new HashMap<>();
        map1.put("client_id","1814813670");
        map1.put("client_secret","446d4d376686f708da628b8aa9a05daa");
        map1.put("grant_type","authorization_code");
        map1.put("redirect_uri","http://passport.gmall.com:8075/loginSuccess");
        map1.put("code",code);
        String s1=HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token?",map1);
        Map<String,Object> mapJson=JSON.parseObject(s1,Map.class);
        String access_token=(String) mapJson.get("access_token");
        String uid=(String)mapJson.get("uid");
        //使用access_token获取第三方平台中用户的信息
        String userString=HttpclientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid);
        Map<String,Object> userMap=JSON.parseObject(userString,Map.class);
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid((String)userMap.get("idstr"));
        umsMember.setNickname((String)userMap.get("screen_name"));
        umsMember.setCity((String)userMap.get("location"));
        String gender=(String)userMap.get("gender");
        if(gender.equals("m")){
            gender="1";
        }else if (gender.equals("f")){
            gender="2";
        }else {
            gender="0";
        }
        umsMember.setGender(gender);
        umsMember.setSourceType("1");
        umsMember.setCreateTime(new Date());
        umsMember.setAccessCode(access_token);
        umsMember.setAccessCode(code);
        //首先进行判断数据库是否有该用户的信息
        UmsMember umsMemberReturn=null;
        UmsMember umsMemberCheck=userService.checkOAuthUser((String)userMap.get("idstr"));
        if (umsMemberCheck==null){
            //将用户信息存入数据库
             umsMemberReturn=userService.addOAuthUser(umsMember);
        }
        UmsMember user=userService.checkOAuthUser((String)userMap.get("idstr"));
        //生成Jwt token,并且重定向到首页
        String token="";
        //如果返回的结果不为空,说明登录成功
          //制作token
        String memberId=umsMemberReturn.getId();
        String nickname=user.getNickname();
            Map<String,Object> map=new HashMap<>();
            map.put("memberId",memberId);
            map.put("nickname",nickname);
            //从request中获取nginx
            String ip = request.getHeader("X-real-ip");
            if (StringUtils.isBlank(ip)){
                ip=request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    ip="127.0.0.1";
                }
            }
            token=JwtUtil.encode("gmall20201030",map,ip);
            //将token存入reids
            userService.addTokenCache(token,memberId);
        return "redirect:http://search.gmall.com:8073/index?token="+token;
    }
    //verify 认证中心
    @ResponseBody
    @RequestMapping("verify")
    public String verify(String token,String currentIp){
        Map<String,String> map=new HashMap<>();
        Map<String, Object> decode = JwtUtil.decode(token, "gmall20201030", currentIp);
        if (decode!=null){
              map.put("memberId",(String)decode.get("memberId"));
              map.put("nickname",(String)decode.get("nickname"));
              map.put("status","success");
        }else{
            map.put("status","fail");
        }
        return JSON.toJSONString(map);
    }
    @ResponseBody
    @RequestMapping("login")
    public String login(UmsMember umsMember, HttpServletRequest request){
        //调用用户验证信息
        UmsMember umsMemberLogin=userService.login(umsMember);
        String token="";
        //如果返回的结果不为空,说明登录成功
        if (umsMemberLogin!=null){
              //制作token
            String memberId=umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String,Object> map=new HashMap<>();
            map.put("memberId",memberId);
            map.put("nickname",nickname);
            //从request中获取nginx
            String ip = request.getHeader("X-real-ip");
            if (StringUtils.isBlank(ip)){
                ip=request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    ip="127.0.0.1";
                }
            }
            token=JwtUtil.encode("gmall20201030",map,ip);
            //将token存入reids
            userService.addTokenCache(token,memberId);
            //如果返回的结果为空说明登录失败返回fail
        }else {
            token="fail";
        }
        return token;


    }

    @RequestMapping("index")
       public String index(String ReturnUrl, ModelMap modelMap){
        modelMap.put("ReturnUrl",ReturnUrl);
           return "index";
        }

}
