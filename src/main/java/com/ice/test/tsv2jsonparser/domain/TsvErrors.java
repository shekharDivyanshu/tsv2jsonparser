package com.ice.test.tsv2jsonparser.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TsvErrors {

    @SerializedName("line")
    private String lineNumber;

    @SerializedName("errors")
    private List<String> errors;

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
