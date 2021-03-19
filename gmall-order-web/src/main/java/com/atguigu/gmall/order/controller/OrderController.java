package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    SkuService skuService;
    @Reference
    UserService userService;
    @Reference
    CartService cartService;
    //submitOrder
    @Reference
    OrderService orderService;
    @LoginRequired(loginSuccess = true)
    @RequestMapping("submitOrder")
    public ModelAndView submitOrder(String tradeCode,String receivedAddressId,BigDecimal totalAmount,HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        String memberId=(String)request.getAttribute("memberId");
        String nickname=(String)request.getAttribute("nickname");
        //检查交易码
        String success=orderService.checkOrderCode(memberId,tradeCode);
        if (success.equals("success")){
            List<OmsOrderItem> omsOrderItems=new ArrayList<>();
            List<OmsCartItem> omsCartItems = cartService.getCartList(memberId);
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")){
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    boolean isPrice=skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if (!isPrice){
                        ModelAndView mv=new ModelAndView("tradeFail");
                        return mv;
                    }
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItems.add(omsOrderItem);
                }
            }
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOmsOrderItems(omsOrderItems);
            omsOrder.setAutoConfirmDay(7);
            UmsMemberReceiveAddress address=userService.getAddressById(receivedAddressId);
            omsOrder.setReceiverCity(address.getCity());
            omsOrder.setReceiverPostCode(address.getPostCode());
            omsOrder.setReceiverProvince(address.getProvince());
            omsOrder.setReceiverDetailAddress(address.getDetailAddress());
            omsOrder.setReceiverPhone(address.getPhoneNumber());
            omsOrder.setReceiverRegion(address.getRegion());
            omsOrder.setMemberUsername(address.getName());
            omsOrder.setMemberId(address.getMemberId());
            omsOrder.setCreateTime(new Date());
            //构造外部订单编号
            String orderSn="gmall";
            orderSn=orderSn+System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("YYYYMMDDHHmmss");
            orderSn=orderSn+format.format(new Date());
            //外部订单号
            omsOrder.setOrderSn(orderSn);
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setTotalAmount(totalAmount);
            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.DATE,1);
            Date time=instance.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType(0);
            omsOrder.setOrderType(1);
            omsOrder.setReceiverName("666");
            //将订单和订单详情写入到数据库\
            orderService.saveOrder(omsOrder);
            //删除购物车
            //重定向到支付系统
            ModelAndView mv=new ModelAndView("redirect:http://payment.gmall.com:8077/index");
            mv.addObject("orderSn",orderSn);
            mv.addObject("totalAmount",totalAmount);
            return mv;
        }else{
            ModelAndView mv=new ModelAndView("tradeFail");
            return mv;
        }
    }
    //toTrade
    @LoginRequired(loginSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        String memberId=(String)request.getAttribute("memberId");
        String nickname=(String)request.getAttribute("nickname");
        List<UmsMemberReceiveAddress> umsMemberReceiveAddress = userService.getUmsMemberReceiveAddress(memberId);
        modelMap.put("userAddressList",umsMemberReceiveAddress);
        List<OmsCartItem> cartItemList = cartService.getCartList(memberId);
        List<OmsOrderItem> omsOrderItems=new ArrayList<>();
        for (OmsCartItem omsCartItem : cartItemList) {
            if (omsCartItem.getIsChecked().equals("1")){
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                 omsOrderItem.setProductName(omsCartItem.getProductName());
                 omsOrderItem.setProductPic(omsCartItem.getProductPic());
                 omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItems.add(omsOrderItem);
            }
        }
        BigDecimal  totalAmount=new BigDecimal("0");
        if (cartItemList!=null){
              totalAmount=getTotalAmount(cartItemList);
        }

        modelMap.put("totalAmount",totalAmount);
        modelMap.put("omsOrderItems",omsOrderItems);
        //生成交易码
        String tradeCode=orderService.genTradeCode(memberId);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount=new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")){
                totalAmount=totalAmount.add(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            }
        }
        return totalAmount;
    }
}
