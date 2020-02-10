package com.ice.test.tsv2jsonparser.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Order {

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("order_date")
    private String orderDate;

    @SerializedName("line_items")
    private List<LineItem> lineItems;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }
}
