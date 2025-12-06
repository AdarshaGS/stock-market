package com.stocks.diversification.recommendations.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocks.diversification.recommendations.data.Recommendation;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

}
