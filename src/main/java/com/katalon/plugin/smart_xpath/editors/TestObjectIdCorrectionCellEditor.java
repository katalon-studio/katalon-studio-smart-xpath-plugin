package com.katalon.plugin.smart_xpath.editors;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.katalon.plugin.smart_xpath.dialog.TestObjectIdCorrectiongDialog;
import com.katalon.plugin.smart_xpath.entity.BrokenTestObject;

public class TestObjectIdCorrectionCellEditor extends DialogCellEditor {

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		TestObjectIdCorrectiongDialog dialog = new TestObjectIdCorrectiongDialog(Display.getDefault().getActiveShell());
		if(dialog.open() == TestObjectIdCorrectiongDialog.OK){
			return dialog.getSelectedTestObjectId();
		}
		return "";
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
