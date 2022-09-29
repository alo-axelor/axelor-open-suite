/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;

@Singleton
public class TaxService {

  /**
   * Fonction permettant de récupérer le taux de TVA d'une TVA
   *
   * @param tax Une TVA
   * @return Le taux de TVA
   * @throws AxelorException
   */
  public BigDecimal getTaxRate(Tax tax, LocalDate localDate) throws AxelorException {

    return this.getTaxLine(tax, localDate).getValue();
  }

  /**
   * Fonction permettant de récupérer le taux de TVA d'une TVA
   *
   * @param tax Une TVA
   * @return Le taux de TVA
   * @throws AxelorException
   */
  public TaxLine getTaxLine(Tax tax, LocalDate localDate) throws AxelorException {

    if (tax == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(BaseExceptionMessage.TAX_2));
    }

    if (tax.getActiveTaxLine() != null) {
      return tax.getActiveTaxLine();
    }
    if (localDate != null) {
      if (tax.getTaxLineList() != null && !tax.getTaxLineList().isEmpty()) {

        for (TaxLine taxLine : tax.getTaxLineList()) {

          if (DateTool.isBetween(taxLine.getStartDate(), taxLine.getEndDate(), localDate)) {
            return taxLine;
          }
        }
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.TAX_DATE_MISSING),
          tax.getName());
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BaseExceptionMessage.TAX_1),
        tax.getName());
  }

  /**
   * Convert a product's unit price from incl. tax to ex. tax or the other way round.
   *
   * <p>If the price is ati, it will be converted to ex. tax, and if it isn't it will be converted
   * to ati.
   *
   * @param priceIsAti a boolean indicating if the price is ati.
   * @param taxLine the tax to apply.
   * @param price the unit price to convert.
   * @return the converted price as a BigDecimal.
   */
  public BigDecimal convertUnitPrice(Boolean priceIsAti, TaxLine taxLine, BigDecimal price) {

    if (taxLine == null) {
      return price;
    }

    if (priceIsAti) {
      price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    } else {
      price = price.add(price.multiply(taxLine.getValue()));
    }
    return price;
  }
}
