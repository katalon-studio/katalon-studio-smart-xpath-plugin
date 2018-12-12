package com.katalon.plugin.smart_xpath;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import com.katalon.platform.api.extension.ui.toolbar.ToolItemWithMenuDescription;
import com.katalon.platform.api.model.Entity;
import com.katalon.platform.api.preference.PluginPreferenceStore;
import com.katalon.platform.api.service.ApplicationManager;

public class SmartXPathToolItemWithMenuDescription implements ToolItemWithMenuDescription {
	private Menu newMenu;
	private MenuItem smartXPathEnable;
	private MenuItem smartXPathDisable;
	@Override
	public Menu getMenu(Control arg0) {
		newMenu = new Menu(arg0);
		// Evaluate and add menu items in case user installs this plug-in inside an opened project
		evaluateAndAddMenuItem(newMenu);
		return newMenu;
	}
	
	private void evaluateAndAddMenuItem(Menu newMenu){
		// Dispose all items
		for(MenuItem item : newMenu.getItems()){
			item.dispose();
		}
		smartXPathEnable = null;
		smartXPathDisable = null;
		
		// Re-evaluate the PreferenceStore and add the appropriate menu item
		try {
			Entity currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
			if(currentProject != null){
				PluginPreferenceStore preferenceStore = new PluginPreferenceStore(currentProject.getFolderLocation(),
						"com.katalon.plugin.smart_xpath");
				if (preferenceStore.getBoolean("SmartXPathEnabled", false)) {
					addDisableSmartXPathMenuItem(newMenu);
				} else {
					addEnableSmartXPathMenuItem(newMenu);
				}
			} else {
				System.out.println("Open/create a project to start using Smart XPath");
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	private MenuItem addEnableSmartXPathMenuItem(Menu parentMenu){
		smartXPathEnable = new MenuItem(parentMenu, SWT.PUSH);
		smartXPathEnable.setText("Smart XPath Enable");
		smartXPathEnable.setToolTipText("Enable Smart XPath");
		smartXPathEnable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Retrieve PreferenceStore again in case the user installed the plugin when no project was opened
				Entity currentProject = ApplicationManager.getInstance().getProjectManager()
						.getCurrentProject();
				PluginPreferenceStore preferenceStore = new PluginPreferenceStore(
						currentProject.getFolderLocation(), "com.katalon.plugin.smart_xpath");
				try {
					preferenceStore.setProperty("SmartXPathEnabled", true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		return smartXPathEnable;
	}
	
	private MenuItem addDisableSmartXPathMenuItem(Menu parentMenu){
		smartXPathDisable = new MenuItem(parentMenu, SWT.PUSH);
		smartXPathDisable.setText("Smart XPath Disable");
		smartXPathDisable.setToolTipText("Disable Smart XPath");
		smartXPathDisable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Retrieve PreferenceStore again in case the user installed the plugin when no project was opened
				Entity currentProject = ApplicationManager.getInstance().getProjectManager()
						.getCurrentProject();
				PluginPreferenceStore preferenceStore = new PluginPreferenceStore(
						currentProject.getFolderLocation(), "com.katalon.plugin.smart_xpath");
				try {
					preferenceStore.setProperty("SmartXPathEnabled", false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		return smartXPathDisable;
	}
	
	@Override
	public void defaultEventHandler(){
		if(newMenu != null){
			evaluateAndAddMenuItem(newMenu);
			// Display menu at the mouse position (guaranteed to be within the ToolItem icon)
			newMenu.setVisible(true);
		}
	}

	@Override
	public String iconUrl() {
		return "platform:/plugin/com.katalon.katalon-studio-smart-xpath/icons/bug_16@2x.png";
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
