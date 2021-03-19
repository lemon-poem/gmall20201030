package com.atguigu.gmall.gmallredissiontest.redission;

import java.util.ArrayList;
import java.util.List;

public class Test2 {
    public static void main(String[] args) {
        System.out.println("666");
        int[] nums={4,2,4,5,6};
        Test2 test2=new Test2();
        int i=test2.maximumUniqueSubarray(nums);
        System.out.println(i);
    }
    public int maximumUniqueSubarray(int[] nums) {
        List<Integer> list=new ArrayList<>();
        int max=0;
        for (int i = 0; i < nums.length; i++) {
            int max1=0;
            for (int j = i; j < nums.length; j++) {
                if(!list.contains(nums[j])){
                    list.add(nums[j]);
                    max1=max1+nums[j];
                }else {
                    break;
                }

            }
            list.clear();
            if(max1>max){
                max=max1;
            }
        }
        return max;

    }
}
