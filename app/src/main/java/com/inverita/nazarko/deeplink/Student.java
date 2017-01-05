package com.inverita.nazarko.deeplink;

/**
 * Created by nazarko on 12/1/16.
 */

public class Student {
    public String name;
    public  int age;

    public Student(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
