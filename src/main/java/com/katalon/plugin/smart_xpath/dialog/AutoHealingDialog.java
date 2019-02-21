package com.katalon.plugin.smart_xpath.dialog;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.katalon.plugin.smart_xpath.controller.AutoHealingController;
import com.katalon.plugin.smart_xpath.dialog.provider.CheckBoxColumnEditingSupport;
import com.katalon.plugin.smart_xpath.entity.BrokenTestObject;

public class AutoHealingDialog extends Dialog {
	protected Composite tablePropertyComposite;
	private String dialogTitle = "";
	private TableViewer tbViewer;
	private TableColumnLayout tableColumnLayout;
	private Table table;
	private Set<BrokenTestObject> unapprovedBrokenEntities;
	private Set<BrokenTestObject> approvedAutoHealingEntities;

	public AutoHealingDialog(Shell parentShell) {
		super(parentShell);
		unapprovedBrokenEntities = new HashSet<>();
		approvedAutoHealingEntities = new HashSet<>();
	}

	@Override
	public void create() {
		setShellStyle(SWT.DIALOG_TRIM);
		super.create();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		tablePropertyComposite = new Composite(parent, SWT.NONE);
		GridData ldTableComposite = new GridData(SWT.FILL, SWT.FILL, true, true);
		ldTableComposite.widthHint = 1000;
		ldTableComposite.heightHint = 380;
		tablePropertyComposite.setLayoutData(ldTableComposite);
		tableColumnLayout = new TableColumnLayout();
		tablePropertyComposite.setLayout(tableColumnLayout);
		
		tbViewer = new TableViewer(tablePropertyComposite,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);

		createColumns();

		tbViewer.setContentProvider(ArrayContentProvider.getInstance());
		loadAutoHealingEntities();

		table = tbViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tbViewer.setInput(unapprovedBrokenEntities);
		return tablePropertyComposite;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Smart XPath Auto Healing");
	}

	private void createColumns() {
		
		TableViewerColumn colObjectId = new TableViewerColumn(tbViewer, SWT.NONE);
		colObjectId.getColumn().setText("Test Object ID");
		colObjectId.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String testObjectId = ((BrokenTestObject) element).getTestObjectId();
				return testObjectId;
			}
		});

		TableViewerColumn colOldXPath = new TableViewerColumn(tbViewer, SWT.NONE);
		colOldXPath.getColumn().setText("Broken XPath");
		colOldXPath.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String oldXPath = ((BrokenTestObject) element).getBrokenXPath();
				return oldXPath;
			}
		});

		TableViewerColumn colNewXPath = new TableViewerColumn(tbViewer, SWT.NONE);
		colNewXPath.getColumn().setText("Proposed XPath");
		colNewXPath.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String newXPath = ((BrokenTestObject) element).getProposedXPath();
				return newXPath;
			}
		});

		TableViewerColumn colApproveNewXPath = new TableViewerColumn(tbViewer, SWT.NONE);
		colApproveNewXPath.getColumn().setText("Approve");
		colApproveNewXPath.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(getCheckboxSymbol(((BrokenTestObject) cell.getElement()).getApproved()));
			}
		});

		colApproveNewXPath.setEditingSupport(new CheckBoxColumnEditingSupport(tbViewer));

		tableColumnLayout.setColumnData(colObjectId.getColumn(), new ColumnWeightData(35, 100));
		tableColumnLayout.setColumnData(colOldXPath.getColumn(), new ColumnWeightData(30, 100));
		tableColumnLayout.setColumnData(colNewXPath.getColumn(), new ColumnWeightData(30, 100));
		tableColumnLayout.setColumnData(colApproveNewXPath.getColumn(), new ColumnWeightData(5, 100));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Change parent layout data to fill the whole bar
		parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		createButton(parent, IDialogConstants.FINISH_ID, "Approve all", true);

		getButton(IDialogConstants.FINISH_ID).addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionCount() < table.getItemCount()) {
					((Set<BrokenTestObject>) tbViewer.getInput()).stream().forEach(a -> a.setApproved(true));
					table.selectAll();
					tbViewer.refresh();
				} else {
					((Set<BrokenTestObject>) tbViewer.getInput()).stream().forEach(a -> a.setApproved(false));
					table.deselectAll();
					tbViewer.refresh();
				}
			}
		});

		// Create a spacer label
		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		spacer.setText("Please refresh the Object Repository to see XPath values updated after approval");
		// Update layout of the parent composite to count the spacer
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;
		createButton(parent, IDialogConstants.OK_ID, "OK", false);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
	}

	@Override
	protected void okPressed() {
		approvedAutoHealingEntities.clear();
		unapprovedBrokenEntities.stream().filter(a -> a != null).filter(a -> a.getApproved() == true).forEach(a -> {
			approvedAutoHealingEntities.add(a);
		});
		
		unapprovedBrokenEntities = unapprovedBrokenEntities.stream()
				.filter(a -> !a.getApproved())
				.collect(Collectors.toSet());

		super.okPressed();
	}

	private String getCheckboxSymbol(boolean isChecked) {
		return isChecked ? "\u2611" : "\u2610";
	}

	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
	}

	public String getDialogTitle() {
		return this.dialogTitle;
	}

	public void loadAutoHealingEntities() {
		unapprovedBrokenEntities.clear();
		unapprovedBrokenEntities = AutoHealingController.readUnapprovedBrokenTestObjects();
		unapprovedBrokenEntities = unapprovedBrokenEntities.stream()
				.filter(a -> !a.getApproved())
				.collect(Collectors.toSet());
	}

	public Set<BrokenTestObject> getUnapprovedAutoHealingEntities() {
		return unapprovedBrokenEntities;
	}

	public Set<BrokenTestObject> getApprovedAutoHealingEntities() {
		return approvedAutoHealingEntities;
	}
}
