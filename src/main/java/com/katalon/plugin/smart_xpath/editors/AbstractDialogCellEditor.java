package com.katalon.plugin.smart_xpath.editors;

import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractDialogCellEditor extends DialogCellEditor {
	protected String defaultContent;
	protected Composite editor;
	protected boolean isEditorClosed;
	private Composite parent;

	public AbstractDialogCellEditor(Composite parent) {
		this(parent, "");
	}

	public AbstractDialogCellEditor(Composite parent, String defaultContent) {
		super(parent, SWT.NONE);
		this.parent = parent;
		this.isEditorClosed = false;
		this.defaultContent = defaultContent;
	}

	@Override
	protected void updateContents(Object value) {
		if (defaultContent != null) {
			super.updateContents(defaultContent.replace("&", "&&"));
		} else {
			super.updateContents(value);
		}
	}

	@Override
	protected Button createButton(Composite parent) {
		Button button = super.createButton(parent);
		button.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				getControl().notifyListeners(SWT.Traverse, e);
			}
		});
		return button;
	}

	@Override
	protected boolean dependsOnExternalFocusListener() {
		return false;
	}

	@Override
	public void activate(ColumnViewerEditorActivationEvent activationEvent) {
		super.activate(activationEvent);
		doShowDialog();
	}

	@Override
	protected int getDoubleClickTimeout() {
		return 0;
	}

	@Override
	public void deactivate() {
		super.deactivate();
		isEditorClosed = true;
	}

	protected void waitTofireApplyEditorValue() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!isEditorClosed) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// interrupted, do nothing
					}
				}
				fireApplyEditorValue();
			}
		}).start();
	}

	protected void waitTofireCancelEditor() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!isEditorClosed) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// interrupted, do nothing
					}
				}
				fireCancelEditor();
			}
		}).start();
	}

	protected void doShowDialog() {
		Object newValue = openDialogBox(editor);
		if (newValue == null) {
			fireCancelEditor();
			return;
		}
		if (!isCorrect(newValue)) {
			return;
		}
		markDirty();
		doSetValue(newValue);
		fireApplyEditorValue();
	}

	@Override
	protected final Control createControl(Composite parent) {
		return null;
	}

	public void applyEditingValue() {
		fireApplyEditorValue();
	}

	protected Shell getParentShell() {
		return parent.getShell();
	}
}
