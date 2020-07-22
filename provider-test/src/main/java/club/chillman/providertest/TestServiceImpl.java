package club.chillman.providertest;


import club.chillman.serviceapi.Test;
import club.chillman.serviceapi.TestService;

/**
 * @author NIU
 * @createTime 2020/7/22 14:20
 */
public class TestServiceImpl implements TestService {
    @Override
    public String wow(Test test) {
        return test.getStr() + " wow";
    }
}
