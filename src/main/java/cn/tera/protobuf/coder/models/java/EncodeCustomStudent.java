package cn.tera.protobuf.coder.models.java;

import cn.tera.protobuf.coder.annotations.AppVersion;
import cn.tera.protobuf.coder.annotations.EncodeDefault;
import cn.tera.protobuf.coder.annotations.EncodeDefaults;
import cn.tera.protobuf.coder.annotations.Version;

import java.util.List;

public class EncodeCustomStudent {
    @EncodeDefault(value = {"Peter"})
    public String defaultName;

    @EncodeDefault(value = {"Peter", "Mary", "John"})
    public String multipleDefaults;

    @EncodeDefault(value = {"响应%s市号召，不再提供一次性用品"}, replace = true)
    public String replacedDefault;
}
