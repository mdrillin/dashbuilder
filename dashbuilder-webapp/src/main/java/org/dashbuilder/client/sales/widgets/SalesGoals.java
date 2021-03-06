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
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerHelper;

import static org.dashbuilder.shared.sales.SalesConstants.*;
import static org.dashbuilder.dataset.group.DateIntervalType.*;
import static org.dashbuilder.dataset.sort.SortOrder.*;
import static org.dashbuilder.dataset.group.AggregateFunctionType.*;

/**
 * A composite widget that represents an entire dashboard sample composed using an UI binder template.
 * <p>The dashboard itself is composed by a set of Displayer instances.</p>
 */
public class SalesGoals extends Composite {

    interface SalesDashboardBinder extends UiBinder<Widget, SalesGoals>{}
    private static final SalesDashboardBinder uiBinder = GWT.create(SalesDashboardBinder.class);

    @UiField(provided = true)
    Displayer meterChartAmount;

    @UiField(provided = true)
    Displayer lineChartByDate;

    @UiField(provided = true)
    Displayer barChartByProduct;

    @UiField(provided = true)
    Displayer barChartByEmployee;

    @UiField(provided = true)
    Displayer bubbleByCountry;

    DisplayerCoordinator displayerCoordinator = new DisplayerCoordinator();

    public String getTitle() {
        return "Sales goals";
    }

    public SalesGoals() {

        // Create the chart definitions

        meterChartAmount = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newMeterChartSettings()
                .dataset(SALES_OPPS)
                .column(AMOUNT, SUM, "Total amount")
                .title("Sales goal")
                .titleVisible(true)
                .width(200).height(200)
                .meter(0, 15000000, 25000000, 35000000)
                .filterOn(false, true, true)
                .buildSettings());

        lineChartByDate = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newLineChartSettings()
                .dataset(SALES_OPPS)
                .group(CLOSING_DATE).dynamic(80, MONTH, true)
                .column(CLOSING_DATE, "Closing date")
                .column(AMOUNT, SUM, "Total amount")
                .column(EXPECTED_AMOUNT, SUM, "Expected amount")
                .title("Expected pipeline")
                .titleVisible(true)
                .width(800).height(200)
                .margins(10, 80, 80, 100)
                .filterOn(false, true, true)
                .buildSettings());

        barChartByProduct = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                .dataset(SALES_OPPS)
                .group(PRODUCT)
                .column(PRODUCT, "Product")
                .column(AMOUNT, SUM, "Amount")
                .column(EXPECTED_AMOUNT, SUM, "Expected")
                .title("By product")
                .titleVisible(true)
                .width(400).height(150)
                .margins(10, 80, 80, 10)
                .vertical()
                .filterOn(false, true, true)
                .buildSettings());

        barChartByEmployee = DisplayerHelper.lookupDisplayer(
                DisplayerSettingsFactory.newBarChartSettings()
                .dataset(SALES_OPPS)
                .group(SALES_PERSON)
                .column(SALES_PERSON, "Employee")
                .column(AMOUNT, SUM, "Amount")
                .sort(AMOUNT, DESCENDING)
                .title("By employee")
                .titleVisible(true)
                .width(400).height(150)
                .margins(10, 80, 80, 10)
                .vertical()
                .filterOn(false, true, true)
                .buildSettings());

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
                .width(550).height(250)
                .margins(10, 30, 50, 0)
                .filterOn(false, true, true)
                .buildSettings());

        // Make the charts interact among them
        displayerCoordinator.addDisplayer(meterChartAmount);
        displayerCoordinator.addDisplayer(lineChartByDate);
        displayerCoordinator.addDisplayer(barChartByProduct);
        displayerCoordinator.addDisplayer(barChartByEmployee);
        displayerCoordinator.addDisplayer(bubbleByCountry);

        // Init the dashboard from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // Draw the charts
        displayerCoordinator.drawAll();
    }

    public void redrawAll() {
        displayerCoordinator.redrawAll();
    }
}
