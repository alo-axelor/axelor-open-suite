package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppSupplychain;

import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineOnChangeProjectServiceImpl {

    protected AccountConfigRepository accountConfigRepository;

    @Override
    public Map<String, Object> invoicingModeSelectOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
            throws AxelorException {

        Map<String, Object> saleOrderLineMap = new HashMap<>();
        int invoicingModeSelect = saleOrderLine.getInvoicingModeSelect();
        boolean toInvoice = invoicingModeSelect == SaleOrderLineRepository.INVOICING_MODE_PROGRESS_BILLING || invoicingModeSelect == SaleOrderLineRepository.INVOICING_MODE_ON_DELIVERY;
        saleOrderLine.setToInvoice(toInvoice);
        saleOrderLineMap.put("toInvoice", toInvoice);
        return saleOrderLineMap;
    }

    protected void checkCoefficientConfig(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
        AccountConfig accountConfig = accountConfigRepository.findByCompany(saleOrder.getCompany());
        if (accountConfig == null) {
            return;
        }

        int invoicingModeSelect = saleOrderLine.getInvoicingModeSelect();
        if(accountConfig.getIsInvoiceCoefficientEnabled() && invoicingModeSelect == SaleOrderLineRepository.INVOICING_MODE_PROGRESS_BILLING){
            throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get("Coefficient on invoice line must be enabled for progress billing."));
        }
    }
}
