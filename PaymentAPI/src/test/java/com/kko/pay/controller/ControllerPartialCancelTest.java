package com.kko.pay.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kko.pay.common.ResultCode;
import com.kko.pay.entity.Payment;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class ControllerPartialCancelTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper obmapper;

	String tid;

	@Test
	public void testPartialCancel1() throws Exception {
		// TEST1 부분취소

		Payment payment = new Payment();
		payment.setCardNo("1010202030304040");
		payment.setAmount(11000);
		payment.setExpDate("0620");
		payment.setCvc("123");
		payment.setInstmt("11");
		payment.setVat(1000);
		testPayment(payment);
		Thread.sleep(1000);

		Payment cPay = new Payment();
		cPay.setTid(tid);
		cPay.setAmount(1100);
		cPay.setVat(100);
		testPayCancel(cPay, ResultCode.success);
		Thread.sleep(1000);

		cPay.setAmount(3300);
		cPay.setVat(null);
		testPayCancel(cPay, ResultCode.success);
		Thread.sleep(1000);

		cPay.setAmount(7000);
		testPayCancel(cPay, ResultCode.over_cancel_amount);
		Thread.sleep(1000);

		cPay.setAmount(6600);
		cPay.setVat(700);
		testPayCancel(cPay, ResultCode.over_cancel_vat);
		Thread.sleep(1000);

		cPay.setAmount(6600);
		cPay.setVat(600);
		testPayCancel(cPay, ResultCode.success);
		Thread.sleep(1000);

		cPay.setAmount(100);
		cPay.setVat(null);
		testPayCancel(cPay, ResultCode.over_cancel_amount);

	}

	@Test
	public void testPartialCancel2() throws Exception {

		// TEST2 부분취소
		Payment payment = new Payment();
		payment.setCardNo("2020303040405050");
		payment.setAmount(20000);
		payment.setExpDate("0620");
		payment.setCvc("123");
		payment.setInstmt("11");
		payment.setVat(909);
		testPayment(payment);
		Thread.sleep(1000);

		Payment cPay = new Payment();
		cPay = new Payment();
		cPay.setTid(tid);
		cPay.setAmount(10000);
		cPay.setVat(0);
		testPayCancel(cPay, ResultCode.success);
		Thread.sleep(1000);

		cPay.setAmount(10000);
		testPayCancel(cPay, ResultCode.remain_vat);
		Thread.sleep(1000);

		cPay.setAmount(10000);
		cPay.setVat(909);
		testPayCancel(cPay, ResultCode.success);
		Thread.sleep(1000);
	}

	@Test
	public void testPartialCancel3() throws Exception {

		// TEST3 부분취소
		Payment payment = new Payment();

		payment.setCardNo("4040505060607070");
		payment.setAmount(20000);
		payment.setExpDate("0620");
		payment.setCvc("123");
		payment.setInstmt("11");
		payment.setVat(null);
		testPayment(payment);
		Thread.sleep(1000);

		Payment cPay = new Payment();
		cPay.setTid(tid);
		cPay.setAmount(10000);
		cPay.setVat(1000);
		testPayCancel(cPay, ResultCode.success);
		Thread.sleep(1000);

		cPay.setAmount(10000);
		cPay.setVat(909);
		testPayCancel(cPay, ResultCode.over_cancel_vat);
		Thread.sleep(1000);

		cPay.setAmount(10000);
		cPay.setVat(null);
		testPayCancel(cPay, ResultCode.success);

	}

	/**
	 * 결제 테스트
	 * 
	 * @param payment
	 * @throws Exception
	 */
	public void testPayment(Payment payment) throws Exception {
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.post("/payment").content(obmapper.writeValueAsString(payment))
						.contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCd").value(ResultCode.success.getCode())).andReturn();

		String content = result.getResponse().getContentAsString();

		@SuppressWarnings("unchecked")
		Map<String, Object> map = obmapper.readValue(content, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, String> resultData = (Map<String, String>) map.get("resultData");
		this.tid = resultData.get("tid");

		Assertions.assertThat(map.get("resultCd")).isEqualTo(ResultCode.success.getCode());

	}

	/**
	 * 결제취소 테스트
	 * 
	 * @param payment
	 * @param resultCd
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	public void testPayCancel(Payment payment, ResultCode resultCd) throws JsonProcessingException, Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/payCancel").content(obmapper.writeValueAsString(payment))
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.resultCd").value(resultCd.getCode()));
	}

}
