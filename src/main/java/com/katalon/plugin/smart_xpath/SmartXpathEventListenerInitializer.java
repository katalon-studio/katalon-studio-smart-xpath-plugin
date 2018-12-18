package com.katalon.plugin.smart_xpath;

import org.osgi.service.event.Event;

import com.katalon.platform.api.extension.event.EventListener;
import com.katalon.platform.api.extension.event.EventListenerInitializer;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.service.ApplicationManager;
import com.katalon.platform.api.service.EventConstants;

public class SmartXpathEventListenerInitializer implements EventListenerInitializer {
	@Override
	public void registerListener(EventListener eventListener) {
		eventListener.on(Event.class, event -> {
			try {

				if (event.getTopic().equals("KATALON_PLUGIN/CURRENT_PROJECT_CHANGED")) {
					ProjectEntity projectEntity = (ProjectEntity) event
							.getProperty(EventConstants.EVENT_DATA_PROPERTY_NAME);
					AutoHealingController.createNecessarySmartXPathFiles(projectEntity);
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});
	}

	@Override
	public void onInstall(String arg0) {
		ProjectEntity projectEntity = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
		if (projectEntity != null) {
			AutoHealingController.createNecessarySmartXPathFiles(projectEntity);
		}
	}

	@Override
	public void onUninstall(String arg0) {
		
	}

}
