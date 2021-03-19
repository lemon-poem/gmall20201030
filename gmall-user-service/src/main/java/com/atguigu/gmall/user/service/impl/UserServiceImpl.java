package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = userMapper.selectAllUser();
        return umsMembers;
    }

    @Override
    public List<UmsMemberReceiveAddress> getUmsMemberReceiveAddress(String memberId) {
        /*UmsMemberReceiveAddress umsMemberReceiveAddress=new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMember_id(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses=umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);*/
        Example example = new Example(UmsMemberReceiveAddress.class);
        example.createCriteria().andEqualTo("memberId", memberId);
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(example);
        return umsMemberReceiveAddresses;

    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            //查询reids缓存
            jedis = redisUtil.getJedis();
            //如果jedis连接正常
            if (jedis != null) {
                //查询缓存得到用户数据
                String userMemberStr = jedis.get("user:" + umsMember.getUsername()+umsMember.getPassword() + ":info");
                //如果缓存里面有这个用户的数据
                if (StringUtils.isNotBlank(userMemberStr)) {
                    UmsMember umsMemberFromCache = JSON.parseObject(userMemberStr, UmsMember.class);
                    String password = umsMemberFromCache.getPassword();
                    if (password.equals(umsMember.getPassword())) {
                        return umsMemberFromCache;
                    }
                    return null;
                    //如果缓存里面没用这个用户的数据,查询数据库
                } else {
                    UmsMember umsMemberFromDb = loginFromDb(umsMember);
                    //如果查询结果不为空，把结果写入redis
                    if (umsMemberFromDb != null) {
                        jedis.setex("user:" + umsMember.getUsername()+umsMember.getPassword() + ":info", 60 * 60 * 3, JSON.toJSONString(umsMemberFromDb));
                        return umsMemberFromDb;

                    }
                    return null;
                }
                //如果没有连接上redis查询数据库
            } else {
                UmsMember umsMemberFromDb = loginFromDb(umsMember);
                //如果查询结果不为空，把结果写入redis
                if (umsMemberFromDb != null) {
                    jedis.setex("user:" + umsMember.getPassword() + ":info", 60 * 60 * 3, JSON.toJSONString(umsMemberFromDb));

                    return umsMemberFromDb;
                }
                return null;
            }
        } finally {
            jedis.close();
        }

    }

    @Override
    public void addTokenCache(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+memberId+":token",60*60*2,token);
        jedis.close();
    }

    @Override
    public UmsMember addOAuthUser(UmsMember umsMember) {
       userMapper.insertSelective(umsMember);
       return umsMember;
    }

    @Override
    public UmsMember checkOAuthUser(String idstr) {
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(idstr);
        UmsMember umsMemberCheck = userMapper.selectOne(umsMember);
        return umsMemberCheck;
    }

    @Override
    public UmsMemberReceiveAddress getAddressById(String receivedAddressId) {
        UmsMemberReceiveAddress address = new UmsMemberReceiveAddress();
        address.setId(receivedAddressId);
        UmsMemberReceiveAddress address1 = umsMemberReceiveAddressMapper.selectOne(address);
        return address1;
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> umsMembers = userMapper.select(umsMember);
        if (umsMembers != null) {
            return umsMembers.get(0);
        }
        return null;
    }
}
