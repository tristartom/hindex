package tthbase.test;
import tthbase.client.HTableGetByIndex;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.Bytes;
import java.util.*;
import java.io.IOException;

public class GetByIndexTest extends BaseHBaseTestcase {
    Map<String, String> data = new HashMap<String, String>();

    public void setUp() {
        super.setUp();

        data.put("k0", "v01");
        data.put("k1", "v11");
        data.put("k1", "v21");
        data.put("k2", "v21");
        data.put("k3", "v31");

      try{
        setupIndexingCoprocessor("hindex");
        loadData();
      } catch (Exception e) {
        
        if(htable == null){
          throw new RuntimeException(" htable==null!: can not create htable with name " + Bytes.toString(testTableName));
        } else {
          e.printStackTrace();
        }
      }
    }

    public void loadData() throws IOException {
        for(Map.Entry<String, String> entry : data.entrySet()){
            put(entry.getKey(), Bytes.toString(columnFamily), indexedColumnName, Bytes.toBytes(entry.getValue()), -1);
        }
    }

    public void testGetByIndexPoint() {
        unitTestGetByIndexPoint("v11", null);
        unitTestGetByIndexPoint("v21", new String[]{"k1", "k2"});
        unitTestGetByIndexPoint("v31", new String[]{"k3"});
    }

    public void unitTestGetByIndexPoint(String value, String[] expectedKeys) {
        try{
//            long begin = System.currentTimeMillis();
            List<byte[]> res = htable.getByIndex(columnFamily, Bytes.toBytes(indexedColumnName), Bytes.toBytes(value));
//            long dur = System.currentTimeMillis() - begin;
//            int index = 0;
            utilCheck(value, expectedKeys, res);
//            System.out.println("execution time = " + dur + " ms");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void utilCheck(String value, String[] expectedKeys, List<byte[]> res) {
            if (expectedKeys == null || expectedKeys.length == 0){
                assertTrue(res == null || res.size() == 0);
            } else {
                if(res != null && res.size() == expectedKeys.length) {
                    List<String> keysList = Arrays.asList(expectedKeys);
                    for(byte[] r : res){
//System.out.println(value + "=>" + Bytes.toString(r));
                        assertTrue(keysList.contains(Bytes.toString(r)));
                    }
                } else { 
                    fail("incorrect result list of " + (res == null ? 0 : res.size()) + " items.");
                }
            }
    }

    public void testGetByIndexRange() {
        String[] expectedValueKeys = null;
        expectedValueKeys = new String[]{
           "v21/k1",
           "v21/k2",
        };
        unittestGetByIndexRange("v21", "v22", expectedValueKeys);

        expectedValueKeys = new String[]{
           "v01/k0",
           "v21/k1",
           "v21/k2",
        };
        unittestGetByIndexRange("v01", "v21", expectedValueKeys);
    }

    public void unittestGetByIndexRange(String valueStart, String valueEnd, String[] expectedValueKeys) {
         try{
//            long begin = System.currentTimeMillis();
            Map<byte[], List<byte[]> > res = htable.getByIndexByRange(columnFamily, Bytes.toBytes(indexedColumnName), Bytes.toBytes(valueStart), Bytes.toBytes(valueEnd));
//            long dur = System.currentTimeMillis() - begin;
//            int index = 0;

 
           if (expectedValueKeys == null || expectedValueKeys.length == 0){
                assertTrue(res == null || res.size() == 0);
            } else {
                if(res != null) {
                  List<String> valueKeysList = Arrays.asList(expectedValueKeys);

                  for(Map.Entry<byte[], List<byte[]> > entry : res.entrySet()){
                    for(byte[] key : entry.getValue())
                        assertTrue(valueKeysList.contains(Bytes.toString(entry.getKey()) + "/" + Bytes.toString(key)));
                  }
                } else {
/*
System.out.print("index read res-" + res.size() + ": ");
for(Map.Entry<byte[], List<byte[]> > e : res.entrySet())
{
  System.out.print(Bytes.toString(e.getKey()));
  List<byte[]> keys = e.getValue();
  System.out.print("=>{");
  for(byte[] key : keys){
    System.out.print(Bytes.toString(key) + ",");
  }
  System.out.print("},");
}
System.out.println();
*/
                    fail("incorrect result of " + (res == null ? 0 : res.size()) + " items, but expected " + expectedValueKeys.length + " items.");
                }
            }
//            System.out.println("execution time = " + dur + " ms");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
