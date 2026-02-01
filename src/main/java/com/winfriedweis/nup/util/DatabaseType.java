package com.winfriedweis.nup.util;

public enum DatabaseType {
    MYSQL("com.mysql.cj.jdbc.Driver", 3306, "jdbc:mysql://"),
    POSTGRESQL("org.postgresql.Driver", 5432, "jdbc:postgresql://");

    private final String driverClassName;
    private final int defaultPort;
    private final String jdbcPrefix;

    DatabaseType(String driverClassName, int defaultPort, String jdbcPrefix) {
        this.driverClassName = driverClassName;
        this.defaultPort = defaultPort;
        this.jdbcPrefix = jdbcPrefix;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public String getJdbcPrefix() {
        return jdbcPrefix;
    }

    public String buildJdbcUrl(String host, int port, String database) {
        return jdbcPrefix + host + ":" + port + "/" + database;
    }
}
