package com.example.demo;

import java.math.BigDecimal;

public class Test1 {
    public static void main(String[] args) {
        BigDecimal b1=new BigDecimal(199);
        BigDecimal b2=new BigDecimal(199.00);
        System.out.println(b1.compareTo(b2));

    }
}
