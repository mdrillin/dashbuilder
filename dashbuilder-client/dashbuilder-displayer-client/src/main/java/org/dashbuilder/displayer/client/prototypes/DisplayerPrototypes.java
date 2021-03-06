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
package org.dashbuilder.displayer.client.prototypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.displayer.DisplayerType;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

import static org.dashbuilder.displayer.client.prototypes.DataSetPrototypes.*;

@ApplicationScoped
public class DisplayerPrototypes {

    public static DisplayerPrototypes get() {
        Collection<IOCBeanDef<DisplayerPrototypes>> beans = IOC.getBeanManager().lookupBeans(DisplayerPrototypes.class);
        IOCBeanDef<DisplayerPrototypes> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }

    @Inject
    DataSetPrototypes dataSetPrototypes;

    private Map<DisplayerType,DisplayerSettings> prototypeMap = new HashMap<DisplayerType,DisplayerSettings>();

    @PostConstruct
    private void init() {
        prototypeMap.put(DisplayerType.PIECHART, DisplayerSettingsFactory
                .newPieChartSettings()
                .uuid("pieChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .width(300).height(300)
                .margins(0, 0, 0, 0)
                .set3d(false)
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.BARCHART, DisplayerSettingsFactory
                .newBarChartSettings()
                .uuid("barChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .width(500).height(250)
                .margins(10, 20, 90, 50)
                .horizontal().set3d(false)
                .legendOff()
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.LINECHART, DisplayerSettingsFactory
                .newLineChartSettings()
                .uuid("lineChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .width(500).height(300)
                .margins(10, 40, 90, 10)
                .legendOff()
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.AREACHART, DisplayerSettingsFactory
                .newAreaChartSettings()
                .uuid("areaChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .width(500).height(300)
                .margins(10, 40, 90, 40)
                .legendOff()
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.BUBBLECHART, DisplayerSettingsFactory
                .newBubbleChartSettings()
                .uuid("bubbleChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulationExt())
                .title("Population per Continent")
                .titleVisible(false)
                .width(500).height(300)
                .margins(10, 30, 50, 50)
                .legendOff()
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.METERCHART, DisplayerSettingsFactory
                .newMeterChartSettings()
                .uuid("meterChartPrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .width(400).height(300)
                .margins(10, 10, 10, 10)
                .meter(0, 1000000000L, 3000000000L, 6000000000L)
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.MAP, DisplayerSettingsFactory
                .newMapChartSettings()
                .uuid("mapChartPrototype")
                .dataset(dataSetPrototypes.getCountryPopulation())
                .title("World Population")
                .titleVisible(false)
                .width(500).height(300)
                .margins(10, 10, 10, 10)
                .filterOn(false, true, true)
                .buildSettings());

        prototypeMap.put(DisplayerType.TABLE, DisplayerSettingsFactory
                .newTableSettings()
                .uuid("tablePrototype")
                .dataset(dataSetPrototypes.getContinentPopulation())
                .title("Population per Continent")
                .titleVisible(false)
                .tableOrderEnabled(true)
                .tableOrderDefault(POPULATION, SortOrder.DESCENDING)
                .tablePageSize(8)
                .filterOn(false, true, true)
                .buildSettings());
    }

    public DisplayerSettings getProto(DisplayerType type) {
        return prototypeMap.get(type);
    }
}
