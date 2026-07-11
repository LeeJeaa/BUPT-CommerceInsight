package com.bupt.commerceinsight.tpcc;

import com.bupt.commerceinsight.common.ApiResponse;
import com.bupt.commerceinsight.tpcc.dto.NewOrderRequest;
import com.bupt.commerceinsight.tpcc.dto.PaymentRequest;
import com.bupt.commerceinsight.tpcc.vo.NewOrderVO;
import com.bupt.commerceinsight.tpcc.vo.PaymentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tpcc")
public class TpccController {

    private final TpccService tpccService;

    public TpccController(TpccService tpccService) {
        this.tpccService = tpccService;
    }

    @PostMapping("/new-order")
    public ApiResponse<NewOrderVO> newOrder(@Valid @RequestBody NewOrderRequest request) {
        return ApiResponse.success(tpccService.newOrder(request));
    }

    @PostMapping("/payment")
    public ApiResponse<PaymentVO> payment(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.success(tpccService.payment(request));
    }
}
