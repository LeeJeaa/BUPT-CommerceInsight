package com.bupt.commerceinsight.tpcc;

import com.bupt.commerceinsight.tpcc.dto.NewOrderItemRequest;
import com.bupt.commerceinsight.tpcc.dto.NewOrderRequest;
import com.bupt.commerceinsight.tpcc.dto.PaymentRequest;
import com.bupt.commerceinsight.tpcc.vo.NewOrderVO;
import com.bupt.commerceinsight.tpcc.vo.PaymentVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockTpccService implements TpccService {

    private static final DateTimeFormatter ID_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private final AtomicLong newOrderSequence = new AtomicLong(0);
    private final AtomicLong paymentSequence = new AtomicLong(0);

    @Override
    public NewOrderVO newOrder(NewOrderRequest request) {
        long start = System.currentTimeMillis();
        long orderId = 3000 + newOrderSequence.incrementAndGet();
        BigDecimal totalAmount = request.getItems().stream()
            .map(this::mockAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        return new NewOrderVO(nextId("NO", newOrderSequence.get()), "committed", orderId, totalAmount, elapsed(start));
    }

    @Override
    public PaymentVO payment(PaymentRequest request) {
        long start = System.currentTimeMillis();
        long sequence = paymentSequence.incrementAndGet();
        BigDecimal newBalance = new BigDecimal("620.25").subtract(request.getPaymentAmount()).setScale(2, RoundingMode.HALF_UP);
        return new PaymentVO(nextId("PAY", sequence), "committed", request.getCustomerId(), newBalance, elapsed(start));
    }

    private BigDecimal mockAmount(NewOrderItemRequest item) {
        return new BigDecimal("25.10").multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private String nextId(String prefix, long sequence) {
        return "%s-%s-%04d".formatted(prefix, LocalDate.now().format(ID_DATE), sequence);
    }

    private Long elapsed(long start) {
        return Math.max(1L, System.currentTimeMillis() - start);
    }
}
