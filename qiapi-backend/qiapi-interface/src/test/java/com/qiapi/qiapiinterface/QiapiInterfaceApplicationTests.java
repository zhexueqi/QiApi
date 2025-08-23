package com.qiapi.qiapiinterface;

import com.qiapi.client.QiApiClient;
import com.qiapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
class QiapiInterfaceApplicationTests {

    @Autowired
    private QiApiClient qiApiClient;

    @Test
    void test() {
        User user = new User();
        user.setName("zhexueqi");
        String result = qiApiClient.getNameByRestful(user);
        System.out.println(result);
    }
}
