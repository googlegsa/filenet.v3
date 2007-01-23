// Copyright (C) 2006-2007 Google Inc.

package com.google.enterprise.connector.file;

import com.google.enterprise.connector.spi.ValueType;

/**
 * Describes the properties returned to the caller and the fields
 * in Livelink needed to implement them. A null
 * <code>fieldName</code> indicates a property that is in not in
 * the recarray selected from the database. These properties are
 * implemented separately. A null <code>propertyName</code>
 * indicates a field that is required from the database but which
 * is not returned as a property to the caller.
 */
public final class Field {
    public final String fieldName;
    public final ValueType fieldType;
    public final String propertyName;

    public Field(String fieldName, ValueType fieldType,
        String propertyName) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.propertyName = propertyName;
    }
}
    
