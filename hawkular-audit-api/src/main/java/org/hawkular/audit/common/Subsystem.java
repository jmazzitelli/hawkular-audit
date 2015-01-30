/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.audit.common;

import com.google.gson.annotations.Expose;

/**
 * Identifies the name of an audited subsystem.
 */
public class Subsystem {
    // a generic catch-all subsystem
    public static final Subsystem MISCELLANEOUS = new Subsystem("MISC");

    @Expose
    private String name;

    protected Subsystem() {
        // intentionally left blank
    }

    public Subsystem(String name) {
        if (name == null || name.length() == 0) {
            throw new NullPointerException("subsystem name cannot be null or empty");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Subsystem)) {
            return false;
        }
        Subsystem other = (Subsystem) obj;
        return name.equals(other.name);
    }
}
