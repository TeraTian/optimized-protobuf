package cn.tera.protobuf.coder.models.java;

import cn.tera.protobuf.coder.annotations.Ignore;
import cn.tera.protobuf.coder.annotations.Tag;

public class Student {
    public int age;
    public long hairCount;
    public boolean isMale;
    public String name;
    public double score;
    public float score2;

    @Tag(23)
    public String tag;
    @Ignore
    public String ignore;
}
