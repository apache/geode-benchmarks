/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.benchmark.geode.data;


public class Position {
  private long avg20DaysVol = 0;
  private String bondRating;
  private double convRatio;
  private String country;
  private double delta;
  private long industry;
  private long issuer;
  private double mktValue;
  private double qty;
  public String secId;
  public String secIdIndexed;
  private String secLinks;
  public String secType;
  private double sharesOutstanding;
  public String underlyer;
  private long volatility;
  private int pid;
  public static int cnt = 0;
  public int portfolioId = 0;

  public Position() {}

  public Position(String id, double out) {
    secId = id;
    secIdIndexed = secId;
    sharesOutstanding = out;
    secType = "a";
    pid = cnt++;
    this.mktValue = cnt;
  }

  public long getAvg20DaysVol() {
    return avg20DaysVol;
  }

  public void setAvg20DaysVol(long avg20DaysVol) {
    this.avg20DaysVol = avg20DaysVol;
  }

  public String getBondRating() {
    return bondRating;
  }

  public void setBondRating(String bondRating) {
    this.bondRating = bondRating;
  }

  public double getConvRatio() {
    return convRatio;
  }

  public void setConvRatio(double convRatio) {
    this.convRatio = convRatio;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public double getDelta() {
    return delta;
  }

  public void setDelta(double delta) {
    this.delta = delta;
  }

  public long getIndustry() {
    return industry;
  }

  public void setIndustry(long industry) {
    this.industry = industry;
  }

  public long getIssuer() {
    return issuer;
  }

  public void setIssuer(long issuer) {
    this.issuer = issuer;
  }

  public double getMktValue() {
    return mktValue;
  }

  public void setMktValue(double mktValue) {
    this.mktValue = mktValue;
  }

  public double getQty() {
    return qty;
  }

  public void setQty(double qty) {
    this.qty = qty;
  }

  public String getSecId() {
    return secId;
  }

  public void setSecId(String secId) {
    this.secId = secId;
  }

  public String getSecIdIndexed() {
    return secIdIndexed;
  }

  public void setSecIdIndexed(String secIdIndexed) {
    this.secIdIndexed = secIdIndexed;
  }

  public String getSecLinks() {
    return secLinks;
  }

  public void setSecLinks(String secLinks) {
    this.secLinks = secLinks;
  }

  public String getSecType() {
    return secType;
  }

  public void setSecType(String secType) {
    this.secType = secType;
  }

  public double getSharesOutstanding() {
    return sharesOutstanding;
  }

  public void setSharesOutstanding(double sharesOutstanding) {
    this.sharesOutstanding = sharesOutstanding;
  }

  public String getUnderlyer() {
    return underlyer;
  }

  public void setUnderlyer(String underlyer) {
    this.underlyer = underlyer;
  }

  public long getVolatility() {
    return volatility;
  }

  public void setVolatility(long volatility) {
    this.volatility = volatility;
  }

  public int getPid() {
    return pid;
  }

  public void setPid(int pid) {
    this.pid = pid;
  }

  public int getPortfolioId() {
    return portfolioId;
  }

  public void setPortfolioId(int portfolioId) {
    this.portfolioId = portfolioId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Position))
      return false;
    return this.secId.equals(((Position) o).secId);
  }

  @Override
  public int hashCode() {
    return this.secId.hashCode();
  }

  public String toString() {
    return "PositionPdx [secId=" + this.secId + " out=" + this.sharesOutstanding + " type="
        + this.secType + " id=" + this.pid + " mktValue=" + this.mktValue + "]";
  }
}
