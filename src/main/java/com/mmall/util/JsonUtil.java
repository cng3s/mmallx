package com.mmall.util;

import com.google.common.collect.Lists;
import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {

        // 对象的所有字段全部列入
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

        // 取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);

        // 忽略空Bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        // 所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        // 忽略在json字符串中存在，但是在java对象中不存在对应属性的情况，防止错误。
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String)obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error", e);
            return null;
        }
    }

    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj :
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error", e);
            return null;
        }
    }

    // <T>代表该方法是泛型方法，后面跟着的T代表返回值是泛型T
    // 相当于C++中的 : template <typename T> T string2Obj(...) { ... }
    // @clas: str要转换为的类型
    public static <T> T string2Obj(String str, Class<T> clas) {
        if (StringUtils.isEmpty(str) || clas == null) {
            return null;
        }

        try {
            return clas.equals(String.class) ? (T)str : objectMapper.readValue(str, clas);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    // 处理 集合 等高级数据结构，将它们转换成TypeReference类型
    // 这个方法主要是 Jackson序列化会把集合中的具体类型自动转为HashMap
    // 而我们又需要将 HashMap 再转为正确的类型（即原本传入的类型）
    // @typeReference: 目标类型引用
    public static <T> T string2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    // 针对Collection集合的泛型json序列化操作
    // @collectionClass: Collection集合类的类型
    // @elementClasses: 集合
    // 坑：Class<?>不能写成Class<T>，因为这时候还没有确定返回值，T 和 ? 有可能不是一种类型
    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?> ... elementClasses) {
        JavaType javaType = objectMapper.getTypeFactory()
                .constructParametricType(collectionClass, elementClasses);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    public static void main(String[] args) throws IOException {

        User user1 = new User();
        user1.setId(1);
        user1.setEmail("chneg33@outlook.com");

        String user1Json = JsonUtil.obj2String(user1);
        String user1JsonPretty = JsonUtil.obj2StringPretty(user1);
        log.info("user1Json: {}", user1Json);
        log.info("user1JsonPretty: {}", user1JsonPretty);

        User user2 = JsonUtil.string2Obj(user1Json, User.class);
        System.out.println(user2);

        List<User> userList = Lists.newArrayList();
        userList.add(user1);
        userList.add(user2);

        String userListStr = JsonUtil.obj2StringPretty(userList);
        log.info(userListStr);

        // 注意 - 坑：
        // userListObj1数据结构是ArrayList套LinkedHashMap(即链表结点挂上红黑树)
        // 这个操作是Jackson 内部json序列化内部干的
        // 所以当你要调用userListObj1.get(0).getId() 或者 其他的User类型的get方法 是非法的
        // 为什么调用userListObj1.get(0).getId()是非法的？
        // 因为 Java 是强类型语言，需要通过 指定方法转换 才能使用。
        // 所以接下来用 string2Obj(String str, TypeReference<T> typeReference) { ... }
        // 和 string2Obj(String str, Class<?> collectionClass, Class<?> ... elementClasses) { ... }
//        List<User> userListErrObj1 = JsonUtil.string2Obj(userListStr, List.class);
//        System.out.println(userListErrObj1.get(0));
//        for (User user : userListErrObj1) {
//            System.out.println(user);
//        }









        // 使用这种方法，就把LinkedHashMap转为了User，从ArrayList<LinkedHashMap<?>> 变成了 ArrayList<User>
        List<User> userListObj1 = JsonUtil.string2Obj(userListStr, new TypeReference<List<User>>() {});
        if (userListObj1 == null) {
            return;
        }
        System.out.println(userListObj1.get(0).getId());

        List<User> userListObj2 = JsonUtil.string2Obj(userListStr, List.class, User.class);
        if (userListObj2 == null) {
            return;
        }
        System.out.println(userListObj2.get(0).getId());

        System.out.println();
    }
}
