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

## 에러 발생 CASE 4 - 위험한 형식의 파일 업로드

### 방법 
서버 측에서 실행될 수 있는 스크립트 파일(asp, jsp, php 파일 등)이 업로드가능하고, 이 파일을
공격자가 웹으로 직접 실행시킬 수 있는 경우, 시스템 내부명령어를 실행하거나 외부와 연결하여
시스템을 제어할 수 있는 보안약점이다.

- 확장자의 대소문자 변경
- 위에 확장자의 파일을 업로드한뒤 다운로드 하여 해당 파일을 서버에서 실행 시킴
- 서버의 구조 및 경로를 파악하는데 사용

### 위와 같이 위험한 형식의 파일 업로드가 가능한 사이트인 경우 사용할 수 있는 프로그램

- DirBuster

### 예방방법
화이트 리스트 방식으로 허용된 확장자만 업로드를 허용한다. 업로드 되는 파일을 저장할 때에는
파일명과 확장자를 외부사용자가 추측할 수 없는 문자열로 변경하여 저장하며, 저장 경로는 ‘web
document root’ 밖에 위치시켜서 공격자의 웹으로 직접 접근을 차단한다. 또한 파일 실행 여부를
설정할 수 있는 경우, 실행 속성을 제거한다.

### 예제

```java

// 안전하지 않은 코드
MultipartRequest multi
= new MultipartRequest(request,savePath,sizeLimit,"euc-kr",new
DefaultFileRenamePolicy());
......
//업로드 되는 파일명을 검증없이 사용하고 있어 안전하지 않다.
String fileName = multi.getFilesystemName("filename");
......
sql = " INSERT INTO board(email,r_num,w_date,pwd,content,re_step,re_num,filename) "+ " values ( ?, 0, sysdate(), ?, ?, ?, ?, ? ) ";
preparedStatement pstmt = con.prepareStatement(sql);
pstmt.setString(1, stemail);
pstmt.setString(2, stpwd);
pstmt.setString(3, stcontent);
pstmt.setString(4, stre_step);
pstmt.setString(5, stre_num);
pstmt.setString(6, fileName);
pstmt.executeUpdate();
Thumbnail.create(savePath+"/"+fileName, savePath+"/"+"s_"+fileName, 150);

```

아래 코드는 업로드 파일의 확장자를 검사하여 허용되지 않은 확장자인 경우 업로드를 제한하고 있다.

```java
MultipartRequest multi
= new MultipartRequest(request,savePath,sizeLimit,"euc-kr",new
DefaultFileRenamePolicy());
......
String fileName = multi.getFilesystemName("filename");
if (fileName != null) {
  //1.업로드 파일의 마지막 “.” 문자열의 기준으로 실제 확장자 여부를 확인하고, 대소문자 구별을
  해야한다.
  String fileExt =
  FileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
  //2.되도록 화이트 리스트 방식으로 허용되는 확장자로 업로드를 제한해야 안전하다.
  //블랙리스트 방식은 모든 방식에 예외 되는 조건을 모두 추가해야 해서 실행이 굉장히 제한적임
  if (!"gif".equals(fileExt) && !"jpg".equals(fileExt) && !"png".equals(fileExt))
  {
    alertMessage("업로드 불가능한 파일입니다.");
    return;
  }
}
......
sql = " INSERT INTO board(email,r_num,w_date,pwd,content,re_step,re_num,filename) "+ " values ( ?, 0, sysdate(), ?, ?, ?, ?, ? ) ";
PreparedStatement pstmt = con.prepareStatement(sql);
......
Thumbnail.create(savePath+"/"+fileName, savePath+"/"+"s_"+fileName, 150);

```

## 에러 발생 CASE 5 - 부적절한 XML 외부개체 참조

### 공격 코드 01 - receivedXML

```xml
<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE foo [
  <!ELEMENT foo ANY >
  <!ENTITY xxe SYSTEM "file:///etc/passwd" >
]>
<foo>&xxe;</foo>
```

안전하지 않은 코드

