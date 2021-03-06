/*
   Copyright (c) 2014,2015 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.charts.client.pie;

import com.ait.lienzo.charts.client.AbstractChart;
import com.ait.lienzo.charts.client.ChartAttribute;
import com.ait.lienzo.charts.client.ChartNodeType;
import com.ait.lienzo.charts.client.model.DataTable;
import com.ait.lienzo.charts.client.pie.event.DataReloadedEvent;
import com.ait.lienzo.charts.client.pie.event.DataReloadedEventHandler;
import com.ait.lienzo.charts.client.pie.event.ValueSelectedEvent;
import com.ait.lienzo.charts.client.pie.event.ValueSelectedHandler;
import com.ait.lienzo.client.core.animation.*;
import com.ait.lienzo.client.core.event.*;
import com.ait.lienzo.client.core.shape.*;
import com.ait.lienzo.client.core.shape.json.IFactory;
import com.ait.lienzo.client.core.shape.json.validators.ValidationContext;
import com.ait.lienzo.client.core.shape.json.validators.ValidationException;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import java.util.LinkedList;
import java.util.List;

public class PieChart extends AbstractChart<PieChart>
{
    private Group slices = new Group();
    private Group labels = new Group();
    private List<Line> lines = new LinkedList<Line>();
    private List<Text> texts = new LinkedList<Text>();
    private List<PieSlice> pieSlices = new LinkedList<PieSlice>();
    
    private static final ColorName[] DEFAULT_SLICE_COLORS = new ColorName[] {
        ColorName.DEEPPINK, ColorName.YELLOW, ColorName.SALMON, ColorName.CORNFLOWERBLUE,
            ColorName.AQUA, ColorName.DEEPSKYBLUE.GREENYELLOW, ColorName.BLUEVIOLET,
            ColorName.FUCHSIA, ColorName.MAGENTA, ColorName.MAROON
    };

    protected PieChart(JSONObject node, ValidationContext ctx) throws ValidationException
    {
        super(node, ctx);

        setNodeType(ChartNodeType.PIE_CHART);

    }

    public PieChart(DataReloadedEventHandler dataReloadedEventHandler)
    {
        setNodeType(ChartNodeType.PIE_CHART);

        if (dataReloadedEventHandler != null) addDataReloadedHandler(dataReloadedEventHandler);
        
        getMetaData().put("creator", "Dean S. Jones").put("version", "1.0.1.SNAPSHOT");

    }

    public HandlerRegistration addDataReloadedHandler(DataReloadedEventHandler handler)
    {
        return addEnsureHandler(DataReloadedEvent.TYPE, handler);
    }

    public HandlerRegistration addValueSelectedHandler(ValueSelectedHandler handler)
    {
        return addEnsureHandler(ValueSelectedEvent.TYPE, handler);
    }

    @Override
    protected void doBuild() {
        PieChartData data = getData();

        if (getRadius(getChartWidth(), getChartHeight()) <= 0 || (null == data) || (data.size() < 1))
        {
            return;
        }

        _build(data);

        // Apply position and size to inner shapes. 
        redraw(getChartWidth(), getChartHeight(), true);

        // Add the attributes event change handlers.
        this.addAttributesChangedHandler(ChartAttribute.PIE_CHART_DATA, new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                redraw(getChartWidth(), getChartHeight(), true);
            }
        });

        AttributesChangedHandler whhandler = new AttributesChangedHandler() {
            @Override
            public void onAttributesChanged(AttributesChangedEvent event) {
                if (!isReloading[0]) {
                    redraw(getChartWidth(), getChartHeight(), false);
                }
            }
        };

        this.addAttributesChangedHandler(ChartAttribute.WIDTH, whhandler);
        this.addAttributesChangedHandler(ChartAttribute.HEIGHT,whhandler);
    }
    
    private void _build(PieChartData data) {
        final DataTable dataTable = data.getDataTable();
        final String[] categories = dataTable.getColumn(getData().getCategoriesProperty()).getStringValues();
        final Double[] values = dataTable.getColumn(getData().getValuesProperty()).getNumericValues();

        labels.setListening(false);

        for (int i = 0; i < values.length; i++)
        {
            final PieSlice slice = new PieSlice(0, 0, 0);

            final int index = i;
            slice.addNodeMouseClickHandler(new NodeMouseClickHandler() {
                @Override
                public void onNodeMouseClick(NodeMouseClickEvent event) {
                    GWT.log("PieChart - filtering on "  + categories[index] + "/" + index);
                    PieChart.this.fireEvent(new ValueSelectedEvent(getData().getCategoriesProperty(), index));
                }
            });

            slice.setFillColor(getColor(i)).setStrokeColor(ColorName.BLACK).setStrokeWidth(3);

            slice.addNodeMouseEnterHandler(new NodeMouseEnterHandler()
            {
                @Override
                public void onNodeMouseEnter(NodeMouseEnterEvent event)
                {
                    if (false == slice.isAnimating())
                    {
                        slice.setAnimating(true);

                        slice.setListening(false);

                        slice.getLayer().batch();

                        slice.animate(AnimationTweener.LINEAR, AnimationProperties.toPropertyList(AnimationProperty.Properties.SCALE(1.3, 1.3)), 333, new AnimationCallback()
                        {
                            @Override
                            public void onClose(IAnimation animation, IAnimationHandle handle)
                            {
                                slice.animate(AnimationTweener.LINEAR, AnimationProperties.toPropertyList(AnimationProperty.Properties.SCALE(1, 1)), 333, new AnimationCallback()
                                {
                                    @Override
                                    public void onClose(IAnimation animation, IAnimationHandle handle)
                                    {
                                        slice.setScale(null);

                                        slice.setListening(true);

                                        slice.setAnimating(false);

                                        slice.getLayer().batch();
                                    }
                                });
                            }
                        });
                    }
                }
            });
            pieSlices.add(slice);
            slices.add(slice);

            Text text = new Text("", getFontFamily(), getFontStyle(), getFontSize()).setFillColor(ColorName.BLACK).setTextBaseLine(TextBaseLine.MIDDLE).setAlpha(0d);
            texts.add(text);

            Line line = new Line(0, 0, 0, 0).setStrokeColor(ColorName.BLACK).setStrokeWidth(3).setAlpha(0d);
            lines.add(line);

            labels.add(text);

            labels.add(line);

        }

        addOnAreaChartCentered(labels);

        addOnAreaChartCentered(slices);
    }

    protected PieChart redraw(final Double chartWidth, final Double chartHeight, final boolean animate) {

        if (getData() == null) {
            GWT.log("No data");
            return this;
        }

        // Redraw shapes provided by super class.
        super.redraw(chartWidth, chartHeight, animate);
        
        if (lines == null && texts == null && pieSlices == null) {
            lines = new LinkedList<Line>();
            texts = new LinkedList<Text>();
            pieSlices = new LinkedList<PieSlice>();
            _build(getData());
        }
        
        double radius = getRadius(chartWidth, chartHeight);

        PieChartData data = getData();

        if (radius <= 0 || (null == data) || (data.size() < 1))
        {
            return this;
        }

        DataTable dataTable = data.getDataTable();
        String[] categories = dataTable.getColumn(getData().getCategoriesProperty()).getStringValues();
        Double[] values = dataTable.getColumn(getData().getValuesProperty()).getNumericValues();

        double sofar = 0;

        double total = 0;

        for (int i = 0; i < values.length; i++)
        {
            total += values[i];
        }

        labels.setListening(false);

        for (int i = 0; i < values.length; i++)
        {
            double value = values[i] / total;

            PieSlice slice  = pieSlices.get(i);
            if (slice != null) {
                double startAngle = PieSlice.buildStartAngle(sofar);
                double endAngle = PieSlice.buildEngAngle(sofar, value);
                // slice.setRadius(radius).setStartAngle(startAngle).setEndAngle(endAngle);
                setShapeAttributes(slice, radius, startAngle, endAngle, animate);
            } else {
                // TODO: New data values added.
            }

            double s_ang = Math.PI * (2.0 * sofar);

            double e_ang = Math.PI * (2.0 * (sofar + value));

            double n_ang = (s_ang + e_ang) / 2.0;

            if (n_ang > (Math.PI * 2.0))
            {
                n_ang = n_ang - (Math.PI * 2.0);
            }
            else if (n_ang < 0)
            {
                n_ang = n_ang + (Math.PI * 2.0);
            }
            double lx = Math.sin(n_ang) * (radius + 50);

            double ly = 0 - Math.cos(n_ang) * (radius + 50);

            TextAlign align;

            if (n_ang <= (Math.PI * 0.5))
            {
                lx += 2;

                align = TextAlign.LEFT;
            }
            else if ((n_ang > (Math.PI * 0.5)) && (n_ang <= Math.PI))
            {
                lx += 2;

                align = TextAlign.LEFT;
            }
            else if ((n_ang > Math.PI) && (n_ang <= (Math.PI * 1.5)))
            {
                lx -= 2;

                align = TextAlign.RIGHT;
            }
            else
            {
                lx -= 2;

                align = TextAlign.RIGHT;
            }
            String label = categories[i];

            if (null == label)
            {
                label = "";
            }
            else
            {
                label = label + " ";
            }
            Text text = texts.get(i);
            if (text != null) {
                text.setText(label + getLabel(value * 100)).setTextAlign(align);
                setShapeAttributes(text, lx, ly, null, null, null, 1d, animate);
            } else {
                // TODO: New data values added.
            }

            Line line = lines.get(i);
            if (line != null) {
                Point2D startPoint = new Point2D((Math.sin(n_ang) * radius), 0 - (Math.cos(n_ang) * radius));
                Point2D endPoint = new Point2D((Math.sin(n_ang) * (radius + 50)), 0 - (Math.cos(n_ang) * (radius + 50)));
                line.setPoints(new Point2DArray(startPoint, endPoint)).setAlpha(0);
                setShapeAttributes(line, 1d, animate);
            } else {
                // TODO: New data values added.
            }

            sofar += value;
        }

        addOnAreaChartCentered(labels);
        addOnAreaChartCentered(slices);

        return this;
    }

    protected void clear(final Runnable callback) {
        isReloading[0] = true;

        final List<IPrimitive> shapesToClear = new LinkedList<IPrimitive>();
        // Create the animation callback.
        IAnimationCallback animationCallback = new IAnimationCallback() {
            @Override
            public void onStart(IAnimation animation, IAnimationHandle handle) {

            }

            @Override
            public void onFrame(IAnimation animation, IAnimationHandle handle) {

            }

            @Override
            public void onClose(IAnimation animation, IAnimationHandle handle) {
                if (!shapesToClear.isEmpty()) shapesToClear.remove(0);
                if (shapesToClear.isEmpty()) {
                    slices.removeAll();
                    labels.removeAll();
                    callback.run();
                }
            }
        };

        // Apply animation of lines.
        if (lines != null) {
            for (Line line : lines) {
                if (line != null) shapesToClear.add(line);
            }
        }

        // Apply animation to texts.
        if (texts != null ) {
            for (Text text : texts) {
                if (text != null) shapesToClear.add(text);
            }
        }
        
        if (pieSlices != null) {
            for (PieSlice slice : pieSlices) {
                if (slice != null) shapesToClear.add(slice);
            }
        }

        // Create the animation properties.
        AnimationProperties animationProperties = new AnimationProperties();
        animationProperties.push(AnimationProperty.Properties.ALPHA(0d));

        // Apply animation of lines.
        if (lines != null) {
            for (Line line : lines) {
                if (line != null) line.animate(AnimationTweener.LINEAR, animationProperties, CLEAR_ANIMATION_DURATION, animationCallback);
            }
        }

        AnimationProperties animationProperties2 = new AnimationProperties();
        animationProperties2.push(AnimationProperty.Properties.X(0));
        animationProperties2.push(AnimationProperty.Properties.Y(0));
        animationProperties2.push(AnimationProperty.Properties.WIDTH(0));
        if (texts != null ) {
            for (Text text : texts) {
                if (text != null) text.animate(AnimationTweener.LINEAR, animationProperties2, CLEAR_ANIMATION_DURATION, animationCallback);
            }
        }

        AnimationProperties animationProperties3 = new AnimationProperties();
        animationProperties3.push(AnimationProperty.Properties.SCALE(0d));
        if (pieSlices != null) {
            for (PieSlice slice : pieSlices) {
                if (slice != null) slice.animate(AnimationTweener.LINEAR, animationProperties3, CLEAR_ANIMATION_DURATION, animationCallback);
            }
        }

        reset();
        clearAreas();

    }
    
    private void reset() {
        lines = null;
        texts = null;
        pieSlices = null;
        
    }
    
    protected void addOnAreaChartCentered(Group group) {
        chartArea.add(group);
        setGroupAttributes(group, getChartWidth() / 2, getChartHeight() / 2, 1d, false);
    }

    // TODO: Use color strategy.
    protected IColor getColor(int position) {
        int defaultColorsSize = DEFAULT_SLICE_COLORS.length;
        
        if (position < defaultColorsSize) {
            return DEFAULT_SLICE_COLORS[position];
        }
        
        return new Color(position * 20 , 128, 0);
    }

    public final PieChart setData(final PieChartData data)
    {
        // If new data contains different properties on axis, clear current shapes.
        if (isCleanRequired(getData(), data)) {
            clear(new Runnable() {
                @Override
                public void run() {
                    // Reset chart's inner shapes.
                    isReloading[0] = false;
                    _setData(data);
                }
            });
        } else {
            _setData(data);
        }
        
        return this;
    }

    private final PieChart _setData(PieChartData data)
    {
        if (null != data)
        {
            getAttributes().put(ChartAttribute.PIE_CHART_DATA.getProperty(), data.getJSO());
        }

        PieChart.this.fireEvent(new DataReloadedEvent(this));
        
        return this;
    }

    public final PieChartData getData()
    {
        if (getAttributes().isDefined(ChartAttribute.PIE_CHART_DATA)) {
            PieChartData.PieChartDataJSO jso = getAttributes().getObject(ChartAttribute.PIE_CHART_DATA.getProperty()).cast();
            return new PieChartData(jso);
        }

        return null;
    }

    private boolean isCleanRequired(PieChartData currentData, PieChartData newData) {
        if (currentData == null && newData == null) return false;
        if (currentData == null && newData != null) return false;
        if (newData == null && currentData != null) return true;
        String categoriesColumn = currentData.getCategoriesProperty();
        String newCategoriesColumn = newData.getCategoriesProperty();
        if (hasDataColumnChanged(categoriesColumn, newCategoriesColumn)) return true;
        String valuesColumn = currentData.getValuesProperty();
        String newValuesColumn = newData.getValuesProperty();
        return hasDataColumnChanged(valuesColumn, newValuesColumn);
    }
    
    private boolean hasDataColumnChanged(String oldColumn, String newColumn) {
        if (oldColumn == null && newColumn != null) return true;
        if (oldColumn == null && newColumn == null) return false;
        if (oldColumn != null && newColumn == null) return true;
        if (oldColumn != null && !oldColumn.equals(newColumn)) return true;
        return false;
    }

    private final double getRadius(double chartWidth, double chartHeight) {
        double forSize = chartHeight;
        if (getChartWidth() < forSize) forSize = chartWidth;

        return (forSize / 2) - 100;
    }

    private final native String getLabel(double perc)
    /*-{
        var numb = perc;

        return numb.toFixed(2) + "%";
    }-*/;

    @Override
    public double getChartHeight() {
        return getHeight();
    }

    @Override
    public double getChartWidth() {
        return getWidth();
    }

    @Override
    public JSONObject toJSONObject()
    {
        JSONObject object = new JSONObject();

        object.put("type", new JSONString(getNodeType().getValue()));

        if (false == getMetaData().isEmpty())
        {
            object.put("meta", new JSONObject(getMetaData().getJSO()));
        }
        object.put("attributes", new JSONObject(getAttributes().getJSO()));

        return object;
    }

    @Override
    public IFactory<Group> getFactory()
    {
        return new PieChartFactory();
    }

    public static class PieChartFactory extends ChartFactory
    {
        public PieChartFactory()
        {
            super();

            setTypeName(ChartNodeType.PIE_CHART.getValue());

            addAttribute(ChartAttribute.PIE_CHART_DATA, true);
        }

        @Override
        public boolean addNodeForContainer(IContainer<?, ?> container, Node<?> node, ValidationContext ctx) {
            return false;
        }

        @Override
        public PieChart create(JSONObject node, ValidationContext ctx) throws ValidationException
        {
            return new PieChart(node, ctx);
        }
    }

    private static class PieSlice extends Slice
    {
        private boolean m_animating = false;

        public PieSlice(double radius, double sofar, double value)
        {
            super(radius, Math.PI * (-0.5 + 2 * sofar), Math.PI * (-0.5 + 2 * (sofar + value)), false);
        }

        protected static double buildStartAngle(double sofar) {
            return Math.PI * (-0.5 + 2 * sofar);
        }

        protected static double buildEngAngle(double sofar, double value) {
            return Math.PI * (-0.5 + 2 * (sofar + value));
        }

        public final void setAnimating(boolean animating)
        {
            m_animating = animating;
        }

        public final boolean isAnimating()
        {
            return m_animating;
        }
    }
}