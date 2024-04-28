package com.mytech.api.repositories.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mytech.api.models.wallet.WalletType;

@Repository
public interface WalletTypeRepository extends JpaRepository<WalletType, Integer> {

}
