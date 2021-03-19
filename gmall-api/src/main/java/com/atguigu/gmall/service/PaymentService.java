package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendDelyQueueGetPaymentStatus(String out_trade_no,String checkCount);


    Map<String,Object> checkPaymentStatus(String out_trade_no);
}
