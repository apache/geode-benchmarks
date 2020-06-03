/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package benchmark.geode.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.geode.DataSerializable;
import org.apache.geode.DataSerializer;
import org.apache.geode.Delta;
import org.apache.geode.InvalidDeltaException;

/**
 * The {@link benchmark.geode.data.Session} class is an Abstract Data Type (ADT) modeling a user's
 * session.
 *
 * @author John Blum
 * @see Comparable
 * @see DataSerializable
 * @see DataSerializer
 * @see Delta
 */
public class Session implements Comparable<Session>, DataSerializable, Delta {

  private static final boolean ALLOW_JAVA_SERIALIZATION = false;

  public static benchmark.geode.data.Session create(String id) {
    return new benchmark.geode.data.Session(id);
  }

  private transient boolean delta = false;

  protected transient volatile boolean toDataCalled = false;
  protected transient volatile boolean toDeltaCalled = false;

  private Instant creationTime;
  private Instant lastAccessedTime;

  private final Map<String, Object> attributes = new HashMap<>();
  private final Map<String, Object> attributeDeltas = new HashMap<>();

  private String id;

  public Session(String id) {

    this.id = id;
    this.creationTime = Instant.now();
    this.lastAccessedTime = this.creationTime;
    this.delta = true;
  }

  public synchronized String getId() {
    return this.id;
  }

  public synchronized Object setAttribute(String name, Object value) {

    return value != null
        ? doSetAttribute(name, value)
        : removeAttribute(name);
  }

  public synchronized void setAttributes(Map<String, Object> attributes) {
    this.attributes.putAll(attributes);
  }

  private Object doSetAttribute(String name, Object value) {

    Object previousValue = this.attributes.put(name, value);

    if (!value.equals(previousValue)) {
      this.attributeDeltas.put(name, value);
      this.delta = true;
    }

    return previousValue;
  }

  public synchronized Object removeAttribute(String name) {

    if (getAttributes().containsKey(name)) {
      this.attributeDeltas.put(name, null);
      this.delta = true;
    }

    return getAttributes().remove(name);
  }

  public synchronized Object getAttribute(String name) {
    return getAttributes().get(name);
  }

  public synchronized Set<String> getAttributeNames() {
    return Collections.unmodifiableSet(new HashSet<>(getAttributes().keySet()));
  }

  protected synchronized Map<String, Object> getAttributes() {
    return this.attributes;
  }

  public synchronized Instant getCreationTime() {
    return this.creationTime;
  }

  protected synchronized void setLastAccessedTime(Instant lastAccessedTime) {
    this.lastAccessedTime = lastAccessedTime;
  }

  public synchronized Instant getLastAccessedTime() {
    return this.lastAccessedTime;
  }

  public synchronized benchmark.geode.data.Session commit() {

    this.delta = false;
    this.attributeDeltas.clear();

    return this;
  }

  public synchronized benchmark.geode.data.Session touch() {

    Instant newLastAccessedTime = Instant.now();

    this.delta |= !newLastAccessedTime.equals(getLastAccessedTime());
    setLastAccessedTime(newLastAccessedTime);

    return this;
  }

  @Override
  public synchronized void toData(DataOutput out) throws IOException {

    this.toDataCalled = true;

    out.writeUTF(this.getId());
    out.writeLong(this.getCreationTime().toEpochMilli());
    out.writeLong(this.getLastAccessedTime().toEpochMilli());
    DataSerializer.writeHashMap(getAttributes(), out);
  }

  @Override
  public synchronized void fromData(DataInput in) throws IOException, ClassNotFoundException {

    this.id = in.readUTF();
    this.creationTime = Instant.ofEpochMilli(in.readLong());

    setLastAccessedTime(Instant.ofEpochMilli(in.readLong()));

    this.setAttributes(DataSerializer.readHashMap(in));
  }

  @Override
  public synchronized void toDelta(DataOutput out) throws IOException {

    this.toDeltaCalled = true;

    out.writeLong(getLastAccessedTime().toEpochMilli());
    out.writeInt(this.attributeDeltas.size());

    for (Entry<String, Object> entry : this.attributeDeltas.entrySet()) {
      out.writeUTF(entry.getKey());
      DataSerializer.writeObject(entry.getValue(), out, ALLOW_JAVA_SERIALIZATION);
    }
  }

  @Override
  public synchronized void fromDelta(DataInput in) throws IOException, InvalidDeltaException {

    String key = null;

    try {

      setLastAccessedTime(Instant.ofEpochMilli(in.readLong()));

      for (int count = in.readInt(); count > 0; count--) {
        key = in.readUTF();
        setAttribute(key, DataSerializer.readObject(in));
      }
    } catch (ClassNotFoundException cause) {
      throw new IOException(
          String.format("Failed to resolve type in delta for attribute [%s]", key), cause);
    }
  }

  @Override
  public synchronized boolean hasDelta() {
    return this.delta || !this.attributeDeltas.isEmpty();
  }

  public synchronized boolean wasToDataCalled() {

    boolean toDataCalled = this.toDataCalled;

    this.toDataCalled = false;

    return toDataCalled;
  }

  public synchronized boolean wasToDeltaCalled() {

    boolean toDeltaCalled = this.toDeltaCalled;

    this.toDeltaCalled = false;

    return toDeltaCalled;
  }


  @Override
  public int compareTo(benchmark.geode.data.Session session) {
    return this.getId().compareTo(session.getId());
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof benchmark.geode.data.Session)) {
      return false;
    }

    benchmark.geode.data.Session that = (benchmark.geode.data.Session) obj;

    return this.getId().equals(that.getId());
  }

  @Override
  public int hashCode() {

    int hashValue = 17;

    hashValue = 37 * hashValue + getId().hashCode();

    return hashValue;
  }

  @Override
  public String toString() {
    return getId();
  }
}
