/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataset.index;

import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.dataset.index.stats.SizeEstimator;

/**
 * An interval index
 */
public class DataSetIntervalIndex extends DataSetRowsIndex {

    String intervalName = null;

    DataSetIntervalIndex(DataSetGroupIndex parent, Interval interval) {
        super(parent, interval.getRows());
        this.intervalName = interval.getName();
    }

    public String getName() {
        return intervalName;
    }

    public long getEstimatedSize() {
        long result = super.getEstimatedSize();
        if (intervalName != null) {
            result += SizeEstimator.sizeOfString(intervalName);
        }
        return result;
    }
}

