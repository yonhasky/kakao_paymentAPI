package com.kko.pay.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.kko.pay.common.CommUtil;
import com.kko.pay.common.Parser;
import com.kko.pay.common.ResultCode;
import com.kko.pay.encrypt.AES256;
import com.kko.pay.entity.Payment;
import com.kko.pay.entity.PaymentTran;
import com.kko.pay.repository.PaymentTranRepository;

@Service
public class PaymentService {
	@Autowired
	private PaymentTranRepository payReps;

	@Autowired
	private AES256 aes256;
	private static final String PAYMENT = "PAYMENT";
	private static final String CANCEL = "CANCEL";

	/**
	 * 결제
	 * 
	 * @param card
	 * @return
	 */
	@Transactional(rollbackFor = { Exception.class })
	public Map<String, Object> payment(Payment payment) {
		ResultCode resultCd = ResultCode.fail;
		Map<String, Object> resultData = new HashMap<>();

		String tid = "";
		int totalAmt = 0;
		try {

			tid = CommUtil.makeId();// 거래ID발급
			payment.setTid(tid);
			payment.setPayDiv(PAYMENT);

			if (payment.getVat() != null && payment.getVat() > payment.getAmount()) {
				resultCd = ResultCode.over_vat;
				return CommUtil.resMap(resultCd, resultData);
			}

			if (payment.getVat() == null) {
				// 수수료 없을 경우 계산
				int calVat = CommUtil.calVat(payment.getAmount());
				payment.setVat(calVat);
			}

			// encrypt(카드정보|유효기간|cvc)
			String encCardInfo = payment.getCardNo() + "|" + payment.getExpDate() + "|" + payment.getCvc();
			encCardInfo = aes256.encrypt(encCardInfo);
			payment.setEncCardInfo(encCardInfo);

			String encData = Parser.makeEncData(payment);
			payment.setEncData(encData);

			// 결제정보 카드사 전송
			PaymentTran pay = new PaymentTran();
			pay.setTid(tid);
			pay.setEncData(encData);
			pay.setEncCardInfo(encCardInfo);
			pay.setPayDiv("PAYMENT");
			pay.setRegDt(CommUtil.getDate()); // 거래시간

			totalAmt = payment.getAmount() + payment.getVat();

			if (beforeOverlapCheck(pay)) {
				PaymentTran savePay = payReps.save(pay); // 저장
				if (savePay != null && afterOverlapCheck(pay)) {
					resultCd = ResultCode.success;
					resultData.put("tid", tid);
					resultData.put("amount", payment.getAmount());
					resultData.put("vat", payment.getVat());
					resultData.put("totalAmt", totalAmt);
				} else {
					throw new Exception();
				}
			} else {
				resultCd = ResultCode.dup_fail;
			}
		} catch (Exception e) {
			resultCd = ResultCode.fail;
			e.printStackTrace();
		}

		return CommUtil.resMap(resultCd, resultData);

	}

	/**
	 * 조회
	 * 
	 * @param tid
	 * @return
	 */
	public Map<String, Object> payHis(String tid) {
		Map<String, Object> resultData = new LinkedHashMap<>();
		ResultCode resultCd = ResultCode.fail;

		String cardNo = "";
		String cvc = "";
		String expDate = "";
		String payDiv = "";
		String amount = "";
		String vat = "";

		try {
			PaymentTran pay = new PaymentTran();
			if (payReps.findById(tid).isPresent()) {
				pay = payReps.findById(tid).get();

				Map<String, Object> decMap = Parser.makeDecData(pay.getEncData());
				payDiv = decMap.get("payDiv").toString();
				tid = decMap.get("tid").toString();

				resultData.put("tid", tid);
				resultData.put("payDiv", payDiv);

				String encCardInfo = decMap.get("encCardInfo").toString();
				String decCard = aes256.decrypt(encCardInfo);
				String[] cardSplt = decCard.split("[|]");
				cardNo = cardSplt[0];
				expDate = cardSplt[1];
				cvc = cardSplt[2];

				cardNo = CommUtil.maskedCardNo(cardNo); // masked

				Map<String, Object> cardInfo = new LinkedHashMap<>();
				cardInfo.put("cardNo", cardNo);
				cardInfo.put("expDate", expDate);
				cardInfo.put("cvc", cvc);
				resultData.put("cardInfo", cardInfo);

				amount = decMap.get("amount").toString();
				vat = decMap.get("vat").toString();
				vat = vat.replaceAll("^0+", ""); // 0제거

				Map<String, Object> priceInfo = new LinkedHashMap<>();
				priceInfo.put("amount", amount);
				priceInfo.put("vat", vat);
				resultData.put("priceInfo", priceInfo);

				resultCd = ResultCode.success;

			} else {
				resultCd = ResultCode.no_search_data;

			}

		} catch (Exception e) {
			resultCd = ResultCode.fail;
			e.printStackTrace();
		}

		return CommUtil.resMap(resultCd, resultData);
	}

