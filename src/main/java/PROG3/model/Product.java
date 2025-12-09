package PROG3.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private int id;
    private String name;
    private double price;
    private Instant creationDatetime;
    private List<Category> categories;

    public Product(int id, String name, double price, Instant creationDatetime) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.creationDatetime = creationDatetime;
        this.categories = new ArrayList<>();
    }

    public Product(int id, String name, double price, Instant creationDatetime, List<Category> categories) {
        this(id, name, price, creationDatetime);
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    public void addCategory(Category category) {
        if (category != null && !this.categories.contains(category)) {
            this.categories.add(category);
        }
    }

    public List<String> getCategoryNames() {
        return categories.stream().map(Category::getName).toList();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public Instant getCreationDatetime() { return creationDatetime; }
    public List<Category> getCategories() { return categories; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", creationDatetime=" + creationDatetime +
                ", categories=" + categories +
                '}';
    }
}
