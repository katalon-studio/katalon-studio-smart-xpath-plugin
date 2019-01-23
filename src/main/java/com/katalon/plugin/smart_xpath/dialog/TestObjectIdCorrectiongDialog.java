package com.katalon.plugin.smart_xpath.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.katalon.platform.api.controller.TestObjectController;
import com.katalon.platform.api.exception.ResourceException;
import com.katalon.platform.api.model.ProjectEntity;
import com.katalon.platform.api.model.TestObjectEntity;
import com.katalon.platform.api.service.ApplicationManager;

public class TestObjectIdCorrectiongDialog extends AbstractDialog {
	private TreeViewer treeViewer;

	private List<TestObjectEntity> testObjects;

	private ProjectEntity currentProject;

	private TestObjectEntity selectedTestObject;

	public TestObjectIdCorrectiongDialog(Shell parentShell) {
		super(parentShell);
		currentProject = ApplicationManager.getInstance().getProjectManager().getCurrentProject();
		try {
			testObjects = ApplicationManager.getInstance().getControllerManager()
					.getController(TestObjectController.class).getTestObjects(currentProject);
		} catch (ResourceException e) {
			e.printStackTrace(System.out);
			testObjects = new ArrayList<>();
		}
	}

	@Override
	protected Control createDialogContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		treeViewer = new TreeViewer(container, SWT.BORDER | SWT.MULTI);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		treeViewer.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				String testObjectId = "";
				if (cell.getElement() != null && cell.getElement() instanceof TestObjectEntity) {
					testObjectId = ((TestObjectEntity) cell.getElement()).getId();
				}
				cell.setText(testObjectId);
			}
		});

		treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement == null) {
					return new Object[0];
				}
				if (inputElement.getClass().isArray()) {
					return (Object[]) inputElement;
				}
				if (inputElement instanceof List<?>) {
					return ((List<?>) inputElement).toArray();
				}
				return new Object[0];
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return new Object[0];
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				return false;
			}
		});

		treeViewer.setAutoExpandLevel(2);

		return container;
	}

	@Override
	protected void registerControlModifyListeners() {
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TestObjectEntity testObject = (TestObjectEntity) treeViewer.getStructuredSelection().getFirstElement();
				if (testObject != null) {
					selectedTestObject = testObject;
					okPressed();
				}
			}
		});

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				TestObjectEntity testObject = (TestObjectEntity) treeViewer.getStructuredSelection().getFirstElement();
				if (testObject != null) {
					selectedTestObject = testObject;
					getButton(OK).setEnabled(true);
					return;
				}
				getButton(OK).setEnabled(false);
			}
		});
	}

	@Override
	protected void setInput() {
		treeViewer.setInput(testObjects);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

	@Override
	public String getDialogTitle() {
		return "Test Object ID correction dialog";
	}

	public String getSelectedTestObjectId() {
		return selectedTestObject != null ? selectedTestObject.getId() : "";
	}

}
