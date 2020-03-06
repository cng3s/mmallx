package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {

    // 这里域名要写对了.例如: img.imooc11.com 和 www.imooc11.com 不是一个域名
    // 则它们之间的cookie是不能互相使用的。
    // 但是我们常看到的一个网站可能域名前缀互不相同，但cookie可以相互访问是为什么呢？
    // 是因为它们之间cookie验证信息是使用一个共享的redis来存储的。
    // 不同域名的tomcat上的程序都访问这个redis服务器，是用过软件方式实现共享。
    private final static String COOKIE_DOMAIN = ".imooc11.com";

    private final static String COOKIE_NAME = "mmall_login_token";

    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            for (Cookie ck : cks) {
                log.info("Read cookieName: {}, cookieValue: {}", ck.getName(), ck.getValue());
                if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {
                    log.info("Return cookieName: {}, cookieValue: {}", ck.getName(), ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    public static void writeLoginToken(HttpServletResponse response, String token) {
        Cookie ck = new Cookie(COOKIE_NAME, token);
        ck.setDomain(COOKIE_DOMAIN);
        ck.setPath("/"); // 代表设置在根目录
        ck.setHttpOnly(true);

        // 单位是秒
        // 如果这个maxage不设置的话，cookie就不会写入硬盘(向浏览器中写入cookie)，而是写在内存。只在当前页面有效。
        ck.setMaxAge(60*60*24*365); // 如果是-1，则代表永久
        log.info("Write cookieName: {}, cookieValue: {}", ck.getName(), ck.getValue());
        response.addCookie(ck);
    }

    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            for (Cookie ck : cks) {
                if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    ck.setMaxAge(0); // 设置为0， 代表删除此cookie
                    log.info("Del cookieName: {}, cookieValue: {}", ck.getName(), ck.getValue());
                    response.addCookie(ck);
                    return;
                }
            }
        }
    }

}
