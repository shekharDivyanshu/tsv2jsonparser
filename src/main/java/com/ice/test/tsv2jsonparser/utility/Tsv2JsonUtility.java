package com.ice.test.tsv2jsonparser.utility;

import com.ice.test.tsv2jsonparser.domain.LineItem;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.ice.test.tsv2jsonparser.constants.constant .*;

/**
 * Utility class containing all business rules to convert tsv to Json.
 *
 */
@Component
public class Tsv2JsonUtility {

    private static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    private static final String DATE_FORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public Tsv2JsonUtility(){

    }

    /**
     *
     * @param headerPosMap
     * @param lineToken
     * @return
     */
    private String getProductUrlFromLineToken(Map<String, Integer> headerPosMap, String[] lineToken) {
        try{
            String baseUrl = "https://www.foo.com";
            String category = lineToken[headerPosMap.get(TSV_HEADER_CATEGORY)];
            String subCategory = lineToken[headerPosMap.get(TSV_HEADER_SUB_CATEGORY)];
            String productId = lineToken[headerPosMap.get(TSV_HEADER_PRODUCT_ID)];
            return String.join("/", Arrays.asList(
                    baseUrl,
                    URLEncoder.encode(category, StandardCharsets.UTF_8.toString()),
                    URLEncoder.encode(subCategory, StandardCharsets.UTF_8.toString()),
                    URLEncoder.encode(productId, StandardCharsets.UTF_8.toString())
            ));
        }catch(Exception exp){
            return "";
        }catch(Throwable th){
            return "";
        }
    }

    /**
     *
     * @param headerPosMap
     * @param lineToken
     * @return
     */
    private double getPriceFromSales(Map<String, Integer> headerPosMap, String[] lineToken){
        try{
          String salesPrice = lineToken[headerPosMap.get(TSV_HEADER_SALES)];
          return Double.valueOf(salesPrice);
        }catch(Exception exp){
            return Double.valueOf("0.0");
        }
    }

    /**
     *
     * @param headerPosMap
     * @param lineToken
     * @return
     */
    public LineItem getLineItem(Map<String, Integer> headerPosMap, String[] lineToken){
        LineItem lineItem = new LineItem();
        String productUrl = getProductUrlFromLineToken(headerPosMap, lineToken);
        double salePrice = getPriceFromSales(headerPosMap, lineToken);
        lineItem.setRevenue(salePrice);
        lineItem.setProductUrl(productUrl);
        return lineItem;
    }

    /**
     *
     * @param headerPosMap
     * @param lineToken
     * @param allowedDateString
     * @return
     */
    public String getISOOrderDateIfAfter(Map<String, Integer> headerPosMap, String[] lineToken, String allowedDateString){
        String orderDateStr = lineToken[headerPosMap.get(TSV_HEADER_ORDER_DATE)];
        String formattedOrderDtStr = getFormattedDateString(orderDateStr);
        if(StringUtils.isEmpty(formattedOrderDtStr)) return null;
        LocalDate orderDate = LocalDate.parse(formattedOrderDtStr, DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD));
        LocalDate allowedDate = LocalDate.parse(allowedDateString, DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD));
        if(orderDate.isAfter(allowedDate)){
            return getOrderDateToIsoDate(orderDateStr);
        }
        return null;
    }

    /**
     *
     * @param dateStr
     * @return
     */
    private String getFormattedDateString(String dateStr){
        String formattedDateString = Strings.EMPTY;

        if( StringUtils.isEmpty(dateStr))return formattedDateString;

        String[] dateStrArray = dateStr.split("/");
        String month = (dateStrArray[0].length() == 2)?dateStrArray[0]:'0'+dateStrArray[0];
        String day = (dateStrArray[1].length() == 2)?dateStrArray[1]:'0'+dateStrArray[1];
        String year = String.valueOf(2000+Integer.parseInt(dateStrArray[2]));

        formattedDateString = year+"-"+month+"-"+day;

        return formattedDateString;
    }

    /**
     *
     * @param orderDateStr
     * @return
     */
    private String getOrderDateToIsoDate(String orderDateStr){
        String orderDateToISODate = Strings.EMPTY;

        String formatterDateStr = getFormattedDateString(orderDateStr);
        SimpleDateFormat sdf;
        try{
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
            Date orderDate = formatter.parse(formatterDateStr);

            sdf = new SimpleDateFormat(DATE_FORMAT_ISO_8601);
            sdf.setTimeZone(TimeZone.getTimeZone("EDT"));
            orderDateToISODate = sdf.format(orderDate);
        }catch(Exception exp){
            System.out.println(exp);
        }
        return orderDateToISODate;
    }

    /**
     *
     * @param valueLine
     * @param headerPosMap
     * @return
     */
    public List<String> getLineErrorList(String valueLine, Map<String, Integer> headerPosMap){
        List<String> errorList = new ArrayList<>();
        String[] lineTokens = valueLine.split("\t");
        headerPosMap.entrySet().stream().forEach(p->{
            if(lineTokens.length<=p.getValue()){
                errorList.add("No value found for [ "+p.getKey()+" ] !!");
            }
        });
        return errorList;
    }

    /**
     *
     * @param valueLine
     * @param headerPosMap
     * @return
     */
    public boolean isLineValid(String valueLine, Map<String, Integer> headerPosMap){
        if(StringUtils.isEmpty(valueLine) || null == headerPosMap)return false;
        String[] lineTokens = valueLine.split("\t");
        for(int position : headerPosMap.values()){
            if(lineTokens.length <= position) return false;
        }
        return true;
    }

    /**
     *
     * @param headerLine
     * @return
     */
    public boolean isHeaderValid(String headerLine){
        if(StringUtils.isEmpty(headerLine))return false;
        String[] headers = headerLine.split("\t");
        List<String> headerList = Arrays.asList(headers);
        boolean isValid = headerList.contains(TSV_HEADER_ORDER_DATE)
                  && headerList.contains(TSV_HEADER_CATEGORY)
                && headerList.contains(TSV_HEADER_SUB_CATEGORY)
                && headerList.contains(TSV_HEADER_PRODUCT_ID)
                && headerList.contains(TSV_HEADER_SALES)
                && headerList.contains(TSV_HEADER_ORDER_ID);
        return isValid;
    }

}
