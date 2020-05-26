package cn.tera.test.protobuf;

import cn.tera.protobuf.model.AddressBookProtos;
import com.google.protobuf.InvalidProtocolBufferException;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ProtobufTest {
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
}
