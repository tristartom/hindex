package tthbase.test;
import tthbase.client.HTableGetByIndex;
import tthbase.util.HIndexConstantsAndUtils;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.HConstants;

import java.io.IOException;
public class BaseHBaseTestcase extends junit.framework.TestCase {

    int coprocessorIndex = 1;
    byte[] testTableName = Bytes.toBytes("tablefortest");
    byte[] columnFamily = Bytes.toBytes("cf");
    String indexedColumnName = "country";

    Configuration conf = null;
    HTableGetByIndex htable = null;

//    String coprocessorJarLoc = "hdfs://node1:8020/hbase_cp/libHbaseCoprocessor.jar";
    String curdir = BaseHBaseTestcase.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    String coprocessorJarLoc = "file:" + curdir;

    /**
    1. create table 'testtable'
    2. load coprocessor into the table
    3. initialize the connection to/handler of the testtable
    */
    @Override
    public void setUp() {
        try{
            conf = HBaseConfiguration.create();

            HBaseAdmin admin = new HBaseAdmin(conf);
            boolean avail = admin.isTableAvailable(testTableName);
 
            //if table not exist, create one
            if(!avail){
                HIndexConstantsAndUtils.createAndConfigBaseTable(conf, testTableName, columnFamily, new String[]{indexedColumnName});
                //create index table
                byte[] indexTableName = HIndexConstantsAndUtils.generateIndexTableName(testTableName, columnFamily,  Bytes.toBytes(indexedColumnName)/*TODO column family in index table*/);
                HIndexConstantsAndUtils.createAndConfigIndexTable(conf, indexTableName, columnFamily);
            }
            htable = new HTableGetByIndex(conf, testTableName);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
    1. drop table 'testtable'
    */
    public void tearDown() {
        coprocessorIndex = 1;
        try{
            if(htable != null){
                htable.close();
            }
            byte[] indexTableName = HIndexConstantsAndUtils.generateIndexTableName(testTableName, columnFamily, Bytes.toBytes(indexedColumnName)/*TODO column family in index table*/);
            HIndexConstantsAndUtils.deleteTable(conf, indexTableName);
            HIndexConstantsAndUtils.deleteTable(conf, testTableName);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void testTableAvailability() throws Exception{
        HBaseAdmin admin = new HBaseAdmin(conf);
        boolean avail = admin.isTableAvailable(testTableName);
        if (!avail){
            fail("base table not available");
        } else {
            System.out.println("TTDEBUG: base table " + Bytes.toString(testTableName) + " successfully created!");
        }

        byte[] indexTableName = HIndexConstantsAndUtils.generateIndexTableName(testTableName, columnFamily, Bytes.toBytes(indexedColumnName)/*TODO column family in index table*/);
        avail = admin.isTableAvailable(indexTableName);
        if (!avail){
            fail("index table not available");
        } else {
            System.out.println("TTDEBUG: index table " + Bytes.toString(indexTableName) + " successfully created!");
        }
    }

    void setupIndexingCoprocessor(String policyIndexing) throws IOException {
//System.out.println("CoProcessor location: " + coprocessorJarLoc);
        if(policyIndexing.equals("baseline")){
            //update coprocessor
            HIndexConstantsAndUtils.updateCoprocessor(conf, testTableName, coprocessorIndex++, true, coprocessorJarLoc, "tthbase.coprocessor.IndexObserverBaseline");
            htable.configPolicy(HTableGetByIndex.PLY_FASTREAD);
        } else if(policyIndexing.equals("hindex")){
            HIndexConstantsAndUtils.updateCoprocessor(conf, testTableName, coprocessorIndex++, true, coprocessorJarLoc, "tthbase.coprocessor.IndexObserverwReadRepair");
            htable.configPolicy(HTableGetByIndex.PLY_READCHECK);
        }
    }

    /**
     * @param: ts is set to < 1 to use hbase internal versioning.
    */
    public void put(String key, String family, String qualifier, byte[] value, long ts) throws IOException {
         Put put = null;
         if(ts >= 0) {
             put = new Put(Bytes.toBytes(key), ts);
         } else {
             put = new Put(Bytes.toBytes(key));
         }
         put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), value); 
         htable.put(put);
    }
}
