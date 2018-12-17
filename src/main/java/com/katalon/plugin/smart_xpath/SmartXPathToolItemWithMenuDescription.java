package com.katalon.plugin.smart_xpath;

import java.io.IOException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.katalon.platform.api.extension.ui.toolbar.ToolItemWithMenuDescription;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.preference.PluginPreferenceStore;
import com.katalon.platform.api.service.ApplicationManager;

public class SmartXPathToolItemWithMenuDescription implements ToolItemWithMenuDescription {
	private Menu newMenu;
	private MenuItem smartXPathEnable;
	private MenuItem smartXPathDisable;
	private MenuItem autoHealing;
	private Control parent;

	@Override
	public Menu getMenu(Control arg0) {
		parent = arg0;
		newMenu = new Menu(arg0);
		evaluateAndAddMenuItem(newMenu);
		// This is intentional, updating static menu's item is troublesome, so
		// I'd display MenuItem on clicking on ToolItem
		return null;
	}

	@Override
	public void defaultEventHandler() {
		if (newMenu != null) {
			evaluateAndAddMenuItem(newMenu);
			// Display menu at the mouse position (guaranteed to be within the
			// ToolItem icon)
			newMenu.setVisible(true);
		}
	}

	private void evaluateAndAddMenuItem(Menu newMenu) {
		// Dispose all items
		for (MenuItem item : newMenu.getItems()) {
			item.dispose();
		}
		smartXPathEnable = null;
		smartXPathDisable = null;

		// Re-evaluate the PreferenceStore and add the appropriate menu item
		try {
			ProjectEntity currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
			if (currentProject != null) {
				PluginPreferenceStore preferenceStore = new PluginPreferenceStore(currentProject.getFolderLocation(),
						"com.katalon.plugin.smart_xpath");
				if (preferenceStore.getBoolean("SmartXPathEnabled", false)) {
					addDisableSmartXPathMenuItem(newMenu, true);
				} else {
					addEnableSmartXPathMenuItem(newMenu, true);
				}
				addLoadAutoHealingEntitiesMenuItem(newMenu, true);
			} else {
				addLoadAutoHealingEntitiesMenuItem(newMenu, false);
			}
		} catch (IOException e2) {
			e2.printStackTrace(System.out);
		}
	}

	private MenuItem addEnableSmartXPathMenuItem(Menu parentMenu, boolean enable) {
		smartXPathEnable = new MenuItem(parentMenu, SWT.PUSH);
		smartXPathEnable.setText("Smart XPath Enable");
		smartXPathEnable.setToolTipText("Enable Smart XPath");
		smartXPathEnable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Retrieve PreferenceStore on click in case user installed this
				// plug-in when no project was opened
				ProjectEntity currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
				PluginPreferenceStore preferenceStore = new PluginPreferenceStore(currentProject.getFolderLocation(),
						"com.katalon.plugin.smart_xpath");
				try {
					preferenceStore.setProperty("SmartXPathEnabled", true);
				} catch (IOException e1) {
					e1.printStackTrace(System.out);
				}
			}
		});
		return smartXPathEnable;
	}

	private MenuItem addDisableSmartXPathMenuItem(Menu parentMenu, boolean enable) {
		smartXPathDisable = new MenuItem(parentMenu, SWT.PUSH);
		smartXPathDisable.setText("Smart XPath Disable");
		smartXPathDisable.setToolTipText("Disable Smart XPath");
		smartXPathDisable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Retrieve PreferenceStore again in case the user installed the
				// plugin when no project was opened
				ProjectEntity currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
				PluginPreferenceStore preferenceStore = new PluginPreferenceStore(currentProject.getFolderLocation(),
						"com.katalon.plugin.smart_xpath");
				try {
					preferenceStore.setProperty("SmartXPathEnabled", false);
				} catch (IOException e1) {
					e1.printStackTrace(System.out);
				}
			}
		});
		return smartXPathDisable;
	}

	private MenuItem addLoadAutoHealingEntitiesMenuItem(Menu parentMenu, boolean enable) {
		autoHealing = new MenuItem(parentMenu, SWT.PUSH);
		autoHealing.setText("XPath Auto-healing Logs");
		autoHealing.setEnabled(enable);
		autoHealing.setToolTipText("Approve or reject Smart XPath auto-healing effect on failed locators");
		autoHealing.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AutoHealingDialog autoHealingDialog = new AutoHealingDialog(parent.getShell());
				if (autoHealingDialog.open() == IDialogConstants.OK_ID) {
					AutoHealer.autoHealBrokenTestObjects(autoHealingDialog.getApprovedAutoHealingEntities());
				}
			}
		});
		return autoHealing;
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
