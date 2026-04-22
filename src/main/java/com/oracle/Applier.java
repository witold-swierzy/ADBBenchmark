package com.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
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
    private long selStartTime = 0;
    private long selEndTime   = 0;
    private long delStartTime = 0;
    private long delEndTime   = 0;
    private boolean execInserts,execUpdates,execSelects,execDeletes;
    private PreparedStatement insert,update,select,delete;

    public Applier (int threadId,
                    String tableName,
                    String tnsAdmin,
                    String dbName,
                    String username,
                    String password,
                    int numOfOperations,
                    boolean execInserts,
                    boolean execUpdates,
                    boolean execSelects,
                    boolean execDeletes) {
        this.threadId        = threadId;
        this.tableName       = tableName;
        this.tnsAdmin        = tnsAdmin;
        this.dbName          = dbName;
        this.username        = username;
        this.password        = password;
        this.numOfOperations = numOfOperations;
        this.execInserts     = execInserts;
        this.execUpdates     = execUpdates;
        this.execSelects     = execSelects;
        this.execDeletes     = execDeletes;

        try {
            ods = new OracleDataSource();
            ods.setURL("jdbc:oracle:thin:@" + dbName + "?TNS_ADMIN=" + tnsAdmin);
            ods.setUser(username);
            ods.setPassword(password);
            connection = ods.getConnection();
            connection.setAutoCommit(false);
            insert = connection.prepareStatement("INSERT INTO "+this.tableName+" values (?,?)");
            update = connection.prepareStatement("UPDATE "+this.tableName+" SET SK=? WHERE PK=?");
            select = connection.prepareStatement("SELECT * FROM "+this.tableName+" WHERE PK=?");
            delete = connection.prepareStatement("DELETE FROM "+this.tableName+" WHERE PK=?");
        }
        catch (Exception e) {e.printStackTrace();};
    }

    public void run () {
        int start = numOfOperations*threadId;
        int end   = numOfOperations*threadId + numOfOperations;
        try {
            // inserts
            startTime = System.currentTimeMillis();
            if (this.execInserts) {
                connection.setAutoCommit(false);
                insStartTime = System.currentTimeMillis();
                for (int i = start; i < end; i++) {
                    insert.setInt(1, i);
                    insert.setString(2, "OldValue");
                    insert.execute();
                    //insert.addBatch();
                    //insert.clearParameters();
                }

                //insert.executeBatch();
                connection.commit();
                insEndTime = System.currentTimeMillis();
            }
            // updates
            if (this.execUpdates) {
                updStartTime = System.currentTimeMillis();
                for (int i = start; i < end; i++) {
                    update.setString(1, "NewValue");
                    update.setInt(2, i);
                    update.execute();
                    //update.addBatch();
                    //update.clearParameters();
                }
                //update.executeBatch();
                connection.commit();
                updEndTime = System.currentTimeMillis();
            }
            if (this.execSelects) {
                selStartTime = System.currentTimeMillis();
                for (int i = start; i < end; i++) {
                    select.setInt(1, i);
                    select.execute();
                }
                selEndTime = System.currentTimeMillis();
            }

            // deletes
            if (this.execDeletes) {
                delStartTime = System.currentTimeMillis();
                for (int i = start; i < end; i++) {
                    delete.setInt(1, i);
                    delete.execute();
                    //delete.addBatch();
                    //delete.clearParameters();
                }
                //delete.executeBatch();
                connection.commit();
                delEndTime = System.currentTimeMillis();
            }
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

    public long getSelectTime() {
        return this.selEndTime - this.selStartTime;
    }

    public long getDeleteTime() {
        return this.delEndTime - this.delStartTime;
    }
}
