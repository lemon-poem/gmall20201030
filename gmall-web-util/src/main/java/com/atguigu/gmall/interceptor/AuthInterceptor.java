package com.atguigu.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截加了拦截注解的方法
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        //如果没有加拦截器注解，直接放行
        if (methodAnnotation == null) {
            return true;
        }
        boolean   loginSuccess = methodAnnotation.loginSuccess();
        //如果加了拦截器，先判断token值
        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        Map<String,String> successMap=new HashMap<String, String>();
        //判断是否通过,调用用户中心进行验证
        String success = "false";
        if (StringUtils.isNotBlank(token)) {
            String ip = request.getHeader("X-real-ip");
            if (StringUtils.isBlank(ip)){
                ip=request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    ip="127.0.0.1";
                }
            }
            String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8075/verify?token=" + token+"&currentIp="+ip);
            successMap= JSON.parseObject(successJson,Map.class);
            success=successMap.get("status");
        }
        //是否需要验证登录
        //需要验证才能登录
        if (loginSuccess) {
            //验证通过登录
            if (!success.equals("success")) {
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8075/index?ReturnUrl=" + requestURL);
                return false;
            }
            //需要将新的token的信息写入
            if (StringUtils.isNotBlank(token)) {
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
                //刷下cookie
                if (StringUtils.isNotBlank(token)) {
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 24 * 3, true);

                }            }

            //没有登录也能使用
        } else {
            if (success.equals("success")) {
                if (StringUtils.isNotBlank(token)) {
                        request.setAttribute("memberId", successMap.get("memberId"));
                    request.setAttribute("memberNickname", successMap.get("nickname"));
                    //刷下cookie
                    if (StringUtils.isNotBlank(token)) {
                        CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 24 * 3, true);

                    }
                }

            }
        }


        return true;
    }
}