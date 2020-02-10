package com.ice.test.tsv2jsonparser.domain;

import com.google.gson.annotations.SerializedName;

public class LineItem {

    @SerializedName("product_url")
    private String productUrl;

    @SerializedName("revenue")
    private double revenue;

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
