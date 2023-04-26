## 시큐어 코딩 교육

### 테스트 사이트
http://61.39.155.24:50002/

### 테스트 환경 
- JAVA
- SpotBug
- PMD
- ASP

## 에러 발생 CASE 1 - SQL INJECTION

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
1. PreparedStatement : 컴파일된 쿼리 객체로 MySQL, Oracle, DB2, SQL Server 등에서 지원하며, Java의 JDBC, Perl의 DBI, PHP의 PDO, ASP의 ADO를 이용하여 사용가능

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

2. mybatis 

```xml

<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN“
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
......
<select id="boardSearch" parameterType="map" resultType="BoardDto">
//$기호를 사용하는 경우 외부에서 입력된 keyword값을 문자열에 결합한 형태로 쿼리에 반영되므로
안전하지 않다.
select * from tbl_board where title like '%$ {keyword }%' order by pos asc
</select>

```

- 안전한 케이스 

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
......
<select id="boardSearch" parameterType="map" resultType="BoardDto">
//$ 대신 #기호를 사용하여 변수가 쿼리맵에 바인딩 될 수 있도록 수정하는 것이 안전하다.
select * from tbl_board where title like '%'||# {keyword }||'%' order by pos asc
</select>

```

## 에러 발생 CASE 2 - 경로조작 및 자원삽임

### URL을 통해 접속된 경로의 파일을 찾아 보는 방식 
- 경로에 관련한 특수 문자를 URL에 삽입하여 원하는 경로의 자원을 획득하는 방법
- 다운로드 방식을 통해 DB 접속 정보를 가져옴
- 설사 해당 정보가 암호화 되었다해도 decrypte key를 조회 할 수 있음

### 위와 같이 경로조작 및 자원삽임이 의심되는 사이트인 경우 사용할 수 있는 프로그램
1. sqlgate

### 예방 방법
외부의 입력을 자원(파일, 소켓의 포트 등)의 식별자로 사용하는 경우, 적절한 검증을 거치도록
하거나, 사전에 정의된 적합한 리스트에서 선택되도록 한다. 특히, 외부의 입력이 파일명인 경우에는
경로순회(directory traversal)3) 공격의 위험이 있는 문자( “ / ￦ .. 등 )를 제거할 수 있는 필터를
이용한다.

### 잘못된 코드 예제
외부 입력값(P)이 버퍼로 내용을 옮길 파일의 경로설정에 사용되고 있다. 만일 공격자에 의해 P의
값으로 ../../../rootFile.txt와 같은 값을 전달하면 의도하지 않았던 파일의 내용이 버퍼에 쓰여
시스템에 악영향을 준다.

```java
//외부로부터 입력받은 값을 검증 없이 사용할 경우 안전하지 않다.
String fileName = request.getParameter("P");
BufferedInputStream bis = null;
BufferedOutputStream bos = null;
FileInputStream fis = null;
try {
  response.setHeader("Content-Disposition", "attachment;filename="+fileName+";");
  ...
  //외부로부터 입력받은 값이 검증 또는 처리 없이 파일처리에 수행되었다.
  fis = new FileInputStream("C:/datas/" + fileName);
  bis = new BufferedInputStream(fis);
  bos = new BufferedOutputStream(response.getOutputStream())
```

외부 입력값에 대하여 상대경로를 설정할 수 없도록 경로순회 문자열( / ￦ & .. 등 )을 제거하고
파일의 경로설정에 사용한다.

```java
String fileName = request.getParameter("P");
BufferedInputStream bis = null;
BufferedOutputStream bos = null;
FileInputStream fis = null;
  try {
    response.setHeader("Content-Disposition", "attachment;filename="+fileName+";");
    ...
    // 외부 입력받은 값을 경로순회 문자열(./￦)을 제거하고 사용해야한다.
    filename = filename.replaceAll("￦￦.", "").replaceAll("/", "").replaceAll("￦￦￦￦", "");
    fis = new FileInputStream("C:/datas/" + fileName);
    bis = new BufferedInputStream(fis);
    bos = new BufferedOutputStream(response.getOutputStream());
    int read;
    while((read = bis.read(buffer, 0, 1024)) != -1) {
    bos.write(buffer,0,read);
  }
}
```

## 에러 발생 CASE 3 - 크로스사이트 스크립트

```html
<iframe src="http://nate.com"></iframe>
```

해당 태그의 값이 게시판 게시물이나 사용자가 입력이 가능한 페이지에 삽입되어 입력된 경우
해당 사이트가 게시글에서 OPEN됨

만약 해당 사이트가 피싱 사이트 이거나 , 의심되는 주소로 사용자를 곤란에 빠뜨릴 가능성이 있음

### 예방방법

```JSP

keyword = keyword.replaceAll(")", "&#x29;");
검색어 : <%=keyword%>
//방법2. JSP에서 출력값에 JSTL c:out 을 사용하여 처리한다.
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
검색결과 : <c:out value="$ {m.content}"/>
<script type="text/javascript">
//방법3. 잘 만들어진 외부 라이브러리를 활용(NAVER Lucy-XSS-Filter, OWASP ESAPI,OWASP Java-Encoder-Project)
document.write("keyword:"+
<%=Encoder.encodeForJS(Encoder.encodeForHTML(keyword))%>);
</script>

```


