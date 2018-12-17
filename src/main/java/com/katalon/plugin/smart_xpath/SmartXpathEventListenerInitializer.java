package com.katalon.plugin.smart_xpath;

import java.io.File;
import org.osgi.service.event.Event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
					createSmartXPathFolder(projectEntity);
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
			createSmartXPathFolder(projectEntity);
		}
	}

	@Override
	public void onUninstall(String arg0) {

	}

	public void createSmartXPathFolder(ProjectEntity projectEntity) {
		try {
			System.out.println("Searching for /smart_xpath folder");
			String smartXPathDir = StringUtils.getStandardPath(projectEntity.getFolderLocation() + "/smart_xpath");
			boolean smartXPathFolderExists = new File(smartXPathDir).isDirectory();
			boolean createdSmartXPathFolder = new File(smartXPathDir).mkdirs();
			boolean createdAutoHealingJsonFile = false;
			if (createdSmartXPathFolder || smartXPathFolderExists) {
				System.out.println("/smart_xpath folder exists, attempting to create auto-healing.json");
				String fnAutoHealing = StringUtils.getStandardPath(smartXPathDir + "/auto-healing.json");
				File autoHealingFile = new File(fnAutoHealing);
				createdAutoHealingJsonFile = autoHealingFile.createNewFile();
				if (createdAutoHealingJsonFile) {
					System.out.println("auto-healing.json was created");
					BrokenTestObjects emptyBrokenTestObjects = new BrokenTestObjects();
					ObjectMapper mapper = new ObjectMapper();
					mapper.enable(SerializationFeature.INDENT_OUTPUT);
					mapper.writeValue(autoHealingFile, emptyBrokenTestObjects);
				} else {
					System.out.println("auto-healing.json already exists");
				}
			} else {
				System.out.println("/smart_xpath folder does not exist, no file is created");
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

}