	/**
	 * 결제 취소
	 * 
	 * @param card
	 * @return
	 */
	@Transactional(rollbackFor = { Exception.class })
	public Map<String, Object> payCancel(Payment payment) {
		Map<String, Object> resultData = new HashMap<>();
		ResultCode resultCd = ResultCode.fail;

		String tid = "";
		int remainAmt = 0;
		int remainVat = 0;

		try {
			if (payment.getAmount() == null || payment.getTid() == null) {
				resultCd = ResultCode.required_fail;
			} else {
				int vat = 0;
				int oAmount = 0;
				int oVat = 0;
				// 원 거래 내역 조회
				if (!payReps.findById(payment.getTid()).isPresent()) {
					resultCd = ResultCode.no_pay_data;
					return CommUtil.resMap(resultCd, resultData);
				}

				PaymentTran oriPayInfo = payReps.findById(payment.getTid()).get();
				Map<String, Object> oriMap = Parser.makeDecData(oriPayInfo.getEncData());

				oAmount = Integer.parseInt(oriMap.get("amount").toString());
				oVat = Integer.parseInt(oriMap.get("vat").toString());

				// 취소 내역 조회
				ArrayList<PaymentTran> pays = (ArrayList<PaymentTran>) payReps.findAll();

				int tAmount = 0;
				int tVat = 0;
				for (int i = 0; i < pays.size(); i++) {
					Map<String, Object> decMap = Parser.makeDecData(pays.get(i).getEncData());
					if (decMap.get("oid").equals(payment.getTid())) {
						tAmount += Integer.parseInt(decMap.get("amount").toString());
						tVat += Integer.parseInt(decMap.get("vat").toString());
					}
				}

				remainAmt = oAmount - tAmount;
				remainVat = oVat - tVat;

				// 부가세 없을 경우 기존 결제내역 부가세 가져옴
				if (payment.getVat() == null) {
					vat = CommUtil.calVat(payment.getAmount());
					vat = vat > remainVat ? remainVat : vat; // 계산된 부가세가 남은 부가세보다 크면 남은 부가세 부여
				} else {
					vat = payment.getVat();
				}

				if (remainAmt < payment.getAmount()) {
					resultCd = ResultCode.over_cancel_amount;
					return CommUtil.resMap(resultCd, resultData);
				} else if (remainVat < vat) {
					resultCd = ResultCode.over_cancel_vat;
					return CommUtil.resMap(resultCd, resultData);
				} else if (remainAmt == 0 && remainVat == 0) {
					resultCd = ResultCode.already_cancel;
					return CommUtil.resMap(resultCd, resultData);
				} else if (remainAmt == payment.getAmount() && remainVat > vat) {
					resultCd = ResultCode.remain_vat;
					return CommUtil.resMap(resultCd, resultData);
				}

				remainAmt -= payment.getAmount();
				remainVat -= vat;

				// 취소주문 생성
				Payment cpayment = new Payment();
				tid = CommUtil.makeId();
				cpayment.setTid(tid);
				cpayment.setPayDiv(CANCEL); // 결제 취소
				cpayment.setInstmt("00"); // 할부개월수 00
				cpayment.setOid(oriMap.get("tid").toString()); // 결제거래ID
				cpayment.setAmount(payment.getAmount()); // 취소금액
				cpayment.setEncCardInfo(oriMap.get("encCardInfo").toString());
				String encCardInfo = oriMap.get("encCardInfo").toString();
				String decCard = aes256.decrypt(encCardInfo);
				String[] cardInfo = decCard.split("[|]");
				String cardNo = cardInfo[0];
				String expDate = cardInfo[1];
				String cvc = cardInfo[2];

				cpayment.setCardNo(cardNo);
				cpayment.setCvc(cvc);
				cpayment.setExpDate(expDate);
				cpayment.setVat(vat);

				String encData = Parser.makeEncData(cpayment); // 취소정보 저장
				PaymentTran pay = new PaymentTran();
				pay.setTid(cpayment.getTid());
				pay.setOid(oriMap.get("tid").toString());
				pay.setEncData(encData);
				pay.setEncCardInfo(encCardInfo);
				pay.setPayDiv(cpayment.getPayDiv());
				pay.setRegDt(CommUtil.getDate()); // 거래시간

				if (beforeOverlapCheck(pay)) {
					PaymentTran savePay = payReps.save(pay); // 저장
					if (savePay != null && afterOverlapCheck(pay)) {
						resultCd = ResultCode.success;
						resultData.put("tid", tid);
						resultData.put("remainAmt", remainAmt);
						resultData.put("remainVat", remainVat);
					} else {
						throw new Exception();
					}
				} else {
					resultCd = ResultCode.dup_fail;
				}

//				synchronized (pay) {
//					// 동시간 동일카드 결제 건 확인
//					List<PaymentTran> dupPayment = payReps.findAllByEncCardInfoAndPayDivAndRegDt(encCardInfo,
//							pay.getPayDiv(), pay.getRegDt());
//					if (dupPayment.size() >= 1) {
//						resultCd = ResultCode.dup_fail;
//					} else {
//						PaymentTran savePay = payReps.save(pay); // 저장
//						if (savePay != null) {
//							// 동시간 동일카드 결제 건 확인
//							dupPayment = payReps.findAllByEncCardInfoAndPayDivAndRegDt(encCardInfo, pay.getPayDiv(),
//									pay.getRegDt());
//							if (dupPayment.size() > 1) {
//								resultCd = ResultCode.dup_fail;
//								throw new Exception();
//							} else {
//								resultCd = ResultCode.success;
//								resultData.put("tid", tid);
//								resultData.put("remainAmt", remainAmt);
//								resultData.put("remainVat", remainVat);
//							}
//						}
//					}
//				}

			}
		} catch (Exception e) {
			resultCd = ResultCode.fail;
			e.printStackTrace();
		}

		return CommUtil.resMap(resultCd, resultData);
	}

	// DB중복 저장 체크
	public Boolean beforeOverlapCheck(PaymentTran pay) throws Exception {
		Boolean result = false;
		// 동시간 동일카드 결제 건 확인
		List<PaymentTran> payList = payReps.findAllByEncCardInfoAndPayDivAndRegDt(pay.getEncCardInfo(), pay.getPayDiv(),
				pay.getRegDt());
		if (payList.size() >= 1) {
			result = false;
		} else {
			result = true;
		}

		return result;

	}

	// DB중복 저장 체크
	public Boolean afterOverlapCheck(PaymentTran pay) throws Exception {
		Boolean result = false;
		// 동시간 동일카드 결제 건 확인
		List<PaymentTran> payList = payReps.findAllByEncCardInfoAndPayDivAndRegDt(pay.getEncCardInfo(), pay.getPayDiv(),
				pay.getRegDt());
		if (payList.size() > 1) {
			result = false;
			throw new Exception();
		} else {
			result = true;
		}

		return result;

	}

}
