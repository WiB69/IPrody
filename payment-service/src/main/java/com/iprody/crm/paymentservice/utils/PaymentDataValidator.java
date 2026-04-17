package com.iprody.crm.paymentservice.utils;

import com.iprody.crm.paymentservice.exception.PaymentException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataValidator {

    public static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = 10;
    private static final int FIRST_LIMIT = 25;
    private static final int SECOND_LIMIT = 50;

    public Pageable validatePageParamsAndGetValid(Integer offset, Integer limit) {
        int validOffset = getValidOffset(offset);
        int validLimit = getValidLimit(limit);
        return PageRequest.of(validOffset, validLimit);
    }

    private int getValidOffset(Integer offset) {
        int result = offset != null ? offset : DEFAULT_OFFSET;
        if (result < 0) {
            throw new PaymentException("offset must be greater than 0");
        }
        return result;
    }

    private int getValidLimit(Integer limit) {
        int result = limit != null ? limit : DEFAULT_LIMIT;
        if (result < 0) {
            throw new PaymentException("limit must be greater than 0");
        }
        return applyLimitBounds(result);
    }

    private int applyLimitBounds(int limit) {
        if (limit > DEFAULT_LIMIT && limit <= FIRST_LIMIT) {
            return FIRST_LIMIT;
        }
        if (limit > FIRST_LIMIT) {
            return SECOND_LIMIT;
        }
        return limit;
    }
}
