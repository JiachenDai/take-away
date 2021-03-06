package com.restkeeper.order;

import com.restkeeper.utils.SequenceUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SequenceTest {

    @Test
    public void demo(){
        String sequence = SequenceUtils.getSequence("1345845122902769665");
        System.out.println(sequence);
    }
}
