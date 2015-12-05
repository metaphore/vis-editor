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

package com.kotcrab.vis.editor.module.editor;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kotcrab.vis.editor.serializer.json.*;
import com.kotcrab.vis.runtime.component.AssetReference;

import java.lang.reflect.Modifier;

/** @author Kotcrab */
public class GsonModule extends EditorModule {
	private ExtensionStorageModule extensionStorage;

	private Gson gson;

	@SuppressWarnings("rawtypes")
	@Override
	public void postInit () {
		ClassJsonSerializer classSerializer;

		GsonBuilder builder = new GsonBuilder()
				.setPrettyPrinting()
				.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
				.registerTypeAdapter(Array.class, new ArrayJsonSerializer())
				.registerTypeAdapter(IntArray.class, new IntArrayJsonSerializer())
				.registerTypeAdapter(IntMap.class, new IntMapJsonSerializer())
				.registerTypeAdapter(ObjectMap.class, new ObjectMapJsonSerializer())
				.registerTypeAdapter(Class.class, classSerializer = new ClassJsonSerializer(Thread.currentThread().getContextClassLoader()))
				.registerTypeAdapter(AssetReference.class, new AssetComponentSerializer());

		//TODO: [plugin] plugin entry point, allow plugin to simpler serializer registration, currently requires making EditorEntitySupport
		//register plugins serializers
		extensionStorage.getEntitiesSupports().forEach(
				support -> support.getJsonTypeAdapters().forEach(
						typeObjectEntry -> builder.registerTypeAdapter(typeObjectEntry.key, typeObjectEntry.value)));

		gson = builder.create();

		EditorJsonTags.registerTags(new GsonTagRegistrar(classSerializer));
	}

	public Gson getCommonGson () {
		return gson;
	}
}
