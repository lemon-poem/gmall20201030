package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SecKillController {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RedisUtil redisUtil;
    @ResponseBody
    @RequestMapping("indexRedission")
    //先到先得，信号灯
    public String indexRedission(){
        Jedis jedis = redisUtil.getJedis();
        int stock=Integer.parseInt(jedis.get("phone"));
        RSemaphore semaphore = redissonClient.getSemaphore("phone");
        boolean b = semaphore.tryAcquire();

        if(b){
            System.out.println("当前用户抢购成功"+"库存剩余数量"+stock+"当前抢购人数"+(1000-stock));
        }else{
            System.out.println("当前用户抢购失败");
        }
        jedis.close();
        return "1";
    }
    //凭手气秒杀,同一个时间内只有一个能成功
    @ResponseBody
    @RequestMapping("index")
    public String index(){
        Jedis jedis = redisUtil.getJedis();
        String phone = jedis.get("phone");
        jedis.watch("phone");
        int stock=Integer.parseInt(jedis.get("phone"));
        Transaction multi = jedis.multi();
        multi.decr("phone");
        List<Object> exec = multi.exec();
        if(exec!=null&&exec.size()>0){
            System.out.println("当前用户抢购成功"+"库存剩余数量"+stock+"当前抢购人数"+(1000-stock));
        }
        System.out.println("start:"+phone);
        if(Integer.parseInt(phone)>0){
            System.out.println("当前用户抢购失败");
        }
        jedis.close();
        return "1";
    }
}
