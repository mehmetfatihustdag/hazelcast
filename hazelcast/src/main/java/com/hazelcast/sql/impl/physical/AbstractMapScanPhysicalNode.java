/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.sql.impl.physical;

import com.hazelcast.internal.serialization.impl.SerializationUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.sql.impl.expression.Expression;
import com.hazelcast.sql.impl.schema.SqlTopObjectDescriptor;
import com.hazelcast.sql.impl.type.QueryDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class to scan a map.
 */
public abstract class AbstractMapScanPhysicalNode extends ZeroInputPhysicalNode {
    /** Map name. */
    protected String mapName;

    /** Key descriptor. */
    protected SqlTopObjectDescriptor keyDescriptor;

    /** Value descriptor. */
    protected SqlTopObjectDescriptor valueDescriptor;

    /** Field names. */
    protected List<String> fieldNames;

    /** Field types. */
    protected List<QueryDataType> fieldTypes;

    /** Projects. */
    protected List<Integer> projects;

    /** Filter. */
    protected Expression<Boolean> filter;

    protected AbstractMapScanPhysicalNode() {
        // No-op.
    }

    protected AbstractMapScanPhysicalNode(
        int id,
        String mapName,
        SqlTopObjectDescriptor keyDescriptor,
        SqlTopObjectDescriptor valueDescriptor,
        List<String> fieldNames,
        List<QueryDataType> fieldTypes,
        List<Integer> projects,
        Expression<Boolean> filter
    ) {
        super(id);

        this.mapName = mapName;
        this.keyDescriptor = keyDescriptor;
        this.valueDescriptor = valueDescriptor;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.projects = projects;
        this.filter = filter;
    }

    public String getMapName() {
        return mapName;
    }

    public SqlTopObjectDescriptor getKeyDescriptor() {
        return keyDescriptor;
    }

    public SqlTopObjectDescriptor getValueDescriptor() {
        return valueDescriptor;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public List<QueryDataType> getFieldTypes() {
        return fieldTypes;
    }

    public List<Integer> getProjects() {
        return projects;
    }

    public Expression<Boolean> getFilter() {
        return filter;
    }

    @Override
    public PhysicalNodeSchema getSchema0() {
        List<QueryDataType> types = new ArrayList<>(projects.size());

        for (Integer project : projects) {
            types.add(fieldTypes.get(project));
        }

        return new PhysicalNodeSchema(types);
    }

    @Override
    protected void writeData0(ObjectDataOutput out) throws IOException {
        out.writeUTF(mapName);
        keyDescriptor.writeData(out);
        valueDescriptor.writeData(out);
        SerializationUtil.writeList(fieldNames, out);
        SerializationUtil.writeList(fieldTypes, out);
        SerializationUtil.writeList(projects, out);
        out.writeObject(filter);
    }

    @Override
    protected void readData0(ObjectDataInput in) throws IOException {
        mapName = in.readUTF();
        keyDescriptor = SqlTopObjectDescriptor.readData(in);
        valueDescriptor = SqlTopObjectDescriptor.readData(in);
        fieldNames = SerializationUtil.readList(in);
        fieldTypes = SerializationUtil.readList(in);
        projects = SerializationUtil.readList(in);
        filter = in.readObject();
    }
}
