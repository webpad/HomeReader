/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.preferences;

import android.content.Context;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class ZLStringChoicePreference extends ZLStringListPreference {
	private final ZLStringOption myOption;
	Context context;

	ZLStringChoicePreference(Context context, ZLResource resource, ZLStringOption option, String[] values) {
		super(context, resource);
		setList(values);
		setInitialValue(option.getValue());
		myOption = option;
		this.context=context;
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		myOption.setValue(getValue());
		
//		for (int i=0;i<Globals.pageString.length;i++){
//			if (getValue().equals(Globals.pageString[i])){
//				setRefreshPage(i+1);
//				break;
//			}
//		}	
		
	}
//	private void setRefreshPage(int eachPage){
//			android.provider.Settings.System.putInt(context.getContentResolver(),
//				      android.provider.Settings.System.SCREEN_REFRESH_FREQUENCY,
//				      eachPage);
//	}
}
