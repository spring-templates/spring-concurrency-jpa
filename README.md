![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?logo=junit5&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-D22128?logo=jacoco&logoColor=white)
![Codecov](https://img.shields.io/badge/Codecov-F01F7A?logo=codecov&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)

# 주문 서버 관심사
### 주문 및 결제 과정

1. 클라이언트는 주문 서버에 주문 요청
2. 주문 서버는 클라이언트 요청의 유효성을 검사
   1. `PENDING_ORDER` 상태의 유형제품을 선택
   2. 유형제품의 상태를 `PROCESSING`로 변경
   3. 주문에 유형 제품을 추가
   4. 만약 주문 요청한 수량만큼 `PENDING_ORDER` 상태의 유형제품이 없다면, `재고가 부족합니다. ` 문구를 클라이언트에게 응답
3. 주문 서버가 결제 서버에 결제 요청
4. 결제 서버가 결제 요청 받았다고 응답
5. 주문 서버는 결제 요청이 들어간 것을 클라이언트에게 전달
6. 결제 서버가 결제 결과를 주문 서버에 전달
    1. 결제가 정상적으로 완료되면,
       1. 유형제품의 상태를 `SHIPPING`으로 변경
       2. 핵심제품의 재고를 차감
    2. 결제가 실패되면, 
       1. 재고 유지
       2. 주문의 유형제품 상태를 `PENDING_ORDER`로 변경
7. 주문 서버는 받는 결과를 클라이언트에게 전달

### 클라이언트 주문 요청 형태

```json
{
    "core_products" : {
        "1" : "30",
        "2" : "10"
    },
    "client_type" : "InexperiencedCustomer",
    "payment_method" : "CREDIT_CARD"
}
```

### 결제 요청 형태
```json
{
    "buyer": {
        "name": "John Doe",
        "email": "a1@ex.com"
    },
    "seller": {
        "name": "Jane Doe",
        "email": "a2@ex.com"
    },
    "payment": "CREDIT_CARD",
    "price": 100,
    "redirect": "http://localhost:8080/payment/confirm"
}
```

### 상품의 상태에 대한 설명

`PENDING_ORDER` : 유형제품이 주문에 포함되지 않는 기본적인 상태를 의미. 여러 주문에서 동시에 접근하면 한 주문에만 들어간다.

`PROCESSING` : 유형제품이 주문에 포함되며, 결제 결과를 기다리는 상태를 의미.

`SHIPPING` : 결제가 정상적으로 종료되고, 해당 유형제품이 온전히 고객의 소유가 되는 상태.  

`DELIVERED` : 상품이 고객에게 배송 완료된 상태. 현재 프로젝트에서는 배송까지는 관심사가 아니기 때문에 사용하지 않는다.

### 클라이언트 요청 유효성 검사 리스트

- [ ] 핵심제품 id가 존재하는가?
- [ ] 핵심제품의 재고가 충분한가?
- [ ] `PENDING_ORDER` 상태의 유형제품이 충분한가? 
- [ ] 클라이언트가 상품 구매 권한이 있는가?
- [ ] 결제 방식이 유효한 방식인가?

## 시퀀스 다이어그램

![count-sequence.png](./count-sequence.png)

**Transaction 1**

- 사용자 권한 확인
- 재고 확인
- 주문 생성
- 상품을 결제 중으로 상태 변환

**Transaction 2**

- 결제 결과가 성공일 때,
    -  상품 상태를 SHIPMENT 로 변경
    - 주문의 상태를 FINISH 로 변경
- 에러가 발생하거나, 결제가 실패했을 때,
  - 상품의 상태를 PENDING_ORDER 으로 변경
  - 상품 재고를 원복
  - 주문의 상태를 FAIL 로 변경

**Transaction 3**
- 리디렉션 정보에 따라 주문을 읽기