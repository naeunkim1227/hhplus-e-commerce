package io.hhplus.ecommerce.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        // 데이터베이스 연결 풀 설정
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(System.getenv("DB_URL") != null ?
                System.getenv("DB_URL") : "jdbc:mysql://localhost:3306/ecommerce");
        dataSource.setUsername(System.getenv("DB_USER") != null ?
                System.getenv("DB_USER") : "ecommerce_user");
        dataSource.setPassword(System.getenv("DB_PASSWORD") != null ?
                System.getenv("DB_PASSWORD") : "qwer1234");
        return dataSource;
    }
}
