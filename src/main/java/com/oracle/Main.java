package com.oracle;

import oracle.jdbc.pool.OracleDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {


    static String tnsAdmin    = System.getenv("BENCH_TNS_ADMIN");
    static String dbName      = System.getenv("BENCH_DB");
    static String username    = System.getenv("BENCH_DB_USER");
    static String password    = System.getenv("BENCH_DB_PASSWORD");
    static String tableName   = System.getenv("BENCH_TABLE").toUpperCase();
    static int threadPoolSize  = Integer.parseInt(System.getenv("BENCH_THREADS"));
    static int numOfOperations = Integer.parseInt(System.getenv("BENCH_OPS"));
    static int numOfTables     = 0;

    public static void main() {
        long startTime=0,endTime=0;
        Applier[] appliers = new Applier[threadPoolSize];
        try {
            System.out.println("ADB Benchmark initialization");
            OracleDataSource ods = new OracleDataSource();
            ods.setURL("jdbc:oracle:thin:@" + dbName + "?TNS_ADMIN=" + tnsAdmin);
            ods.setUser(username);
            ods.setPassword(password);
            Connection con = ods.getConnection();

            Statement tableExists = con.createStatement();
            ResultSet resultSet = tableExists.executeQuery("SELECT COUNT(*) NUM_TABLES FROM USER_TABLES WHERE TABLE_NAME = '"+tableName+"'");
            while (resultSet.next() ) {
                numOfTables = resultSet.getInt("NUM_TABLES");
                if (numOfTables != 0)
                    con.createStatement().execute("DROP TABLE " + tableName);
            }
            con.createStatement().execute("CREATE TABLE "+tableName+"(PK NUMBER(10),SK VARCHAR2(1000))");
            con.close();

            for (int i = 0; i < appliers.length; i++)
                appliers[i] = new Applier(i,tableName,tnsAdmin,dbName,username,password,numOfOperations);

            System.out.println("ADB Benchmark initialized successfully.");
            System.out.println("Starting Threads");
            startTime = System.currentTimeMillis();
            for (int i = 0; i < appliers.length; i++)
                appliers[i].start();

            for (int i = 0; i < appliers.length; i++)
                appliers[i].join();

            endTime = System.currentTimeMillis();
            System.out.println("ADB Test completed.");
            System.out.println("Table Name                      : "+tableName);
            System.out.println("Number of threads               : "+threadPoolSize);
            System.out.println("Number of CRUD ops sets         : "+numOfOperations);
            System.out.println("Total execution time (ms)       : "+(endTime-startTime));
            System.out.println("Execution times of threads (ms) : ");
            for (int i=0; i<threadPoolSize;i++)
                System.out.println("Thread #"+i+", total : "+appliers[i].getExecutionTime() +
                        " ,Insert : "+appliers[i].getInsertTime() +
                        " ,Update : "+appliers[i].getUpdateTime() +
                        " ,Select : "+appliers[i].getSelectTime() +
                        " ,Delete : "+appliers[i].getDeleteTime() );
        }
        catch (Exception e) {e.printStackTrace();}
    }
}
