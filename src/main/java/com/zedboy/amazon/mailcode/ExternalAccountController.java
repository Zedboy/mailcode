package com.zedboy.amazon.mailcode;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import java.io.IOException;
import java.util.Map;

/**
 * @author 12037
 * @ClassName ExternalAccountController
 * @Date 2020/3/16 16:47
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/external")
public class ExternalAccountController {
    Logger logger = LoggerFactory.getLogger(ExternalAccountController.class);

    @PostMapping(value = "/getVerifyEmailByPOP3")
    public ResponseEntity<Object> getVerifyEmailByPOP3(@RequestBody Map<String, String> requestMap) {
        RetrieveEmailsUsingPOP3 oep = new RetrieveEmailsUsingPOP3();
        String emailCode = null;
        String email = requestMap.get("email");
        try {
            emailCode = oep.getAmazonVerify(requestMap.get("email"), requestMap.get("emailPassword"), "ssl");
            logger.info(String.format("get verify mail code: %s ==> %s", email, emailCode));
        } catch (AuthenticationFailedException ex) {
            return new ResponseEntity<>(401, HttpStatus.UNAUTHORIZED); // 401"密码错误或者请开通账户访问权限"
        } catch (NoSuchProviderException ex) {
            return new ResponseEntity<>(406, HttpStatus.NOT_ACCEPTABLE); // 406 "未开通IMAP/POP3"
        } catch (MessagingException e) {
            return new ResponseEntity<>(407, HttpStatus.PROXY_AUTHENTICATION_REQUIRED); // 407 "请检查网络,无法连接到邮箱服务器"
        } catch (IOException e) {
            return new ResponseEntity<>(400, HttpStatus.BAD_REQUEST); // 400 "获取验证码失败"
        }
        return new ResponseEntity<>(emailCode, HttpStatus.OK);
    }
}
