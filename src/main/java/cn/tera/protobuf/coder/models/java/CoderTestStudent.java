package cn.tera.protobuf.coder.models.java;

import java.util.List;

public class CoderTestStudent {
    public int age;
    public Parent father;
    public List<String> friends;
    public long hairCount;
    public double height;
    public List<Hobby> hobbies;
    public boolean isMale;
    public Parent mother;
    public String name;
    public float weight;

    public static class Parent {
        public int age;
        public String name;
    }

    public static class Hobby {
        public int cost;
        public String name;
    }
}