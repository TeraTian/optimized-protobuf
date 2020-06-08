package cn.tera.test.protobuf;

import cn.tera.protobuf.coder.Helper;
import cn.tera.protobuf.coder.decoder.CustomProtobufDecoder;
import cn.tera.protobuf.coder.encoder.CustomProtobufEncoder;
import cn.tera.protobuf.coder.models.java.DecodeCustomStudent;
import cn.tera.protobuf.coder.models.java.EncodeCustomStudent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

public class CustomCoderTest {
    @Test
    void defaultValueTest() {
        //默认值
        String source = "{\"defaultName\":\"Peter\"}";
        test(source, EncodeCustomStudent.class, DecodeCustomStudent.class);

        //非默认值
        String source2 = "{\"defaultName\":\"NotDefault\"}";
        test(source2, EncodeCustomStudent.class, DecodeCustomStudent.class);
    }

    @Test
    void multipleDefaultsTest() {
        //第一个默认值
        String source = "{\"multipleDefaults\":\"Peter\"}";
        test(source, EncodeCustomStudent.class, DecodeCustomStudent.class);

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
            System.out.println();
            System.out.println("-------------------  tera encode result  -------------------");
            T javaModel = JSON.parseObject(source, encodeClass);
            byte[] teraBytes = new CustomProtobufEncoder("47", (app, target) -> {
                int a = Integer.parseInt(app);
                int b = Integer.parseInt(target);
                return a > b ? 1 : (a == b ? 0 : -1);
            }).serialize(javaModel, encodeClass);
            Helper.printBytes(teraBytes);


            System.out.println("-------------------  tera decode result  -------------------");
            U deserialJavaModel = new CustomProtobufDecoder().deserialize(teraBytes, decodeClass);
            SerializerFeature[] features = new SerializerFeature[]{
                    SerializerFeature.PrettyFormat
            };
            System.out.println(JSON.toJSONString(deserialJavaModel, features));
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~分割线~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println();
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("");
    }
}
