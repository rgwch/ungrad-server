/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 */

package ch.elexis.ungrad.server

import ch.rgw.tools.json.JsonUtil

/**
 * Created by gerry on 05.09.16.
 */
interface IPersistor {
    fun read(id: String): JsonUtil
    fun write(id: String, value: JsonUtil): Boolean
}