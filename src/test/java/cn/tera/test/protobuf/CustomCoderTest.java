package cn.tera.test.protobuf;

import cn.tera.protobuf.coder.Helper;
import cn.tera.protobuf.coder.decoder.CustomProtobufDecoder;
import cn.tera.protobuf.coder.encoder.CustomProtobufEncoder;
import cn.tera.protobuf.coder.models.java.DecodeCustomStudent;
import cn.tera.protobuf.coder.models.java.DefaultStringStudent;
import cn.tera.protobuf.coder.models.java.EncodeCustomStudent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

public class CustomCoderTest {
@Test
void defaultValueTest() {
    //默认值
    String source = "{" +
            "  \"defaultName\": \"Peter\"," +
            "  \"multipleDefaults\": \"Mary\"," +
            "  \"replacedDefault\": \"亲爱的Tera用户您好，欢迎回来\"" +
            "}";
    test(source, DefaultStringStudent.class, DefaultStringStudent.class);

    //非默认值
    String source2 = "{" +
            "  \"defaultName\": \"NotDefault\"," +
            "  \"multipleDefaults\": \"Ben\"," +
            "  \"replacedDefault\": \"不是默认值\"" +
            "}";
    test(source2, DefaultStringStudent.class, DefaultStringStudent.class);
}

    @Test
    void multipleDefaultsTest() {
        //第一个默认值
        String source = "{\"multipleDefaults\":\"Peter\"}";
        test(source, DefaultStringStudent.class, DefaultStringStudent.class);

        //第二个默认值
        String source2 = "{\"multipleDefaults\":\"Mary\"}";
        test(source2, EncodeCustomStudent.class, DecodeCustomStudent.class);

        //非默认值
        String source3 = "{\"multipleDefaults\":\"NotDefault\"}";
        test(source3, EncodeCustomStudent.class, DecodeCustomStudent.class);
    }

    @Test
    void replaceDefaultsTest() {
        //上海默认值
        String source = "{\"replacedDefault\":\"响应上海市号召，不再提供一次性用品\"}";
        test(source, EncodeCustomStudent.class, DecodeCustomStudent.class);

        //New York默认值
        String source2 = "{\"replacedDefault\":\"响应New York市号召，不再提供一次性用品\"}";
        test(source2, EncodeCustomStudent.class, DecodeCustomStudent.class);

        //无关文案
        String source3 = "{\"replacedDefault\":\"无关的文案，也不会被丢弃\"}";
        test(source3, EncodeCustomStudent.class, DecodeCustomStudent.class);
    }

    /**
     * test method
     *
     * @param source      model json
     * @param encodeClass encode class
     * @param decodeClass decode Class
     */
static <T, U> void test(String source, Class<T> encodeClass, Class<U> decodeClass) {
    try {
        System.out.println(source);
        System.out.println("-------------------  编码结果  -------------------");
        T javaModel = JSON.parseObject(source, encodeClass);
        //这里传入了一个APP版本的比较方法，以确定默认值能否匹配当前请求对应的APP版本
        byte[] teraBytes = new CustomProtobufEncoder("47", (app, target) -> {
            int a = Integer.parseInt(app);
            int b = Integer.parseInt(target);
            return a > b ? 1 : (a == b ? 0 : -1);
        }).serialize(javaModel, encodeClass);
        Helper.printBytes(teraBytes);

//            System.out.println("-------------------  tera decode result  -------------------");
//            U deserialJavaModel = new CustomProtobufDecoder().deserialize(teraBytes, decodeClass);
//            SerializerFeature[] features = new SerializerFeature[]{
//                    SerializerFeature.PrettyFormat
//            };
//            System.out.println(JSON.toJSONString(deserialJavaModel, features));
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }

    System.out.println("");
}
}
