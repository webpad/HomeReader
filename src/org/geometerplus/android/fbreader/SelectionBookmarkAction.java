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

package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.bookmark.EditBookmarkActivity;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.SuperToast.Background;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.view.View;
import ntx.reader3.R;

public class SelectionBookmarkAction extends FBAndroidAction {
	SelectionBookmarkAction(FBReader baseApplication, FBReaderApp fbreader) {
		super(baseApplication, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final Bookmark bookmark;
		if (params.length != 0) {
			bookmark = (Bookmark)params[0];
		} else {
			bookmark = Reader.addSelectionBookmark();
		}
		if (bookmark == null) {
			return;
		}

		final SuperActivityToast toast =
			new SuperActivityToast(BaseActivity, SuperToast.Type.BUTTON);
		toast.setText(bookmark.getText());
//        style.textColor = Color.DKGRAY;
//        style.background = getBackground(WHITE);
//        style.dividerColor = Color.DKGRAY;
//        style.buttonTextColor = Color.GRAY;
		toast.setTextColor(Color.BLACK);
		toast.setButtonTextColor(Color.BLACK);
		toast.setBackground(Background.WHITE);
		toast.setDividerColor(Color.BLACK);
		toast.getView().setBackgroundColor(Color.rgb(245,245,245));
		toast.getView().setBackgroundResource(R.drawable.bg_white);
/*		toast.getView().setBackgroundResource(R.bg_white);
		toast.getView().setBackgroundResource(android.R.drawable.btn_dialog);*/



	
		toast.setDuration(SuperToast.Duration.EXTRA_LONG);

//        toast.setDuration(SuperToast.Duration.VERY_SHORT);
		toast.setButtonIcon(
			android.R.drawable.ic_menu_edit,
			ZLResource.resource("dialog").getResource("button").getResource("edit").getValue()
		);

//		toast.getView().setBackgroundColor(Color.argb(150, 0,255, 255));
//		toast.getView().setBackgroundColor(Color.WHITE);
		
		toast.setOnClickWrapper(new OnClickWrapper("bkmk", new SuperToast.OnClickListener() {
			@Override
			public void onClick(View view, Parcelable token) {
				final Intent intent =
					new Intent(BaseActivity.getApplicationContext(), EditBookmarkActivity.class);
				FBReaderIntents.putBookmarkExtra(intent, bookmark);
				OrientationUtil.startActivity(BaseActivity, intent);
			}
		}));
		BaseActivity.showToast(toast);
	}
}
