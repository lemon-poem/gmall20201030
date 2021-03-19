package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Reference
    OrderService orderService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    AlipayClient alipayClient;
    @LoginRequired(loginSuccess = true)
    @RequestMapping("alipay/callback/return")
    public String alipayCallbackReturn(String orderSn, String totalAmount, HttpServletRequest request, ModelMap modelMap) {
         //获取支付宝返回的参数
        String sign=(String)request.getAttribute("sign");
        String out_trade_no=(String)request.getAttribute("out_trade_no");
        String trade_no=(String)request.getAttribute("trade_no");
        String total_amount=(String)request.getAttribute("total_amount");
        String subject=(String)request.getAttribute("subject");
        String call_back_content=request.getQueryString();
        //更新用户状态
        if(StringUtils.isNotBlank(sign)){
            PaymentInfo paymentInfo=new PaymentInfo();
            paymentInfo.setSubject(subject);
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setTotalAmount(new BigDecimal(totalAmount));
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(call_back_content);
            paymentService.updatePaymentInfo(paymentInfo);
        }
        //给其他模块消费
        //支付成功后，引起的服务更新 订单服务-》库存服务-》物流服务
        //发送延迟队列



        return "finish";
    }
    public String alipay(String orderSn, String totalAmount, HttpServletRequest request, ModelMap modelMap){
        String form=null;
        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest();
        //回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map map =new HashMap();
        map.put("subject","poem智能手机");
        map.put("out_trade_no",orderSn);
        map.put("total_amount",totalAmount);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
       String  jsonString= JSON.toJSONString(map);
        alipayRequest.setBizContent(jsonString);
        try  {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
            System.out.println(form);

        }  catch  (AlipayApiException e) {
            e.printStackTrace();
        }
        //通过外部订单号查询用户订单信息
       OmsOrder omsOrder= orderService.getOmsOrderByOrderSn(orderSn);
        //生成并提交用户信息
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(omsOrder.getOrderSn());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject("poem手机Subject");
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setTotalAmount(new BigDecimal(totalAmount));
        paymentService.savePaymentInfo(paymentInfo);
        //向消息中间件发送一个检查支付状态的延迟消息队列
        paymentService.sendDelyQueueGetPaymentStatus(orderSn,"5");
        return form;
    }
    @LoginRequired(loginSuccess = true)
    @RequestMapping("wx/submit")
    public String wx(String orderSn, String totalAmount, HttpServletRequest request, ModelMap modelMap){
        return  null;
    }
    @LoginRequired(loginSuccess = true)
    @RequestMapping("index")
    public String index(String orderSn, String totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId=(String)request.getAttribute("memberId");
        String  nickName=(String)request.getAttribute("nickname");
        modelMap.put("nickName",nickName);
        modelMap.put("orderSn",orderSn);
        modelMap.put("totalAmount",totalAmount);
        return "index";
    }
}
