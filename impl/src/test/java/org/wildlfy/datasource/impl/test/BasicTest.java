package org.wildlfy.datasource.impl.test;

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.datasource.api.WildFlyDataSource;
import org.wildfly.datasource.api.WildFlyDataSourceFactory;
import org.wildfly.datasource.api.configuration.ConnectionFactoryConfigurationBuilder;
import org.wildfly.datasource.api.configuration.ConnectionPoolConfiguration;
import org.wildfly.datasource.api.configuration.ConnectionPoolConfigurationBuilder;
import org.wildfly.datasource.api.configuration.DataSourceConfiguration;
import org.wildfly.datasource.api.configuration.DataSourceConfigurationBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="lbarreiro@redhat.com">Luis Barreiro</a>
 */
public class BasicTest {

    private static final String H2_JDBC_URL = "jdbc:h2:mem:test";
    private static final String H2_DRIVER_CLASS = "org.h2.Driver";

    @Test
    public void basicTest() throws SQLException {
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfigurationBuilder()
                .setDataSourceImplementation( DataSourceConfiguration.DataSourceImplementation.WILDFLY )

                .setConnectionPoolConfiguration( new ConnectionPoolConfigurationBuilder()
                        .setMinSize( 5 )
                        .setMaxSize( 10 )
                        .setPreFillMode( ConnectionPoolConfiguration.PreFillMode.MIN )
                        .setConnectionFactoryConfiguration( new ConnectionFactoryConfigurationBuilder()
                                .setDriverClassName( H2_DRIVER_CLASS )
                                .setJdbcUrl( H2_JDBC_URL )
                                .build()
                        )
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

    @Test
    public void basicConcurrentTest() throws SQLException {
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfigurationBuilder()
                .setDataSourceImplementation( DataSourceConfiguration.DataSourceImplementation.WILDFLY )
                .setMetricsEnabled( true )
                .setConnectionPoolConfiguration( new ConnectionPoolConfigurationBuilder()
                        .setMinSize( 5 )
                        .setMaxSize( 10 )
                        .setPreFillMode( ConnectionPoolConfiguration.PreFillMode.MIN )
                        .setConnectionFactoryConfiguration( new ConnectionFactoryConfigurationBuilder()
                                .setDriverClassName( H2_DRIVER_CLASS )
                                .setJdbcUrl( H2_JDBC_URL )
                                .build()
                        )
                        .build()
                )
                .build();

        int THREAD_POOL_SIZE = 15;
        int CALLS = 500;
        int SLEEP_TIME = 100;

        ExecutorService executor = Executors.newFixedThreadPool( THREAD_POOL_SIZE );

        try ( WildFlyDataSource dataSource = WildFlyDataSourceFactory.create( dataSourceConfiguration ) ) {
            CountDownLatch latch = new CountDownLatch( CALLS );

            for ( int i = 0; i < CALLS; i++ ) {
                executor.submit( () -> {
                    try {
                        Connection connection = dataSource.getConnection();
                        System.out.println( "" + Thread.currentThread().getName() + " connection = " + connection );
                        Thread.sleep( SLEEP_TIME );
                        connection.close();
                    } catch ( SQLException e ) {
                        Assert.fail( "Unexpected SQLException " + e.getMessage() );
                    } catch ( InterruptedException e ) {
                        Assert.fail( "Interrupted " + e.getMessage() );
                    } finally {
                        latch.countDown();
                    }
                } );
            }
            try {
                latch.await();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }

            System.out.println( dataSource.getMetrics() );
        }

    }

}
