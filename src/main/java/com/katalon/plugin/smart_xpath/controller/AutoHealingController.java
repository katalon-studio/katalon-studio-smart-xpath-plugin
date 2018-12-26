package com.katalon.plugin.smart_xpath.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.katalon.platform.api.model.Entity;
import com.katalon.platform.api.service.ApplicationManager;
import com.katalon.plugin.smart_xpath.constant.SmartXPathConstants;
import com.katalon.plugin.smart_xpath.entity.BrokenTestObject;
import com.katalon.plugin.smart_xpath.entity.BrokenTestObjects;
import com.katalon.plugin.smart_xpath.util.StringUtils;

public class AutoHealingController {	
	public static boolean autoHealBrokenTestObjects(Shell shell, List<BrokenTestObject> approvedAutoHealingEntities) {
		try {
			new ProgressMonitorDialog(shell).run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Auto healing broken test objects ... ", 1);
					try {
						autoHealBrokenTestObjects(approvedAutoHealingEntities);
					} catch (XPathExpressionException | ParserConfigurationException | TransformerException
							| SAXException | IOException e) {
						e.printStackTrace(System.out);
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			return false;
		}
		return true;
	}
	
	private static void autoHealBrokenTestObjects(List<BrokenTestObject> approvedAutoHealingEntities)
			throws XPathExpressionException, ParserConfigurationException, TransformerException, SAXException, IOException {
		Entity currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
		if (currentProject != null) {
			String currentProjectDir = currentProject.getFolderLocation();
			for (BrokenTestObject brokenTestObject : approvedAutoHealingEntities) {
				String pathToThisTestObject = StringUtils
						.getStandardPath(currentProjectDir + "/" + brokenTestObject.getTestObjectId() + ".rs");
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(pathToThisTestObject);
				// Update the first XPATH value in selectorCollection (i.e default XPath value)
				XPath xPathToBrokenXPath = XPathFactory.newInstance().newXPath();
				Node nodeBrokenXPath = (Node) xPathToBrokenXPath
						.compile("//selectorCollection//key[text()='XPATH'][1]/following::value[1]")
						.evaluate(doc, XPathConstants.NODE);
				nodeBrokenXPath.setTextContent(brokenTestObject.getProposedXPath());

				Transformer tf = TransformerFactory.newInstance().newTransformer();
				tf.setOutputProperty(OutputKeys.INDENT, "yes");
				tf.setOutputProperty(OutputKeys.METHOD, "xml");
				tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

				DOMSource domSource = new DOMSource(doc);
				StreamResult sr = new StreamResult(pathToThisTestObject);
				tf.transform(domSource, sr);
				System.out.println("Updated " + brokenTestObject.getTestObjectId());
			}
		}
	}
	
	public static List<BrokenTestObject> readUnapprovedBrokenTestObjects() {
		try {
			Gson gson = new Gson();
			Entity projectEntity = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
			if (projectEntity != null) {
				String projectDir = projectEntity.getFolderLocation();
				String jsonAutoHealingDir = StringUtils.getStandardPath(projectDir + SmartXPathConstants.WAITING_FOR_APPROVAL_FILE_SUFFIX);
				JsonReader reader = new JsonReader(new FileReader(jsonAutoHealingDir));
				BrokenTestObjects brokenTestObjects = gson.fromJson(reader, BrokenTestObjects.class);
				List<BrokenTestObject> unapprovedBrokenTestObjects = brokenTestObjects.getBrokenTestObjects();
				// Remove potential threats
				unapprovedBrokenTestObjects.removeAll(Collections.singleton(null));
				return unapprovedBrokenTestObjects;
			} else {
				System.out.println("Current project directory is not detected, no project is open");
			}
		} catch (FileNotFoundException e) {
			System.out.println(SmartXPathConstants.WAITING_FOR_APPROVAL_FILE_SUFFIX + "is not detected, no broken test objects are loaded");
			e.printStackTrace(System.out);
		}
		return null;
	}
	
	/**
	 * Set content of the file to a BrokenTestObjects entity which consists of a list of BrokenTestObjects
	 */
	public static void writeToFilesWithBrokenObjects(List<BrokenTestObject> brokenTestObjectsToUpdate,
			String filePath) {
		try {
			BrokenTestObjects brokenTestObjects = new BrokenTestObjects();
			brokenTestObjects.setBrokenTestObjects(brokenTestObjectsToUpdate);
			File file = new File(filePath);
			if(file.exists()){
				ObjectMapper mapper = new ObjectMapper();
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				brokenTestObjectsToUpdate.stream().forEach(a -> System.out.println(a.getApproved()));
				mapper.writeValue(file, brokenTestObjects);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
	
    // Assume a JSON file with a JSON object containing at least a JSON array, this method
    // appends @arg1 to @arg2 by replacing "]}" with "@arg1]}"
    // Note that if the array is initially empty then ",@arg1" will be written in between,
    // thus at any given time [0] will be a null object
	public static void appendToFileWithBrokenObjects(List<BrokenTestObject> brokenTestObjectsToUpdate, String filePath) {
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
			// Set cursor to the position of "]"
			long pos = randomAccessFile.length();
			while (randomAccessFile.length() > 0) {
				pos--;
				randomAccessFile.seek(pos);
				if (randomAccessFile.readByte() == ']') {
					randomAccessFile.seek(pos);
					break;
				}
			}
			for(BrokenTestObject brokenTestObject : brokenTestObjectsToUpdate){
				Gson gson = new GsonBuilder().create();
				String jsonString = gson.toJson(brokenTestObject);
				randomAccessFile.writeBytes("\n," + jsonString + "\n");	
			}
			randomAccessFile.writeBytes("\n]\n}");
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	private static File createSmartXPathFile(Entity projectEntity, String fileName) {
		try {
			String smartXPathDir = StringUtils.getStandardPath(projectEntity.getFolderLocation() + SmartXPathConstants.SMART_XPATH_FOLDER_SUFFIX);
			boolean smartXPathFolderExists = new File(smartXPathDir).isDirectory();
			boolean createdSmartXPathFolder = new File(smartXPathDir).mkdirs();
			boolean createdAutoHealingJsonFile = false;
			if (createdSmartXPathFolder || smartXPathFolderExists) {
				String fnAutoHealing = StringUtils.getStandardPath(smartXPathDir + "/" + fileName + ".json");
				File autoHealingFile = new File(fnAutoHealing);
				createdAutoHealingJsonFile = autoHealingFile.createNewFile();
				if (createdAutoHealingJsonFile) {
					BrokenTestObjects emptyBrokenTestObjects = new BrokenTestObjects();
					ObjectMapper mapper = new ObjectMapper();
					mapper.enable(SerializationFeature.INDENT_OUTPUT);
					mapper.writeValue(autoHealingFile, emptyBrokenTestObjects);
					return autoHealingFile;
				}
			} else {
				System.out.println("/" + SmartXPathConstants.SMART_XPATH_FOLDER_SUFFIX + " folder does not exist, no file is created");
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private static boolean removeFile(File fileToRemove){
		try {
			return Files.deleteIfExists(fileToRemove.toPath());
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		return false;
	}
	
	public static void createNecessarySmartXPathFiles(Entity projectEntity){
		createSmartXPathFile(projectEntity, "waiting-for-approval");
		createSmartXPathFile(projectEntity, "approved");
	}
}
