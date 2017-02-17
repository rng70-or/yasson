/*******************************************************************************
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved.
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

package org.eclipse.yasson.model;

import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

import javax.json.bind.JsonbException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Object holding reference to Constructor / Method for custom object creation.
 *
 * @author Roman Grigoriadi
 */
public class JsonbCreator {

    private final Executable executable;

    private final CreatorParam[] params;

    public JsonbCreator(Executable executable, CreatorParam[] creatorParams) {
        this.executable = executable;
        this.params = creatorParams;
    }

    /**
     * Create instance by either constructor or factory method, with provided parameter values and a Class to call on.
     *
     * @param params parameters to be passed into constructor / factory method
     * @param on class to call onto
     * @param <T> Type of class / instance
     * @return instance
     */
    @SuppressWarnings("unchecked")
    public <T> T call(Object[] params, Class<T> on) {
        try {
            if (executable instanceof Constructor) {
                return ((Constructor<T>) executable).newInstance(params);
            } else {
                return (T) ((Method) executable).invoke(on, params);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new JsonbException(Messages.getMessage(MessageKeys.ERROR_CALLING_JSONB_CREATOR), e);
        }
    }

    /**
     * True if param name is one of creator params.
     * @param paramName param name to check
     * @return true if found
     */
    public boolean contains(String paramName) {
        return findByName(paramName) != null;
    }

    /**
     * Find creator parameter by name.
     * @param paramName parameter name as it appear in json document.
     * @return creator parameter
     */
    public CreatorParam findByName(String paramName) {
        for (CreatorParam param : params) {
            if (param.getName().equals(paramName)) {
                return param;
            }
        }
        return null;
    }

    /**
     * Parameters of this creator.
     * @return parameters
     */
    public CreatorParam[] getParams() {
        return params;
    }
}
