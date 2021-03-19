package com.atguigu.gmall.payment.mq;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Map;

@Component
public class PaymentServiceMqListener {
    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "CHECK_PAYMENT_STATUS", concurrency = "jmsQueueListener")
    public void consumeDelyQueueGetPaymentStatus(MapMessage message) {
        try {
            Integer countCheck=0;
            if(!message.getString("count").isEmpty()){
                countCheck=Integer.parseInt(message.getString("countCheck"));
            }
            String out_trade_no = message.getString("out_trade_no");
            System.out.println("进行支付状态检查，调用PaymentService的检查服务");
            Map<String, Object> resMap = paymentService.checkPaymentStatus(out_trade_no);
            if (resMap != null && !resMap.isEmpty()) {  //说明支付成功
                String tradeStatus = (String) resMap.get("tradeStatus");
                if (StringUtils.isNotEmpty(tradeStatus) && tradeStatus.equals("TRADE_SUCCESS")) {
                    //说明支付成功，发送支付成功消息队列,更新支付状态
                    PaymentInfo paymentInfo = new PaymentInfo();
                    paymentInfo.setCallbackContent("");
                    paymentInfo.setPaymentStatus("已支付");
                    paymentInfo.setAlipayTradeNo((String) resMap.get("trade_no"));
                    paymentInfo.setOrderSn((String)resMap.get("out_trade_no"));
                    paymentService.updatePaymentInfo(paymentInfo);
                    return;
                }
                if(countCheck>0){
                    //继续发送延迟队列请求
                    System.out.println("没有支付成功,计算延迟时间，继续发送查询请求");
                    paymentService.checkPaymentStatus(out_trade_no);
                    countCheck--;
                }else{
                    System.out.println("检查次数用完，结束发送检查");
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
