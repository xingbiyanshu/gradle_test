package com.example.sissi.database_test;

/**
 * Created by Sissi on 2/1/2018.
 */

public class Employee {
    private int id;
    private String name;
    private String gender;
    private int birth;
    private String nativePlace;
    private String address;
    private String phone;
    private String email;
    private int departmentId;

    public Employee(/*int id, */String name, String gender, int birth, String nativePlace,
                    String address, String phone, String email, int departmentId){
//        this.id = id; // 数据库主键自动增长
        this.name = name;
        this.gender = gender;
        this.birth = birth;
        this.nativePlace = nativePlace;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.departmentId = departmentId;
    }
}
