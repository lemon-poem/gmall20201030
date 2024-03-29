package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.manager.util.PmsUploadUtil;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuListController {
    @Reference
    SpuService spuService;
    //spuImageList
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId) {
        List<PmsProductImage> pmsProductImages=spuService.spuImageList(spuId);
        return pmsProductImages;
    }
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrs=spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }
   //fileUpload
   @RequestMapping("fileUpload")
   @ResponseBody
   public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        String imgUrl= PmsUploadUtil.uploadImage(multipartFile);
       System.out.println(imgUrl);
       return imgUrl;
   }
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
         spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }
    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id) {
        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

}
