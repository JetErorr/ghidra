/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.plugin.core.compositeeditor;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import docking.ActionContext;
import docking.DockingWindowManager;
import ghidra.program.model.data.*;
import ghidra.util.exception.AssertException;

/**
 * Action for use in the composite data type editor.
 * This action has help associated with it.
 */
public class AddBitFieldAction extends CompositeEditorTableAction {

	private final static String ACTION_NAME = "Add Bitfield";
	private final static String GROUP_NAME = BITFIELD_ACTION_GROUP;
	private final static String DESCRIPTION =
		"Add a bitfield at the position of a selected component";
	private static String[] popupPath = new String[] { ACTION_NAME };

	public AddBitFieldAction(CompositeEditorProvider provider) {
		super(provider, EDIT_ACTION_PREFIX + ACTION_NAME, GROUP_NAME, popupPath, null, null);
		setDescription(DESCRIPTION);
		if (!(model instanceof CompEditorModel)) {
			throw new AssertException("unsupported use");
		}
		adjustEnablement();
	}

	@Override
	public void actionPerformed(ActionContext context) {

		CompEditorModel editorModel = (CompEditorModel) model;
		if (editorModel.getNumSelectedRows() != 1 || editorModel.isFlexibleArraySelection()) {
			return;
		}
		int rowIndex = model.getSelectedRows()[0];

		if (editorModel.isAligned()) {
			// Insert before selected component
			// ordinal based, user input needed: 
			//   1. bitfield base datatype
			//   2. bitfield size
			//   3. bitfield name (can be renamed later)
			int ordinal = -1;
			DataType baseDataType = null;
			if (!editorModel.isAtEnd(rowIndex)) {
				DataTypeComponent component = editorModel.getComponent(rowIndex);
				ordinal = component.getOrdinal();
				if (component.isBitFieldComponent()) {
					BitFieldDataType currentBitfield = (BitFieldDataType) component.getDataType();
					baseDataType = currentBitfield.getBaseDataType();
				}
			}
			insertBitField(ordinal, baseDataType);
		}
		else {
			BitFieldEditorDialog dlg =
				new BitFieldEditorDialog(editorModel.viewComposite, provider.dtmService,
					-(rowIndex + 1), ordinal -> refreshTableAndSelection(editorModel, ordinal));
			Component c = provider.getComponent();
			Window w = SwingUtilities.windowForComponent(c);
			DockingWindowManager.showDialog(w, dlg, c);
		}

		requestTableFocus();
	}

	private void refreshTableAndSelection(CompEditorModel editorModel, int ordinal) {
		editorModel.fireTableDataChanged();
		editorModel.compositeInfoChanged();
		JTable editorTable = provider.getTable();
		editorTable.getSelectionModel().setSelectionInterval(ordinal, ordinal);
	}

	private void insertBitField(int ordinal, DataType baseDataType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjustEnablement() {
		boolean enabled = true;
		CompEditorModel editorModel = (CompEditorModel) model;
		if (editorModel.viewComposite == null || editorModel.getNumSelectedRows() != 1 ||
			editorModel.isFlexibleArraySelection()) {
			enabled = false;
		}
		setEnabled(enabled);
	}

}
