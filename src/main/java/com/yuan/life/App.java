package com.yuan.life;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.primitives.Ints;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.WorkbookUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) throws Exception
    {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final BasicHeader headerContentType = new BasicHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        HttpPost httpPost = new HttpPost("https://pxapi.icaile.com/api/LotteryResults/GetLotteryResultsBySiteIdDate");
        httpPost.addHeader(headerContentType);

//        CloseableHttpResponse response1 = httpclient.execute(httpGet);
// The underlying HTTP connection is still held by the response object
// to allow the response content to be streamed directly from the network socket.
// In order to ensure correct deallocation of system resources pull
// the user MUST call CloseableHttpResponse#close() from a finally clause.
// Please note that if response content is not fully consumed the underlying
// connection cannot be safely re-used and will be shut down and discarded
// by the connection manager.

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(40000).build();
        httpPost.setConfig(requestConfig);


        List<NameValuePair> params = new ArrayList<NameValuePair>() {
            {
                add(new BasicNameValuePair("SiteId", "501"));
                add(new BasicNameValuePair("Date", "2018-09-02 00:00:00"));
            }
        };

        ContentType bodyParamContentType = ContentType.APPLICATION_FORM_URLENCODED.withCharset("UTF-8");

        EntityBuilder requstEntityBuilder = EntityBuilder.create().setParameters(params).setContentEncoding("UTF-8").setContentType(bodyParamContentType);


        httpPost.setEntity(requstEntityBuilder.build());
        httpclient.execute(httpPost);

//        " + Instant.now().toString() + "
        try (CloseableHttpResponse response1 = httpclient.execute(httpPost); OutputStream fileOut = new FileOutputStream("D:\\temp\\10.xls")) {

            HttpEntity entity1 = response1.getEntity();
            JSONObject responseContentJSONObject = JSONObject.parseObject(EntityUtils.toString(entity1, "UTF-8"));
            JSONArray lotteryResultsJSONArray = responseContentJSONObject.getJSONObject("Data").getJSONArray("LotteryResults");
            // xls 是低版本的 excel 文件。对应POI 的 HSSFWorkbook
            // 下面的 wb 实体是一个 HSSFWorkbook
//            Workbook wb = createByFilePath("D:\\temp\\江苏快三.xls");
            Workbook wb = new HSSFWorkbook();
            String safeSheetName = WorkbookUtil.createSafeSheetName("9月份");
            Sheet sheet = wb.createSheet(safeSheetName);
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
            // do something useful with the response body
            // and ensure it is fully consumed
            wb.write(fileOut);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
        }

//        HttpPost httpPost = new HttpPost("http://targethost/login");
//        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//        nvps.add(new BasicNameValuePair("SiteId", "501"));
//        nvps.add(new BasicNameValuePair("Date", "2018-09-02 00:00:00"));
//        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//
//
//        try (CloseableHttpResponse response2 = httpclient.execute(httpPost)) {
//            System.out.println(response2.getStatusLine());
//            HttpEntity entity2 = response2.getEntity();
//            // do something useful with the response body
//            // and ensure it is fully consumed
//            EntityUtils.consume(entity2);
//        } catch (Exception e) {
//            System.out.print(e.getMessage());
//        }


    }

    /**
     * Creates a cell and aligns it a certain way.
     *
     * @param wb     the workbook
     * @param row    the row to create the cell in
     * @param column the column number to create the cell in
     * @param halign the horizontal alignment for the cell.
     * @param valign the vertical alignment for the cell.
     */
    private static void createCell(Workbook wb, Row row, int column, HorizontalAlignment halign, VerticalAlignment valign) {
        Cell cell = row.createCell(column);
        cell.setCellValue("Align It");
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(halign);
        cellStyle.setVerticalAlignment(valign);
        cell.setCellStyle(cellStyle);

    }


    /**
     * 橙色 填充
     *
     * @param wb Workbook
     * @return CellStyle
     */
    private static CellStyle rowOrangeStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static Workbook createByFilePath(String fullFilePath) throws IOException {
        // 这里可以做些 文件路径的检查什么的
        return WorkbookFactory.create(new File(fullFilePath));
    }
}
