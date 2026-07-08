package com.bupt.commerceinsight.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successUsesFrozenResponseShape() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("ok");
    }
}
