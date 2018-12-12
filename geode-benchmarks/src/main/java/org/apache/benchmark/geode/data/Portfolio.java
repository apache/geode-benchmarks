/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.benchmark.geode.data;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.geode.internal.Assert;


public class Portfolio {

  public enum Day {
    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
  }

  public Day aDay;
  public short shortID;
  private static transient List dayList;
  private long ID;
  public String pkid;
  public Position position1;
  public Position position2;
  public Object[] position3;
  int position3Size;
  public String description;
  public long createTime;
  public HashMap<String, Position> positions = new HashMap<String, Position>();
  public HashMap<String, CollectionHolder> collectionHolderMap =
      new HashMap<String, CollectionHolder>();
  String type;
  public String status;
  public String[] names = {"aaa", "bbb", "ccc", "ddd"};
  public String unicodeṤtring;

  public static String secIds[] = {"SUN", "IBM", "YHOO", "GOOG", "MSFT", "AOL", "APPL", "ORCL",
      "SAP", "DELL", "RHAT", "NOVL", "HP"};

  static {
    dayList = new ArrayList();
    dayList.addAll(EnumSet.allOf(Day.class));
  }

  public Portfolio() {}

  public Portfolio(long i) {
    aDay = (Day) (dayList.get((int) (i % dayList.size())));
    ID = i;
    if (i % 2 == 0) {
      description = null;
    } else {
      description = "XXXX";
    }
    pkid = "" + i;
    status = i % 2 == 0 ? "active" : "inactive";
    type = "type" + (i % 3);
    position1 = new Position(secIds[Position.cnt % secIds.length], Position.cnt * 1000L);
    if (i % 2 != 0) {
      position2 = new Position(secIds[Position.cnt % secIds.length], Position.cnt * 1000L);
    } else {
      position2 = null;
    }

    positions.put(secIds[Position.cnt % secIds.length],
        new Position(secIds[Position.cnt % secIds.length], Position.cnt * 1000L));
    positions.put(secIds[Position.cnt % secIds.length],
        new Position(secIds[Position.cnt % secIds.length], Position.cnt * 1000L));

    collectionHolderMap.put("0", new CollectionHolder());
    collectionHolderMap.put("1", new CollectionHolder());
    collectionHolderMap.put("2", new CollectionHolder());
    collectionHolderMap.put("3", new CollectionHolder());

    unicodeṤtring = i % 2 == 0 ? "ṤṶẐ" : "ṤẐṶ";
    Assert.assertTrue(unicodeṤtring.length() == 3);
  }

  public Portfolio(int i, int j) {
    this(i);
    this.position1.portfolioId = j;
    this.position3 = new Object[3];
    for (int k = 0; k < position3.length; k++) {
      Position p = new Position(secIds[k], (k + 1) * 1000L);
      p.portfolioId = (k + 1);
      this.position3[k] = p;
    }
  }

  public Day getaDay() {
    return aDay;
  }

  public void setaDay(Day aDay) {
    this.aDay = aDay;
  }

  public short getShortID() {
    return shortID;
  }

  public void setShortID(short shortID) {
    this.shortID = shortID;
  }

  public long getID() {
    return ID;
  }

  public void setID(long ID) {
    this.ID = ID;
  }

  public String getPkid() {
    return pkid;
  }

  public void setPkid(String pkid) {
    this.pkid = pkid;
  }

  public Position getPosition1() {
    return position1;
  }

  public void setPosition1(Position position1) {
    this.position1 = position1;
  }

  public Position getPosition2() {
    return position2;
  }

  public void setPosition2(Position position2) {
    this.position2 = position2;
  }

  public Object[] getPosition3() {
    return position3;
  }

  public void setPosition3(Object[] position3) {
    this.position3 = position3;
  }

  public int getPosition3Size() {
    return position3Size;
  }

  public void setPosition3Size(int position3Size) {
    this.position3Size = position3Size;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public HashMap<String, Position> getPositions() {
    return positions;
  }

  public void setPositions(HashMap<String, Position> positions) {
    this.positions = positions;
  }

  public HashMap<String, CollectionHolder> getCollectionHolderMap() {
    return collectionHolderMap;
  }

  public void setCollectionHolderMap(HashMap<String, CollectionHolder> collectionHolderMap) {
    this.collectionHolderMap = collectionHolderMap;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String[] getNames() {
    return names;
  }

  public void setNames(String[] names) {
    this.names = names;
  }

  public String getUnicodeṤtring() {
    return unicodeṤtring;
  }

  public void setUnicodeṤtring(String unicodeṤtring) {
    this.unicodeṤtring = unicodeṤtring;
  }

  public long getLongMinValue() {
    long longMinValue = Long.MIN_VALUE;
    return longMinValue;
  }

  public float getFloatMinValue() {
    float floatMinValue = Float.MIN_VALUE;
    return floatMinValue;
  }

  public double getDoubleMinValue() {
    double doubleMinValue = Double.MIN_VALUE;
    return doubleMinValue;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Portfolio)) {
      return false;
    }
    Portfolio p2 = (Portfolio) o;
    return this.ID == p2.ID;
  }

  @Override
  public int hashCode() {
    return ((Long) this.ID).hashCode();
  }


  public String toString() {
    String out =
        "PortfolioPdx [ID=" + ID + " status=" + status + " type=" + type + " pkid=" + pkid + "\n ";
    Iterator iter = positions.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      out += entry.getKey() + ":" + entry.getValue() + ", ";
    }
    out += "\n P1:" + position1 + ", P2:" + position2;
    return out + "\n]";
  }

}
