package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {
    @Reference
    AttrService attrService;
    @Reference
    SearchService searchService;

    @RequestMapping("list.html")
    public String list(PmsSearchParm pmsSearchParm, ModelMap modelMap) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParm);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        Set<String> skuIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                skuIdSet.add(pmsSkuAttrValue.getValueId());
            }
        }
        List<PmsBaseAttrInfo> attrList = attrService.getAttrValueListByValueId(skuIdSet);
        //attrList
        modelMap.put("attrList", attrList);
        String[] delValueId = pmsSearchParm.getValueId();
        List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<PmsSearchCrumb>();
        if (delValueId != null) {

            for (String s : delValueId) {
                Iterator<PmsBaseAttrInfo> iterator = attrList.iterator();
                //新建一个面包屑
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //对参数进行处理
                pmsSearchCrumb.setValueId(s);
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParm, s));

                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {

                        if (s.equals(pmsBaseAttrValue.getId())) {
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }

                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
        }

        String urlParam = getUrlParam(pmsSearchParm);
        modelMap.put("urlParam", urlParam);
      /*  List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<PmsSearchCrumb>();
        for (String s : delValueId) {
            PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
            //对参数进行处理
            pmsSearchCrumb.setValueId(s);
            pmsSearchCrumb.setValueName(s);
            pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParm, s));
            pmsSearchCrumbs.add(pmsSearchCrumb);
        }*/
        modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        return "list";
    }

    private String getUrlParamForCrumb(PmsSearchParm pmsSearchParm, String delValueId) {
        String urlParam = "";
        String keyword = pmsSearchParm.getKeyword();
        String catalog3Id = pmsSearchParm.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParm.getValueId();
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
            ;
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue;
                if (!valueId.equals(delValueId)) {
                    urlParam = urlParam + "&valueId=" + valueId;

                }
            }

        }
        return urlParam;
    }

    private String getUrlParam(PmsSearchParm pmsSearchParm) {
        String urlParam = "";
        String keyword = pmsSearchParm.getKeyword();
        String catalog3Id = pmsSearchParm.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParm.getValueId();
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
            ;
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue;
                urlParam = urlParam + "&valueId=" + valueId;
            }

        }
        return urlParam;
    }
    @LoginRequired(loginSuccess = false)
    @RequestMapping("index")
    public String index() {
        return "index";
    }


}
