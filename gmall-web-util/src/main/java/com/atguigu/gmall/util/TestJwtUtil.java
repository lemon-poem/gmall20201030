package com.atguigu.gmall.util;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwtUtil {
    public static void main(String[] args) {
        String time=new SimpleDateFormat("yyyymmdd hhmmss").format(new Date());
        String ip="127.0.0.1";
        Map<String,Object> map=new HashMap<>();
        map.put("memberId","123456");
        map.put("memberName","lemon");
        String encode = JwtUtil.encode("gmall20201030", map, ip + time);
        System.err.println(encode);
        //String tokenUserInfo = StringUtils.substringBetween(encode, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode("eyJtZW1iZXJOYW1lIjoibGVtb24iLCJtZW1iZXJJZCI6IjEyMzQ1NiJ9");
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map1 = JSON.parseObject(tokenJson, Map.class);
        System.out.println("64="+map1);
    }
}
