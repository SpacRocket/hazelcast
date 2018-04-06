/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.map.impl;

import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.map.merge.IgnoreMergingEntryMapMergePolicy;
import com.hazelcast.spi.impl.merge.BaseSplitBrainHandlerService;

import java.util.Collection;
import java.util.Iterator;

import static com.hazelcast.util.ThreadUtil.assertRunningOnPartitionThread;

class MapSplitBrainHandlerService extends BaseSplitBrainHandlerService<RecordStore> {

    private final MapServiceContext mapServiceContext;

    MapSplitBrainHandlerService(MapServiceContext mapServiceContext) {
        super(mapServiceContext.getNodeEngine());
        this.mapServiceContext = mapServiceContext;
    }

    @Override
    protected Runnable newMergeRunnable(Collection<RecordStore> mergingStores,
                                        BaseSplitBrainHandlerService<RecordStore> splitBrainHandlerService) {
        return new MapMergeRunnable(mergingStores, splitBrainHandlerService, mapServiceContext);
    }

    @Override
    protected Iterator<RecordStore> storeIterator(int partitionId) {
        PartitionContainer partitionContainer = mapServiceContext.getPartitionContainer(partitionId);
        Collection<RecordStore> recordStores = partitionContainer.getAllRecordStores();
        return recordStores.iterator();
    }

    @Override
    protected void destroyStore(RecordStore store) {
        assertRunningOnPartitionThread();

        store.destroyInternals();
    }

    @Override
    protected boolean hasDiscardPolicy(Object mergePolicy) {
        return mergePolicy instanceof IgnoreMergingEntryMapMergePolicy
                || super.hasDiscardPolicy(mergePolicy);
    }

    @Override
    protected boolean hasEntry(RecordStore store) {
        assertRunningOnPartitionThread();

        return store.size() > 0;
    }
}
