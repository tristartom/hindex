package tthbase.coprocessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.wal.HLog;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.regionserver.Store;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

public class BasicIndexObserver extends LoggedObserver {
    private boolean initialized;

    protected HTableUpdateIndexByPut dataTableWithIndexes = null;

    private void tryInitialize(HTableDescriptor desc) throws IOException {
        if(initialized == false) {
            synchronized(this) {
                if(initialized == false) {
                    Configuration conf = HBaseConfiguration.create();
                    dataTableWithIndexes = new HTableUpdateIndexByPut(conf, desc.getName()); //this will make copy of data table instance.
                    initialized = true;
                }
            }
        }
    }

    @Override
    public void start(CoprocessorEnvironment e) throws IOException {
        setFunctionLevelLogging(false);
        initialized = false;
        super.start(e);
    }

    @Override
    public void prePut(final ObserverContext<RegionCoprocessorEnvironment> e, final Put put, final WALEdit edit, final boolean writeToWAL) throws IOException {
        super.prePut(e, put, edit, writeToWAL);
        tryInitialize(e.getEnvironment().getRegion().getTableDesc());
    }

    @Override
    public void postPut(final ObserverContext<RegionCoprocessorEnvironment> e, final Put put, final WALEdit edit, final boolean writeToWAL) throws IOException {
        super.postPut(e, put, edit, writeToWAL);
    }

    @Override
    public void preDelete(ObserverContext<RegionCoprocessorEnvironment> e, Delete delete, WALEdit edit, boolean writeToWAL) throws IOException {
        super.preDelete(e, delete, edit, writeToWAL);
        tryInitialize(e.getEnvironment().getRegion().getTableDesc());
    }

    @Override
    public InternalScanner preCompact(ObserverContext<RegionCoprocessorEnvironment> e, Store store, InternalScanner scanner) throws IOException{
        InternalScanner toRet = super.preCompact(e, store, scanner);
        tryInitialize(e.getEnvironment().getRegion().getTableDesc());
        return toRet;
    }

    @Override
    public void preGet(ObserverContext<RegionCoprocessorEnvironment> e, Get get, List<KeyValue> result) throws IOException {
        super.preGet(e, get, result);
        tryInitialize(e.getEnvironment().getRegion().getTableDesc());
    }

    @Override
    public void stop(CoprocessorEnvironment e) throws IOException { 
        super.stop(e);
        if(dataTableWithIndexes != null){
            dataTableWithIndexes.close();
        }
    }
}
