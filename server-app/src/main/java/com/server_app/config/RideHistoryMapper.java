package com.server_app.config;

import com.server_app.dto.RideHistorySearchCriteria;
import com.uber.common.dto.RideHistoryFilterDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class RideHistoryMapper {
    public RideHistorySearchCriteria toSearchCriteria(RideHistoryFilterDto filterDto) {
        return RideHistorySearchCriteria.builder()
                .startDate(filterDto.getStartDate())
                .endDate(filterDto.getEndDate())
                .status(filterDto.getStatus())
                .isPaid(filterDto.getIsPaid())
                .paymentType(filterDto.getPaymentType())
                .build();
    }

    public PageRequest toPageRequest(RideHistoryFilterDto filterDto) {
        Sort.Direction direction = Sort.Direction.valueOf(
                filterDto.getSortDirection() != null ? filterDto.getSortDirection().toUpperCase() : "DESC"
        );

        String sortBy = filterDto.getSortBy() != null ? filterDto.getSortBy() : "createdAt";

        return PageRequest.of(
                filterDto.getPage() != null ? filterDto.getPage() : 0,
                filterDto.getSize() != null ? filterDto.getSize() : 20,
                Sort.by(direction, sortBy)
        );
    }
}