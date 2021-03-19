package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
    @Reference
    CartService cartService;
    @Reference
    SkuService skuService;

    @LoginRequired(loginSuccess = false)
    @RequestMapping("checkCart")
    public String checkCart(String skuId,String isChecked,HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        String memberId=(String)request.getAttribute("memberId");
        String nickname=(String)request.getAttribute("nickname");
        //查询数据库,

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setMemberId(memberId);
        cartService.checkCart(omsCartItem);
        List<OmsCartItem> cartList = cartService.getCartList(memberId);
        for (OmsCartItem cartItem : cartList) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
        }
        modelMap.put("cartList",cartList);
        BigDecimal totalAmount=getTotalAmount(cartList);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }
    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        String memberId=(String)request.getAttribute("memberId");
        String nickname=(String)request.getAttribute("nickname");
        List<OmsCartItem> omsCartItems=new ArrayList<>();
        //判断如果userId不为空，说明用户已经登录,查询缓存
        if (StringUtils.isNotBlank(memberId)){
            omsCartItems=cartService.getCartList(memberId);
            if (omsCartItems!=null){
                for (OmsCartItem omsCartItem : omsCartItems) {
                    omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                }
            }

            //如果uerId为空,查询cookie
        }else{
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            omsCartItems=JSON.parseArray(cartListCookie,OmsCartItem.class);
            if (omsCartItems!=null) {
                for (OmsCartItem omsCartItem : omsCartItems) {
                    omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                }
            }
        }
        BigDecimal totalAmount=getTotalAmount(omsCartItems);
        modelMap.put("cartList",omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartList";
    }

    @RequestMapping("addToCart")
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response){
       //调用服务查询商品信息
      ;
       PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
       //将商品信息封装进购物车对象里面
       OmsCartItem omsCartItem = new OmsCartItem();
       omsCartItem.setCreateDate(new Date());
       omsCartItem.setDeleteStatus(0);
       omsCartItem.setPrice(skuInfo.getPrice());
       omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
       omsCartItem.setQuantity(new BigDecimal(quantity));
       omsCartItem.setProductSkuId(skuId);
       omsCartItem.setProductId(skuInfo.getProductId());
       omsCartItem.setProductName(skuInfo.getSkuName());
       omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());

        String memberId=(String)request.getAttribute("memberId");
        String nickname=(String)request.getAttribute("nickname");

       //判断用户是否登录
       List<OmsCartItem> omsCartItems=new ArrayList<>();
       //用户已经登录
       if (StringUtils.isNotBlank(memberId)){
           omsCartItem.setMemberNickname("小明");
           omsCartItem.setMemberId(memberId);
          //查询数据库，查看用户是否添加过该商品
           OmsCartItem omsCartItemForDb=cartService.ifCartExitByUser(memberId,skuId);
           //如果为空说明用户第一次添加该商品，直接添加
           if (omsCartItemForDb==null){
               cartService.addCart(omsCartItem);
           }else {
               //如果不为空说明用户添加过，更新数据
               omsCartItemForDb.setQuantity(omsCartItem.getQuantity().add(omsCartItemForDb.getQuantity()));
               cartService.updateCart(omsCartItemForDb);
           }
           //刷新缓存
           cartService.flushCartCache(memberId);
       }else{
           //调用cookie
           //判断是否存在cookie
           String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
           //首先判断cookie是否为空
           if (StringUtils.isBlank(cartListCookie)){
               //如果为空的话，直接添加
               omsCartItems.add(omsCartItem);
               CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItems),60*60*24*3,true);
           }else {   //如果cookie不为空的话
               List<OmsCartItem> omsCartItemCookies = JSON.parseArray(cartListCookie, OmsCartItem.class);
               boolean exist=if_is_exist(omsCartItem,omsCartItemCookies);
               //如果已经存在这个cookie，修改
               if (exist){
                   for (OmsCartItem omsCartItemCookie : omsCartItemCookies) {
                       if (omsCartItemCookie.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                           omsCartItemCookie.setQuantity(omsCartItem.getQuantity().add(omsCartItemCookie.getQuantity()));
                           omsCartItemCookie.setPrice(omsCartItem.getPrice().add(omsCartItemCookie.getPrice()));
                       }
                   }

               }else {
                   //如果不存在这个cookie，直接添加
                   omsCartItemCookies.add(omsCartItem);
               }

               CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItemCookies),60*60*24*3,true);

           }

       }
        return "redirect:/success.html";
    }
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount=new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")){
                totalAmount=totalAmount.add(omsCartItem.getTotalPrice());
            }
        }
        return totalAmount;
    }
    private boolean if_is_exist(OmsCartItem omsCartItem, List<OmsCartItem> omsCartItemCookies) {
        boolean b=false;
        for (OmsCartItem omsCartItemCookie : omsCartItemCookies) {
            if (omsCartItemCookie.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                b=true;
                return b;
            }
        }
        return b;
    }
}
