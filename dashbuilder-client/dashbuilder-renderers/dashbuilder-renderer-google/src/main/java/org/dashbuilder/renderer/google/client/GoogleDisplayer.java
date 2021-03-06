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
package org.dashbuilder.renderer.google.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.format.DateFormat;
import com.googlecode.gwt.charts.client.format.DateFormatOptions;
import com.googlecode.gwt.charts.client.format.NumberFormat;
import com.googlecode.gwt.charts.client.format.NumberFormatOptions;
import com.googlecode.gwt.charts.client.options.FormatType;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.renderer.google.client.resources.i18n.GoogleDisplayerConstants;

public abstract class GoogleDisplayer extends AbstractDisplayer {

    protected boolean drawn = false;
    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();

    protected DataSet dataSet;
    protected DataTable googleTable = null;

    public GoogleDisplayer() {
        initWidget(panel);
    }

    /**
     * Draw the displayer by getting first the underlying data set.
     * Ensure the displayer is also ready for display, which means the Google Visualization API has been loaded.
     */
    public void draw() {
        if (!drawn) {
            drawn = true;

            if (displayerSettings == null) {
                displayMessage("ERROR: DisplayerSettings property not set");
            }
            else if (dataSetHandler == null) {
                displayMessage("ERROR: DataSetHandler property not set");
            }
            else {
                try {
                    String initMsg = GoogleDisplayerConstants.INSTANCE.googleDisplayer_initalizing();
                    //if (!StringUtils.isBlank(displayerSettings.getTitle())) initMsg += " '" + displayerSettings.getTitle() + "'";
                    displayMessage(initMsg + " ...");

                    beforeDataSetLookup();
                    dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                        public void callback(DataSet result) {
                            dataSet = result;
                            afterDataSetLookup(result);
                            Widget w = createVisualization();
                            panel.clear();
                            panel.add(w);

                            // Set the id of the container panel so that the displayer can be easily located
                            // by testing tools for instance.
                            String id = getDisplayerId();
                            if (!StringUtils.isBlank(id)) {
                                panel.getElement().setId(id);
                            }
                            // Draw done
                            afterDraw();
                        }
                        public void notFound() {
                            displayMessage("ERROR: Data set not found.");
                        }
                    });
                } catch (Exception e) {
                    displayMessage("ERROR: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Just reload the data set and make the current Google Displayer redraw.
     */
    public void redraw() {
        if (!drawn) {
            draw();
        } else {
            try {
                beforeDataSetLookup();
                dataSetHandler.lookupDataSet(new DataSetReadyCallback() {
                    public void callback(DataSet result) {
                        dataSet = result;
                        afterDataSetLookup(result);
                        updateVisualization();

                        // Redraw done
                        afterRedraw();
                    }
                    public void notFound() {
                        displayMessage("ERROR: Data set not found.");
                    }
                });
            } catch (Exception e) {
                displayMessage("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * Close the displayer
     */
    public void close() {
        panel.clear();

        // Close done
        afterClose();
    }

    /**
     * Get the Google Visualization package this displayer requires.
     */
    protected abstract ChartPackage getPackage();

    /**
     * Create the widget used by concrete Google displayer implementation.
     */
    protected abstract Widget createVisualization();

    /**
     * Update the widget used by concrete Google displayer implementation.
     */
    protected abstract void updateVisualization();

    /**
     * Call back method invoked just before the data set lookup is executed.
     */
    protected void beforeDataSetLookup() {
    }

    /**
     * Call back method invoked just after the data set lookup is executed.
     */
    protected void afterDataSetLookup(DataSet dataSet) {
    }

    /**
     * Clear the current display and show a notification message.
     */
    public void displayMessage(String msg) {
        panel.clear();
        panel.add(label);
        label.setText(msg);
    }

    // Google DataTable manipulation methods

    public DataTable createTable() {

        // Init & populate the table
        googleTable = DataTable.create();
        googleTable.addRows(dataSet.getRowCount());
        List<DataColumn> columns = dataSet.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            DataColumn dataColumn = columns.get(i);
            List columnValues = dataColumn.getValues();
            ColumnType columnType = dataColumn.getColumnType();
            String columnId = dataColumn.getId();
            String columnName = dataColumn.getName();
            if (columnName == null) columnName = columnId;

            googleTable.addColumn(getColumnType(dataColumn), columnName, columnId);
            for (int j = 0; j < columnValues.size(); j++) {
                Object value = columnValues.get(j);
                if (ColumnType.LABEL.equals(columnType)) value = super.formatValue(value, dataColumn);
                setTableValue(googleTable, columnType, value, j, i);
            }
        }

        // Format the table values
        DateFormatOptions dateFormatOptions = DateFormatOptions.create();
        dateFormatOptions.setFormatType(FormatType.MEDIUM);
        DateFormat dateFormat = DateFormat.create(dateFormatOptions);
        NumberFormatOptions numberFormatOptions = NumberFormatOptions.create();
        numberFormatOptions.setPattern("#,###.##");
        NumberFormat numberFormat = NumberFormat.create(numberFormatOptions);

        for (int i = 0; i < googleTable.getNumberOfColumns(); i++) {
            com.googlecode.gwt.charts.client.ColumnType type = googleTable.getColumnType(i);
            if (com.googlecode.gwt.charts.client.ColumnType.DATE.equals(type)) {
                dateFormat.format(googleTable, i);
            }
            else if (com.googlecode.gwt.charts.client.ColumnType.NUMBER.equals(type)) {
                numberFormat.format(googleTable, i);
            }
        }
        return googleTable;
    }

    protected com.google.gwt.i18n.client.NumberFormat numberFormat = com.google.gwt.i18n.client.NumberFormat.getFormat("#0.00");

    public void setTableValue(DataTable gTable, ColumnType type, Object value, int row, int column) {
        if (ColumnType.DATE.equals(type)) {
            if (value == null) gTable.setValue(row, column, new Date());
            else gTable.setValue(row, column, (Date) value);
        }
        else if (ColumnType.NUMBER.equals(type)) {
            if (value == null) {
                gTable.setValue(row, column, 0d);
            } else {
                String valueStr = numberFormat.format((Number) value);
                gTable.setValue(row, column, Double.parseDouble(valueStr));
            }
        }
        else {
            gTable.setValue(row, column, value.toString());
        }
    }

    public com.googlecode.gwt.charts.client.ColumnType getColumnType(DataColumn dataColumn) {
        ColumnType type = dataColumn.getColumnType();
        if (ColumnType.LABEL.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.STRING;
        if (ColumnType.TEXT.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.STRING;
        if (ColumnType.NUMBER.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.NUMBER;
        if (ColumnType.DATE.equals(type)) return com.googlecode.gwt.charts.client.ColumnType.DATE;
        return com.googlecode.gwt.charts.client.ColumnType.STRING;
    }
}
