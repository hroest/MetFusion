/**
 * created by Michael Gerlich, Apr 11, 2013 - 4:24:04 PM

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

import java.util.Comparator;

/**
 * Custom comparator of Result objects based on the provided score value.
 * 
 * @author mgerlich
 *
 */
public class ResultComparator implements Comparator<Result> {

	@Override
	public int compare(Result r1, Result r2) {
		return (r1.getScore() < r2.getScore() ? -1 : (r1.getScore() == r2.getScore() ? 0 : 1));
	}

}
