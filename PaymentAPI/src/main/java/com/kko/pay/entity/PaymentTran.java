package com.kko.pay.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
@Entity
@Table(name = "paymentTran")
public class PaymentTran {

	@Id
	private String tid;
	@NotEmpty
	@Column(length = 1000)
	private String encData; // 결제String
	private String payDiv;
	private String oid;
	private int amount, vat;
	private String encCardInfo;
	private String regDt;

}
