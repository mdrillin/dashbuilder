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
package org.dashbuilder.client.sales.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.renderer.table.client.TableRenderer;

import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

/**
 * A composite widget that represents an entire dashboard sample composed using an UI binder template.
 * <p>The dashboard itself is composed by a set of Displayer instances.</p>
 */
public class SalesDistributionByCountry extends Composite {

    interface SalesDashboardBinder extends UiBinder<Widget, SalesDistributionByCountry>{}
    private static final SalesDashboardBinder uiBinder = GWT.create(SalesDashboardBinder.class);

    @UiField(provided = true)
    Displayer bubbleByCountry;

    @UiField(provided = true)
    Displayer mapByCountry;

    @UiField(provided = true)
    Displayer tableAll;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "Sales by country";
    }

    public SalesDistributionByCountry() {

        // Create the chart definitions

        bubbleByCountry = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBubbleChartSettings()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .column(COUNTRY, "Country")
                .column(COUNT, "Number of opportunities")
                .column(PROBABILITY, AVERAGE, "Average probability")
                .column(COUNTRY, "Country")
                .column(EXPECTED_AMOUNT, SUM, "Expected amount")
                .title("Opportunities distribution by Country ")
                .width(450).height(300)
                .margins(20, 50, 50, 0)
                .filterOn(false, true, true)
                .buildSettings());

        mapByCountry = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMapChartSettings()
                .dataset(SALES_OPPS)
                .group(COUNTRY)
                .column(COUNTRY, "Country")
                .column(COUNT, "Number of opportunities")
                .column(EXPECTED_AMOUNT, SUM, "Total amount")
                .title("By Country")
                .width(450).height(290)
                .margins(10, 10, 10, 10)
                .filterOn(false, true, true)
                .buildSettings());

        tableAll = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newTableSettings()
                .dataset(SALES_OPPS)
                .title("List of Opportunities")
                .titleVisible(true)
                .tablePageSize(8)
                .tableOrderEnabled(true)
                .tableOrderDefault(AMOUNT, DESCENDING)
                .column(COUNTRY, "Country")
                .column(CUSTOMER, "Customer")
                .column(PRODUCT, "Product")
                .column(SALES_PERSON, "Salesman")
                .column(STATUS, "Status")
                .column(CREATION_DATE, "Creation")
                .column(EXPECTED_AMOUNT, "Expected")
                .column(CLOSING_DATE, "Closing")
                .column(AMOUNT, "Amount")
                .filterOn(true, true, true)
                .tableWidth(900)
                .renderer(TableRenderer.UUID)
                .buildSettings());

        // Make that charts interact among them
        displayerCoordinator.addDisplayer(bubbleByCountry);
        displayerCoordinator.addDisplayer(mapByCountry);
        displayerCoordinator.addDisplayer(tableAll);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        displayerCoordinator.drawAll();
    }

    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }
}
