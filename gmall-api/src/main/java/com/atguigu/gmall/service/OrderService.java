package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsOrder;

public interface OrderService {

    String checkOrderCode(String memberId, String tradeCode);

    String genTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOmsOrderByOrderSn(String orderSn);

    void updateOrder(OmsOrder omsOrder);
}
