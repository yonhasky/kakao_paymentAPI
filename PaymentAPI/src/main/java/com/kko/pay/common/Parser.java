package com.kko.pay.common;

import java.util.HashMap;
import java.util.Map;

import com.kko.pay.entity.Payment;

public class Parser {

	/**
	 * 원문 데이터 파싱
	 * 
	 * @param endData
	 * @return
	 */
	public static Map<String, Object> makeDecData(String endData) {

		int div[] = new int[] { 4, 10, 20, 20, 2, 4, 3, 10, 10, 20, 300, 47 };
		int cnt = div.length;
		int spt = 0;
		String[] rs = { "dataLength", "payDiv", "tid", "cardNo", "instmt", "expDate", "cvc", "amount", "vat", "oid",
				"encCardInfo", "option" };

		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < cnt; i++) {
			map.put(rs[i], (endData.substring(spt, (spt + div[i]))).trim());
			spt += div[i];
		}

		return map;

	}

	/**
	 * 결제 데이터 생성
	 * 
	 * @param card
	 * @return
	 */
	public static String makeEncData(Payment payment) {

		String cardNo = String.format("%-20s", payment.getCardNo());
		String instmt = String.format("%02d", Integer.parseInt(payment.getInstmt()));
		String expDate = String.format("%-4s", payment.getExpDate());
		String cvc = String.format("%-3s", payment.getCvc());
		String amount = String.format("%10d", payment.getAmount());
		String vat = String.format("%010d", payment.getVat());
		String oid = String.format("%-20s", payment.getOid());
		String encCardInfo = String.format("%-300s", payment.getEncCardInfo());
		String option = String.format("%-47s", "");

		// header
		String payDiv = String.format("%-10s", payment.getPayDiv());
		String tid = String.format("%-20s", payment.getTid());

		String resultData = payDiv + tid + cardNo + instmt + expDate + cvc + amount + vat + oid + encCardInfo + option;
		String dataLength = String.format("%4d", resultData.length());

		String header = dataLength + payDiv + tid; // 헤더 합치기
		resultData = header + cardNo + instmt + expDate + cvc + amount + vat + oid + encCardInfo + option;

		return resultData;

	}
}
