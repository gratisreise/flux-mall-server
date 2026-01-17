package com.fluxmall.review.service;


import com.fluxmall.review.domain.Review;
import com.fluxmall.review.repository.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    public Review findById(Long id) {
        return null;
    }

    public List<Review> findAllByProductId(Long productId) {
        return null;
    }

    public Double getAverageRating(Long productId) {
        return null;
    }

    public void writeReview(Review review) {
    }

    public void updateReview(Review review) {
    }

    public void deleteReview(Long id) {
    }
}