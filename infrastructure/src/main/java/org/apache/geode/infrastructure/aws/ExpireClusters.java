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
package org.apache.geode.infrastructure.aws;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Tag;

public class ExpireClusters {
  static final int days_old = 7;

  static Ec2Client ec2 = Ec2Client.create();

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 0) {
      usage("Usage: ExpireClusters");
      return;

    }

    List<String> expiredClusters = findExpiredClusters();
    for (String expiredCluster : expiredClusters) {
      String[] arguments = new String[1];
      arguments[0] = expiredCluster;

      System.out.println("Destroying cluster with tag " + expiredCluster + ".");
      DestroyCluster.main(arguments);
    }
  }

  private static List<String> findExpiredClusters() {
    Instant expirationTime = getExpirationTime().toInstant();

    DescribeInstancesResponse describeInstancesResponse = ec2.describeInstances(
        DescribeInstancesRequest.builder()
            .filters(Filter.builder()
                .name("instance-state-name")
                .values("running")
                .build())
            .build());
    Stream<Instance> instances = describeInstancesResponse.reservations().stream().flatMap(reservation -> reservation.instances().stream());

    Stream<Instance> expiredInstances = instances.filter(instance -> isBefore(instance.getValueForField("LaunchTime", Instant.class), expirationTime));
    Stream<String> tags = expiredInstances.map(ExpireClusters::getTagForInstance);
    List<String> distinctTags = tags.distinct().filter(tag -> !tag.isEmpty()).collect(Collectors.toList());
    return distinctTags;
  }

  private static boolean isBefore(Optional<Instant> launchTime, Instant expirationTime) {
    if(launchTime.isPresent()) {
      if(launchTime.get().isBefore(expirationTime)) {
        return true;
      }
    }
    return false;
  }

  private static String getTagForInstance(Instance expiredInstance) {
    Stream<Tag> expiredInstanceTagStream = expiredInstance.tags().stream();
    Stream<Tag> geodeBenchmarksTagStream = expiredInstanceTagStream.filter(tag -> tag.key().equals("geode-benchmarks"));
    List<String> expiredTags = geodeBenchmarksTagStream.map(Tag::value).collect(Collectors.toList());
    if (expiredTags.size() > 0) {
      return expiredTags.get(0);
    }
    else {
      return "";
    }
  }

//  private static Date convertToDate(Optional<Instant> optionalDate) {
//    try {
//      // "2018-03-20T21:38:47.000Z"
//      if(optionalDate.isPresent()) {
//        String date = optionalDate.get();
//        return new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSSZ").parse(date);
//      }
//    } catch (ParseException e) {
//      e.printStackTrace();
//    }
//
//    return null;
//  }

  private static void usage(String s) {
    throw new IllegalStateException(s);
  }

  private static Date getExpirationTime() {
    long DAY_IN_MS = 1000 * 60 * 60 * 24;
    return new Date(System.currentTimeMillis() - (days_old * DAY_IN_MS));
  }
}
