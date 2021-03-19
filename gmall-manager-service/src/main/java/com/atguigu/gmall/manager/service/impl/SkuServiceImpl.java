package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.manager.mapper.PmsSkuAtrrValueMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAtrrValueMapper pmsSkuAtrrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //添加skuInfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        // 插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuAtrrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        // 插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        // 插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

    }

    @Override
    public PmsSkuInfo getSkuByIdFromDb(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //链接缓存
        UUID randomUUID = UUID.randomUUID();
        String skuValue = randomUUID.toString();
        Jedis jedis = redisUtil.getJedis();
        String skuKey = "sku:" + skuId + ":info";
        String skujson = jedis.get(skuKey);
        //查询缓存
        if (StringUtils.isNotBlank(skujson)) {
            pmsSkuInfo = JSON.parseObject(skujson, PmsSkuInfo.class);
            //如果缓存中没有查询数据库mysql
        } else {
            //设置redis分布式锁
            String ok = jedis.set("sku:" + skuId + ":lock", skuValue, "nx", "px", 10);
            //设置成功，有权在10秒有效访问时间内访问数据库
            if (StringUtils.isNotBlank(ok) && ok.equals("OK")) {
                pmsSkuInfo = getSkuByIdFromDb(skuId);
                //mysql查询结构存入redis
                if (pmsSkuInfo != null) {
                    jedis.set("sku:"+skuId+":info", JSON.toJSONString(pmsSkuInfo));
                } else {
                    //为了防止缓冲穿透,将null值或空串设置 给redis
                    jedis.setex("sku:"+skuId+":info", 3 * 60, JSON.toJSONString(""));
                }
                //任务完成之后把占用的锁释放
                //进行判断是不是这个线程的锁是的话才删除
                String token=jedis.get("sku:"+skuId+":lock");
                if (StringUtils.isNotBlank(token)&&token.equals(skuValue)) {
                    jedis.del("sku:"+skuId+":lock");
                }

                //设置失败，自旋（该线程在睡眠几秒后重新访问）
            } else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);
            }
        }
        jedis.close();

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> SkuSaleAttrValueList = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return SkuSaleAttrValueList;
    }

    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId=pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAtrrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {
        boolean b=false;
        PmsSkuInfo skuInfo = new PmsSkuInfo();
        skuInfo.setId(productSkuId);
        PmsSkuInfo skuInfo1 = pmsSkuInfoMapper.selectOne(skuInfo);
        BigDecimal price1=skuInfo1.getPrice();
        if (price.compareTo(price1)==0){
            b=true;
        }
        return b;
    }
}
