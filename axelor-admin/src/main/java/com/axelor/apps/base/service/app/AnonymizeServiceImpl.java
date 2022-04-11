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
  protected static final int MIN_YEAR_DATE = 2000;
  protected static final int MAX_MONTH_DATE = 12;
  protected static final int MAX_DAY_DATE = 28;
  protected static final int MIN_DATE = 1;

  @Override
  public Object anonymizeValue(Object object, Property property) {
    switch (property.getType()) {
      case TEXT:
      case STRING:
        byte[] shaInBytes = hashString(object.toString());

        if (property.getMaxSize() != null && shaInBytes.length > (int) property.getMaxSize()) {
          return bytesToHex(shaInBytes).substring(0, (int) property.getMaxSize());
        } else {
          return bytesToHex(shaInBytes);
        }

      case INTEGER:
        Random randomInt = new Random();
        return BigInteger.valueOf(randomInt.nextInt() & Integer.MAX_VALUE);

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
        return randomLocalDate();

      case TIME:
        return randomLocalTime();

      case DATETIME:
        return randomLocalDateTime();

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

  protected LocalDateTime randomLocalDateTime() {
    return LocalDateTime.of(randomLocalDate(), randomLocalTime());
  }

  protected LocalDate randomLocalDate() {
    Random randomDate = new Random();
    return LocalDate.of(
        randomDate.nextInt(LocalDate.now().getYear() + 1 - MIN_YEAR_DATE) + MIN_YEAR_DATE,
        randomDate.nextInt(MAX_MONTH_DATE + 1 - MIN_DATE) + 1,
        randomDate.nextInt(MAX_DAY_DATE + 1 - MIN_DATE) + 1);
  }

  protected LocalTime randomLocalTime() {
    Random randomTime = new Random();
    return LocalTime.of(randomTime.nextInt(23), randomTime.nextInt(59), randomTime.nextInt(59));
  }
}
