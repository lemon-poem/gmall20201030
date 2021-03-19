package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSearchParm;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParm pmsSearchParm);
}
