package cn.tera.protobuf.coder.decoder;

import cn.tera.protobuf.coder.Helper;
import cn.tera.protobuf.coder.annotations.DecodeDefault;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomProtobufDecoder {
    private byte[] buffer;
    private int pos;
    private int limit;
    private String split = "%s";

    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        buffer = bytes;
        pos = 0;
        limit = bytes.length;
        return deserializeObject(bytes.length, clazz);
    }

    private <T> T deserializeObject(int limit, Class<T> clazz) {
        try {
            T result = clazz.newInstance();
            List<Field> fields = Helper.getAllFields(clazz);
            if (fields.size() == 0) {
                pos = limit;
                return result;
            }
            Map<Integer, Field> fieldMap = Helper.sortFields(fields);
            int max = fieldMap.keySet().stream().max(Comparator.comparing(f -> f)).get();
            while (pos < limit) {
                int[] tag = readTag();
                int fieldNum = tag[0];
                if (fieldNum > max) {
                    pos = limit;
                    return result;
                }
                boolean isDefault = tag[1] == 1;
                if (fieldMap.containsKey(fieldNum)) {
                    Field field = fieldMap.get(fieldNum);
                    if (field.getType().equals(double.class)) {
                        field.set(result, readDouble());
                    } else if (field.getType().equals(float.class)) {
                        field.set(result, readFloat());
                    } else if (field.getType().equals(String.class)) {
                        //判断是否需要走默认值逻辑
                        if (isDefault) {
                            int defaultIndex = readRawVarint32();
                            int index = defaultIndex >> 1;
                            //获取序号字节类型的最后一个bit，判断是否要走替换的逻辑
                            boolean replace = (defaultIndex & 1) == 1;
                            //对于客户端来说，没有多版本的概念，所以直接
                            DecodeDefault feature = field.getAnnotation(DecodeDefault.class);
                            if (feature != null) {
                                String[] values = feature.value();
                                if (values.length > index) {
                                    if (replace) {
                                        //如果需要替换，那么久一个一个替换
                                        String params = readString();
                                        List<String> paramList = Arrays.asList(params.split(split));
                                        field.set(result, Helper.format(values[index], paramList, split));
                                    } else {
                                        //不需要替换，那就直接取索引对应的默认值即可
                                        field.set(result, values[index]);
                                    }
                                }
                            }
                        } else {
                            //不需要默认值，那么就正常解码即可
                            field.set(result, readString());
                        }
                    } else if (field.getType().equals(int.class)) {
                        field.set(result, readInt32());
                    } else if (field.getType().equals(boolean.class)) {
                        field.set(result, readBoolean());
                    } else if (field.getType().equals(long.class)) {
                        field.set(result, readInt64());
                    } else if (field.getType().equals(List.class)) {
                        String generciType = field.getGenericType().getTypeName();
                        if (field.get(result) != null) {
                            ((List) field.get(result)).addAll(readList(generciType));
                        } else {
                            field.set(result, readList(generciType));
                        }
                    } else {
                        field.set(result, readObject(field.getType()));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


    private int[] readTag() throws IOException {
        int[] result = new int[2];
        int tag = readRawVarint32();
        result[0] = tag >> 1;
        result[1] = tag & 1;
        return result;
    }

    private List readList(String genericType) throws IOException {
        if ("java.util.List<java.lang.Integer>".equalsIgnoreCase(genericType)) {
            return readIntegerList();
        } else if ("java.util.List<java.lang.String>".equalsIgnoreCase(genericType)) {
            return readStringList();
        } else if ("java.util.List<java.lang.Double>".equalsIgnoreCase(genericType)) {
            return readDoubleList();
        } else if ("java.util.List<java.lang.Float>".equalsIgnoreCase(genericType)) {
            return readFloatList();
        } else if ("java.util.List<java.lang.Boolean>".equalsIgnoreCase(genericType)) {
            return readBooleanList();
        } else if ("java.util.List<java.lang.Long>".equalsIgnoreCase(genericType)) {
            return readLongList();
        } else {
            return readObjectList(genericType);
        }
    }

    private <T> T readObject(Class<T> clazz) {
        try {
            int length = readRawVarint32();
            int limit = pos + length;
            return deserializeObject(limit, clazz);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private List readObjectList(String genericType) {
        List result = new ArrayList();
        try {
            Pattern pattern = Pattern.compile("<(.*?)>");
            Matcher matcher = pattern.matcher(genericType);
            if (matcher.find()) {
                String className = matcher.group(1);
                result.add(readObject(Class.forName(className)));
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    private List readLongList() throws IOException {
        List result = new ArrayList();
        int length = readRawVarint32();
        int limit = pos + length;
        while (pos < limit) {
            long ss = readInt64();
            result.add(ss);
        }
        return result;
    }

    private List readBooleanList() throws IOException {
        List result = new ArrayList();
        int length = readRawVarint32();
        int limit = pos + length;
        while (pos < limit) {
            result.add(readBoolean());
        }
        return result;
    }

    private List readDoubleList() throws IOException {
        List result = new ArrayList();
        int length = readRawVarint32();
        int limit = pos + length;
        while (pos < limit) {
            result.add(readDouble());
        }
        return result;
    }

    private List readFloatList() throws IOException {
        List result = new ArrayList();
        int length = readRawVarint32();
        int limit = pos + length;
        while (pos < limit) {
            result.add(readFloat());
        }
        return result;
    }

    private List readIntegerList() throws IOException {
        List result = new ArrayList();
        int length = readRawVarint32();
        int limit = pos + length;
        while (pos < limit) {
            result.add(readInt32());
        }
        return result;
    }

    private List readStringList() throws IOException {
        List result = new ArrayList();
        result.add(readString());
        return result;
    }

    private boolean readBoolean() throws IOException {
        return this.readRawVarint64() != 0L;
    }

    private double readDouble() {
        long sss = this.readRawLittleEndian64();
        return Double.longBitsToDouble(sss);
    }

    private float readFloat() {
        return Float.intBitsToFloat(readRawLittleEndian32());
    }

    private int readInt32() throws IOException {
        return readRawVarint32();
    }

    public long readInt64() throws IOException {
        return this.readRawVarint64();
    }

    private String readString() throws IOException {
        int size = this.readRawVarint32();
        if (size > 0 && size <= this.limit - this.pos) {
            String result = new String(this.buffer, this.pos, size, Charset.forName("UTF8"));
            this.pos += size;
            return result;
        } else if (size == 0) {
            return "";
        } else if (size <= 0) {
            throw new IOException("InvalidProtocolBufferException.negativeSize");
        } else {
            throw new IOException("InvalidProtocolBufferException.truncatedMessage");
        }
    }

    public long readRawVarint64() throws IOException {
        int tempPos;
        long x;
        label51:
        {
            tempPos = this.pos;
            if (this.limit != tempPos) {
                byte[] buffer = this.buffer;
                int y;
                if ((y = buffer[tempPos++]) >= 0) {
                    this.pos = tempPos;
                    return (long) y;
                }

                if (this.limit - tempPos >= 9) {
                    if ((y = y ^ buffer[tempPos++] << 7) < 0) {
                        x = (long) (y ^ -128);
                        break label51;
                    }

                    if ((y ^= buffer[tempPos++] << 14) >= 0) {
                        x = (long) (y ^ 16256);
                        break label51;
                    }

                    if ((y ^= buffer[tempPos++] << 21) < 0) {
                        x = (long) (y ^ -2080896);
                        break label51;
                    }

                    if ((x = (long) y ^ (long) buffer[tempPos++] << 28) >= 0L) {
                        x ^= 266354560L;
                        break label51;
                    }

                    if ((x ^= (long) buffer[tempPos++] << 35) < 0L) {
                        x ^= -34093383808L;
                        break label51;
                    }

                    if ((x ^= (long) buffer[tempPos++] << 42) >= 0L) {
                        x ^= 4363953127296L;
                        break label51;
                    }

                    if ((x ^= (long) buffer[tempPos++] << 49) < 0L) {
                        x ^= -558586000294016L;
                        break label51;
                    }

                    x ^= (long) buffer[tempPos++] << 56;
                    x ^= 71499008037633920L;
                    if (x >= 0L || (long) buffer[tempPos++] >= 0L) {
                        break label51;
                    }
                }
            }

            return this.readRawVarint64SlowPath();
        }

        this.pos = tempPos;
        return x;
    }

    public int readRawVarint32() throws IOException {
        int tempPos;
        int x;
        label47:
        {
            tempPos = this.pos;
            if (this.limit != tempPos) {
                byte[] buffer = this.buffer;
                if ((x = buffer[tempPos++]) >= 0) {
                    this.pos = tempPos;
                    return x;
                }

                if (this.limit - tempPos >= 9) {
                    if ((x = x ^ buffer[tempPos++] << 7) < 0) {
                        x ^= -128;
                        break label47;
                    }

                    if ((x ^= buffer[tempPos++] << 14) >= 0) {
                        x ^= 16256;
                        break label47;
                    }

                    if ((x ^= buffer[tempPos++] << 21) < 0) {
                        x ^= -2080896;
                        break label47;
                    }

                    int y = buffer[tempPos++];
                    x ^= y << 28;
                    x ^= 266354560;
                    if (y >= 0 || buffer[tempPos++] >= 0 || buffer[tempPos++] >= 0 || buffer[tempPos++] >= 0 || buffer[tempPos++] >= 0 || buffer[tempPos++] >= 0) {
                        break label47;
                    }
                }
            }

            return (int) this.readRawVarint64SlowPath();
        }

        this.pos = tempPos;
        return x;
    }

    private long readRawVarint64SlowPath() throws IOException {
        long result = 0L;

        for (int shift = 0; shift < 64; shift += 7) {
            byte b = this.readRawByte();
            result |= (long) (b & 127) << shift;
            if ((b & 128) == 0) {
                return result;
            }
        }

        throw new IOException("InvalidProtocolBufferException.malformedVarint");
    }

    private byte readRawByte() throws IOException {
        if (this.pos == this.limit) {
            throw new IOException("InvalidProtocolBufferException.truncatedMessage");
        } else {
            return this.buffer[this.pos++];
        }
    }

    private long readRawLittleEndian64() {
        int tempPos = this.pos;
        byte[] buffer = this.buffer;
        this.pos = tempPos + 8;
        return (long) buffer[tempPos] & 255L | ((long) buffer[tempPos + 1] & 255L) << 8 | ((long) buffer[tempPos + 2] & 255L) << 16 | ((long) buffer[tempPos + 3] & 255L) << 24 | ((long) buffer[tempPos + 4] & 255L) << 32 | ((long) buffer[tempPos + 5] & 255L) << 40 | ((long) buffer[tempPos + 6] & 255L) << 48 | ((long) buffer[tempPos + 7] & 255L) << 56;
    }

    private int readRawLittleEndian32() {
        int tempPos = this.pos;
        byte[] buffer = this.buffer;
        this.pos = tempPos + 4;
        return buffer[tempPos] & 255 | (buffer[tempPos + 1] & 255) << 8 | (buffer[tempPos + 2] & 255) << 16 | (buffer[tempPos + 3] & 255) << 24;
    }
}
