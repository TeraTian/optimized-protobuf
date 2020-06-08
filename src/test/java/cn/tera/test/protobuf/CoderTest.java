package cn.tera.test.protobuf;

import cn.tera.protobuf.coder.Helper;
import cn.tera.protobuf.coder.decoder.BasicDecoder;
import cn.tera.protobuf.coder.encoder.BasicEncoder;
import cn.tera.protobuf.coder.models.java.Student;
import cn.tera.protobuf.coder.models.protobuf.ProtobufStudent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.Message;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoderTest {
    @Test
    void basicEncoderTest() {
        String source = "{\"score2\":13213.1231,\"age\":5,\"name\":\"Peter\",\"hairCount\":183728182371871131,\"isMale\":true,\"score\":13213.1231}";
        test(source, Student.class, ProtobufStudent.Student.class);
    }

    @Test
    void tagAnnotationTest(){
        String source = "{\"name\":\"Peter\",\"score\":13213.1231,\"tag\":\"tagtest\"}";
        test(source, Student.class, ProtobufStudent.Student.class);
    }

    @Test
    void ignoreAnnotationTest(){
        String source = "{\"name\":\"Peter\",\"score\":13213.1231,\"ignore\":\"ignoretest\"}";
        test(source, Student.class, ProtobufStudent.Student.class);
    }



    /**
     * test method
     *
     * @param source        model json
     * @param javaClass     java class
     * @param protobufClass protobuf class
     */
    static <T, P extends Message> void test(String source, Class<T> javaClass, Class<P> protobufClass) {
        try {
            System.out.println("-------------------     source json     --------------------");
            System.out.println(source);
            System.out.println("count:" + source.getBytes().length);
            System.out.println();
            System.out.println("-------------------protobuf encode result-------------------");
            Message.Builder builder = (Message.Builder) protobufClass.getMethod("newBuilder").invoke(null);
            byte[] protoBytes = Helper.protobufSerialize(source, builder);
            Helper.printBytes(protoBytes);
            builder.mergeFrom(protoBytes);


            System.out.println();
            System.out.println("-------------------  tera encode result  -------------------");
            T javaModel = JSON.parseObject(source, javaClass);
            byte[] teraBytes = BasicEncoder.serialize(javaModel, javaClass);
            Helper.printBytes(teraBytes);

            System.out.println();
            System.out.println("------------------- bytes compare result -------------------");
            System.out.println(Helper.compareBytes(protoBytes, teraBytes));

            System.out.println();
            System.out.println("-------------------  tera decode result  -------------------");
            T deserialJavaModel = new BasicDecoder().deserialize(teraBytes, javaClass);
            System.out.println(JSON.toJSON(deserialJavaModel));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("");
    }
}
