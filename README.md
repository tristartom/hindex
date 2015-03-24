HIndex: Secondary Index for HBase and other NoSQL Systems
======

Introduction
------
This project is to add a secondary-index layer on top of HBase. In current implementation, we consider mutable data where data updates overwrite previous data versions of the same key. Index is declared at the column level. While it is possible to support various kinds of value-based queries, current implementation handles exact-match and range query (on secondary indexed column).

Index maintenance and use for query processing (in other words, index write and read paths) are both implemented on the server side, by using [HBase CoProcessor](https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/coprocessor/package-summary.html). For more details, check out the [src](https://github.com/tristartom/hindex/blob/master/src) directory. 
 
Quick start
------
1. Download the proj, following the link provided by github.
2. Set up an `HBase` cluster instance, locally, with the gateway port 2181 (which is the `Zookeeper` clientPort). The setup of an HBase cluster can be consulted from [HBase tutorial](http://hbase.apache.org/book/quickstart.html) or my [autohbase](http://www.cc.gatech.edu/~ytang36/software/autohbase/index.html) scripts.
3. Set up `ant` and `java-1.6+` locally (http://ant.apache.org/manual/index.html)
4. Simply run `ant` 
```
 cd $project_directory
 ant 
```
In addition to compiling and building the project, the `ant` command above will automatically 1) create a base table named `testtable` with column family `cf` and an index table `testtable_cf_country` (here `country` is the indexed column), 2) set up indexing CoProcessor on the base table, 3) populate the base table with test data entries, which triggers the index updates, and 4) run the test cases in the project. The created tables will be finally disposed when the runtime finishes the execution (so the tables are not there any more). 


References
---

"Lightweight Indexing for Log-Structured Key-Value Stores", Yuzhe Tang, Arun Iyengar, Wei Tan, Liana Fong, Ling Liu, in Proceedings of the 15th IEEE/ACM International Symposium on Cluster, Cloud and Grid Computing (CCGrid 2015), Shenzhen, Guangdong, China, May 2015, [[pdf](http://tristartom.github.io/docs/ccgrid15.pdf)]

