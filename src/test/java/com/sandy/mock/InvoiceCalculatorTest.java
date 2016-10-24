package com.sandy.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Random;

public class InvoiceCalculatorTest {

    @Mock private InternalSpendService internalSpendService;
    @Mock private ExternalSpendService externalSpendService;
    @Mock private ErrorSpendService errorSpendService;
    @Mock private MarginService marginService;
    @InjectMocks private InvoiceCalculator invoiceCalculator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void whenErrorSpendIsGreaterThanInternalAndExternalSpendThenInvoiceShouldBeZero() throws Exception {
        int customerId = new Random().nextInt();
        Mockito.when(internalSpendService.getSpend(customerId)).thenReturn(100.0);
        Mockito.when(externalSpendService.getSpend(customerId)).thenReturn(20.0);
        Mockito.when(errorSpendService.getSpend(customerId)).thenReturn(150.0);
        Mockito.when(marginService.getMargin(customerId)).thenReturn(0.39);

        Assert.assertEquals(0.0, invoiceCalculator.calculateInvoice(customerId), 0.0);
    }

    @Test (expected = IllegalArgumentException.class)
    public void whenInvalidMarginIsSpecifiedThenExceptionIsExcepted() throws Exception {
        Mockito.when(internalSpendService.getSpend(Mockito.anyInt())).thenReturn(100.0);
        Mockito.when(externalSpendService.getSpend(Mockito.anyInt())).thenReturn(20.0);
        Mockito.when(errorSpendService.getSpend(Mockito.anyInt())).thenReturn(50.0);
        Mockito.when(marginService.getMargin(Mockito.anyInt())).thenReturn(-123.0);
        invoiceCalculator.calculateInvoice(123);
    }

    @Test
    public void checkOrderOfServicesCalled() throws Exception {
        invoiceCalculator.calculateInvoice(123);

        InOrder inOrder = Mockito.inOrder(internalSpendService, externalSpendService, errorSpendService, marginService);
        inOrder.verify(internalSpendService).getSpend(Mockito.anyInt());
        inOrder.verify(externalSpendService).getSpend(Mockito.anyInt());
        inOrder.verify(errorSpendService).getSpend(Mockito.anyInt());
        inOrder.verify(marginService).getMargin(Mockito.anyInt());
    }

    @Test
    public void whenZeroMarginIsSpecifiedThenInvoiceIsSameAsSpend() throws Exception {
        int customerId = new Random().nextInt();
        Mockito.when(internalSpendService.getSpend(customerId)).thenReturn(100.0);
        Mockito.when(externalSpendService.getSpend(customerId)).thenReturn(20.0);
        Mockito.when(errorSpendService.getSpend(customerId)).thenReturn(50.0);
        Mockito.when(marginService.getMargin(customerId)).thenReturn(0.0);

        Assert.assertEquals(70.0, invoiceCalculator.calculateInvoice(customerId), 0.01);
    }

    @Test (expected = Exception.class)
    public void exceptionIsThrown() throws Exception {
        int customerId = new Random().nextInt();
        Mockito.doThrow(new Exception("Test")).when(marginService.getMargin(customerId));

        invoiceCalculator.calculateInvoice(customerId);
    }

    @Test
    public void verifyCorrectInvoiceIsCalculated() throws Exception {
        int customerId = new Random().nextInt();

        Mockito.doAnswer(o -> 100.0).when(internalSpendService).getSpend(customerId);
        Mockito.doReturn(20.0).when(externalSpendService).getSpend(customerId);
        Mockito.when(errorSpendService.getSpend(customerId)).thenReturn(10.0);
        Mockito.when(marginService.getMargin(customerId)).thenReturn(0.39);

        Assert.assertEquals(180.33, invoiceCalculator.calculateInvoice(customerId), 0.01);
    }

    @Test
    public void verifyIfCorrectArgumentsArePassed() throws Exception {
        int customerId = new Random().nextInt();
        invoiceCalculator.calculateInvoice(customerId);

        ArgumentCaptor<Integer> customerIdCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(internalSpendService).getSpend(customerIdCaptor.capture());
        Assert.assertEquals(customerId, customerIdCaptor.getValue().intValue());
    }
}
