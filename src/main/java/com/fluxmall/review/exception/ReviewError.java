package com.fluxmall.review.exception;

import com.fluxmall.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewError implements ErrorCode {

    NOT_FOUND("RV001", "리뷰를 찾을 수 없습니다."),
    NOT_REVIEW_OWNER("RV010", "해당 리뷰에 대한 권한이 없습니다."),
    ALREADY_REVIEWED("RV020", "이미 리뷰를 작성한 상품입니다."),
    ORDER_NOT_DELIVERED("RV021", "배송 완료된 주문만 리뷰를 작성할 수 있습니다."),
    INVALID_RATING("RV030", "평점은 1~5 사이여야 합니다."),
    TOO_MANY_IMAGES("RV031", "이미지는 최대 5장까지 첨부할 수 있습니다.");

    private final String code;
    private final String message;
}
