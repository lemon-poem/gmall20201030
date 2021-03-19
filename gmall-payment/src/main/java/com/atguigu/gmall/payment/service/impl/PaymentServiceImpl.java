package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {
        //1.进行幂等性检查，如果这个订单已经支付完成，直接返回支付成功
        PaymentInfo checkPaymentInfo = new PaymentInfo();
        checkPaymentInfo.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfoRes = paymentInfoMapper.selectOne(checkPaymentInfo);
        String paymentStatus = paymentInfoRes.getPaymentStatus();
        if(StringUtils.isNotBlank(paymentStatus)&&paymentInfo.equals("已支付")){
            return;
        }
        Example example = new Example(PaymentInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderSn", paymentInfo.getOrderSn());
        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Session session = null;
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payhment_success_queue = session.createQueue("PAYHMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(payhment_success_queue);
            MapMessage message = new ActiveMQMapMessage();
            message.setString("orderSn", paymentInfo.getOrderSn());
            producer.send(message);
            session.commit();
        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }

        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void sendDelyQueueGetPaymentStatus(String out_trade_no,String checkCount) {
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue check_payment_status = session.createQueue("CHECK_PAYMENT_STATUS");
            MessageProducer producer = session.createProducer(check_payment_status);
            MapMessage message = new ActiveMQMapMessage();
            message.setString("out_trade_no", out_trade_no);
            message.setString("checkCount",checkCount);
            //为消息设置延迟时间
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);
            producer.send(message);
            session.commit();
        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }finally{
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Map<String, Object> checkPaymentStatus(String out_trade_no) {
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> map=new HashMap<>();
        map.put("out_trade_no",out_trade_no);
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        Map resMap=new HashMap();
        try {
            response = alipayClient.execute(request);
            if(response.isSuccess()){
                String tradeStatus = response.getTradeStatus();
                String tradeNo = response.getTradeNo();
                String outTradeNo = response.getOutTradeNo();
                String msg = response.getMsg();
                resMap.put("tradeStatus",tradeStatus);
                resMap.put("tradeNo",tradeNo);
                resMap.put("outTradeNo",outTradeNo);
                resMap.put("msg",msg);
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return resMap;
    }
}
