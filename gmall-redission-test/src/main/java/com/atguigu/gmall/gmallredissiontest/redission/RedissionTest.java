package com.atguigu.gmall.gmallredissiontest.redission;

import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
public class RedissionTest {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;
    @ResponseBody
    @RequestMapping("testRedssion")
    public String testRedission(){
        Jedis jedis = redisUtil.getJedis();
        RLock lock = redissonClient.getLock("lock");//声明锁
        lock.lock();//上锁
        String v = jedis.get("k");
      try {
          if (StringUtils.isBlank(v)){
              v="1";
          }
          jedis.set("k",(Integer.parseInt(v)+1)+"");
          System.out.println(v+"=>");
      }
      finally {
          jedis.close();
          lock.unlock();//解锁
      }



//        RLock lock = redissonClient.getLock("lock");
        return "success";
    }

}
