package com.kko.pay.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class CommUtil {
	
	public static Map<String, Object> resMap(ResultCode code, Object obj) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put("resultCd", code.getCode());
		resultMap.put("resultMsg", code.getMsg());
		resultMap.put("resultData", obj);

		return resultMap;
	}

	/**
	 * 거래 ID 생성(년월일시분초+랜덤6) 20자리
	 * 
	 * @return
	 */
	public static String makeId() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String time = sdf.format(date);
		Random rd = new Random(System.currentTimeMillis());
		int ran = Math.abs(rd.nextInt(899990) + 100000);
		String id = time + ran;

		return id;
	}

	/**
	 * 부가세 계산
	 * 
	 * @param amount
	 * @return
	 */
	public static int calVat(int amount) {
		int vat = 0;
		double dbVal = amount;
		dbVal = dbVal / 11;
		vat = (int) (Math.round(dbVal));

		return vat;
	}

	/**
	 * 현재날짜 구하기
	 * 
	 * @return
	 */
	public static String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String time = sdf.format(date);
		return time;
	}

	/**
	 * 카드번호 마스킹(10~16)
	 * @param cardNo
	 * @return
	 */
	public static String maskedCardNo(String cardNo) {
		String maskedCardNo = "";
		String lastNo = cardNo.substring(cardNo.length() - 3, cardNo.length()); // 카드 마지막3자리
		cardNo = cardNo.replaceAll("(?<=.{6}).", "*");
		cardNo = cardNo.substring(0, cardNo.length() - 3);
		cardNo += lastNo;
		maskedCardNo = cardNo;
		
		return maskedCardNo;
	}

}
