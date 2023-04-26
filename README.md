## 시큐어 코딩 교육

### 테스트 사이트
http://61.39.155.24:50002/

### 테스트 환경 
- JAVA
- SpotBug
- PMD
- ASP

### 에러 발생 CASE 1 - SQL INJECTION

- TEST 
ID -> 무작위 텍스트
PW -> '

```
Microsoft OLE DB Provider for SQL Server error '80040e14'
문자열 '' '의 따옴표가 짝이 맞지 않습니다.
/demoshop/login/login_check.asp, line 18
```
- 인증 우회 방법 1
ID는 무작위 텍스트
PW는 'or 1 = 1 -- 
***이렇게 입력***

- 인증 우회 방법 2
ID는  kisec'--
PW는 'or 1 = 1 -- 

- 아주 간단한 테스트 키워드
'and 1=1 --    
=> 이 경우 전부 다 조회
'and 1=2 --
=> 이 경우 조회 건수가 안됨

- SQL INJECTION 사례 
1. 뽐뿌
2. WTO
3. 여기어때

- 위와 같이 SQL Injection이 의심되는 사이트인 경우 사용할 수 있는 프로그램
1. sqlmap

- 방어 방법
PreparedStatement : 컴파일된 쿼리 객체로 MySQL, Oracle, DB2, SQL Server 등에서 지원하며, Java의 JDBC, Perl의 DBI, PHP의 PDO, ASP의 ADO를 이용하여 사용가능

```java
String gubun = request.getParameter("gubun");
......
//1. 사용자에 의해 외부로부터 입력받은 값은 안전하지 않을 수 있으므로, PreparedStatement
사용을 위해 ?문자로 바인딩 변수를 사용한다.
String sql = "SELECT * FROM board WHERE b_gubun = ?";
Connection con = db.getConnection();
//2. PreparedStatement 사용한다.
PreparedStatement pstmt = con.prepareStatement(sql);
//3. PreparedStatement 객체를 상수 스트링으로 생성하고, 파라미터 부분을 setString 등의
메소드로 설정하여 안전하다.
pstmt.setString(1, gubun);
ResultSet rs = pstmt.executeQuery();
```

