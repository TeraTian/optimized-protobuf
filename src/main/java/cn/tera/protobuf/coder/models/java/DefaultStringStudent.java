package cn.tera.protobuf.coder.models.java;

import cn.tera.protobuf.coder.annotations.DecodeDefault;
import cn.tera.protobuf.coder.annotations.EncodeDefault;

public class DefaultStringStudent {
    @EncodeDefault(value = {"Peter"})
    @DecodeDefault(value = {"Peter"})
    public String defaultName;

    @EncodeDefault(value = {"Peter", "Mary", "John"})
    @DecodeDefault(value = {"Peter", "Mary", "John"})
    public String multipleDefaults;

    @EncodeDefault(value = {"亲爱的%s用户您好，欢迎回来"}, replace = true)
    @DecodeDefault(value = {"亲爱的%s用户您好，欢迎回来"})
    public String replacedDefault;
}
