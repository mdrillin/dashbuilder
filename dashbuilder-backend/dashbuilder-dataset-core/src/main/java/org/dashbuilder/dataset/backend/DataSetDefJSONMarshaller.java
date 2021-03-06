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
package org.dashbuilder.dataset.backend;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataprovider.DataSetProviderRegistry;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * DataSetDef from/to JSON utilities
 */
public class DataSetDefJSONMarshaller {

    // General settings
    public static final String UUID = "uuid";
    public static final String PROVIDER = "provider";
    public static final String ISPUBLIC = "isPublic";
    public static final String PUSH_ENABLED = "pushEnabled";
    public static final String PUSH_MAXSIZE = "pushMaxSize";
    public static final String COLUMNS = "columns";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PATTERN = "pattern";
    public static final String FILTERS = "filters";

    // CSV related
    public static final String FILEURL = "fileURL";
    public static final String FILEPATH = "filePath";
    public static final String SEPARATORCHAR = "separatorChar";
    public static final String QUOTECHAR = "quoteChar";
    public static final String ESCAPECHAR = "escapeChar";
    public static final String DATEPATTERN = "datePattern";
    public static final String NUMBERPATTERN = "numberPattern";

    // SQL related
    public static final String DATA_SOURCE = "dataSource";
    public static final String DB_SCHEMA = "dbSchema";
    public static final String DB_TABLE = "dbTable";
    public static final String ALL_COLUMNS = "allColumns";
    public static final String CACHE_ENABLED = "cacheEnabled";
    public static final String CACHE_MAXROWS = "cacheMaxRows";
    public static final String REFRESH_TIME = "refreshTime";
    public static final String REFRESH_ALWAYS = "refreshAlways";

    // ElasticSearch related
    public static final String SERVER_URL = "serverURL";
    public static final String CLUSTER_NAME = "clusterName";
    public static final String INDEX = "index";
    public static final String TYPE = "type";
    public static final String QUERY = "query";
    public static final String RELEVANCE = "relevance";
    public static final String CACHE_SYNCED = "cacheSynced";
    
    // Bean related
    public static final String GENERATOR_CLASS = "generatorClass";
    public static final String GENERATOR_PARAMS = "generatorParams";
    public static final String PARAM = "param";
    public static final String VALUE = "value";

    // Other
    public static final String COMMA = ",";
    
    @Inject
    DataSetProviderRegistry dataSetProviderRegistry;

    @Inject
    DataSetLookupJSONMarshaller dataSetLookupJSONMarshaller;

    public DataSetDef fromJson(String jsonString) throws Exception {
        JSONObject json = new JSONObject(jsonString);
        DataSetProviderType providerType = readProviderType(json);
        DataSetDef dataSetDef = DataSetProviderType.createDataSetDef(providerType);

        readGeneralSettings(dataSetDef, json);

        switch (providerType) {
            case CSV:
                readCSVSettings((CSVDataSetDef) dataSetDef, json);
                break;
            case SQL:
                readSQLSettings((SQLDataSetDef) dataSetDef, json);
                break;
            case ELASTICSEARCH:
                readElasticSearchSettings((ElasticSearchDataSetDef) dataSetDef, json);
                break;
            case BEAN:
                readBeanSettings((BeanDataSetDef) dataSetDef, json);
                break;
        }
        return dataSetDef;
    }

