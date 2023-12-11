package com.mabsplace.mabsplaceback.utils;

import java.util.UUID;

public class Utils {
  public static String generateUniqueName(String entityId, String extension) {
    String uuid = UUID.randomUUID().toString();
    return entityId + "-" + uuid + "." + extension;
  }

  public static String generateUniqueName2(long entityId, String entityName) {
    return entityId + "-" + entityName + "-" + "image";
  }
}