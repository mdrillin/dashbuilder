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
package org.dashbuilder.storage.memory.group;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.model.dataset.DataColumn;
import org.dashbuilder.model.dataset.group.Domain;

/**
 * Interval builder for label columns which generates one interval per label.
 */
@ApplicationScoped
public class IntervalBuilderDynamicLabel implements IntervalBuilder {

    public IntervalList build(DataColumn column, Domain domain) {
        IntervalListLabel result = new IntervalListLabel(domain);
        List values = column.getValues();
        return result.indexValues(values);
    }

    private class IntervalListLabel extends IntervalList {

        private IntervalListLabel(Domain domain) {
            super(domain);
        }

        public Interval indexValue(Object value, int row) {
            Interval interval = locateInterval(value);
            if (interval == null) {
                // TODO: create a composite interval when the maxIntervals are reached.
                this.add(interval = new Interval(value == null ? null : value.toString()));
            }
            interval.rows.add(row);
            return interval;
        }

        public Interval locateInterval(Object value) {
            for (Interval interval : this) {
                if (interval.name == value || (interval.name != null && interval.name.equals(value))) {
                    return interval;
                }
            }
            return null;
        }
    }
}