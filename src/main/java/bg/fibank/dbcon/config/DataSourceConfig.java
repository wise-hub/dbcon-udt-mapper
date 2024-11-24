package bg.fibank.dbcon.config;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceConfig {
    // Database connection properties
    private String url;
    private String username;
    private String password;

    // UCP connection pool properties
    private int initialPoolSize = 5;
    private int minPoolSize = 5;
    private int maxPoolSize = 20;
    private int connectionWaitTimeout = 30;
    private int timeoutCheckInterval = 5;
    private int inactiveConnectionTimeout = 120;
    private int abandonedConnectionTimeout = 180;
    private boolean validateConnectionOnBorrow = true;
    private int maxConnectionLifetime = 1800;
    private int maxConnectionReuseCount = 5000;

    /**
     * Registers the DataSource bean.
     *
     * @return The configured DataSource.
     * @throws Exception If the configuration fails.
     */
    @Bean
    public DataSource dataSource() throws Exception {
        PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setConnectionFactoryClassName("oracle.jdbc.OracleDriver");
        poolDataSource.setURL(url);
        poolDataSource.setUser(username);
        poolDataSource.setPassword(password);

        // UCP pool configuration
        poolDataSource.setInitialPoolSize(initialPoolSize);
        poolDataSource.setMinPoolSize(minPoolSize);
        poolDataSource.setMaxPoolSize(maxPoolSize);
        poolDataSource.setConnectionWaitTimeout(connectionWaitTimeout);
        poolDataSource.setTimeoutCheckInterval(timeoutCheckInterval);
        poolDataSource.setInactiveConnectionTimeout(inactiveConnectionTimeout);
        poolDataSource.setAbandonedConnectionTimeout(abandonedConnectionTimeout);
        poolDataSource.setValidateConnectionOnBorrow(validateConnectionOnBorrow);
        poolDataSource.setMaxConnectionReuseTime(maxConnectionLifetime);
        poolDataSource.setMaxConnectionReuseCount(maxConnectionReuseCount);

        return poolDataSource;
    }

    /**
     * Registers the JdbcTemplate bean for database interactions.
     *
     * @param dataSource The DataSource to use for JdbcTemplate.
     * @return The configured JdbcTemplate.
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // Getters and setters for configuration properties

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getConnectionWaitTimeout() {
        return connectionWaitTimeout;
    }

    public void setConnectionWaitTimeout(int connectionWaitTimeout) {
        this.connectionWaitTimeout = connectionWaitTimeout;
    }

    public int getTimeoutCheckInterval() {
        return timeoutCheckInterval;
    }

    public void setTimeoutCheckInterval(int timeoutCheckInterval) {
        this.timeoutCheckInterval = timeoutCheckInterval;
    }

    public int getInactiveConnectionTimeout() {
        return inactiveConnectionTimeout;
    }

    public void setInactiveConnectionTimeout(int inactiveConnectionTimeout) {
        this.inactiveConnectionTimeout = inactiveConnectionTimeout;
    }

    public int getAbandonedConnectionTimeout() {
        return abandonedConnectionTimeout;
    }

    public void setAbandonedConnectionTimeout(int abandonedConnectionTimeout) {
        this.abandonedConnectionTimeout = abandonedConnectionTimeout;
    }

    public boolean isValidateConnectionOnBorrow() {
        return validateConnectionOnBorrow;
    }

    public void setValidateConnectionOnBorrow(boolean validateConnectionOnBorrow) {
        this.validateConnectionOnBorrow = validateConnectionOnBorrow;
    }

    public int getMaxConnectionLifetime() {
        return maxConnectionLifetime;
    }

    public void setMaxConnectionLifetime(int maxConnectionLifetime) {
        this.maxConnectionLifetime = maxConnectionLifetime;
    }

    public int getMaxConnectionReuseCount() {
        return maxConnectionReuseCount;
    }

    public void setMaxConnectionReuseCount(int maxConnectionReuseCount) {
        this.maxConnectionReuseCount = maxConnectionReuseCount;
    }
}
