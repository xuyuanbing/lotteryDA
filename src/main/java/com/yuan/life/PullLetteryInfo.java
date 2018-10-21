package com.yuan.life;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.WorkbookUtil;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PullLetteryInfo {

    public static void main(String[] args) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
        LocalDateTime localDateTime = LocalDateTime.parse("2018-08-01 00:00:00", formatter);
        LocalDateTime localDateTimeFirstDayOfMonth = localDateTime
                .with(TemporalAdjusters.firstDayOfMonth())
                .with(ChronoField.MILLI_OF_DAY, 0);
        LocalDateTime endLocalDateTime = localDateTimeFirstDayOfMonth.with(ChronoField.DAY_OF_MONTH, Longs.tryParse("10"));
        LocalDate tempLocalDate = localDateTimeFirstDayOfMonth.toLocalDate();
        System.out.println(tempLocalDate.isAfter(endLocalDateTime.toLocalDate()));
        while (tempLocalDate.isBefore(endLocalDateTime.toLocalDate())) {
            System.out.println(tempLocalDate.atStartOfDay().format(formatter));
            tempLocalDate = tempLocalDate.plusDays(1L);

        }

    }

    public void createExcel(String dateString, String endDay, String excelPathString, String requestURL) {

        try (OutputStream outputFile = new FileOutputStream(new File(excelPathString))) {
            Workbook wb = buildLotteryWorkbook(bulidHttpClient(requestURL, buildPostParams(dateString, endDay, "501")));
            wb.write(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Workbook buildLotteryWorkbook(List<JSONObject> lotteryJSONObjects) {
        Workbook wb = new HSSFWorkbook();
        //"CurrentPeriod": "180902-082"
        DateTimeFormatter sheetNameFormatToLocalDate = DateTimeFormatter.ofPattern("yyMMdd");
        DateTimeFormatter sheetNameFormatter = DateTimeFormatter.ofPattern("MM月dd日");

        for (int n = 0, lotterySize = lotteryJSONObjects.size(); n < lotterySize; n++) {
            //"CurrentPeriod": "180902-082"
            String sheetNameStr = lotteryJSONObjects.get(n).getJSONObject("Data").getString("CurrentPeriod").substring(0, 6);
            JSONArray lotteryResultsJSONArray = lotteryJSONObjects.get(n).getJSONObject("Data").getJSONArray("LotteryResults");
            String sheetName = LocalDate.parse(sheetNameStr, sheetNameFormatToLocalDate).format(sheetNameFormatter);
            Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
            Row row0 = sheet.createRow(0);
            row0.setHeightInPoints(22F);
            CellStyle firstRowCellStyle = wb.createCellStyle();
            firstRowCellStyle.setAlignment(HorizontalAlignment.CENTER);
            firstRowCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font firstRowFont = wb.createFont();
            firstRowFont.setFontHeightInPoints((short) 18);
            firstRowCellStyle.setFont(firstRowFont);
            CellUtil.createCell(row0, 0, null, firstRowCellStyle);
            CellUtil.createCell(row0, 1, "期号", firstRowCellStyle);
            CellUtil.createCell(row0, 2, "和值", firstRowCellStyle);
            CellUtil.createCell(row0, 3, "大", firstRowCellStyle);
            CellUtil.createCell(row0, 4, "小", firstRowCellStyle);
            CellUtil.createCell(row0, 5, null, firstRowCellStyle);
            CellUtil.createCell(row0, 6, "单", firstRowCellStyle);
            CellUtil.createCell(row0, 7, "双", firstRowCellStyle);
            CellUtil.createCell(row0, 8, null, firstRowCellStyle);
            CellUtil.createCell(row0, 9, "号码", firstRowCellStyle);

            firstRowFont.setFontHeightInPoints((short) 12);
            int[] lotteryValues = new int[3];
            for (int i = 0, lotteryResultSize = lotteryResultsJSONArray.size(); i < lotteryResultSize; i++) {
                JSONObject lotteryJSONObject = lotteryResultsJSONArray.getJSONObject(i);
                String periodNo = lotteryJSONObject.getString("PeriodNo");
                int sumValue = lotteryJSONObject.getJSONObject("Sum").getIntValue("Value");
                JSONArray lotteryNumbersJSONArray = lotteryJSONObject.getJSONArray("LotteryNumbers");
                for (int j = 0, length = lotteryNumbersJSONArray.size(); j < length; j++) {
                    lotteryValues[j] = lotteryNumbersJSONArray.getJSONObject(j).getIntValue("Number");
                }
                String lotteryNumbersString = Ints.join(",", lotteryValues);
                Row currentRow = sheet.createRow(i + 1);
                CellUtil.createCell(currentRow, 0, null, firstRowCellStyle);
                CellUtil.createCell(currentRow, 1, periodNo, firstRowCellStyle);
                Cell sumValueCell = currentRow.createCell(2);
                sumValueCell.setCellValue(sumValue);
                sumValueCell.setCellStyle(firstRowCellStyle);

                if (sumValue < 10) {
                    CellUtil.createCell(currentRow, 3, null, firstRowCellStyle);
                    CellUtil.createCell(currentRow, 4, "小", firstRowCellStyle);
                    CellUtil.createCell(currentRow, 5, null, firstRowCellStyle);


                } else {
                    CellUtil.createCell(currentRow, 3, "大", firstRowCellStyle);
                    CellUtil.createCell(currentRow, 4, null, firstRowCellStyle);
                    CellUtil.createCell(currentRow, 5, null, firstRowCellStyle);

                }

                if (sumValue % 2 == 0) {
                    CellUtil.createCell(currentRow, 6, null, firstRowCellStyle);
                    CellUtil.createCell(currentRow, 7, "双", firstRowCellStyle);
                    CellUtil.createCell(currentRow, 8, null, firstRowCellStyle);
                } else {
                    CellUtil.createCell(currentRow, 6, "单", firstRowCellStyle);
                    CellUtil.createCell(currentRow, 7, null, firstRowCellStyle);
                    CellUtil.createCell(currentRow, 8, null, firstRowCellStyle);
                }

                CellUtil.createCell(currentRow, 9, lotteryNumbersString, firstRowCellStyle);

            }

        }
        return wb;
    }

    /**
     * 根据 请求地址 去获取数据。
     *
     * @param httpEntities 请求参数列表
     * @param requestURL   地址
     */
    public List<JSONObject> bulidHttpClient(String requestURL, List<HttpEntity> httpEntities) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000).build();
        final List<JSONObject> allResponses = new ArrayList<>(httpEntities.size() + 20);

        try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            httpclient.start();

            HttpPost httpPost = new HttpPost(requestURL);
            final CountDownLatch latch = new CountDownLatch(httpEntities.size());

            for (final HttpEntity httpEntity : httpEntities) {
                httpPost.setEntity(httpEntity);
                httpclient.execute(httpPost, new FutureCallback<HttpResponse>() {

                    @Override
                    public void completed(final HttpResponse response) {
                        try {
                            String responseStr = EntityUtils.toString(response.getEntity(), IOUtils.UTF8);
                            allResponses.add(JSONObject.parseObject(responseStr));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            latch.countDown();

                        }
                    }

                    @Override
                    public void failed(final Exception ex) {
                        latch.countDown();
                        System.out.println(httpPost.getRequestLine() + "->" + ex);
                    }

                    @Override
                    public void cancelled() {
                        latch.countDown();
                        System.out.println(httpPost.getRequestLine() + " cancelled");
                    }

                });
            }
            latch.await(3, TimeUnit.SECONDS);
            System.out.println("Shutting down");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return allResponses;
    }

    /**
     * @param dateString yyyy-mm-dd 开始日期从该月的1日开始
     * @param endDay     dd  结束的当月日期
     * @return
     */
    private List<HttpEntity> buildPostParams(String dateString, String endDay, String lotteryType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
        LocalDateTime localDateTimeFirstDayOfMonth = localDateTime
                .with(TemporalAdjusters.firstDayOfMonth())
                .with(ChronoField.MILLI_OF_DAY, 0);
        LocalDateTime endLocalDateTime = localDateTimeFirstDayOfMonth.with(ChronoField.DAY_OF_MONTH, Longs.tryParse(endDay));

        int days = endLocalDateTime.getDayOfMonth();
        List<HttpEntity> httpEntities = new ArrayList<>(days);
        for (LocalDate tempLocalDate = localDateTimeFirstDayOfMonth.toLocalDate(); tempLocalDate.isBefore(endLocalDateTime.toLocalDate()); tempLocalDate = tempLocalDate.plusDays(1L)) {

            ContentType bodyParamContentType = ContentType.APPLICATION_FORM_URLENCODED.withCharset("UTF-8");

            EntityBuilder requestEntityBuilder = EntityBuilder.create()
                    .setParameters(new BasicNameValuePair("SiteId", lotteryType),
                            new BasicNameValuePair("Date", tempLocalDate.atStartOfDay().format(formatter)))
                    .setContentEncoding("UTF-8").setContentType(bodyParamContentType);
            httpEntities.add(requestEntityBuilder.build());
        }
        return httpEntities;
    }
}
