package com.kko.pay.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import com.kko.pay.common.ResultCode;
import com.kko.pay.entity.Payment;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class ControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper obmapper;

	String tid;

	@Test
	public void testPayments() throws Exception {
		testPayment();
		testPayHis(tid);
		testPayCancel(tid);
	}

	public void testPayment() throws Exception {
		Payment payment = new Payment();
		payment.setCardNo("1234123412341234");
		payment.setAmount(1000);
		payment.setExpDate("0620");
		payment.setCvc("123");
		payment.setInstmt("11");
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
	}

	public void testPayHis(String tid) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/payHis").param("tid", tid)).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.resultCd").value(ResultCode.success.getCode()));
	}

	public void testPayCancel(String tid) throws JsonProcessingException, Exception {
		Payment payment = new Payment();
		payment.setTid(tid);
		payment.setAmount(1000);
		mockMvc.perform(MockMvcRequestBuilders.post("/payCancel").content(obmapper.writeValueAsString(payment))
				.contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.resultCd").value(ResultCode.success.getCode()));

	}

}
