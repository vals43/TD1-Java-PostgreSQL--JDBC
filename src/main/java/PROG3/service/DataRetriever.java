package PROG3.service;

import PROG3.DB.DBConnection;
import PROG3.model.Category;
import PROG3.model.Product;

import java.sql.*;
import java.time.Instant;
import java.util.*;

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

        Map<Integer, Product> productMap = new LinkedHashMap<>();

        while (rs.next()) {
            int productId = rs.getInt("id");

            // si product pas encore créé → le créer
            productMap.putIfAbsent(productId, new Product(
                    productId,
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getTimestamp("creation_datetime").toInstant(),
                    new ArrayList<>()
            ));

            // Ajouter catégorie si elle existe et pas déjà ajoutée
            int catId = rs.getInt("category_id");
            if (catId != 0) {
                Product prod = productMap.get(productId);

                boolean exists = prod.getCategories().stream()
                        .anyMatch(c -> c.getId() == catId);

                if (!exists) {
                    prod.getCategories().add(new Category(catId, rs.getString("category_name")));
                }
            }
        }

        return new ArrayList<>(productMap.values());
    }

    public List<Product> getProductsByCriteria(String productName, String categoryName,
                                               Instant creationMin, Instant creationMax) throws SQLException {

        StringBuilder query = new StringBuilder("""
            SELECT p.id, p.name, p.price, p.creation_datetime, c.id AS category_id, c.name AS category_name
            FROM product p
            LEFT JOIN product_category c ON p.id=c.product_id
            WHERE 1=1
        """);

        if (productName != null) query.append(" AND p.name ILIKE '%" + productName + "%'");
        if (categoryName != null) query.append(" AND c.name ILIKE '%"+categoryName+"%'");
        if (creationMin != null) query.append(" AND p.creation_datetime >= '"+Timestamp.from(creationMin)+"'");
        if (creationMax != null) query.append(" AND p.creation_datetime <= '"+Timestamp.from(creationMax)+"'");

        Connection connection = dbConnection.getDBConnection();
        PreparedStatement statement = connection.prepareStatement(query.toString());
        ResultSet rs = statement.executeQuery();

        Map<Integer, Product> productMap = new LinkedHashMap<>();

        while (rs.next()) {
            int productId = rs.getInt("id");

            productMap.putIfAbsent(productId, new Product(
                    productId,
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getTimestamp("creation_datetime").toInstant(),
                    new ArrayList<>()
            ));

            int catId = rs.getInt("category_id");
            if (catId != 0) {
                Product prod = productMap.get(productId);

                boolean exists = prod.getCategories().stream()
                        .anyMatch(c -> c.getId() == catId);

                if (!exists) {
                    prod.getCategories().add(new Category(catId, rs.getString("category_name")));
                }
            }
        }

        return new ArrayList<>(productMap.values());
    }

    public List<Product> getProductsByCriteria(String productName, String categoryName,
                                               Instant creationMin, Instant creationMax, int page, int size) throws SQLException {

        List<Product> filteredProducts = getProductsByCriteria(productName, categoryName, creationMin, creationMax);
        int fromIndex = Math.max(0, (page - 1) * size);
        int toIndex = Math.min(filteredProducts.size(), fromIndex + size);

        return filteredProducts.subList(fromIndex, toIndex);
    }
}
