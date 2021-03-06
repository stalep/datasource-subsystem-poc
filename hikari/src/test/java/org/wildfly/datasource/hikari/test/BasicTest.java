package org.wildfly.datasource.hikari.test;

import org.junit.Test;
import org.wildfly.datasource.api.WildFlyDataSource;
import org.wildfly.datasource.api.WildFlyDataSourceFactory;
import org.wildfly.datasource.api.configuration.ConnectionFactoryConfigurationBuilder;
import org.wildfly.datasource.api.configuration.ConnectionPoolConfigurationBuilder;
import org.wildfly.datasource.api.configuration.DataSourceConfiguration;
import org.wildfly.datasource.api.configuration.DataSourceConfigurationBuilder;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author <a href="lbarreiro@redhat.com">Luis Barreiro</a>
 */
public class BasicTest {

    private static final String H2_JDBC_URL = "jdbc:h2:mem:test";
    private static final String H2_DRIVER_CLASS = "org.h2.Driver";

    @Test
    public void basicTest() throws SQLException {
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfigurationBuilder()
                .setDataSourceImplementation( DataSourceConfiguration.DataSourceImplementation.HIKARI )
                .setConnectionPoolConfiguration( new ConnectionPoolConfigurationBuilder()
                        .setConnectionFactoryConfiguration( new ConnectionFactoryConfigurationBuilder()
                                .setDriverClassName( H2_DRIVER_CLASS )
                                .setJdbcUrl( H2_JDBC_URL )
                                .build()
                        )
                        .setMaxSize( 10 )
                        .build()
                )
                .build();

        try( WildFlyDataSource dataSource = WildFlyDataSourceFactory.create( dataSourceConfiguration ) ) {
            for ( int i = 0; i < 50; i++ ) {
                Connection connection = dataSource.getConnection();
                System.out.println( "connection = " + connection );
                connection.close();
            }
        }
    }

}
