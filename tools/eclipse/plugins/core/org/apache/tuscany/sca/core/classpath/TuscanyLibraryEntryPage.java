/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.core.classpath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A classpath container page for the Tuscany runtime library.
 *
 * @version $Rev$ $Date$
 */
public class TuscanyLibraryEntryPage extends WizardPage implements IClasspathContainerPage {

    private IClasspathEntry classpathEntry;

    public TuscanyLibraryEntryPage() {
        super("Tuscany");
    }

    public void createControl(Composite parent) {
        setTitle("Tuscany Library");

        Label label = new Label(parent, SWT.NONE);
        label.setText("Press Finish to add the Tuscany Library");
        label.setFont(parent.getFont());

        setControl(label);
    }

    public boolean finish() {
        classpathEntry = JavaCore.newContainerEntry(TuscanyClasspathContainer.TUSCANY_LIBRARY_CONTAINER);
        return true;
    }

    public boolean isPageComplete() {
        return true;
    }

    public IClasspathEntry getSelection() {
        return classpathEntry;
    }

    public void setSelection(IClasspathEntry containerEntry) {
        this.classpathEntry = containerEntry;
    }
}
