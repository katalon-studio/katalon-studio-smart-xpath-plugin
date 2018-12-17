package com.katalon.plugin.smart_xpath;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
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

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.service.ApplicationManager;

public class AutoHealingDialog extends Dialog {
	protected Composite tablePropertyComposite;
	private String dialogTitle = "";
	private TableViewer tbViewer;
	private TableColumnLayout tableColumnLayout;
	private Table table;
	private List<BrokenTestObject> autoHealingEntities;
	private List<BrokenTestObject> backUpAutoHealingEntities;
	private List<BrokenTestObject> approvedAutoHealingEntities;

	protected AutoHealingDialog(Shell parentShell) {
		super(parentShell);
		autoHealingEntities = new ArrayList<>();
		backUpAutoHealingEntities = new ArrayList<>();
		approvedAutoHealingEntities = new ArrayList<>();
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
		ldTableComposite.widthHint = 800;
		ldTableComposite.heightHint = 380;
		tablePropertyComposite.setLayoutData(ldTableComposite);
		tableColumnLayout = new TableColumnLayout();
		tablePropertyComposite.setLayout(tableColumnLayout);

		tbViewer = new TableViewer(tablePropertyComposite,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);

		createColumns();

		tbViewer.setContentProvider(ArrayContentProvider.getInstance());
		loadAutoHealingEntities();
		tbViewer.setInput(autoHealingEntities);

		table = tbViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		return tablePropertyComposite;
	}

	private void createColumns() {
		TableViewerColumn colObjectId = new TableViewerColumn(tbViewer, SWT.NONE);
		colObjectId.getColumn().setText("Test Object Id");
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

		tableColumnLayout.setColumnData(colObjectId.getColumn(), new ColumnWeightData(30, 100));
		tableColumnLayout.setColumnData(colOldXPath.getColumn(), new ColumnWeightData(30, 100));
		tableColumnLayout.setColumnData(colNewXPath.getColumn(), new ColumnWeightData(30, 100));
		tableColumnLayout.setColumnData(colApproveNewXPath.getColumn(), new ColumnWeightData(10, 100));
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
					((List<BrokenTestObject>) tbViewer.getInput()).stream().forEach(a -> a.setApproved(true));
					table.selectAll();
					tbViewer.refresh();
				} else {
					((List<BrokenTestObject>) tbViewer.getInput()).stream().forEach(a -> a.setApproved(false));
					table.deselectAll();
					tbViewer.refresh();
				}
			}
		});

		// Create a spacer label
		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// Update layout of the parent composite to count the spacer
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;
		createButton(parent, IDialogConstants.OK_ID, "OK", false);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) tbViewer.getSelection();
		approvedAutoHealingEntities = selection.toList();
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
		backUpAutoHealingEntities.clear();
		backUpAutoHealingEntities.addAll(autoHealingEntities);
		autoHealingEntities.clear();
		try {
			Gson gson = new Gson();
			ProjectEntity projectEntity = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
			System.out.println("Attempting to read current project directory");
			if (projectEntity != null) {
				System.out.println(
						"Current project directory detected, attempt to read auto-healing.json in smart_xpath");
				String projectDir = projectEntity.getFolderLocation();
				String jsonAutoHealingDir = StringUtils.getStandardPath(projectDir + "/smart_xpath/auto-healing.json");
				JsonReader reader = new JsonReader(new FileReader(jsonAutoHealingDir));
				System.out
						.println("auto-healing.json is read, attempting to parse the content into broken test objects");
				BrokenTestObjects brokenTestObjects = gson.fromJson(reader, BrokenTestObjects.class);
				autoHealingEntities = brokenTestObjects.getBrokenTestObjects();
				// Remove potential threats
				autoHealingEntities.removeAll(Collections.singleton(null));
				System.out.println("Broken test objects parsed successfully");
			} else {
				System.out.println("Current project directory is not detected, no project is open");
			}
		} catch (FileNotFoundException e) {
			System.out.println("/smart_xpath/auto-healing.json is not detected, no broken test objects are loaded");
			e.printStackTrace(System.out);
		}
	}

	public List<BrokenTestObject> getApprovedAutoHealingEntities() {
		return approvedAutoHealingEntities;
	}
}
