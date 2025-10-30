[STEP03] 기능 요구사항 분석 및 API 설계

---
## 📑 문서 목록

### 1. [📋 요구사항 정의](../docs/requirements.md) 
### 2. [👤 사용자 스토리](../docs/user-stories.md)
### 3. [🗄️ 데이터 모델](../docs/data-models.md)
### 4. [🔌 API 명세서](../docs/api-specification.md)
### 5. [📊 시퀀스 다이어그램](../docs/sequence-diagram.md)

### **커밋 링크**


### **리뷰 포인트(질문)**
- stock_reservations을 두는 것이 맞는지 궁금합니다. 제가 테이블을 둔 이유는 다음과 같습니다. 
  - 로직: 재고 차감 시점을 결제 전에 선차감을 한 뒤 결제 실패가 되면 보상 트랜잭션을 통해 다시 재고 차감 취소 
  - 없을 경우 :  
    - 성공 :stock에 update 1번  실패 : stock에 update 2번 
  - 있을 경우 :
    - 성공 : stock_reservations에 insert 1번 update 1번 + stock에 update 1번  실패 :stock_reservations 에 insert 1번 + update 1번 
  
  이렇게 되는거 같아서 stock에 대한 경합을 막을 수도 있을 거 같고, 개인적으로는 update는 데드튜플을 더 많이 발생시키니 DB성능 상에도 stock에 대해 update를 최대한 하지 않도록 하는 것이 좋을 거 같아 테이블을 구성해봤는데 코치님의 의견이 궁금합니다..!

- 리뷰 포인트 2

### **간단 회고** (3줄 이내)
- **잘한 점**:
- **어려운 점**:
- **다음 시도**: