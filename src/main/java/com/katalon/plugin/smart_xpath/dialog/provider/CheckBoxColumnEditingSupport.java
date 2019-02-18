package com.katalon.plugin.smart_xpath.dialog.provider;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

import com.katalon.plugin.smart_xpath.entity.BrokenTestObject;

public class CheckBoxColumnEditingSupport extends EditingSupport {

	private TableViewer tableViewer;

	public CheckBoxColumnEditingSupport(TableViewer viewer) {
		super(viewer);
		this.tableViewer = viewer;
	}

	@Override
	protected boolean canEdit(Object arg0) {
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object arg0) {
		return new CheckboxCellEditor(null, SWT.CHECK);
	}

	@Override
	protected Object getValue(Object arg0) {
		return (boolean) (((BrokenTestObject) arg0).getApproved());
	}

	@Override
	protected void setValue(Object arg0, Object arg1) {
		((BrokenTestObject) arg0).setApproved((boolean) arg1);
		tableViewer.refresh();
	}
}
