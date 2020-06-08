package cn.tera.protobuf.coder.models.java;

import cn.tera.protobuf.coder.annotations.DecodeDefault;
import cn.tera.protobuf.coder.annotations.EncodeDefault;

public class DecodeCustomStudent {
    @DecodeDefault(value = {"Peter"})
    public String defaultName;

    @DecodeDefault(value = {"Peter", "Mary", "John"})
    public String multipleDefaults;

    @DecodeDefault(value = {"响应%s市号召，不再提供一次性用品"})
    public String replacedDefault;
}
