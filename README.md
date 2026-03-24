This repository contains small Java application, which can be used to test the performance of Oracle ADB

License Copyright (c) 2024 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See LICENSE for more details.

Usage

1. Download wallet for the ADB service you want to test and unzip it
2. Set the following environment variables
   BENCH_TNS_ADMIN   : directory storing the wallet content
   BENCH_DB_USER     : name of the database username
   BENCH_DB_PASSWORD : database password
   BENCH_DB          : database service used for the test
   BENCH_THREADS     : number of parallel threads testing the performance the service
   BENCH_OPS         : number of CRUD operations set (every set consists of INSERT/UPDATE/SELECT/DELETE statements)
   BENCH_TABLE       : table used for the test (every time the test is running, the table is recreated)
3. run the test in the following way :

   java -jar ./ADBBenchmark.jar 
