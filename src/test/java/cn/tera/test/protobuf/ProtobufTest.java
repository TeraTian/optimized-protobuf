package cn.tera.test.protobuf;

import cn.tera.protobuf.model.AddressBookJson;
import cn.tera.protobuf.model.AddressBookProtos;
import cn.tera.protobuf.model.ModelTransformation;
import cn.tera.protobuf.model.NestedTestModel;
import cn.tera.protobuf.utility.Utility;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.util.JsonFormat;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ProtobufTest {
    /**
     * protobuf的基础使用
     */
    @Test
    void basicUse() {
        //build a person
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
                .setId(5)
                .setName("Peter")
                .setEmail("peter@google.com")
                .build();
        System.out.println("Peter's name is " + peter.getName());

        //encode to bytes
        byte[] bytes = peter.toByteArray();

        //decode from bytes
        AddressBookProtos.Person clone = null;
        try {
            clone = AddressBookProtos.Person.parseFrom(bytes);
            System.out.println("The clone's name is " + clone.getName());
        } catch (InvalidProtocolBufferException e) {
        }
        assertEquals(peter.getName(), clone.getName());
    }

    /**
     * varint数字编码
     */
    @Test
    void varintTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
//                .setId(91809)
                .setId(150)
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * protobuf基础编码
     */
    @Test
    void protobufBaseEncodeTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
//                .setLarge(150)
                .setLargeStr("abc")
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * 负数
     */
    @Test
    void negativeIntTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
//                .setId(-12392)
                .setId(-22)
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * uint类型
     */
    @Test
    void negativeUIntTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
                .setUintf(-22)
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * sint类型
     */
    @Test
    void negativeSIntTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
                .setSintf(23)
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * double和float
     */
    @Test
    void doubleAndFloatTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
                .setF2(8.25F)
//                .setD(1)
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * 子对象
     */
    @Test
    void embeddedTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
                .setEmbedded(
                        AddressBookProtos.Embedded.newBuilder()
                                .setE1(1))
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * repeated string
     */
    @Test
    void repeatedStringTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
                .addContacts("1")
                .addContacts("2")
                .addContacts("3")
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * repeated int
     */
    @Test
    void repeatedIntTest() {
        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
                .addNumList(1)
                .addNumList(2)
                .addNumList(3)
                .build();
        Utility.printByte(peter.toByteArray());
    }

    /**
     * json和protobuf的互相转换
     */
    @Test
    void jsonToProtobuf() {
        //构造简单的模型
        AddressBookJson model = new AddressBookJson();
        model.email = "test@tera.com";
        model.id = 1;
        model.name = "tera";
        List<String> contacts = new ArrayList<String>();
        contacts.add("mum");
        contacts.add("dad");
        contacts.add("sister");
        String json = JSON.toJSONString(model);
        System.out.println(json);
        System.out.println();

        //parser
        JsonFormat.Parser parser = JsonFormat.parser();
        //需要build才能转换
        AddressBookProtos.Person.Builder personBuilder = AddressBookProtos.Person.newBuilder();
        try {
            //转换并打印
            parser.merge(json, personBuilder);
            AddressBookProtos.Person person = personBuilder.build();
            //需要注意的是，protobuf的toString方法并不会自动转换成json，而是以更简单的方式呈现，所以一般没法直接用
            System.out.println(person.toString());

            //修改protobuf模型中的字段，并再转换会json字符串
            person = person.toBuilder().setName("protobuf").setId(2).build();
            String buftoJson = JsonFormat.printer().print(person);
            System.out.println(buftoJson);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * 不同模型之间的转换
     */
    @Test
    void modelTransformationTest() {
        //build a person
        ModelTransformation.Person peter = ModelTransformation.Person.newBuilder()
                .setId(5)
                .setName("Peter")
                .setEmail("peter@google.com")
                .build();
        System.out.println(peter.toString());
        System.out.println();

        //encode to bytes
        byte[] bytes = peter.toByteArray();

        //decode from bytes
        ModelTransformation.Computer computer = null;
        try {
            computer = ModelTransformation.Computer.parseFrom(bytes);
            System.out.println(computer.toString());
        } catch (InvalidProtocolBufferException e) {
        }
    }

    /**
     * 比较较多数量的字段分别采用平铺和子对象方式的编码大小
     */
    @Test
    void compareNestedAndNotNestedModel() {
        //采用子对象的方式
        NestedTestModel.Nested nested = NestedTestModel.Nested.newBuilder()
                .setNested1(
                        NestedTestModel.Nested1.newBuilder()
                                .setField1("1").setField2("2").setField3("3").setField4("4").setField5("5").setField6("6").setField7("7").setField8("8")
                                .setField9("9").setField10("10").setField11("11").setField12("12").setField13("13").setField14("14").setField15("15")
                                .build()
                )
                .setNested2(
                        NestedTestModel.Nested2.newBuilder()
                                .setField16("16").setField17("17").setField18("18").setField19("19").setField20("20").setField21("21").setField22("22").setField23("23")
                                .setField24("24").setField25("25").setField26("26").setField27("27").setField28("28").setField29("29").setField30("30")
                                .build()
                ).build();
        System.out.println(String.format("nested size:%s", nested.toByteArray().length));


        //平铺的方式
        NestedTestModel.NotNested notNested = NestedTestModel.NotNested.newBuilder()
                .setField1("1").setField2("2").setField3("3").setField4("4").setField5("5").setField6("6").setField7("7").setField8("8")
                .setField9("9").setField10("10").setField11("11").setField12("12").setField13("13").setField14("14").setField15("15")
                .setField16("16").setField17("17").setField18("18").setField19("19").setField20("20").setField21("21").setField22("22").setField23("23")
                .setField24("24").setField25("25").setField26("26").setField27("27").setField28("28").setField29("29").setField30("30")
                .build();
        System.out.println(String.format("not nested size:%s", notNested.toByteArray().length));
    }

    /**
     * 比较json编码与protobuf编码同样信息量下的编码大小，其中还包括了采用子对象和平铺编码方式的比较
     * 为了使得比较相对更趋近于真实情况，因此对于每个字段的key采用6个字符，value采用8个字符
     */
    @Test
    void CompareFieldNest() {
        String source0 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\"}";
        Utility.PrintCompareResult("七个字段", source0, com.tera.huazhu.protobufmodel.Nested.Seven1.newBuilder());

        String source1 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"}";
        Utility.PrintCompareResult("十四个字段", source1, com.tera.huazhu.protobufmodel.Nested.Fourteen.newBuilder());

        String source2 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"part\":{\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"}}";
        Utility.PrintCompareResult("十四个字段，包含一个七字段", source2, com.tera.huazhu.protobufmodel.Nested.OneNestedFourteen.newBuilder());

        String source3 = "{\"part1\":{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\"},\"part2\":{\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"}}";
        Utility.PrintCompareResult("十四个字段，包含两个七字段", source3, com.tera.huazhu.protobufmodel.Nested.TwoNestedFourteen.newBuilder());

        String source4 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\",\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\"}";
        Utility.PrintCompareResult("二十八个字段", source4, com.tera.huazhu.protobufmodel.Nested.TwentyEight.newBuilder());


        String source6 = "{\"field15\":{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"},\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\"}";
        Utility.PrintCompareResult("二十八个字段，包含一个十四字段", source6, com.tera.huazhu.protobufmodel.Nested.OneNestedTwentyEight.newBuilder());

        String source5 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\",\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\",\"xsxscd\":\"xkclvodo\",\"nvnnnv\":\":L()#_)(\",\"IUJNUJ\":\"893iU&*(\",\"LKOKIK\":\"LLL!!)(*\",\"AAAAAl\":\"~)!_+_)(\",\"kciII8\":\"zkcl,.(*\",\"LMK909\":\"L:>;/.,;\",\"QWE89f\":\"LKOkid90\",\"LKX983\":\"O()(OIKD\",\"LOPO90\":\"!@#*(kjC\",\"lbkvof\":\"LOPA(Q)O\",\"POlop0\":\"LKCI893i\",\"QWEOPI\":\"l;lv0()(\"}";
        Utility.PrintCompareResult("四十一个字段", source5, com.tera.huazhu.protobufmodel.Nested.FortyOne.newBuilder());

        String source7 = "{\"part\":{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"},\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\",\"xsxscd\":\"xkclvodo\",\"nvnnnv\":\":L()#_)(\",\"IUJNUJ\":\"893iU&*(\",\"LKOKIK\":\"LLL!!)(*\",\"AAAAAl\":\"~)!_+_)(\",\"kciII8\":\"zkcl,.(*\",\"LMK909\":\"L:>;/.,;\",\"QWE89f\":\"LKOkid90\",\"LKX983\":\"O()(OIKD\",\"LOPO90\":\"!@#*(kjC\",\"lbkvof\":\"LOPA(Q)O\",\"POlop0\":\"LKCI893i\",\"QWEOPI\":\"l;lv0()(\"}";
        Utility.PrintCompareResult("四十一个字段，包含一个十四字段", source7, com.tera.huazhu.protobufmodel.Nested.OneNestedFortyOne.newBuilder());

        String source8 = "{\"part1\":{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"},\"part2\":{\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\"},\"xsxscd\":\"xkclvodo\",\"nvnnnv\":\":L()#_)(\",\"IUJNUJ\":\"893iU&*(\",\"LKOKIK\":\"LLL!!)(*\",\"AAAAAl\":\"~)!_+_)(\",\"kciII8\":\"zkcl,.(*\",\"LMK909\":\"L:>;/.,;\",\"QWE89f\":\"LKOkid90\",\"LKX983\":\"O()(OIKD\",\"LOPO90\":\"!@#*(kjC\",\"lbkvof\":\"LOPA(Q)O\",\"POlop0\":\"LKCI893i\",\"QWEOPI\":\"l;lv0()(\"}";
        Utility.PrintCompareResult("四十一个字段，包含两个十四字段", source8, com.tera.huazhu.protobufmodel.Nested.TwoNestedFortyOne.newBuilder());
    }
}
