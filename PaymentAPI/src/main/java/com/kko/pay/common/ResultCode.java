package com.kko.pay.common;

public enum ResultCode {

	success("0000", "성공")
	, fail("9999", "실패")
	, required_fail("9001", "필수 요청파라미터가 없습니다.")
	, no_search_data("9002", "조회내역이 없습니다.")
	, no_pay_data("9003", "결제 내역이 없습니다.")
	, over_cancel_amount("9004", "취소금액이 잔여 결제금액을 초과하였습니다.")
	, over_cancel_vat("9005", "취소 부가세금액이 잔여 부가세금액을 초과하였습니다.")
	, already_cancel("9006", "이미 취소된 거래입니다.")
	, remain_vat("9007", "취소시 잔여 부가세가 존재합니다.")
	, over_vat("9008", "부가가치세는 결제금액보다 클 수 없습니다.")
	, dup_fail("9009", "결제를 실패하였습니다.")
	;

	String code;
	String msg;

	private ResultCode(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return msg;
	}

	public Object getMsg() {
		return msg;
	}
}