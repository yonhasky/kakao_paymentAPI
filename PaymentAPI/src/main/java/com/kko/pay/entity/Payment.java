package com.kko.pay.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
@Entity
public class Payment {

	@Id
	private String tid;
	@NotEmpty
	@Size(min = 10, max = 20)
	private String cardNo;
	@NotEmpty
	private String cvc;
	@NotEmpty
	@Size(min = 4, max = 4)
	private String expDate;
	private String oid;
	@NotNull
	@Min(100)
	private Integer amount;
	private Integer vat;
	@NotEmpty
	@Min(2)
	@Max(12)
	private String instmt;
	private String encData;
	private String payDiv;
	private String encCardInfo;

}
