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

package com.kotcrab.vis.editor.module.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.kotcrab.vis.editor.App;
import com.kotcrab.vis.editor.api.Assets;
import com.kotcrab.vis.editor.event.StatusBarEvent;
import com.kotcrab.vis.editor.event.TexturesReloadedEvent;
import com.kotcrab.vis.editor.util.DirectoryWatcher.WatchListener;
import com.kotcrab.vis.editor.util.Log;

public class TextureCacheModule extends ProjectModule implements WatchListener {
	private AssetsWatcherModule watcher;

	private String gfxPath;
	private String outPath;

	private Settings settings;

	private ObjectMap<String, TextureRegion> regions;

	private TextureRegion loadingRegion;
	private TextureRegion missingRegion;

	private FileHandle cacheFile;
	private TextureAtlas cache;

	private Timer waitTimer;

	private boolean firstReload = true;

	@Override
	public void init () {
		FileAccessModule fileAccess = projectContainer.get(FileAccessModule.class);
		watcher = projectContainer.get(AssetsWatcherModule.class);

		regions = new ObjectMap<>();

		waitTimer = new Timer();

		settings = new Settings();
		settings.combineSubdirectories = true;
		settings.silent = true;
		settings.fast = true;

		loadingRegion = Assets.icons.findRegion("refresh-big");
		missingRegion = Assets.icons.findRegion("file-question-big");

		FileHandle out = fileAccess.getModuleFolder(".textureCache");
		outPath = out.path();
		cacheFile = out.child("cache.atlas");

		gfxPath = fileAccess.getAssetsFolder().child("gfx").path();

		watcher.addListener(this);

		try {
			if (cacheFile.exists()) cache = new TextureAtlas(cacheFile);
		} catch (Exception e) {
			Log.error("Error while loading texture cache, texture cache will be regenerated");
		}

		updateCache();
	}

	private void updateCache () {
		new Thread(this::performUpdate, "TextureCache").start();
	}

	private void performUpdate () {
		TexturePacker.processIfModified(settings, gfxPath, outPath, "cache");

		Gdx.app.postRunnable(this::reloadAtlas);
	}

	private void reloadAtlas () {
		if (cacheFile.exists()) {
			TextureAtlas oldCache = null;

			if (cache != null) oldCache = cache;

			cache = new TextureAtlas(cacheFile);

			for (Entry<String, TextureRegion> e : regions.entries()) {
				String path = e.key.substring(4, e.key.length() - 4);
				TextureRegion region = e.value;

				TextureRegion newRegion = cache.findRegion(path);

				if (newRegion == null)
					region.setRegion(missingRegion);
				else
					region.setRegion(newRegion);
			}

			disposeOldCacheLater(oldCache);

			App.eventBus.post(new TexturesReloadedEvent());
			if (firstReload == false) {
				//we don't want to display 'textures reloaded' right after editor startup / project loaded
				App.eventBus.post(new StatusBarEvent("Textures reloaded"));
				firstReload = true;
			}
		} else
			Log.error("Texture cache not ready, probably they aren't any textures in project or packer failed");
	}

	private void disposeOldCacheLater (final TextureAtlas oldCache) {
		Timer.instance().scheduleTask(new Task() {
			@Override
			public void run () {
				if (oldCache != null) oldCache.dispose();
			}
		}, 0.5f);
	}

	@Override
	public void dispose () {
		if (cache != null)
			cache.dispose();
		watcher.removeListener(this);
	}

	@Override
	public void fileChanged (FileHandle file) {
		if (file.extension().equals("jpg") || file.extension().equals("png")) {
			waitTimer.clear();
			waitTimer.scheduleTask(new Task() {
				@Override
				public void run () {
					updateCache();
				}
			}, 0.5f);
		}
	}

	@Override
	public void fileDeleted (FileHandle file) {

	}

	@Override
	public void fileCreated (FileHandle file) {

	}

	public TextureRegion getRegion (String relativePath) {
		String regionName = relativePath.substring(4, relativePath.length() - 4);

		TextureRegion region = regions.get(regionName);

		if (region == null) {
			if (cache != null) region = cache.findRegion(regionName);

			if (region == null) region = new TextureRegion(loadingRegion);

			regions.put(relativePath, region);
		}

		return region;
	}
}
