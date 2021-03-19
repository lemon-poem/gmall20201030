package com.example.demo;

import java.util.HashMap;
import java.util.Map;

public class Test2 {
    public  static void main(String[] args){
        Map<String,String> map=new HashMap<>();
        System.out.println("map==null?"+map==null);
        System.out.println("map==empty?"+map.isEmpty());
    }
}
