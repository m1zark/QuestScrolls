package com.m1zark.questscrolls.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class SQLStatements {
    private String mainTable;

    public SQLStatements(String mainTable) {
        this.mainTable = mainTable;
    }

    public void createTables() {
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.mainTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, PlayerUUID CHAR(36), Balance Integer, PRIMARY KEY(ID));")) {
                    statement.executeUpdate();
                    connection.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
