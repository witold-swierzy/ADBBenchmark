package com.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;

import oracle.jdbc.pool.OracleDataSource;

public class Applier extends Thread {

    private Connection connection;
    private OracleDataSource ods;

    private int    threadId;
    private String tableName;
    private String tnsAdmin;
    private String dbName;
    private String username;
    private String password;
    private int    numOfOperations;

    private long startTime    = 0;
    private long endTime      = 0;
    private long insStartTime = 0;
    private long insEndTime   = 0;
    private long updStartTime = 0;
    private long updEndTime   = 0;
    private long delStartTime = 0;
    private long delEndTime   = 0;
    private PreparedStatement insert,update,delete;

    public Applier (int threadId,
                    String tableName,
                    String tnsAdmin,
                    String dbName,
                    String username,
                    String password,
                    int numOfOperations) {
        this.threadId        = threadId;
        this.tableName       = tableName;
        this.tnsAdmin        = tnsAdmin;
        this.dbName          = dbName;
        this.username        = username;
        this.password        = password;
        this.numOfOperations = numOfOperations;

        try {
            ods = new OracleDataSource();
            ods.setURL("jdbc:oracle:thin:@" + dbName + "?TNS_ADMIN=" + tnsAdmin);
            ods.setUser(username);
            ods.setPassword(password);
            connection = ods.getConnection();
            connection.setAutoCommit(true);
            insert = connection.prepareStatement("INSERT INTO "+this.tableName+" values (?,?)");
            update = connection.prepareStatement("UPDATE "+this.tableName+" SET SK=? WHERE PK=?");
            delete = connection.prepareStatement("DELETE FROM "+this.tableName+" WHERE PK=?");
        }
        catch (Exception e) {e.printStackTrace();};
    }

    public void run () {
        int start = numOfOperations*threadId + 1;
        int end   = numOfOperations*threadId + numOfOperations + 1;
        try {
            // inserts
            startTime = System.currentTimeMillis();
            insStartTime = System.currentTimeMillis();
            for (int i = start; i < end; i++) {
                insert.setInt(1, i);
                insert.setString(2, "OldValue");
                insert.execute();
            }

            insEndTime = System.currentTimeMillis();
            // updates
            updStartTime = System.currentTimeMillis();
            for (int i = start; i < end; i++) {
                update.setString(1,"NewValue");
                update.setInt(2,i);
                update.execute();
            }
            updEndTime = System.currentTimeMillis();
            // deletes
            delStartTime = System.currentTimeMillis();
            for (int i = start; i < end; i++) {
                delete.setInt(1,i);
                delete.execute();
            }
            delEndTime = System.currentTimeMillis();
            endTime = System.currentTimeMillis();
            connection.close();
        }
        catch (Exception e) {e.printStackTrace();};
    }

    public long getExecutionTime() {
        return this.endTime - this.startTime;
    }

    public long getInsertTime() {
        return this.insEndTime - this.insStartTime;
    }

    public long getUpdateTime() {
        return this.updEndTime - this.updStartTime;
    }

    public long getDeleteTime() {
        return this.delEndTime - this.delStartTime;
    }
}
