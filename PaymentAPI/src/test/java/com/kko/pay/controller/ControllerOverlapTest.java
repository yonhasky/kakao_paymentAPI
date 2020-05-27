package com.kko.pay.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

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
import com.kko.pay.entity.Payment;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class ControllerOverlapTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper obmapper;

	String tid;

	@Test
	public void testOverlapPayment() throws Exception {
		// TEST1 중복결제

		Payment payment = new Payment();
		payment.setCardNo("1010202030304040");
		payment.setAmount(11000);
		payment.setExpDate("0620");
		payment.setCvc("123");
		payment.setInstmt("11");
		payment.setVat(1000);
		testPayment(payment);
		testPayment(payment);
		testPayment(payment);

	}

	@Test
	public void testOverlapCancel() throws Exception {

		// TEST2 중복취소
		Payment payment = new Payment();
		payment.setCardNo("2020303040405050");
		payment.setAmount(20000);
		payment.setExpDate("0620");
		payment.setCvc("123");
		payment.setInstmt("11");
		payment.setVat(909);
		String tid = testPayment(payment);

		Payment cPay = new Payment();
		cPay = new Payment();
		cPay.setTid(tid);
		cPay.setAmount(20000);
		cPay.setVat(909);

		testPayCancel(cPay);
		testPayCancel(cPay);
		testPayCancel(cPay);

	}

	@Test
	public void testOverlapPartialCancel() throws Exception {

		// TEST3 중복부분취소
		Payment payment = new Payment();

		payment.setCardNo("4040505060607070");
		payment.setAmount(20000);
		payment.setExpDate("0620");
		payment.setCvc("123");
		payment.setInstmt("11");
		payment.setVat(null);
		String tid = testPayment(payment);

		Payment cPay = new Payment();
		cPay.setTid(tid);
		cPay.setAmount(1000);
		cPay.setVat(100);
		testPayCancel(cPay);
		testPayCancel(cPay);
		testPayCancel(cPay);

	}

	/**
	 * 결제 테스트
	 * 
	 * @param payment
	 * @throws Exception
	 */
	public String testPayment(Payment payment) throws Exception {
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.post("/payment").content(obmapper.writeValueAsString(payment))
						.contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"))
				.andDo(print()).andExpect(status().isOk()).andReturn();

		String content = result.getResponse().getContentAsString();

		@SuppressWarnings("unchecked")
		Map<String, Object> map = obmapper.readValue(content, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, String> resultData = (Map<String, String>) map.get("resultData");
		
		return resultData.get("tid");

	}

	/**
	 * 결제취소 테스트
	 * 
	 * @param payment
	 * @param resultCd
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	public void testPayCancel(Payment payment) throws JsonProcessingException, Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/payCancel").content(obmapper.writeValueAsString(payment))
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")).andDo(print())
				.andExpect(status().isOk());
	}

}
