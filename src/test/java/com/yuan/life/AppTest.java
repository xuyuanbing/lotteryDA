package com.yuan.life;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
    public static void main(String[] args) {
        PullLetteryInfo pullLetteryInfo = new PullLetteryInfo();
        pullLetteryInfo.createExcel("2018-08-01 00:00:00", "10", "D:\\temp\\8月.xls", "https://pxapi.icaile.com/api/LotteryResults/GetLotteryResultsBySiteIdDate");
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }


}
