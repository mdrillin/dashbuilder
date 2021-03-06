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

package org.dashbuilder.renderer.google.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface GoogleDisplayerConstants extends Messages {

    public static final GoogleDisplayerConstants INSTANCE = GWT.create( GoogleDisplayerConstants.class );

    public String googleDisplayer_initalizing();

    public String googleDisplayer_resetAnchor();

    public String googleTableDisplayer_gotoFirstPage();

    public String googleTableDisplayer_gotoPreviousPage();

    public String googleTableDisplayer_gotoNextPage();

    public String googleTableDisplayer_gotoLastPage();

    public String googleTableDisplayer_pages( String leftMostPageNumber, String rightMostPageNumber, String totalPages);

    public String googleTableDisplayer_rows( String from, String to, String totalRows);

    public String googleTableDisplayer_noData();

}
