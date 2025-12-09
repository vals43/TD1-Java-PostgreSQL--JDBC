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

import java.lang.reflect.Field;
import java.sql.*;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    void setUp() throws Exception {

        this.dataRetriever = new DataRetriever();
        Field dbConnectionField = DataRetriever.class.getDeclaredField("dbConnection");
        dbConnectionField.setAccessible(true);
        dbConnectionField.set(this.dataRetriever, mockDbConnection);


        when(mockDbConnection.getDBConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.getTimestamp(anyString())).thenReturn(new Timestamp(Instant.now().toEpochMilli()));
    }


    @Test
    void getAllCategories_RetourneListeDeCategories() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true) // Ligne 1
                .thenReturn(true) // Ligne 2
                .thenReturn(false); // Fin
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
    void getProductList_VerificationPagination() throws SQLException {
        int page = 3;
        int size = 10;

        when(mockResultSet.next()).thenReturn(false);

        dataRetriever.getProductList(page, size);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(queryCaptor.capture());

        verify(mockStatement).setInt(1, size); // LIMIT = size
        verify(mockStatement).setInt(2, (page - 1) * size); // OFFSET = 20
    }

    @Test
    void getProductList_GestionDeProduitMultiplesCategories() throws SQLException {
        // 1. Configurer le ResultSet pour simuler un produit (ID 101) avec DEUX catégories (1 et 2)
        when(mockResultSet.next())
                .thenReturn(true)  // Ligne 1: Produit 101, Cat 1
                .thenReturn(true)  // Ligne 2: Produit 101, Cat 2
                .thenReturn(false); // Fin

        // Data commune
        when(mockResultSet.getInt("id")).thenReturn(101);
        when(mockResultSet.getString("name")).thenReturn("TV Sony");
        when(mockResultSet.getDouble("price")).thenReturn(1200.00);

        // Data spécifique aux catégories
        when(mockResultSet.getInt("category_id")).thenReturn(1, 2);
        when(mockResultSet.getString("category_name")).thenReturn("Audio", "Électronique");

        // 2. Exécuter la méthode
        List<Product> products = dataRetriever.getProductList(1, 1);

        // 3. Vérifier les assertions
        assertEquals(1, products.size(), "Un seul produit devrait être retourné.");
        Product product = products.get(0);
        assertEquals(101, product.getId());
        assertEquals(2, product.getCategories().size(), "Le produit doit avoir les deux catégories.");
    }


    // --- Tests pour getProductsByCriteria (Question c) ---

    @Test
    void getProductsByCriteria_VerificationQueryProductAndCategory() throws SQLException {
        String name = "iPhone";
        String category = "mobile";
        when(mockResultSet.next()).thenReturn(false);

        // 1. Exécuter la méthode
        dataRetriever.getProductsByCriteria(name, category, null, null);

        // 2. Capturer la requête envoyée et vérifier les clauses WHERE
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(queryCaptor.capture());

        String capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.contains(" AND p.name ILIKE '%iPhone%'"));
        assertTrue(capturedQuery.contains(" AND c.name ILIKE '%mobile%'"));
        assertFalse(capturedQuery.contains("creation_datetime"), "Aucun filtre de temps ne devrait être présent.");
    }

    @Test
    void getProductsByCriteria_VerificationQueryTimeRange() throws SQLException {
        Instant min = Instant.parse("2024-02-01T00:00:00Z");
        Instant max = Instant.parse("2024-03-01T00:00:00Z");
        when(mockResultSet.next()).thenReturn(false);

        // 1. Exécuter la méthode
        dataRetriever.getProductsByCriteria(null, null, min, max);

        // 2. Capturer la requête envoyée et vérifier les clauses WHERE
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(queryCaptor.capture());

        String capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.contains(" AND p.creation_datetime >= '2024-02-01"));
        assertTrue(capturedQuery.contains(" AND p.creation_datetime <= '2024-03-01"));
        assertFalse(capturedQuery.contains("ILIKE"), "Aucun filtre par nom ne devrait être présent.");
    }


    @Test
    void getProductsByCriteria_PaginationEnMemoir() throws SQLException {
        // Simuler 5 produits filtrés au total par la première méthode
        when(mockResultSet.next())
                .thenReturn(true, true, true, true, true)
                .thenReturn(false);

        when(mockResultSet.getInt("id")).thenReturn(10, 20, 30, 40, 50);
        when(mockResultSet.getInt("category_id")).thenReturn(0);
        when(mockResultSet.getString("name")).thenReturn("P1", "P2", "P3", "P4", "P5");

        int page = 2;
        int size = 2;

        List<Product> products = dataRetriever.getProductsByCriteria(null, null, null, null, page, size);

        assertEquals(2, products.size(), "La page doit contenir exactement 'size' éléments.");
        assertEquals(30, products.get(0).getId(), "Le premier élément doit être P3 (ID 30).");
        assertEquals(40, products.get(1).getId(), "Le deuxième élément doit être P4 (ID 40).");

        verify(mockStatement, times(1)).executeQuery();
    }

    @Test
    void getProductsByCriteria_PaginationEnMemoire_DernierePageIncomplete() throws SQLException {
        // Simuler 5 produits filtrés au total
        when(mockResultSet.next())
                .thenReturn(true, true, true, true, true)
                .thenReturn(false);
        when(mockResultSet.getInt("id")).thenReturn(10, 20, 30, 40, 50);
        when(mockResultSet.getInt("category_id")).thenReturn(0);
        when(mockResultSet.getString("name")).thenReturn("P1", "P2", "P3", "P4", "P5");
        int page = 3;
        int size = 2;

        List<Product> products = dataRetriever.getProductsByCriteria(null, null, null, null, page, size);

        assertEquals(1, products.size(), "La dernière page doit contenir 1 élément.");
        assertEquals(50, products.get(0).getId());
    }
}