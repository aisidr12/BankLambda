package com.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


public class LambdaBank implements RequestHandler<BankRequest,BankResponse> {
/*
P = MONTO DEL PRESTAMO
I = TASA DE INTEREES MENSUAL
N = PLAZO DEL CREDIO EN MESES

FORMULA para obtener la cuota mensual  ===>   (P*I)/(1- (1+i)^(-n))
 */

    @Override
    public BankResponse handleRequest(BankRequest bankRequest, Context context) {
        context.getLogger().log("Getting the request "+ bankRequest.toString());
        MathContext mathContext = MathContext.DECIMAL128;
        //amount
        BigDecimal amount = bankRequest.getAmount().setScale(2, RoundingMode.HALF_UP);
        //rate (tasa in spanish)
        BigDecimal monthlyRate  = bankRequest.getRate()
                .setScale(2, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), mathContext);
        //rate when is a registered client
        BigDecimal monthlyRateWithAccount  = bankRequest.getRate()
                .subtract(BigDecimal.valueOf(0.2),mathContext)
                .setScale(2,RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), mathContext);
        Integer term = bankRequest.getTerm();

        BigDecimal monthlyPayment = this.calculateQuota(amount, monthlyRate, term, mathContext);
        BigDecimal monthlyPaymentWithAccount = this.calculateQuota(amount, monthlyRateWithAccount, term, mathContext);

        BankResponse bankResponse = new BankResponse();
        bankResponse.setQuota(monthlyPayment);
        bankResponse.setRate(monthlyRate);
        bankResponse.setTerm(term);
        bankResponse.setQuotaWithAccount(monthlyPaymentWithAccount);
        bankResponse.setRateWithAccount(monthlyRateWithAccount);
        bankResponse.setTermWithAccount(term);

        return bankResponse;
    }

    private BigDecimal calculateQuota(BigDecimal amount,BigDecimal rate, Integer term,MathContext mathContext){
       //  (P*I)/(1- (1+i)^(-n))
//        P = MONTO DEL PRESTAMO
//        I = TASA DE INTEREES MENSUAL
//        N = PLAZO DEL CREDIO EN MESES
        BigDecimal onePlusRate = rate.add(BigDecimal.ONE, mathContext);
        // calculate (1+i)^ -n
        BigDecimal onePlusRateToN = onePlusRate.negate().pow(term,mathContext);
        BigDecimal onePlusRateToNegative = BigDecimal.ONE.divide(onePlusRate,mathContext);
        //calculate monthly quota
        BigDecimal numerator = amount.multiply(rate, mathContext);
        BigDecimal denominator = BigDecimal.ONE.subtract(onePlusRateToNegative, mathContext);
        BigDecimal monthlyPayment = numerator.divide(denominator, mathContext);
        // Set the result to 2 decimals
        monthlyPayment = monthlyPayment.setScale(2, RoundingMode.HALF_UP);
        return monthlyPayment;
    }
}
