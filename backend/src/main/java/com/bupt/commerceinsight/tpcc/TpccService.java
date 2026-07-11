package com.bupt.commerceinsight.tpcc;

import com.bupt.commerceinsight.tpcc.dto.NewOrderRequest;
import com.bupt.commerceinsight.tpcc.dto.PaymentRequest;
import com.bupt.commerceinsight.tpcc.vo.NewOrderVO;
import com.bupt.commerceinsight.tpcc.vo.PaymentVO;

public interface TpccService {

    NewOrderVO newOrder(NewOrderRequest request);

    PaymentVO payment(PaymentRequest request);
}
