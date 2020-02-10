package com.ice.test.tsv2jsonparser.api;

import com.google.gson.Gson;
import com.ice.test.tsv2jsonparser.domain.LineItem;
import com.ice.test.tsv2jsonparser.domain.Order;
import com.ice.test.tsv2jsonparser.domain.Orders;
import com.ice.test.tsv2jsonparser.domain.TsvErrors;
import com.ice.test.tsv2jsonparser.utility.Security.HashGeneratorUtils;
import com.ice.test.tsv2jsonparser.utility.Tsv2JsonUtility;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ice.test.tsv2jsonparser.constants.constant .*;

@CrossOrigin
@RestController
public class TsvParserApi {

    private ConcurrentMap<String, Map<String, Object>>  cache = new ConcurrentHashMap<>();

    private static final String ERROR_KEY = "errors";
    private static final String JSON_KEY = "json";
    private static final String MSG_ID = "msgId";
    private static final String TSV_CONTENT = "tsv_content";
    private static final String ALLOWED_DATE_STRING = "2016-07-31";

    @Autowired
    private Tsv2JsonUtility tsv2JsonUtility;

    /**
     *
     * @param msgId
     * @return
     */
    @RequestMapping(value = "/json/{msgId}" , method = RequestMethod.GET, produces = "application/json")
    public Object fetchTsv2JsonString(@PathVariable String msgId){
        Map<String, Object> response = new HashMap<>();
        Gson gson = new Gson();

        if(StringUtils.isEmpty(msgId)){
            response.put(ERROR_KEY, Arrays.asList("Invalid msg Id in url !!!"));
            return gson.toJson(response);
        }

        if(this.cache.containsKey(msgId) ){
            return gson.toJson(this.cache.get(msgId));
        }else{
            response.put(ERROR_KEY, Arrays.asList("No Json Content found for given msg Id [ "+msgId+" ] !!!"));
        }
        return gson.toJson(response);
    }

    /**
     *
     * @param tsvContent
     * @return
     */
    @RequestMapping(value = "/json/get" , method = RequestMethod.POST, produces = "application/json")
    public Object getTsv2JsonString(@RequestBody String tsvContent){
        Map<String, Object> response = new HashMap<>();
        List<TsvErrors> errorsList = new LinkedList<>();
        Gson gson = new Gson();

        if (StringUtils.isEmpty(tsvContent)){
            response.put(ERROR_KEY, "Invalid TSV content !!!! ");
            return gson.toJson(response);
        }
        response.put(TSV_CONTENT, tsvContent);
        // parse tsv content;
        String[] lines = tsvContent.split("\n");
        String headerStr = lines[0];
        String[] headerArray = headerStr.split("\t");
        boolean isHeaderValid = this.tsv2JsonUtility.isHeaderValid(headerStr);
        if(!isHeaderValid){
            TsvErrors tsvErrors = new TsvErrors();
            tsvErrors.setLineNumber(String.valueOf(0));
            tsvErrors.setErrors( Arrays.asList("Invalid Header found in TSV content !!!"));
            errorsList.add(tsvErrors);
            response.put(ERROR_KEY, errorsList);
            return gson.toJson(response);
        }

        Map<String, Integer> headerPosMap = getHeaderPosMap(headerArray);
        Map<String, Orders> customerOrderMap = new HashMap<>();
        for(int i = 1; i < lines.length; i++){
            //validate line for possible error
            boolean isLineValid = this.tsv2JsonUtility.isLineValid(lines[i],headerPosMap);
            if(!isLineValid){
                // add in error list
                // get line error
                List<String> lineErrorList = this.tsv2JsonUtility.getLineErrorList(lines[i], headerPosMap);
                TsvErrors tsvErrors = new TsvErrors();
                tsvErrors.setLineNumber(String.valueOf(i+1));
                tsvErrors.setErrors(lineErrorList);
                errorsList.add(tsvErrors);
                continue;
            }
            String[] lineTokens = lines[i].split("\t");
            String orderDate = this.tsv2JsonUtility.getISOOrderDateIfAfter(headerPosMap, lineTokens, ALLOWED_DATE_STRING);
            if(StringUtils.isEmpty(orderDate)) continue;
            populateCustomerOrderMap(headerPosMap, customerOrderMap, lineTokens, orderDate);
        }

        response.put(JSON_KEY, gson.toJsonTree(customerOrderMap));


        String msgId = Strings.EMPTY;
        try{
            String source = HashGeneratorUtils.generateSHA256(tsvContent);
            byte[] bytes = source.getBytes("UTF-8");
            UUID uuid = UUID.nameUUIDFromBytes(bytes);
            msgId = uuid.toString();
        }catch (Exception exp){

        }

        if(errorsList.size()>0){
            response.put(ERROR_KEY, errorsList);
        }
        if(null != msgId){
            response.put(MSG_ID, msgId);
            this.cache.put(msgId, response);
        }

        return gson.toJson(response);
    }

    /**
     *
     * @param headerPosMap
     * @param customerOrderMap
     * @param lineTokens
     * @return
     */
    private void populateCustomerOrderMap(Map<String, Integer> headerPosMap, Map<String, Orders> customerOrderMap, String[] lineTokens, String orderDate){

        String customerName = lineTokens[headerPosMap.get("Customer Name")];
        String orderId = lineTokens[headerPosMap.get(TSV_HEADER_ORDER_ID)];
        LineItem lineItem = this.tsv2JsonUtility.getLineItem(headerPosMap, lineTokens);
        if(null == customerOrderMap.get(customerName)){
            Orders orders = new Orders();
            List<Order> orderList = new ArrayList<>();
            Order order = new Order();
            order.setOrderId(orderId);
            order.setOrderDate(orderDate);

            List<LineItem> lineItemList = new ArrayList<>();
            lineItemList.add(lineItem);
            order.setLineItems(lineItemList);

            orderList.add(order);
            orders.setOrders(orderList);
            customerOrderMap.put(customerName, orders);
        }else{
            Orders orders = customerOrderMap.get(customerName);
            Order order = null;
            for(Order existingOrder : orders.getOrders()){
                if(existingOrder.getOrderId().equals(orderId) && existingOrder.getOrderDate().equals(orderDate)){
                    order = existingOrder;
                }
            }

            if(null != order){
                order.getLineItems().add(lineItem);
            } else {
                Order newOrder = new Order();
                newOrder.setOrderId(orderId);
                newOrder.setOrderDate(orderDate);

                List<LineItem> newLineItemList = new ArrayList<>();
                newLineItemList.add(lineItem);
                newOrder.setLineItems(newLineItemList);
                orders.getOrders().add(newOrder);
            }
        }
    }

    /**
     *
     * @param headerArray
     * @return
     */
    private Map<String, Integer> getHeaderPosMap(String[] headerArray){
        Map<String, Integer> headerPosMap = new HashMap<>();
        for(int i = 0; i < headerArray.length; i++){
            headerPosMap.put(headerArray[i], i);
        }
        return headerPosMap;
    }
}
