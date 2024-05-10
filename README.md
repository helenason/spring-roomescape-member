## 기능 요구사항
- [x] 예외 처리
  - [x] 시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때
    - null, "", HH:mm이 아닌 경우
  - [x] 예약 생성 시 예약자명, 날짜, 시간에 유효하지 않은 값이 입력 되었을 때
    - 예약자명: null, ""
    - 날짜: null, "", yyyy-MM-dd이 아닌 경우
    - 시간: null, "", HH:mm이 아닌 경우
  - [x] 특정 시간에 대한 예약이 존재하는데, 그 시간을 삭제하려 할 때
  - [x] 존재하지 않는 id를 가진 데이터에 접근하려 할 때
  - [x] 지나간 날짜와 시간에 대한 예약 생성 불가능
  - [x] 중복 예약 불가능
  - [x] 예약 시간 중복 불가능
<br>
- [x] '테마' 도메인 추가
  - [x] 모든 테마는 시작 시간과 소요 시간이 동일
- [x] 테마 관리 페이지 조회
- [x] 테마 추가 API
- [x] 테마 삭제 API
- [x] 테마 조회 API
<br>
- [x] (관리자가 아닌) 사용자가 예약 가능한 시간을 조회하고, 예약할 수 있도록 기능을 추가/변경
  - [x] 테마와 날짜를 선택하면 예약 가능한 시간 조회
  - [x] 예약 추가
  - [X] /reservation 요청 시 사용자 예약 페이지 조회
- [x] 인기 테마 조회 기능을 추가
  - [x] 최근 일주일을 기준으로 하여 해당 기간 내에 방문하는 예약이 많은 테마 10개를 확인
  - [x] / 요청 시 인기 테마 페이지 조회
<br>
- [x] 사용자 도메인 추가
  - [x] 필드: 이름, 이메일, 비밀번호
- [x] 로그인 기능 구현
  - [x] 로그인 페이지 조회
  - [x] 로그인 API
- [x] 사용자 정보 조회
  - [x] 사용자 정보 조회 API
  - [x] Cookie 이용
<br>
- [x] 사용자 정보 조회 로직 리팩터링
  - [x] Cookie에 담긴 인증 정보를 이용해 멤버 객체 생성 로직 분리
- [x] 예약 생성 API 및 기능 리팩터링
  - [x] 사용자가 예약 생성 시, 로그인한 사용자 정보를 활용하도록
<br>
- [x] 접근 권한 제어
  - [x] 어드민 페이지 진입은 admin 권한이 있는 사람만 할 수 있도록 제한(Member의 Role이 ADMIN인 사람만 /admin 페이지 접근 가능)
<br>  
- [x] 예약 목록 검색
  - [x] 관리자가 조건에 따라 예약을 검색할 수 있도록
  - [x] 예약자별, 테마별, 날짜별 검색 조건을 사용해 예약 검색이 가능하도록 기능 추가
