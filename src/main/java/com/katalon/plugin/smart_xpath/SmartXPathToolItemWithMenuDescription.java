package com.katalon.plugin.smart_xpath;

import java.util.Set;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.katalon.platform.api.exception.ResourceException;
import com.katalon.platform.api.extension.ToolItemWithMenuDescription;
import com.katalon.platform.api.model.Entity;
import com.katalon.platform.api.preference.PluginPreference;
import com.katalon.platform.api.service.ApplicationManager;
import com.katalon.plugin.smart_xpath.constant.SmartXPathConstants;
import com.katalon.plugin.smart_xpath.controller.AutoHealingController;
import com.katalon.plugin.smart_xpath.dialog.AutoHealingDialog;
import com.katalon.plugin.smart_xpath.entity.BrokenTestObject;
import com.katalon.plugin.smart_xpath.entity.BrokenTestObjects;

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
			Entity currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
			if (currentProject != null) {
				AutoHealingController.createXPathFilesIfNecessary(currentProject);
				PluginPreference preferenceStore = ApplicationManager.getInstance().getPreferenceManager()
						.getPluginPreference(currentProject.getId(), "com.katalon.katalon-studio-smart-xpath");
				if (preferenceStore.getBoolean("SmartXPathEnabled", false)) {
					addDisableSmartXPathMenuItem(newMenu, true);
				} else {
					addEnableSmartXPathMenuItem(newMenu, true);
				}
				addLoadAutoHealingEntitiesMenuItem(newMenu, true);
			}
		} catch (ResourceException e) {
			e.printStackTrace(System.out);
		}
	}

	private MenuItem addEnableSmartXPathMenuItem(Menu parentMenu, boolean enable) {
		smartXPathEnable = new MenuItem(parentMenu, SWT.PUSH);
		smartXPathEnable.setText("Smart XPath Enable");
		smartXPathEnable.setToolTipText("Enable Smart XPath");
		smartXPathEnable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// Retrieve PreferenceStore on click in case user installed
					// this plug-in when no project was opened
					Entity currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
					PluginPreference preferenceStore = ApplicationManager.getInstance().getPreferenceManager()
							.getPluginPreference(currentProject.getId(), "com.katalon.katalon-studio-smart-xpath");

					preferenceStore.setBoolean("SmartXPathEnabled", true);
					preferenceStore.save();
				} catch (ResourceException e1) {
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
				try {
					// Retrieve PreferenceStore again in case the user installed
					// the plugin when no project was opened
					Entity currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
					PluginPreference preferenceStore = ApplicationManager.getInstance().getPreferenceManager()
							.getPluginPreference(currentProject.getId(), "com.katalon.katalon-studio-smart-xpath");

					preferenceStore.setBoolean("SmartXPathEnabled", false);
					preferenceStore.save();
				} catch (ResourceException e1) {
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

				if (autoHealingDialog.open() == Window.OK) {
					Set<BrokenTestObject> approvedAutoHealingEntities = autoHealingDialog
							.getApprovedAutoHealingEntities();
					Set<BrokenTestObject> unapprovedAutoHealingEntities = autoHealingDialog
							.getUnapprovedAutoHealingEntities();

					Set<BrokenTestObject> approvedButUnableToHealEntities = AutoHealingController
							.autoHealBrokenTestObjects(parent.getShell(), approvedAutoHealingEntities);

					Entity projectEntity = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
					if (projectEntity != null) {
						// Append approved broken test objects to approved.json
						String pathToApprovedJson = projectEntity.getFolderLocation()
								+ SmartXPathConstants.APPROVED_FILE_SUFFIX;
						BrokenTestObjects brokenTestObjectsInApprovedJson = AutoHealingController
								.readExistingBrokenTestObjects(pathToApprovedJson);
						brokenTestObjectsInApprovedJson.getBrokenTestObjects().addAll(approvedAutoHealingEntities);
						AutoHealingController.writeBrokenTestObjects(brokenTestObjectsInApprovedJson,
								pathToApprovedJson);

						// Replace the content of waiting-for-approval.json with
						// unapproved entities
						String pathToWaitingForApprovalJson = projectEntity.getFolderLocation()
								+ SmartXPathConstants.WAITING_FOR_APPROVAL_FILE_SUFFIX;
						BrokenTestObjects brokenTestObjectsInWaitingForApprovalJson = AutoHealingController
								.readExistingBrokenTestObjects(pathToWaitingForApprovalJson);
						unapprovedAutoHealingEntities.addAll(approvedButUnableToHealEntities);
						brokenTestObjectsInWaitingForApprovalJson.setBrokenTestObjects(unapprovedAutoHealingEntities);
						AutoHealingController.writeBrokenTestObjects(brokenTestObjectsInWaitingForApprovalJson,
								pathToWaitingForApprovalJson);
					}

				}
			}
		});
		return autoHealing;
	}

	@Override
	public String iconUrl() {
		return "platform:/plugin/com.katalon.katalon-studio-smart-xpath/icons/smart-xpath-32x24.png";
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
