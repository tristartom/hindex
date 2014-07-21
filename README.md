hindex: lightweight indexing for log-structured key-value stores
======

Introduction
This project is to add a secondary-index layer on top of HBase. In current implementation, we consider mutable data where data updates overwrite previous data versions of the same key. Index is declared at the column level. While it is possible to support various kinds of value-based queries, current implementation handles exact-match and range query (on secondary indexed column).

Index maintenance and use for query processing (in other words, index write and read paths) are both implemented on the server side, using HBase CoProcessor framework (https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/coprocessor/package-summary.html). For more details, check out the src directory (https://github.com/tristartom/hindex/blob/master/src). 
 
Quick start

1. Download the proj, following the link provided by github.
2. Set up an HBase cluster instance, locally, with the gateway port 2181 (which is the zookeeper clientPort)
3. Set up ant and java locally (http://ant.apache.org/manual/index.html)
4. Run
```
 cd $project_directory
 ant 
```

