package PROG3;

import PROG3.service.DataRetriever;

import java.sql.SQLException;
import java.time.Instant;

public class Main {

    public static void main(String[] args) throws SQLException {
        DataRetriever retriever = new DataRetriever();

        // question a
        System.out.println(retriever.getAllCategories());

        //question b
        System.out.println(retriever.getProductList(1, 10));
        System.out.println(retriever.getProductList(1, 5));
        System.out.println(retriever.getProductList(1, 3));
        System.out.println(retriever.getProductList(2, 2));

        //question c
        System.out.println(retriever.getProductsByCriteria("Dell", null, null, null));
        System.out.println(retriever.getProductsByCriteria(null, "info", null, null));
        System.out.println(retriever.getProductsByCriteria("iPhone", "mobile", null, null));
        System.out.println(retriever.getProductsByCriteria(null, null, Instant.parse("2024-02-01T00:00:00Z"), Instant.parse("2024-03-01T00:00:00Z")));
        System.out.println(retriever.getProductsByCriteria("Samsung", "bureau", null, null));
        System.out.println(retriever.getProductsByCriteria("Sony", "informatique", null, null));
        System.out.println(retriever.getProductsByCriteria(null, "audio", Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-12-01T00:00:00Z")));
        System.out.println(retriever.getProductsByCriteria(null, null, null, null));

        //question d
        System.out.println(retriever.getProductsByCriteria(null, null, null, null, 1, 10));
        System.out.println(retriever.getProductsByCriteria("Dell", null, null, null, 1, 5));
        System.out.println(retriever.getProductsByCriteria(null, "informatique", null, null, 1, 10));
    }
}