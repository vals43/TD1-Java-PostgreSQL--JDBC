package PROG3.service;

import PROG3.DB.DBConnection;
import PROG3.model.Category;
import PROG3.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DataRetrieverTest {

    private DataRetriever dataRetriever;

    @Mock
    private DBConnection mockDbConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setup() throws Exception {

        this.dataRetriever = new DataRetriever();
        Field dbConnectionField = DataRetriever.class.getDeclaredField("dbConnection");
        dbConnectionField.setAccessible(true);
        dbConnectionField.set(this.dataRetriever, mockDbConnection);

        when(mockResultSet.getTimestamp(anyString())).thenReturn(new Timestamp(Instant.now().toEpochMilli()));

        when(mockDbConnection.getDBConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }


    @Test
    void getAllCategories_ShouldReturnListOfCategories() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Informatique", "Mobile");


        List<Category> categories = dataRetriever.getAllCategories();

        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertEquals(1, categories.get(0).getId());
        assertEquals("Mobile", categories.get(1).getName());

        verify(mockStatement).executeQuery();
    }


    @Test
    void getProductList_ShouldApplyPaginationParameters() throws SQLException {
        int page = 3;
        int size = 10;

        when(mockResultSet.next()).thenReturn(false);

        dataRetriever.getProductList(page, size);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(queryCaptor.capture());

        verify(mockStatement).setInt(1, size);
        verify(mockStatement).setInt(2, (page - 1) * size);
    }

    @Test
    void getProductList_ShouldGroupMultiCategoryProduct() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getInt("id")).thenReturn(101);
        when(mockResultSet.getString("name")).thenReturn("TV Sony");
        when(mockResultSet.getDouble("price")).thenReturn(1200.00);

        when(mockResultSet.getInt("category_id")).thenReturn(1, 2);
        when(mockResultSet.getString("category_name")).thenReturn("Audio", "Électronique");

        List<Product> products = dataRetriever.getProductList(1, 1);

        assertEquals(1, products.size());
        Product product = products.get(0);
        assertEquals(101, product.getId());
        assertEquals(2, product.getCategories().size());
    }


    @Test
    void getProductsByCriteria_ShouldFilterByNameAndCategory() throws SQLException {
        String name = "iPhone";
        String category = "mobile";
        when(mockResultSet.next()).thenReturn(false);

        dataRetriever.getProductsByCriteria(name, category, null, null);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(queryCaptor.capture());

        String capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.contains(" AND p.name ILIKE '%iPhone%'"));
        assertTrue(capturedQuery.contains(" AND c.name ILIKE '%mobile%'"));
        // assertFalse(capturedQuery.contains("creation_datetime"));
    }

    @Test
    void getProductsByCriteria_ShouldFilterByTimeRange() throws SQLException {
        Instant min = Instant.parse("2024-02-01T00:00:00Z");
        Instant max = Instant.parse("2024-03-01T00:00:00Z");
        when(mockResultSet.next()).thenReturn(false);

        dataRetriever.getProductsByCriteria(null, null, min, max);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(queryCaptor.capture());

        String capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.contains(" AND p.creation_datetime >= '2024-02-01"));
        assertTrue(capturedQuery.contains(" AND p.creation_datetime <= '2024-03-01"));
        assertFalse(capturedQuery.contains("ILIKE"));
    }


    @Test
    void getProductsByCriteria_ShouldApplyInMemPagination() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true, true, true, true, true)
                .thenReturn(false);

        when(mockResultSet.getInt("id")).thenReturn(10, 20, 30, 40, 50);
        when(mockResultSet.getInt("category_id")).thenReturn(0, 0, 0, 0, 0);
        when(mockResultSet.getString("name")).thenReturn("P1", "P2", "P3", "P4", "P5");

        int page = 2;
        int size = 2;

        List<Product> products = dataRetriever.getProductsByCriteria(null, null, null, null, page, size);

        assertEquals(2, products.size());
        assertEquals(30, products.get(0).getId());
        assertEquals(40, products.get(1).getId());

        verify(mockStatement, times(1)).executeQuery();
    }

    @Test
    void getProductsByCriteria_InMemPagination_LastPageIncomplete() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true, true, true, true, true)
                .thenReturn(false);
        when(mockResultSet.getInt("id")).thenReturn(10, 20, 30, 40, 50);
        when(mockResultSet.getInt("category_id")).thenReturn(0, 0, 0, 0, 0);
        when(mockResultSet.getString("name")).thenReturn("P1", "P2", "P3", "P4", "P5");
        int page = 3;
        int size = 2;

        List<Product> products = dataRetriever.getProductsByCriteria(null, null, null, null, page, size);

        assertEquals(1, products.size());
        assertEquals(50, products.get(0).getId());
    }
}