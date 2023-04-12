package com.javalab.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseExample {
//	[멤버 변수]
	//1. Oracle 드라이버 경로 문자열 상수
	public static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

	//2. Oracle 데이터베이스 접속 경로(url) 문자열 상수
	public static final String DB_URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
	
	//3. 데이터베이스 접속 객체
	public static Connection con = null;
	
	//4. query 실행 객체
	public static PreparedStatement pstmt = null;
	
	//5. select 결과 저장 객체
	public static ResultSet rs = null;
	
	//6. Oracle 계정
	public static String oracleId = "tempdb";
	
	//7. Oracle 계정 패스워드
	public static String oraclePwd = "1234";
	
	//start main 
	public static void main(String[] args) {
//		1. DB 접속 메소드 호출
		connectDB();
		
//		2. member 테이블 생성
		createMemberTBL();
		
//		2-1.member 테이블 데이터 insert
		insertpointTBl();
		
//		3. point 테이블 생성
		createPointTBL();
		
//		4. sequence 생성
		createSequence();
		
//		5. member 테이블에 있는 회원들에게 모두 10포인트를 부여
//		point 테이블에 3개의 레코드(행을 추가)
		insertPoint();
		
//		6.회원들과 보유 포인트를 조회하세요.
//		- admin 컬럼은 0이면 '일반사용자' , 1이면 관리자로 표시하세요.
		selectAllmember();
		
//		7. 이소미 회원에게 추가로 포인트를 15점 부여하세요.
		updatePointSomi();
		
//		8. 관리자에게 포인트를 30점 부여하세요.
		updatePointmanager();
		
//		9. 모든 회원들의 평균 포인트보다 작은 회원을 조회하세요.
		getMemberLessThanAvg();
		
//		10. 자원반납(PreparedStatement, ResultSet)
		closeResource(pstmt,rs);
		
//		11. 자원반납
		closeResuorce();
	}//end main
	
	
	
	private static void getMemberLessThanAvg() {
		try {
			String sql = "select m.user_id, m.name, m.pwd, m.email, m.phone,";
			sql += " decode(m.admin, 0, '일반사용자', 1, '관리자') admin,";
			sql += " p.point_id, p.points, to_char(reg_date,'yyyy-mm-dd') reg_date";
			sql += " from member m left outer join point p on m.user_id = p.user_id";
			sql += " where p.points < (select avg(p.points)";
			sql += "                  from point p)";
			
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				System.out.println(rs.getString("user_id")+"\t"+
						   rs.getString("name")+"\t"+
						   rs.getString("pwd")+"\t"+
						   rs.getString("email")+"\t"+
						   rs.getString("phone")+"\t"+
						   rs.getString("admin")+"\t"+
						   rs.getInt("point_id")+"\t"+
						   rs.getInt("points")+"\t"+
						   rs.getString("reg_date")
					);
			}
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}



	private static void updatePointmanager() {
		try {
			int intPoint = 30;
			int intAdmin = 1;
			
			String sql = "update point p";
				   sql += " set p.points = p.points + ?";
				   sql += " where p.user_id in(select m.user_id";
				   sql += "                  from member m";
				   sql += "                  where m.admin = ?)";
			
				   pstmt = con.prepareStatement(sql);
				   pstmt.setInt(1,intPoint);
				   pstmt.setInt(2,intAdmin);
				   System.out.println("4. pstmt 객체 생성 성공");
				   
				   // 쿼리 실행
				   // 처리된 결과 반환됨(수정된 행수)
				   //commit이 안되있으면 여기서 끝날수 있음
				   int result = pstmt.executeUpdate();
				   
				   if(result > 0) {
					   System.out.println("4.수정 성공");
				   }else {
					   System.out.println("4.수정 실패");
				   }
				   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}



//	7. 이소미 회원에게 추가로 포인트를 15점 부여하세요.
	private static void updatePointSomi() {
		try {
			//수정할 회원 및 포인트 변수 선언
			int intPoint = 15;
			String strName = "이소미";
			
			String sql = "update point p";
				   sql += " set p.points = p.points+?";
				   sql += " where p.user_id = (select m.user_id";
				   sql += " 			      from member m where m.name = ?)";
				   

		//PreparedStatement
		pstmt = con.prepareStatement(sql);
		pstmt.setInt(1,intPoint);
		pstmt.setString(2,strName);
		
		// 쿼리 실행
		// 처리된 결과 반환됨(수정된 행수)
		int resultRows = pstmt.executeUpdate();
		
		if(resultRows > 0) {
			System.out.println("3.수정 성공");
		}else {
			System.out.println("3.수정 실패");
		}
				   
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
		System.out.println();
	}//end updatePointSomi()


//	6.회원들과 보유 포인트를 조회하세요.
//	- admin 컬럼은 0이면 '일반사용자' , 1이면 관리자로 표시하세요.
	private static void selectAllmember() {
		try {
			String sql = "select m.user_id, m.name, m.email, m.phone, decode(m.admin,0,'일반사용자',1,'관리자) admin";
				   sql += " from member m left outer join point p on m.user_id = p.user_id";
				   
				   pstmt = con.prepareStatement(sql);
				   System.out.println("6. 회원들과 보유 포인트 조회");
				   rs = pstmt.executeQuery();
				   System.out.println();
				   
				   while(rs.next()){
					   System.out.println(rs.getString("user_id")+"\t"+
							   			  rs.getString("name")+"\t"+
							   			  rs.getString("email")+"\t"+
							   			  rs.getString("phone")+"\t"+
							   			  rs.getInt("point_id")+"\t"+
							   			  rs.getInt("points")+"\t"+
							   			  rs.getDate("reg_date")+"\t"+
							   			  rs.getString("admin")
							   );
				   }
				   
				   
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}

	
//	5. member 테이블에 있는 회원들에게 모두 10포인트를 부여
//	point 테이블에 3개의 레코드(행을 추가)
	private static void insertPoint() {
		try {
			int intPoints = 10;
			String sql1 = "insert into member(point_id, user_id, points)";
			   	   sql1 += " values (seq_point.nextval, 'somi', ?)";
			String sql2 = "insert into member(point_id, user_id, points)";
				   sql2 += " values (seq_point.nextval, 'sang12', ?)";
			String sql3 = "insert into member(point_id, user_id, points)";
				   sql3 += " values (seq_point.nextval, 'light', ?)";
			
			pstmt = con.prepareStatement(sql1);
			pstmt.setInt(1,intPoints);
			int result = pstmt.executeUpdate();
			
			pstmt = con.prepareStatement(sql2);
			pstmt.setInt(1,intPoints);
			result += pstmt.executeUpdate();
			
			pstmt = con.prepareStatement(sql3);
			pstmt.setInt(1,intPoints);
			result += pstmt.executeUpdate();
			
			if(result > 0) {
				System.out.println("5. 저장 성공");
			}else {
				System.out.println("5. 저장 실패");
			}
			
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}//end insertPoint()
	
//	4. sequence 생성
	private static void createSequence() {
		try{
			String sql = "create sequence seq_point";
			   sql += " increment by 1";
			   sql += " start with 1";
			   
			   pstmt = con.prepareStatement(sql);
			   rs = pstmt.executeQuery();
			   System.out.println("4. seq_point 시퀀스 생성");
			   System.out.println();
			   
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt,rs);
		}
			   
	}//end createSequence()
	
//	3. point 테이블 생성
	private static void createPointTBL() {
		try {
			String sql = "create table point(";
				   sql += " point_id number(10) constraint pk_point_pointid primary key";
				   sql += " )";
				   
				   pstmt = con.prepareStatement(sql);
				   rs = pstmt.executeQuery();
				   
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}
	}//end createPoinTBL()

//	2-1.member 테이블 데이터 insert
	private static void insertpointTBl() {
		try {
			String sql1 = "insert into member(user_id, name,pwd, emial,pone, admin)";
				   sql1 += " values('somi','이소미','1234','somi@naver.com','010-2362-5157',0)";
			String sql2 = "insert into member(user_id, name,pwd, emial,pone, admin)";
				   sql2 += " values('sang12','하상오','1234','sang12@naver.com','010-5629-8888',0)";
			String sql3 = "insert into member(user_id, name,pwd, emial,pone, admin)";
				   sql3 += " values('light','김윤승','1234','light@naver.com','010-2362-5157',0)";
				   
				   pstmt = con.prepareStatement(sql1);
				   int result = pstmt.executeUpdate();
				   pstmt = con.prepareStatement(sql2);
				   result += pstmt.executeUpdate();
				   
				   pstmt = con.prepareStatement(sql3);
				   result += pstmt.executeUpdate();
				   
				   if(result >0) {
					   System.out.println("2-1. member 테이블 데이터 저장 성공");
				   }else {
					   System.out.println("2-2. member 테이블 데이터 저장 실패");
				   }
				   
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}
	}//end insertpointTBl()

//	2. member 테이블 생성
	private static void createMemberTBL() {
		try {
			String sql = "create table member(";
					sql += " user_id varchar2(10) constraint pk_member_userid primary key";
					sql += " name varchar2(10)";
					sql += " pwd varchar2(20)";
					sql += " email varchar2(20)";
					sql += " phone char(13)";
					sql += " admi number(1) default 0)";
					
					pstmt = con.prepareStatement(sql);
					rs = pstmt.executeQuery();
					System.out.println("2. memer 테이블 생성");
					System.out.println();
					
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt,rs);
		}
	}//end createMemberTBL();

	private static void connectDB() {
		try {
		Class.forName(DRIVER_NAME);
		System.out.println("1-1 드라이버 연결 성공");
		System.out.println();
		
		con = DriverManager.getConnection(DB_URL,oracleId,oraclePwd);
		System.out.println("1-2 커넥션 객체 생성 성공");
		System.out.println();
		
		}catch(ClassNotFoundException e) {
			System.out.println("드라이버 ERR : "+e.getMessage());
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}
	}//end connectDB()
	
	
//	10. 자원반납(PreparedStatement, ResultSet)
	private static void closeResource(PreparedStatement pstmt, ResultSet rs) {
		try {
			if(pstmt != null) {
				pstmt.close();
			}
			if(rs != null) {
				rs.close();
			}
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}
	}//end closeResource(pstmt, rs);

//	11. 자원반납
	private static void closeResuorce() {
		try {
			if(con != null) {
				con.close();
			}
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}
	}//end closeResuorce()
	
	
}//end class
