/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.view;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLColor;

public abstract class SelectionCursor {
	public enum Which {
		Left,
		Right
	}
	
	public static void draw(ZLPaintContext context, Which which, int x, int y, ZLColor color) {
//		context.setFillColor(color);
		context.setFillColor(new ZLColor(0,0,0)); // black color


		final int dpi = ZLibrary.Instance().getDisplayDPI();
		final int unit = dpi / 120;
		
		final int cursorCircleSize = unit * 6 * 2; 
		final int cursorWidthIncrease = 12; 
		final int cursorHeightIncrease = 20; 
		
		final int xCenter = which == Which.Left ? x - unit - 1 : x + unit + 1;
		context.fillRectangle(xCenter - unit - (cursorWidthIncrease/2), y + dpi / 8 + (cursorHeightIncrease/2), xCenter + unit + (cursorWidthIncrease/2), y - dpi / 8 - (cursorHeightIncrease/2));

// remove Circle
//		if (which == Which.Left) {
//			context.fillCircle(xCenter, y - dpi / 8 - (cursorHeightIncrease/2), cursorCircleSize);
//		} else {
//			context.fillCircle(xCenter, y + dpi / 8 + (cursorHeightIncrease/2), cursorCircleSize);
//		}
	}
}
