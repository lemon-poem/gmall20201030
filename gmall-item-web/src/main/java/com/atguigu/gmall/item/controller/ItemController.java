package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    SpuService spuService;
    @Reference
    SkuService skuService;
        //@ResponseBody
        @RequestMapping("{skuId}.html")
        public String item(@PathVariable String skuId ,ModelMap modelMap){
       PmsSkuInfo pmsSkuInfo=skuService.getSkuById(skuId);
       List<PmsProductSaleAttr> spuSaleAttrListCheckBySku=spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),pmsSkuInfo.getId());
       modelMap.put("skuInfo",pmsSkuInfo);

       modelMap.put("spuSaleAttrListCheckBySku",spuSaleAttrListCheckBySku);
            Map<String,String> skuSaleAttrHash=new HashMap<>();
      List<PmsSkuInfo> pmsSkuInfos=skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
            for (PmsSkuInfo skuInfo : pmsSkuInfos) {
                String v=skuInfo.getId();
                String k="";
                List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
                for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                    k+=pmsSkuSaleAttrValue.getSaleAttrValueId()+"|";
                }
                skuSaleAttrHash.put(k,v);
            }
            //将销售属性hash表放到页面
            String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
            modelMap.put("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);

            return "item";
}
    @RequestMapping("index")
    public String index(ModelMap modelMap){
        List<String> stringList=new ArrayList<>();
        for (int i = 0; i <7 ; i++) {
            stringList.add("list"+i);
        }
        modelMap.put("stringList",stringList);
        modelMap.put("hello","hello thymeleaf!!!");
        return "index";
    }
}
