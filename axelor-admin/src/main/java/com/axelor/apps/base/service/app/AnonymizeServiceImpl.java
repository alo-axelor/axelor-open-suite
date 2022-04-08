package com.axelor.apps.base.service.app;

import com.axelor.db.mapper.Property;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnonymizeServiceImpl implements AnonymizeService {

  protected Logger LOG = LoggerFactory.getLogger(getClass());

  @Override
  public Object anonymizeValue(Object object, Property property) {
    switch (property.getType()) {
      case STRING:
        byte[] shaInBytes = hashString(object.toString());

        if (property.getMaxSize() != null && shaInBytes.length > (int) property.getMaxSize()) {
          return bytesToHex(shaInBytes).substring(0, (int) property.getMaxSize());
        } else {
          return bytesToHex(shaInBytes);
        }

      case INTEGER:
        Random randomInt = new Random();
        return BigInteger.valueOf(randomInt.nextInt());

      case LONG:
        Random randomLong = new Random();
        return randomLong.nextLong();

      case DOUBLE:
        Random randomDouble = new Random();
        return randomDouble.nextDouble();

      case DECIMAL:
        Random randomDecimal = new Random();
        if (property.getScale() != 0) {
          return new BigDecimal(randomDecimal.nextInt() & Integer.MAX_VALUE)
              .setScale(property.getScale(), RoundingMode.HALF_UP);
        } else {
          return new BigDecimal(randomDecimal.nextInt() & Integer.MAX_VALUE)
              .setScale(2, RoundingMode.HALF_UP);
        }

      case DATE:
        Random randomDate = new Random();
        return LocalDate.of(
            randomDate.nextInt(9999), randomDate.nextInt(12), randomDate.nextInt(28));

      case TIME:
        Random randomTime = new Random();
        return LocalTime.of(randomTime.nextInt(23), randomTime.nextInt(59), randomTime.nextInt(59));

      case DATETIME:
        Random randomDateTime = new Random();
        return LocalDateTime.of(
            LocalDate.of(
                randomDateTime.nextInt(9999),
                randomDateTime.nextInt(12),
                randomDateTime.nextInt(28)),
            LocalTime.of(
                randomDateTime.nextInt(23),
                randomDateTime.nextInt(59),
                randomDateTime.nextInt(59)));

      default:
        return null;
    }
  }

  protected byte[] hashString(String data) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
      md.update(getSalt());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }
    byte[] result = md.digest(data.getBytes(StandardCharsets.UTF_8));
    return result;
  }

  protected byte[] getSalt() throws NoSuchAlgorithmException {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return salt;
  }

  protected String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
