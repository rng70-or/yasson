/*******************************************************************************
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Roman Grigoriadi
 ******************************************************************************/
package org.eclipse.yasson.internal.serializer;

import org.eclipse.yasson.internal.JsonbParser;
import org.eclipse.yasson.internal.JsonbRiParser;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.Unmarshaller;

import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Item implementation for {@link java.util.Map} fields.
 * According to JSON specification object can have only string keys, given that maps could only be parsed
 * from JSON objects, implementation is bound to String type.
 *
 * @author Roman Grigoriadi
 */
public class MapDeserializer<T extends Map<?,?>> extends AbstractContainerDeserializer<T> implements EmbeddedItem {

    /**
     * Type of value in the map.
     * (Keys must always be Strings, because of JSON spec)
     */
    private final Type mapValueRuntimeType;

    private final T instance;

    /**
     * Create instance of current item with its builder.
     *
     * @param builder {@link DeserializerBuilder} used to build this instance
     */
    protected MapDeserializer(DeserializerBuilder builder) {
        super(builder);
        mapValueRuntimeType = getRuntimeType() instanceof ParameterizedType ?
                ReflectionUtils.resolveType(this, ((ParameterizedType) getRuntimeType()).getActualTypeArguments()[1])
                : Object.class;

        this.instance = createInstance();
    }

    @SuppressWarnings("unchecked")
    private T createInstance() {
        Class<T> rawType = (Class<T>) ReflectionUtils.getRawType(getRuntimeType());
        return rawType.isInterface() ? (T) getMapImpl(rawType) : ReflectionUtils.createNoArgConstructorInstance(rawType);
    }

    private Map<Object, Object> getMapImpl(Class ifcType) {
        //SortedMap, NavigableMap
        if (SortedMap.class.isAssignableFrom(ifcType)) {
            return new TreeMap<>();
        }
        return new HashMap<>();
    }

    @Override
    public T getInstance(Unmarshaller unmarshaller) {
        return instance;
    }

    @Override
    public void appendResult(Object result) {
        appendCaptor(parserContext.getLastKeyName(), convertNullToOptionalEmpty(mapValueRuntimeType, result));
    }

    @SuppressWarnings("unchecked")
    private <V> void appendCaptor(String key, V value) {
        ((Map<String, V>) getInstance(null)).put(key, value);
    }

    @Override
    protected void deserializeNext(JsonParser parser, Unmarshaller context) {
        final JsonbDeserializer<?> deserializer = newCollectionOrMapItem(mapValueRuntimeType, context.getJsonbContext());
        appendResult(deserializer.deserialize(parser, context, mapValueRuntimeType));
    }

    @Override
    protected JsonbRiParser.LevelContext moveToFirst(JsonbParser parser) {
        parser.moveTo(JsonParser.Event.START_OBJECT);
        return parser.getCurrentLevel();
    }
}
