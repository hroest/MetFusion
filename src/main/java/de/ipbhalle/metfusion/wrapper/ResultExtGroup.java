/**
 * created by Michael Gerlich, Nov 11, 2010 - 10:04:38 AM
 */

package de.ipbhalle.metfusion.wrapper;

import java.util.ArrayList;
import java.util.List;

import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;

public class ResultExtGroup extends ResultExt {

	protected List<ResultExtGroupBean> childResultRows = new ArrayList<ResultExtGroupBean>();

	public List<ResultExtGroupBean> getChildResultRows() {
		return childResultRows;
	}
}
