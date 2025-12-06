package PROG3.model;

import java.time.Instant;

public class Product {
    private int id;
    private String name;
    private double price;
    private Instant creationDatetime;
    private Category category;

    public Product(int id, String name, double price, Instant creationDatetime, Category category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.creationDatetime = creationDatetime;
        this.category = category;
    }

    public String getCategoryName() {
        return category.getName();
    }

}