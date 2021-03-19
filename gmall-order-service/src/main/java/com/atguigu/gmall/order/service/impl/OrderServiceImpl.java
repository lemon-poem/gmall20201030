package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    RedisUtil redisUtil;
    @Override
    public String checkOrderCode(String memberId, String tradeCode) {
        Jedis jedis=null;
        try {
            jedis=redisUtil.getJedis();
            String tradeCodeKey="user:"+memberId+":tradeCode";
            String tradeCodeFromCache = jedis.get(tradeCodeKey);
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long)jedis.eval(script, Collections.singletonList(tradeCodeKey), Collections.singletonList(tradeCode));
            if (eval!=null&&eval!=0){
                jedis.del(tradeCodeFromCache);
                //使用lua脚本解决高并发的问题
                return "success";
            }else{
                return "fail";
            }
        }finally {
            jedis.close();
        }
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis=null;
        String tradeCode="";
        try{
            jedis = redisUtil.getJedis();
            String tradeKey="user:"+memberId+":tradeCode";
             tradeCode= UUID.randomUUID().toString();
            jedis.setex(tradeKey,60*15,tradeCode);
        }finally {
                 jedis.close();
        }


        return tradeCode;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        String omsOrderId = omsOrder.getId();
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(omsOrderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
        }
    }

    @Override
    public OmsOrder getOmsOrderByOrderSn(String orderSn) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(orderSn);
        OmsOrder omsOrderRes = omsOrderMapper.selectOne(omsOrder);
        return omsOrderRes;
    }
    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example e=new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());
        OmsOrder  updateOrder=new OmsOrder();
        updateOrder.setOrderSn(omsOrder.getOrderSn());
        updateOrder.setStatus(1);
        //发送一个订单已支付队列提供给库存消费
        Connection connection=null;
        Session session=null;
        try {
             connection = activeMQUtil.getConnectionFactory().createConnection();
             session=connection.createSession(true,Session.SESSION_TRANSACTED);
            Queue order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(order_pay_queue);
            TextMessage message=new ActiveMQTextMessage();
            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder OmsOrderResponse = omsOrderMapper.selectOne(omsOrder);
            OmsOrderItem omsOrderItemParam = new OmsOrderItem();
            omsOrderItemParam.setOrderSn(omsOrderParam.getOrderSn());
            List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItemParam);
            OmsOrderResponse.setOmsOrderItems(omsOrderItems);
            message.setText(JSON.toJSONString(OmsOrderResponse));
            omsOrderMapper.updateByExample(updateOrder,e);
             producer.send(message);
             session.commit();

        } catch (JMSException e1) {
            try {
                session.rollback();
            } catch (JMSException e2) {
                e2.printStackTrace();
            }
            e1.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }

    }



}
