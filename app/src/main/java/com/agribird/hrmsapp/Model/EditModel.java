package com.agribird.hrmsapp.Model;

public class EditModel {

    private String label;
    private String value;
    private String key; // Identifier साठी (उदा. name, email, phone)

    public EditModel(String label, String value, String key) {
        this.label = label;
        this.value = value;
        this.key = key;
    }

    public String getLabel() { return label; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getKey() { return key; }
}
