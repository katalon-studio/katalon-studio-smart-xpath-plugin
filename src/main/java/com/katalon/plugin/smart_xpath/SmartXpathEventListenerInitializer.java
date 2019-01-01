package com.katalon.plugin.smart_xpath;

import org.osgi.service.event.Event;

import com.katalon.platform.api.event.EventListener;
import com.katalon.platform.api.extension.EventListenerInitializer;
import com.katalon.platform.api.model.Entity;
import com.katalon.platform.internal.event.EventConstants;
import com.katalon.plugin.smart_xpath.controller.AutoHealingController;

public class SmartXpathEventListenerInitializer implements EventListenerInitializer {
	@Override
	public void registerListener(EventListener eventListener) {
		eventListener.on(Event.class, event -> {
			try {
				System.out.println(event.getTopic());
				if (event.getTopic().equals("KATALON_PLUGIN/CURRENT_PROJECT_CHANGED")) {
					Entity projectEntity = (Entity) event.getProperty(EventConstants.EVENT_DATA_PROPERTY_NAME);
					AutoHealingController.createXPathFilesIfNecessary(projectEntity);
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});
	}
}
