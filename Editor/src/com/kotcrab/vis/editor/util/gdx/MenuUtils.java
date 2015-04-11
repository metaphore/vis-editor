/*
 * Copyright 2014-2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.editor.util.gdx;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.editor.api.Assets;
import com.kotcrab.vis.editor.api.Icons;
import com.kotcrab.vis.editor.ui.ButtonListener;
import com.kotcrab.vis.ui.widget.MenuItem;

public class MenuUtils {
	public static MenuItem createMenuItem (String text, ButtonListener listener) {
		return createMenuItem(text, null, listener);
	}

	public static MenuItem createMenuItem (String text, Icons icon, ButtonListener listener) {
		return new MenuItem(text, icon != null ? Assets.getIcon(icon) : null, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				listener.clicked();
			}
		});
	}

}
