import java.time.Instant;

public class Product {
    private int id;
    private String name;
    private double price;
    private Instant creationDatetime;

    public Product(int id, String name, double price, Instant creationDatetime) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.creationDatetime = creationDatetime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public String toString() {
        return id + " - " + name + " - " + price + " - " + creationDatetime;
    }
}