```java
public void unmarshal(File receivedXml)
throws JAXBException, ParserConfigurationException, SAXException, IOException {
  JAXBContext jaxbContext = JAXBContext.newInstance( Student.class );
  Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
  // 입력받은 receivedXml 을 이용하여 Document를 생성한다.
  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  dbf.setNamespaceAware(true);
  DocumentBuilder db = dbf.newDocumentBuilder();
  Document document = db.parse(receivedXml);
  // 외부 엔티티로 만들어진 document를 이용하여 마샬링을 수행하여 안전하지
  않다.
  Student employee = (Student) jaxbUnmarshaller.unmarshal( document );
}
```

### 공격 코드 02 - secure.xml

```xml
<?xml version="1.0"?>
<!DOCTYPE foo SYSTEM "file:/dev/tty">
<foo>bar</foo>
```

안전하지 않은 코드

```java
import javax.xml.parsers.SAXParsers;
import javax.xml.parsers.SAXParserFactory;
class XXE {
  public static void main(String[] args)
  throws FileNotFoundException, ParserConfigurationException, SAXException,
  IOException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    // 외부개체 참조 제한 설정 없이 secure.xml 파일을 읽어서 파싱하여 안전하지 않다.
    saxParser.parse(new FileInputStream("secure.xml"), new DefaultHandler());
  }
}

```
### 예방방법

```java
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
// XML 파서가 doctype을 정의하지 못하도록 설정한다.
dbf.setFeature("http://apache.org/xml/featuresdisallow-doctype-decl", true);
// 외부 일반 엔티티를 포함하지 않도록 설정한다.
dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
// 외부 파라미터도 포함하지 않도록 설정한다.
dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
// 외부 DTD 비활성화한다.
dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
// XIncude를 사용하지 않는다.
dbf.setXIncludeAware(false);
// 생성된 파서가 엔티티 참조 노드를 확장하지 않도록 한다.
dbf.setExpandEntityReferences(false);
DocumentBuilder db = dbf.newDocumentBuilder();
Document document = db.parse(receivedXml);
Model model = (Model) u.unmarshal(document);
```

## 에러 발생 CASE 6 - 서버사이드 요청 위조 (SSR)



## 시큐어코딩 RULE 

[적절한 인증 없는 중요기능 허용]
1. 관리자 로그인 후 접근 가능한 페이지와 같이 중요정보나 중요한 기능이 있는 페이지를 접근할 때 로그인한 사용자인지 아닌지로 체크로직이 누락된 경우
- URL 	admin/admin_login
- 회원		X
- 관리자	O
- 익명 		X

2. 중요정보나 중요 기능이 있는 페이지를 접근할 때 단순히 아이디와 암호로 인증하는 경우 (1차인증)
- 인증수단
1) id/pw, OTP, MMS, ARS 등등
2) 인증서
   
   
[부적절한 인가]
공지사항 게시글 작성자는 관리지 권한
관리자 권한만 게시글 작성, 수정, 삭제 권한
일반 사용자는 공지사항에 대한 읽기 권한만

일반사용자 권한으로 게시글 읽게 요청!

?no=2&page=262&menucode=3232&mode=view
?no=2&page=262&menucode=3232&mode=***edit***


[전송시 암호화]
- SSL (Secure Socket layer] -> sslv1, sslv2, sslv3(x)
- TLS (Transport Layer Srcuritu] -> TLSv1.2 ~ 이상(O)

**체트하는 방법** -> nmap

[무결성 검사 없는 파일 다운로드]

외부 업체 소프트웨어를 사용하면서 해당 업체(공급망)의 서버를 공격하여 
해당 업데이트 서버에 침투하여 악성 코드 파일을 패치 실행

무결성 체크 방법
1) 파일의 HASH 체크
2) 파일의 사이즈 체크
3) 안티바이러스(백신)로 체크
4) 코드서명을 통한 체크 -> 악성 코드를 정상 코드로 위장 

## 에러처리 : 메세지 정책
- 오류메세지를 통한 정보 노출 : 
- 오류 대응 상황 부재
- 오류 일괄적이 오류 대응