    public ElasticSearchDataSetDef readElasticSearchSettings(ElasticSearchDataSetDef dataSetDef, JSONObject json) throws Exception {
        String serverURL = json.has(SERVER_URL) ? json.getString(SERVER_URL) : null;
        String clusterName = json.has(CLUSTER_NAME) ? json.getString(CLUSTER_NAME) : null;
        String index = json.has(INDEX) ? json.getString(INDEX) : null;
        String type = json.has(TYPE) ? json.getString(TYPE) : null;
        String query = json.has(QUERY) ? json.getString(QUERY) : null;
        String relevance = json.has(RELEVANCE) ? json.getString(RELEVANCE) : null;
        String cacheEnabled = json.has(CACHE_ENABLED) ? json.getString(CACHE_ENABLED) : null;
        String cacheMaxRows = json.has(CACHE_MAXROWS) ? json.getString(CACHE_MAXROWS) : null;
        String cacheSynced = json.has(CACHE_SYNCED) ? json.getString(CACHE_SYNCED) : null;
        String allColumns = json.has(ALL_COLUMNS) ? json.getString(ALL_COLUMNS) : null;


        // ServerURL parameter.
        if (StringUtils.isBlank(serverURL)) {
            throw new IllegalArgumentException("The serverURL property is missing.");
        } else {
            dataSetDef.setServerURL(serverURL);
        }

        // Cluster name parameter.
        if (StringUtils.isBlank(clusterName)) {
            throw new IllegalArgumentException("The clusterName property is missing.");
        } else {
            dataSetDef.setClusterName(clusterName);
        }

        // Index parameter
        if (StringUtils.isBlank(index)) {
            throw new IllegalArgumentException("The index property is missing.");
        } else {
            String[] indexList = index.split(COMMA);
            if (indexList != null && indexList.length > 0) {
                for (String _index : indexList) {
                    dataSetDef.addIndex(_index);
                }
            }
        }

        // Type parameter.
        if (!StringUtils.isBlank(type)) {
            String[] typeList = type.split(COMMA);
            if (typeList != null && typeList.length > 0) {
                for (String _type : typeList) {
                    dataSetDef.addType(_type);
                }
            }
        }

        // Query parameter.
        if (!StringUtils.isBlank(query)) dataSetDef.setQuery(query);

        // Relevance parameter.
        if (!StringUtils.isBlank(relevance)) dataSetDef.setRelevance(relevance);

        // Cache enabled parameter.
        if (!StringUtils.isBlank(cacheEnabled)) dataSetDef.setCacheEnabled(Boolean.parseBoolean(cacheEnabled));

        // Cache max rows parameter.
        if (!StringUtils.isBlank(cacheMaxRows)) dataSetDef.setCacheMaxRows(Integer.parseInt(cacheMaxRows));

        // Cache synced parameter.
        if (!StringUtils.isBlank(cacheSynced)) dataSetDef.setCacheSynced(Boolean.parseBoolean(cacheSynced));

        // All columns flag.
        if (!StringUtils.isBlank(allColumns)) dataSetDef.setAllColumnsEnabled(Boolean.parseBoolean(allColumns));

        return dataSetDef;
    }

    public DataSetProviderType readProviderType(JSONObject json) throws Exception {
        String provider = json.getString(PROVIDER);
        if (StringUtils.isBlank(provider)) {
            throw new IllegalArgumentException("Missing 'provider' property");
        }
        DataSetProviderType type = DataSetProviderType.getByName(provider);
        if (type == null || dataSetProviderRegistry.getDataSetProvider(type) == null) {
            throw new IllegalArgumentException("Provider not supported: " + provider);
        }
        return type;
    }

    public DataSetDef readGeneralSettings(DataSetDef def, JSONObject json) throws Exception {
        String uuid = json.has(UUID) ? json.getString(UUID) : null;
        String isPublic = json.has(ISPUBLIC) ? json.getString(ISPUBLIC) : null;
        String pushEnabled = json.has(PUSH_ENABLED) ? json.getString(PUSH_ENABLED) : null;
        String pushMaxSize = json.has(PUSH_MAXSIZE) ? json.getString(PUSH_MAXSIZE) : null;
        String cacheEnabled = json.has(CACHE_ENABLED) ? json.getString(CACHE_ENABLED) : null;
        String cacheMaxRows = json.has(CACHE_MAXROWS) ? json.getString(CACHE_MAXROWS) : null;
        String refreshTime  = json.has(REFRESH_TIME) ? json.getString(REFRESH_TIME) : null;
        String refreshAlways = json.has(REFRESH_ALWAYS) ? json.getString(REFRESH_ALWAYS) : null;

        if (!StringUtils.isBlank(uuid)) def.setUUID(uuid);
        if (!StringUtils.isBlank(isPublic)) def.setPublic(Boolean.parseBoolean(isPublic));
        if (!StringUtils.isBlank(pushEnabled)) def.setPushEnabled(Boolean.parseBoolean(pushEnabled));
        if (!StringUtils.isBlank(pushMaxSize)) def.setPushMaxSize(Integer.parseInt(pushMaxSize));
        if (!StringUtils.isBlank(cacheEnabled)) def.setCacheEnabled(Boolean.parseBoolean(cacheEnabled));
        if (!StringUtils.isBlank(cacheMaxRows)) def.setCacheMaxRows(Integer.parseInt(cacheMaxRows));
        if (!StringUtils.isBlank(refreshTime)) def.setRefreshTime(refreshTime);
        if (!StringUtils.isBlank(refreshAlways)) def.setRefreshAlways(Boolean.parseBoolean(refreshAlways));

        if (json.has(COLUMNS)) {
            JSONArray array = json.getJSONArray(COLUMNS);
            for (int i=0; i<array.length(); i++) {
                JSONObject column = array.getJSONObject(i);
                String columnId = column.has(COLUMN_ID) ? column.getString(COLUMN_ID) : null;
                String columnName = column.has(COLUMN_NAME) ? column.getString(COLUMN_NAME) : null;
                String columnType = column.has(COLUMN_TYPE) ? column.getString(COLUMN_TYPE) : null;
                String columnPattern = column.has(COLUMN_PATTERN) ? column.getString(COLUMN_PATTERN) : null;

                if (StringUtils.isBlank(columnId)) {
                    throw new IllegalArgumentException("Column id. attribute is mandatory.");
                }
                if (StringUtils.isBlank(columnType)) {
                    throw new IllegalArgumentException("Missing column 'type' attribute: " + columnId);
                }

                ColumnType type = ColumnType.TEXT;
                if (columnType.equals("label")) type = ColumnType.LABEL;
                else if (columnType.equals("date")) type = ColumnType.DATE;
                else if (columnType.equals("number")) type = ColumnType.NUMBER;

                if (StringUtils.isBlank(columnName)) {
                    def.getDataSet().addColumn(columnId, type);
                } else {
                    def.getDataSet().addColumn(columnId, columnName, type);
                }

                if (!StringUtils.isBlank(columnPattern)) {
                    def.setPattern(columnId, columnPattern);
                }
            }
        }
        if (json.has(FILTERS)) {
            JSONArray array = json.getJSONArray(FILTERS);
            DataSetFilter dataSetFilter = dataSetLookupJSONMarshaller.fromJsonFilterOps(array);
            def.setDataSetFilter(dataSetFilter);
        }
        return def;
    }

