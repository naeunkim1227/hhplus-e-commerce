package io.hhplus.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootTest
public class DataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Connected URL: " + conn.getMetaData().getURL());
            System.out.println("Connected User: " + conn.getMetaData().getUserName());

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                System.out.println("Tables in database:");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
