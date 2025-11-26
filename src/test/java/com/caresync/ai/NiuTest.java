package com.caresync.ai;

import com.caresync.ai.utils.PasswordEncoderUtil;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;

import java.io.IOException;

@SpringBootTest
public class NiuTest {
    @Test
    public void testPSWEncrypt() {
        String psw = "123456";
        String encryptPsw = PasswordEncoderUtil.encode(psw);
        System.out.println(encryptPsw);
    }

    @Test
    public void testNiu() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://ac.nowcoder.com/acm/contest/121952#rank/%22searchUserName%22%3A%22%E6%B2%B3%E6%B1%A0%E5%AD%A6%E9%99%A2%22");
            String html = httpClient.execute(httpGet, response -> EntityUtils.toString(response.getEntity()));
            System.out.println(html);
        }
    }

}