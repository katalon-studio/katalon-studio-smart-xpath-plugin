package com.katalon.plugin.smart_xpath;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.katalon.platform.api.extension.ui.toolbar.ToolItemWithMenuDescription;

public class SmartXPathToolItemWithMenuDescription implements ToolItemWithMenuDescription {

	@Override
	public Menu getMenu(Control arg0) {
		Menu newMenu = new Menu(arg0);
		MenuItem smartXPathEnable = new MenuItem(newMenu, SWT.PUSH);
		smartXPathEnable.setText("Smart XPath Enable");
		smartXPathEnable.setToolTipText("Enable Smart XPath");
		smartXPathEnable.addSelectionListener(new SelectionAdapter(){
			 @Override
	            public void widgetSelected(SelectionEvent e) {
				 	IEclipsePreferences store = InstanceScope.INSTANCE.getNode("com.katalon.plugin.smart_xpath");
				 	store.putBoolean("SmartXPathEnabled", true);
	            }	
		});
		
		MenuItem smartXPathDisable = new MenuItem(newMenu, SWT.PUSH);
		smartXPathDisable.setText("Smart XPath Disable");
		smartXPathDisable.setToolTipText("Disable Smart XPath");
		smartXPathDisable.addSelectionListener(new SelectionAdapter(){
			 @Override
	            public void widgetSelected(SelectionEvent e) {
				 	IEclipsePreferences store = InstanceScope.INSTANCE.getNode("com.katalon.plugin.smart_xpath");
				 	store.putBoolean("SmartXPathEnabled", false);
	            }
		});
		
		return newMenu;
	}

	@Override
	public String iconUrl() {
		return "platform:/plugin/com.katalon.plugin.smart_xpath/icons/bug_16@2x.png";
	}

	@Override
	public String name() {
		return "Smart XPath";
	}

	@Override
	public String toolItemId() {
		return "com.katalon.plugin.smart_xpath.smartXpathToolItemWithDescription";
	}
	
}
