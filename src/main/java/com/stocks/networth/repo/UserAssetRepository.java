package com.stocks.networth.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.networth.data.UserAsset;

@Repository
public interface UserAssetRepository extends JpaRepository<UserAsset, Long> {
    List<UserAsset> findByUserId(Long userId);
}
