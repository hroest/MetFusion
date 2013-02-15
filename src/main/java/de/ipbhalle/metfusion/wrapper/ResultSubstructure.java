/**
 * created by Michael Gerlich, Feb 1, 2013 - 11:01:26 AM

 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */ 

package de.ipbhalle.metfusion.wrapper;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.chemspider.www.ExtendedCompoundInfo;


public class ResultSubstructure extends Result {

	private ExtendedCompoundInfo info;
	private IAtomContainer container;
	private boolean used;
	
	
	public ResultSubstructure(ExtendedCompoundInfo info, IAtomContainer container, boolean used) {
		super("SubstructureSearch", String.valueOf(info.getCSID()), info.getCommonName(), 1.0d);
		this.info = info;
		this.container = container;
		this.used = used;
	}

	public ExtendedCompoundInfo getInfo() {
		return info;
	}

	public void setInfo(ExtendedCompoundInfo info) {
		this.info = info;
	}

	public IAtomContainer getContainer() {
		return container;
	}

	public void setContainer(IAtomContainer container) {
		this.container = container;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	
}
