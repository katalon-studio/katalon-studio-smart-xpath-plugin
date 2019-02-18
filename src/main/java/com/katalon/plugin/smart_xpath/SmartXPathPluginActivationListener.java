package com.katalon.plugin.smart_xpath;

import com.katalon.platform.api.Plugin;
import com.katalon.platform.api.extension.PluginActivationListener;
import com.katalon.platform.api.model.Entity;
import com.katalon.platform.api.service.ApplicationManager;
import com.katalon.plugin.smart_xpath.controller.AutoHealingController;

public class SmartXPathPluginActivationListener implements PluginActivationListener {

	@Override
	public void afterActivation(Plugin plugin) {
		Entity projectEntity = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
		if (projectEntity != null) {
			AutoHealingController.createXPathFilesIfNecessary(projectEntity);
		}
	}

}
