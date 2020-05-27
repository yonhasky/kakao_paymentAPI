package com.kko.pay.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.kko.pay.entity.PaymentTran;
import java.util.List;

@Repository
public interface PaymentTranRepository extends CrudRepository<PaymentTran, String> {
	List<PaymentTran> findAllByEncCardInfoAndPayDivAndRegDt(String encCardInfo, String payDiv, String regDt);

}
