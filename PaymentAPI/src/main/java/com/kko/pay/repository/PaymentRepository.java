package com.kko.pay.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.kko.pay.entity.Payment;
@Repository
public interface PaymentRepository extends CrudRepository<Payment, String> {

}
