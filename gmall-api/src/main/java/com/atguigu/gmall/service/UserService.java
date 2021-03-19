package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getUmsMemberReceiveAddress(String memberId);

    UmsMember login(UmsMember umsMember);

    void addTokenCache(String token, String memberId);


    UmsMember addOAuthUser(UmsMember umsMember);

    UmsMember checkOAuthUser(String idstr);

    UmsMemberReceiveAddress getAddressById(String receivedAddressId);
}