    public DataSetDef readBeanSettings(BeanDataSetDef def, JSONObject json) throws Exception {
        String generator = json.has(GENERATOR_CLASS) ? json.getString(GENERATOR_CLASS) : null;

        if (!StringUtils.isBlank(generator)) def.setGeneratorClass(generator);

        if (json.has(GENERATOR_PARAMS)) {
            JSONArray array = json.getJSONArray(GENERATOR_PARAMS);
            for (int i=0; i<array.length(); i++) {
                JSONObject param = array.getJSONObject(i);
                String paramId = param.has(PARAM) ? param.getString(PARAM) : null;
                String value = param.has(VALUE) ? param.getString(VALUE) : null;

                if (!StringUtils.isBlank(paramId)) {
                    def.getParamaterMap().put(paramId, value);
                }
            }
        }
        return def;
    }

    public CSVDataSetDef readCSVSettings(CSVDataSetDef def, JSONObject json) throws Exception {
        String fileURL = json.has(FILEURL) ? json.getString(FILEURL) : null;
        String filePath = json.has(FILEPATH) ? json.getString(FILEPATH) : null;
        String separatorChar = json.has(SEPARATORCHAR) ? json.getString(SEPARATORCHAR) : null;
        String quoteChar = json.has(QUOTECHAR) ? json.getString(QUOTECHAR) : null;
        String escapeChar = json.has(ESCAPECHAR) ? json.getString(ESCAPECHAR) : null;
        String datePattern = json.has(DATEPATTERN) ? json.getString(DATEPATTERN) : null;
        String numberPattern = json.has(NUMBERPATTERN) ? json.getString(NUMBERPATTERN) : null;

        if (!StringUtils.isBlank(fileURL)) def.setFileURL(fileURL);
        if (!StringUtils.isBlank(filePath)) def.setFilePath(filePath);
        if (!StringUtils.isBlank(separatorChar)) def.setSeparatorChar(separatorChar.charAt(0));
        if (!StringUtils.isBlank(quoteChar)) def.setQuoteChar(quoteChar.charAt(0));
        if (!StringUtils.isBlank(escapeChar)) def.setEscapeChar(escapeChar.charAt(0));
        if (!StringUtils.isBlank(numberPattern)) def.setNumberPattern(numberPattern);
        if (!StringUtils.isBlank(datePattern)) def.setDatePattern(datePattern);

        return def;
    }

    public SQLDataSetDef readSQLSettings(SQLDataSetDef def, JSONObject json) throws Exception {
        String dataSource = json.has(DATA_SOURCE) ? json.getString(DATA_SOURCE) : null;
        String dbTable = json.has(DB_TABLE) ? json.getString(DB_TABLE) : null;
        String dbSchema = json.has(DB_SCHEMA) ? json.getString(DB_SCHEMA) : null;
        String allColumns = json.has(ALL_COLUMNS) ? json.getString(ALL_COLUMNS) : null;

        if (!StringUtils.isBlank(dataSource)) def.setDataSource(dataSource);
        if (!StringUtils.isBlank(dbSchema)) def.setDbSchema(dbSchema);
        if (!StringUtils.isBlank(dbTable)) def.setDbTable(dbTable);
        if (!StringUtils.isBlank(allColumns)) def.setAllColumnsEnabled(Boolean.parseBoolean(allColumns));

        return def;
    }
}
