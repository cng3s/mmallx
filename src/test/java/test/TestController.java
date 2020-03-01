package test;

import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ExecutionException;

// 测试natapp能不能使用的一个类
// 如果要使用这个类，只需要把该文件复制到src/java/com/mmall/controller/ 下即可
// 然后访问 网址(natapp提供的外网穿透的网址）/mmallx_war/test/test.do?str=value
// 可以看到testValue:value

@Slf4j
@Controller
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private UserMapper userMapper;

    public static void main(String[] args) {
        Timestamp a = new Timestamp(System.currentTimeMillis());
        System.out.println(a);
        Date c = new Date();
        System.out.println(c);
    }

    @RequestMapping(value = "get_cache.do")
    @ResponseBody
    public String getCache(String key) throws ExecutionException {
        return TokenCache.getKey(key);
    }

    @RequestMapping(value = "test.do")
    @ResponseBody
    public String test(String str) {
        log.info("testinfo");
        log.warn("testwarn");
        log.error("testerror");
        return "testValue:" + str;
    }
}
