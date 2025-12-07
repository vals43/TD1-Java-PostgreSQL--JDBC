package PROG3.service;

import PROG3.DB.DBConnection;
import PROG3.model.Category;
import PROG3.model.Product;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection dbConnection = new DBConnection();

    public List<Category> getAllCategories() throws SQLException {
        Connection connection = dbConnection.getDBConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM product_category");
        ResultSet rs = statement.executeQuery();

        List<Category> categories = new ArrayList<>();
        while (rs.next()) {
            categories.add(new Category(rs.getInt("id"), rs.getString("name")));
        }
        return categories;
    }

    public List<Product> getProductList(int page, int size) throws SQLException {
        Connection connection = dbConnection.getDBConnection();

        String query = """
            SELECT p.id, p.name, p.price, p.creation_datetime, c.id AS category_id, c.name AS category_name
            FROM product p
            LEFT JOIN product_category c ON p.id = c.product_id
            ORDER BY p.id
            LIMIT ? OFFSET ?
        """;

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, size);
        statement.setInt(2, (page - 1) * size);

        ResultSet rs = statement.executeQuery();

        List<Product> products = new ArrayList<>();
        while (rs.next()) {
            Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"));
            products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getTimestamp("creation_datetime").toInstant(),
                    category
            ));
        }
        return products;
    }

    public List<Product> getProductsByCriteria(String productName, String categoryName,
                                               Instant creationMin, Instant creationMax) throws SQLException {

        StringBuilder query = new StringBuilder("""
            SELECT p.id, p.name, p.price, p.creation_datetime, c.id AS category_id, c.name AS category_name
            FROM product p
            LEFT JOIN product_category c ON p.id=c.product_id
            WHERE 1=1
        """);

        if (productName != null) query.append(" AND p.name ILIKE '%").append(productName).append("%'");
        if (categoryName != null) query.append(" AND c.name ILIKE '%").append(categoryName).append("%'");
        if (creationMin != null) query.append(" AND p.creation_datetime >= '").append(Timestamp.from(creationMin)).append("'");
        if (creationMax != null) query.append(" AND p.creation_datetime <= '").append(Timestamp.from(creationMax)).append("'");

        Connection connection = dbConnection.getDBConnection();
        PreparedStatement statement = connection.prepareStatement(query.toString());
        ResultSet rs = statement.executeQuery();

        List<Product> products = new ArrayList<>();

        while (rs.next()) {
            Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"));
            products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getTimestamp("creation_datetime").toInstant(),
                    category
            ));
        }
        return products;
    }

    public List<Product> getProductsByCriteria(String productName, String categoryName,
                                               Instant creationMin, Instant creationMax, int page, int size) throws SQLException {

        List<Product> filteredProducts = getProductsByCriteria(productName, categoryName, creationMin, creationMax);
        int fromIndex = Math.max(0, (page - 1) * size);
        int toIndex = Math.min(filteredProducts.size(), fromIndex + size);

        return filteredProducts.subList(fromIndex, toIndex);
    }
}
