package com.atguigu.gmall.payment;


import com.atguigu.gmall.util.ActiveMQUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.spring.annotation.MapperScan;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@MapperScan("com.atguigu.gmall.payment.mapper")
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Test
    public void contextLoads() throws JMSException {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection = connectionFactory.createConnection();
        System.out.println(connection);
        System.out.println(123);
    }

}
