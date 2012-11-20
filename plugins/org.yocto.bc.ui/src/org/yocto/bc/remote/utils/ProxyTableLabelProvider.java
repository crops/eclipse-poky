package org.yocto.bc.remote.utils;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class ProxyTableLabelProvider extends BaseLabelProvider  implements ITableLabelProvider{

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
			if (element == null) {
				return null;
			}
			ProxyElement data = (ProxyElement) element;
			switch (columnIndex) {
			case 0:
				return null;
			case 1:
				return data.getType();
			case 2:
				return data.getHost();
			case 3:
				return Integer.toString(data.getPort());
			default:
				return null;
			}
	}

}
