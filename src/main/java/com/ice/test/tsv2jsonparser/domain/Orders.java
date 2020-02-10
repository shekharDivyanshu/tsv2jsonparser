package com.ice.test.tsv2jsonparser.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Orders {

    @SerializedName("orders")
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
