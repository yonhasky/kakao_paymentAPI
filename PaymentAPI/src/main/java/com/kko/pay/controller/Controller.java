package com.kko.pay.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kko.pay.common.CommUtil;
import com.kko.pay.common.ResultCode;
import com.kko.pay.entity.Payment;
import com.kko.pay.service.PaymentService;

@RestController
public class Controller {

	@Autowired
	private PaymentService payService;

	public Controller() {
	}

	@PostMapping(value = "/payment", produces = "application/json; charset=utf-8")
	@ResponseStatus(value = HttpStatus.OK)
	public Object payment(@RequestBody @Valid Payment payment, BindingResult bResult) {

		System.out.println(payment.toString());
		Map<String, Object> map = new LinkedHashMap<>();
		ResultCode resultCd = ResultCode.fail;

		if (bResult.hasErrors()) {
			resultCd = ResultCode.required_fail;
			return CommUtil.resMap(resultCd, map);
		} else {
			map = payService.payment(payment);
		}

		return map;
	}

	@GetMapping(value = "/payHis", produces = "application/json; charset=utf-8")
	@ResponseStatus(value = HttpStatus.OK)
	public Map<String, Object> payHis(String tid) {
		Map<String, Object> map = new LinkedHashMap<>();
		map = payService.payHis(tid);

		return map;
	}

	@PostMapping(value = "/payCancel", produces = "application/json; charset=utf-8")
	@ResponseStatus(value = HttpStatus.OK)
	public Map<String, Object> payCancel(@RequestBody Payment payment) {
		Map<String, Object> map = new LinkedHashMap<>();
		ResultCode resultCd = ResultCode.fail;

		System.out.println(payment.toString());
		if (payment.getAmount() == null || payment.getTid() == null) {
			resultCd = ResultCode.required_fail;
			return CommUtil.resMap(resultCd, map);
		}

		map = payService.payCancel(payment);

		return map;
	}

}
