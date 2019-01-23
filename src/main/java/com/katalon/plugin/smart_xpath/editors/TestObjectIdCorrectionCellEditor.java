package com.katalon.plugin.smart_xpath.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import com.katalon.plugin.smart_xpath.dialog.TestObjectIdCorrectiongDialog;
import com.katalon.plugin.smart_xpath.entity.BrokenTestObject;

public class TestObjectIdCorrectionCellEditor extends AbstractDialogCellEditor {

	String defaulTestObjectId = "";
	
	public TestObjectIdCorrectionCellEditor(Composite parent, String defaultTestObjectId) {
		super(parent);
		this.defaulTestObjectId = defaultTestObjectId;
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		TestObjectIdCorrectiongDialog dialog = new TestObjectIdCorrectiongDialog(Display.getDefault().getActiveShell());
		if (dialog.open() == TestObjectIdCorrectiongDialog.OK) {
			String selectedTestObjectId =  dialog.getSelectedTestObjectId();
			System.out.println(selectedTestObjectId + " is selected from Test Object ID Correction Dialog");
			if(selectedTestObjectId.equals("")){
				return defaulTestObjectId;
			}
			return selectedTestObjectId;
		}
		return defaulTestObjectId;
	}

	@Override
	protected void updateContents(Object value) {
		String testObjectId = "";
		if (value instanceof BrokenTestObject) {
			testObjectId = ((BrokenTestObject) value).getTestObjectId();
		}
		super.updateContents(testObjectId);
	}
}
