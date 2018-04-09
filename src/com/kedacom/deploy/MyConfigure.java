package com.kedacom.deploy;

/**
 * Created by Sissi on 3/9/2018.
 */

public class MyConfigure {
    public static void main(String[] args){
        System.out.println("my configure");
        for (String arg : args) {
            System.out.println("arg="+arg);
        }
    }
}
