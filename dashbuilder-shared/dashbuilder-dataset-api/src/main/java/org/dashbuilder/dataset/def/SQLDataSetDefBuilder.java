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
package org.dashbuilder.dataset.def;

/**
 * A builder for defining SQL data sets
 *
 * <pre>
 *    DataSetDef dataSetDef = DataSetDefFactory.newSQLDataSetDef()
 *     .uuid("all_employees")
 *     .dataSource("java:comp/jdbc/test")
 *     .dbTable("employees")
 *     .buildDef();
 * </pre>
 */
public interface SQLDataSetDefBuilder<T extends DataSetDefBuilder> extends DataSetDefBuilder<T> {

    /**
     * Set the data source where this data set is stored.
     *
     * @param dataSource the data source JNDI name
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T dataSource(String dataSource);

    /**
     * Set the database schema where the target table relies
     *
     * @param dbSchema the schema name
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T dbSchema(String dbSchema);

    /**
     * Set the database table holding all the data set rows.
     *
     * @param dbTable the table name
     * @param allColumns If true then all the DB table columns will be part of the data set.
     * If false then only the columns defined (see the DataSetDefBuilder column definition methods)
     * @return The DataSetDefBuilder instance that is being used to configure a DataSetDef.
     */
    T dbTable(String dbTable, boolean allColumns);
}
