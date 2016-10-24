package com.sandy.mock;

import org.springframework.beans.factory.annotation.Autowired;

public class InvoiceCalculator {

    @Autowired private InternalSpendService internalSpendService;
    @Autowired private ExternalSpendService externalSpendService;
    @Autowired private ErrorSpendService errorSpendService;
    @Autowired private MarginService marginService;

    public double calculateInvoice(int customerId) {
        double spend = internalSpendService.getSpend(customerId) + externalSpendService.getSpend(customerId);
        double errorSpend = errorSpendService.getSpend(customerId);

        double totalSpend = spend - errorSpend;
        totalSpend = totalSpend < 0 ? 0 : totalSpend;

        double margin = marginService.getMargin(customerId);
        if (margin < 0)
            throw new IllegalArgumentException("Invalid Margin");

        return totalSpend / (1 - margin);
    }

}
