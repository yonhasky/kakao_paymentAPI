# Rest API 기반 결제시스템
## 프로젝트정보
REST API 기반 결제시스템
1. 결제
2. 조회
3. 취소(부분 취소)

## 개발 환경
1. SpringBoot 2.3.0
2. Java8
3. H2 Database
4. Gradle
5. JPA  

## 테이블 설계
카드사 통신 테이블
- 거래고유ID(PK)
- 결제구분
- 암호화된 카드정보
- StringData
- 결제고유ID(원거래)
- 등록일

## 문제해결 전략
**결제 정보** : 카드정보에 대해 카드사 전송 String데이터를 통해 값을 저장하고 얻어오는 방식.
- 결제 : 결제 String데이터 조합하여 카드사 전송 테이블 저장
- 조회 : 결제 String데이터 추출하여 응답
- 취소 : 원결제 String데이터 추출 후 취소결제 String데이터 저장

결제String 데이터를 조합하고 추출하는 Parse클래스를 생성하여 사용
- String데이터 조합 시 makeEncData로 객체를 받아서 String데이터 조합
- String데이터 조회 시 makeDecData로 Map으로 변환하여 데이터 추출

**거래고유ID구조**
- 거래시간(14)+랜덤값(6)
- ex) 20200527021524783433  

**부가세**  
결제/취소 시 부가세 데이터가 없을 경우 자동계산.

**카드정보 암호화**
- 표준으로 지정된 AES256를 사용

**필수문제**
- 결제API  
필수 요청 파라미터를 validation으로 검증, 부가세가 없을 경우 자동계산한다.  
결제String데이터를 저장하고, 결제요청 후 응답으로 결제금액, 부가세, 총 결제금액을 리턴한다.  
--  
> POST : http://localhost:8099/payment           
 Request :     
{"cardNo" : "1234123412341234", "cvc" : "123", "amount" : "11000", "vat" : "1000", "instmt" : "12", "expDate" : "0520"}  
--  
Response :       
{
    "resultCd": "0000",
    "resultMsg": "성공",
    "resultData": {
        "amount": 11000,
        "totalAmt": 12000,
        "vat": 1000,
        "tid": "20200527021524783433"
    }
}



- 조회API  
결제거래 고유ID로 해당 결제 및 취소내역을 조회한다. 거래ID당 하나의 결제 건을 리턴한다.  
결제/취소 구분과 복호화된 카드 정보 및 마스킹된 카드번호를 포함한 응답데이터를 리턴한다.    
  --  
> GET : http://localhost:8099/payHis  
Request  :   
tid="20200527021524783433"  
--  
Respnese  :     
{
    "resultCd": "0000",
    "resultMsg": "성공",
    "resultData": {
        "tid": "20200527124114378937",
        "payDiv": "PAYMENT",
        "cardInfo": {
            "cardNo": "123412*******234",
            "expDate": "0520",
            "cvc": "123"
        },
        "priceInfo": {
            "amount": "11000",
            "vat": "1000"
        }
    }
}


- 취소API  
원 거래ID와 결제금액으로 취소요청한다. 부가세가 없을 경우 취소 금액에서 자동 계산한다.  
남은 결제금액, 남은 부가세 금액을 리턴한다.  
부분취소기능을 포함한다.  
--  
> POST : http://localhost:8099/payCancel        
Request :     
{"tid" : "20200527124114378937", "amount" : "6600", "vat" : "600" }  
--  
Response :      
{
    "resultCd": "0000",
    "resultMsg": "성공",
    "resultData": {
        "remainAmt": 4400,
        "remainVat": 400,
        "tid": "20200527124735374448"
    }
}


**선택문제**
- 부분취소
 : 취소API로 부분취소 기능 구현  
- 멀티스레드  
 : 동일카드정보와 동시간대 결제 및 취소 요청 데이터가 있을 경우 실패처리한다. 동시간의 기준은 동일동시동분동초로 한다.  
 동시간에 결제/취소는 별도로 판단한다.    
 저장 전 동일 데이터 체크 > not save 
 저장 후 동일 데이터 체크 > rollback  
 

**응답코드**
- 0000~9999
("0000", "성공")  
("9999", "실패")  
("9001", "필수 요청파라미터가 없습니다.")  
("9002", "조회내역이 없습니다.")  
("9003", "결제 내역이 없습니다.")  
("9004", "취소금액이 잔여 결제금액을 초과하였습니다.")  
("9005", "취소 부가세금액이 잔여 부가세금액을 초과하였습니다.")  
("9006", "이미 취소된 거래입니다.")  
("9007", "취소시 잔여 부가세가 존재합니다.")  
("9008", "부가가치세는 결제금액보다 클 수 없습니다.")    
("9009", "결제를 실패하였습니다.")

## 빌드 및 실행 방법

1. 빌드
gradle build
 
2. 실행
java -jar PaymentAPI-0.0.1-SNAPSHOT.jar

3. 실행
gradle bootRun

4. 접근
http://localhost:8099  
http://localhost:8099/payment  
http://localhost:8099/payHis  
http://localhost:8099/payCancel  
