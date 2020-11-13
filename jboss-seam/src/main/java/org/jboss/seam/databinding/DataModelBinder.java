package org.jboss.seam.databinding;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.faces.DataModels;

/**
 * Exposes a List, array, Map or Set to the UI as a JSF DataModel
 * 
 * @author Gavin King
 */
public class DataModelBinder implements DataBinder<DataModel, Object, javax.faces.model.DataModel> {

	public DataModelBinder() {
		super();
	}
	
	@Override
	public String getVariableName(DataModel out) {
		return out.value();
	}

	@Override
	public ScopeType getVariableScope(DataModel out) {
		return out.scope();
	}

	@Override
	public javax.faces.model.DataModel wrap(DataModel out, Object value) {
		return DataModels.instance().getDataModel(value);
	}

	@Override
	public Object getWrappedData(DataModel out, javax.faces.model.DataModel wrapper) {
		return wrapper.getWrappedData();
	}

	@Override
	public Object getSelection(DataModel out, javax.faces.model.DataModel wrapper) {
		if (wrapper.getRowCount() == 0 || wrapper.getRowIndex() < 0 || wrapper.getRowIndex() >= wrapper.getRowCount()) {
			return null;
		} else {
			Object rowData = wrapper.getRowData();
			if (rowData instanceof Map.Entry) {
				return ((Map.Entry) rowData).getValue();
			} else {
				return rowData;
			}
		}
	}

	@Override
	public boolean isDirty(DataModel out, javax.faces.model.DataModel wrapper, Object value) {
		return !getWrappedData(out, wrapper).equals(value);
	}

}
