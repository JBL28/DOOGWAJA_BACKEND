package dev.ssafy.domain.boughtsnack.entity;

import lombok.Getter;

@Getter
public enum BoughtSnackStatusEnum {
    배송중("배송중"),
    재고있음("재고있음"),
    재고없음("재고없음");

    private final String value;

    BoughtSnackStatusEnum(String value) {
        this.value = value;
    }
}
