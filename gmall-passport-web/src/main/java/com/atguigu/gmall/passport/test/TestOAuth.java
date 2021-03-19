package com.atguigu.gmall.passport.test;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpclientUtil;

import java.util.Map;

public class TestOAuth {
    public static void main(String[] args) {
        //App Key：1814813670
        //App Secret：446d4d376686f708da628b8aa9a05daa
        //1.
        //https://api.weibo.com/oauth2/authorize?client_id=1814813670&response_type=code&redirect_uri=http://passport.gmall.com:8075/loginSuccess
        //http://passport.gmall.com:8075/loginSuccess?code=75fdf221f4017d1f3e750b4c49f61fa2
       // String s1 = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=1814813670&response_type=code&redirect_uri=http://passport.gmall.com:8075/loginSuccess");
        //2.返回授权码
        //http://passport.gmall.com:8075/loginSuccess?code=c5fe786fefe29250f42765f5101a58bd
        //3.换取access_token
        //https://api.weibo.com/oauth2/access_token?client_id=1814813670&client_secret=446d4d376686f708da628b8aa9a05daa&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8075/loginSuccess&code=CODE
/*        Map<String,String> map1=new HashMap<>();
        map1.put("client_id","1814813670");
        map1.put("client_secret","446d4d376686f708da628b8aa9a05daa");
        map1.put("grant_type","authorization_code");
        map1.put("redirect_uri","http://passport.gmall.com:8075/loginSuccess");
        map1.put("code","75fdf221f4017d1f3e750b4c49f61fa2");
        String s3= HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token?",map1);
        int a=2;
        Map<String,String> map= JSON.parseObject(s3,Map.class);*/
       // String mapString=map.get("abc");
        //{"access_token":"2.00Jo6_GIOwloyBe20ee216fc1ZF82B","remind_in":"157679999","expires_in":157679999,"uid":"7422348113","isRealName":"true"}

        //4.有了access token 继续交换用户信息
        //https://api.weibo.com/oauth2/access_token?client_id=1814813670&client_secret=446d4d376686f708da628b8aa9a05daa&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8075/loginSuccess&code=2.00Jo6_GIOwloyBe20ee216fc1ZF82B
       //https://api.weibo.com/2/users/show.json?access_token=2.00Jo6_GIOwloyBe20ee216fc1ZF82B&uid=7422348113
        String s4=HttpclientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token=2.00Jo6_GIOwloyBe20ee216fc1ZF82B&uid=7422348113");
        Map<String,String> map= JSON.parseObject(s4,Map.class);
        map.get("abc");
        System.out.println(s4);
    }
}
