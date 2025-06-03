import java.sql.*;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CakeOrderCombine {
    public static void main(String[] args) {
        String dbid="dbeez";
        String userid="root";
        String passwd="jeeinmin2004!!";
        String url="jdbc:mysql://localhost:3306/"+dbid+"?serverTimezone=Asia/Seoul";

        try (Connection conn=DriverManager.getConnection(url,userid,passwd)){
            cakeRegister(conn);
            cakeOrder(conn);
            reviewRegister(conn);
            update(conn);
            selectCustomerMain(conn);
            viewCakeInfoCustomer(conn);
            viewStoreInfoCustomer(conn);
            viewReviewbyNameCustomer(conn);
        }
        catch (SQLException sqle){
            System.out.println("SQLException : "+sqle);
        }
    }


    // insert - 태영
    // Register New Cakes
    public static void cakeRegister(Connection conn){
        // if cakeRegister
        System.out.println("[Register New Cake]\n");
        Scanner s = new Scanner(System.in);
        try(Statement stmt=conn.createStatement();) {
            PreparedStatement pStmt = conn.prepareStatement(
                    "insert into cakes (cake_id, cake_name, price) values(?,?,?)");

            //// cake_id
            ResultSet rset = stmt.executeQuery(
                    "select cast(cake_id as unsigned) as cakeid_int "
                            + "from cakes "
                            + "order by cakeid_int desc");
            if (rset.next()) {
                String cake_id = String.format("%03d", rset.getInt("cakeid_int") + 1);
                pStmt.setString(1, cake_id);
            }

            //// cake_name
            System.out.print("Please enter the name of the cake to register: ");
            String cake_name = s.nextLine();
            pStmt.setString(2, cake_name);

            //// price
            System.out.print("Please enter the price of the cake: ");
            int cake_price = s.nextInt();
            pStmt.setInt(3, cake_price);

            pStmt.executeUpdate();
            System.out.println("Successfully registered!\n");
        } catch(SQLException e) {
            System.out.println(e);
        }
    }

    // Order Cake
    public static void cakeOrder(Connection conn){
        // if cakeOrder
        System.out.println("[Order Cakes]\n");
        Scanner s = new Scanner(System.in);
        try(Statement stmt=conn.createStatement();) {
            PreparedStatement pStmt = conn.prepareStatement(
                    "insert into orders (order_num, order_date, customer_id, cake_id, store_id, price) "
                            + "values(?,?,?,?,?,?)");

            ////order_num
            ResultSet rset = stmt.executeQuery(
                    "select order_num "
                            + "from orders "
                            + "order by order_num desc");
            if(rset.next()) {
                int order_num = 1 + rset.getInt("order_num");
                pStmt.setInt(1, order_num);
            }

            ////order_date: 코드 실행 시각
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            pStmt.setTimestamp(2, timestamp);

            ////customer_id
            System.out.print("Please enter the customer ID (ex.001) : ");
            int customer_id = s.nextInt();
            //Customers 테이블에 해당 ID가 존재하는지 검사
            PreparedStatement checkStmt = conn.prepareStatement("select 1 "
                    + "from Customers "
                    + "where customer_id=?");
            checkStmt.setInt(1, customer_id);
            rset = checkStmt.executeQuery();
            //ID가 존재하지 않으면
            if(!rset.next()) {
                System.out.println("This customer ID does not exist.\n");
                return;
            }
            //ID 존재하면
            pStmt.setInt(3, customer_id);
            s.nextLine();

            ////cake_id
            // 케이크 테이블을 보여주는 게 좋을 것 같다
            rset = stmt.executeQuery("select * from cakes");
            System.out.println("\n<< CAKES >>");
            System.out.println("==============================================");
            System.out.println(" Cake id | Cake               | Price");
            System.out.println("----------------------------------------------");

            // Cakes table empty check
            boolean foundResults = false;
            while (rset.next()) {
                foundResults = true;
                System.out.printf(" %-7s | %-18s | %d\n",
                        rset.getString(1), rset.getString(2), rset.getInt(3));
            }
            if(!foundResults) {
                System.out.println("Empty Data\n");
            }
            System.out.println("==============================================\n");
            System.out.print("Please enter the cake ID you want to order (ex.002) -> ");
            String cake_id = s.nextLine();
            //Cakes 테이블에 해당 ID가 존재하는지 검사
            checkStmt = conn.prepareStatement("select 1 from Cakes where cake_id=?");
            checkStmt.setString(1, cake_id);
            rset = checkStmt.executeQuery();
            //ID가 존재하지 않으면
            if(!rset.next()) {
                System.out.println("This cake ID does not exist.\n");
                return;
            }
            //ID 존재하면
            rset = checkStmt.executeQuery();
            pStmt.setString(4, cake_id);

            ////store_id
            rset = stmt.executeQuery("select store_id, store_name from stores");
            System.out.println("\n<< STORES >>");
            System.out.println("=========================");
            System.out.println(" ID     | Store ");
            System.out.println("-------------------------");
            foundResults = false;
            while (rset.next()) {
                foundResults = true;
                System.out.printf(" %-6s | %s\n",
                        rset.getString(1), rset.getString(2));
            }
            if(!foundResults) {
                System.out.println("Empty Data\n");
            }
            System.out.println("=========================\n");

            System.out.print("Please enter the store ID you want to order from -> ");
            String store_id = s.nextLine();
            //Cakes 테이블에 해당 ID가 존재하는지 검사
            checkStmt = conn.prepareStatement("select 1 from Stores where store_id=?");
            checkStmt.setString(1, store_id);
            rset = checkStmt.executeQuery();
            //ID가 존재하지 않으면
            if(!rset.next()) {
                System.out.println("This store ID does not exist.\n");
                return;
            }
            //ID 존재하면
            pStmt.setString(5, store_id);

            ////price -> cake_id로 찾기
            checkStmt = conn.prepareStatement("select price from cakes where cake_id=?");
            checkStmt.setString(1, cake_id);
            rset = checkStmt.executeQuery();
            if(rset.next()) {
                int price = rset.getInt("price");
                pStmt.setInt(6, price);
            }

            pStmt.executeUpdate();
            System.out.println("Successfully ordered!\n");

        } catch(SQLException e) {
            System.out.println(e);
        }
    }

    // Register a Review
    public static void reviewRegister(Connection conn) {
        //리뷰 등록
        System.out.println("[Register a Review]\n");
        Scanner s = new Scanner(System.in);
        try(Statement stmt=conn.createStatement();) {
            PreparedStatement pStmt = conn.prepareStatement(
                    "insert into reviews (review_num, customer_id, review_rate, "
                            + "cake_id, order_num, store_id) values(?,?,?,?,?,?)"); // 초기화

            ////review_num - automatically increment
            ResultSet rset = stmt.executeQuery(
                    "select cast(review_num as unsigned) as revnum_int "
                            + "from reviews "
                            + "order by revnum_int desc");
            if (rset.next()) {
                String review_num = String.format("%08d", rset.getInt("revnum_int") + 1);
                pStmt.setString(1, review_num);
            }

            ////customer_id
            System.out.print("Please enter the customer ID (number): ");
            int customer_id = s.nextInt();
            s.nextLine();
            //Customers 테이블에 해당 ID가 존재하는지 검사
            PreparedStatement tmpStmt = conn.prepareStatement("select 1 from Customers where customer_id=?");
            tmpStmt.setInt(1, customer_id);
            rset = tmpStmt.executeQuery();
            //ID가 존재하지 않으면
            if(!rset.next()) {
                System.out.println("This customer ID does not exist.\n");
                return;
            }
            //ID 존재하면
            pStmt.setInt(2, customer_id);

            // 본인 주문 내역
            tmpStmt = conn.prepareStatement("select order_num, order_date, cake_id, cake_name, store_id "
                    +"from orders join cakes using (cake_id) "
                    +"where customer_id=?");
            tmpStmt.setInt(1, customer_id);
            rset = tmpStmt.executeQuery();

            System.out.println("\n<< ORDERS >>");
            System.out.println("================================================================");
            System.out.println(" OrderNum | OrderDate  | Cake id | Cake               | Store");
            System.out.println("----------------------------------------------------------------");
            boolean foundResults = false;
            while (rset.next()) {
                foundResults = true;
                System.out.printf(" %-8d | %-8s | %-7s | %-18s | %s\n",
                        rset.getInt(1), rset.getDate(2), rset.getString(3),
                        rset.getString(4), rset.getString(5));
            }
            if(!foundResults) {
                System.out.println("There are no order records.");
            }
            System.out.println("================================================================\n");

            ////order_num
            System.out.print("Please enter the order number of the order you want to review (ex.1) : ");
            String order_num = s.nextLine();
            pStmt.setString(5, order_num);

            ////cake_id & store_id: 주문번호로 select을 통해 입력하기
            String cake_id = null, store_id = null;
            tmpStmt = conn.prepareStatement("select cake_id, store_id from orders "
                    + "where order_num=?");
            tmpStmt.setString(1, order_num);
            rset = tmpStmt.executeQuery();
            if(rset.next()) {
                cake_id = rset.getString(1);
                store_id = rset.getString(2);
                pStmt.setString(4, cake_id);
                pStmt.setString(6, store_id);
            }

            ////review_rate
            System.out.print("Please enter the rating (ex.4.8) : ");
            float review_rate = s.nextFloat();
            while(review_rate < 0.0 || review_rate > 5.0) { // 입력한 평점이 유효하지 않다면
                System.out.println("Invalid rate. Please enter the floating number from 0.0 up to 5.0: ");
                review_rate = s.nextFloat();
            }
            pStmt.setFloat(3, review_rate);
            pStmt.executeUpdate();
            System.out.println("\nReview submitted successfully!\n");

            //rate 반영 - update로 해야 함
            try {
                conn.setAutoCommit(false); // transaction 시작
                System.out.println(store_id);
                stmt.executeUpdate(
                        "update Stores "
                                + "set store_rate = ("
                                + "select avg(review_rate) "
                                + "from Reviews "
                                + "where store_id = '"+store_id+"') "
                                + "where store_id = '"+store_id+"'");
                conn.commit();
            }catch(SQLException e) {
                conn.rollback();
            }finally {
                conn.setAutoCommit(true);
            }

        } catch(SQLException e) {
            System.out.println(e);
        }
    }





    // update - 영서 
    public static void update(Connection conn) {
        try (Statement stmt=conn.createStatement();) {
            Scanner sc = new Scanner(System.in); //입력 받기 위해서 scanner 객체 생성
            String sql_cus = "UPDATE Customers "
                    +" SET customer_name = ?, address = ?, phone_number = ?, email = ? WHERE customer_name = ?";
            String sql_sell = "UPDATE Cakes "
                    +" SET cake_name = ?, price = ? WHERE cake_name = ?";

            String check_cus = "SELECT * from Customers WHERE customer_name = ?";
            String check_cake = "SELECT * from Cakes WHERE cake_name = ?";



            while(true) {

                System.out.println("Customer: number 1, Manager : number 2 >>");
                int role = sc.nextInt();
                sc.nextLine();


                if(role == 1) {
                    //비워두고 나중에 사용자 입력값 넣기
                    PreparedStatement pStmt = conn.prepareStatement(sql_cus);

                    //고객 정보 변경 인풋 받기

                    System.out.println("Enter your name please. >>");
                    String ori_name = sc.nextLine();

                    try (PreparedStatement checkStmt = conn.prepareStatement(check_cus)) {
                        checkStmt.setString(1, ori_name);  // 새 이름 기준으로 SELECT

                        try (ResultSet rs = checkStmt.executeQuery()){ //입력한 이름이 디비에 없을 경우
                            if (!rs.next()) {
                                System.out.println("No Name in DB");
                                continue;  //다시 이름 물어보기
                            }


                        }catch(SQLException sqle) {
                            System.out.println("SQLException: "+sqle);
                        }

                        System.out.println("~~~Updating starts. Please enter your new information.~~~\r\n");

                        System.out.println("Enter your NEW NAME please. >>");
                        String customer_name = sc.nextLine();

                        System.out.println("Enter your NEW ADDRESS please. >>");
                        String address = sc.nextLine();

                        System.out.println("Enter your NEW PHONE NUMBER please. >>");
                        String phone_number = sc.nextLine();

                        System.out.println("Enter your NEW EMAIL please. >>");
                        String email = sc.nextLine();

                        //내용 update(변경) sql
                        try {
                            conn.setAutoCommit(false); //transaction 시작
                            pStmt.setString(1, customer_name);
                            pStmt.setString(2, address);
                            pStmt.setString(3, phone_number);
                            pStmt.setString(4, email);
                            pStmt.setString(5, ori_name);
                            pStmt.executeUpdate();  //함수 실행

                            conn.commit();
                        }catch(SQLException e) {
                            conn.rollback();
                        }finally {
                            conn.setAutoCommit(true);
                        }

                        //터미널 출력
                        String selectSql = "SELECT * FROM Customers WHERE customer_name = ?";
                        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                            selectStmt.setString(1, customer_name);  // 새 이름 기준으로 SELECT

                            try (ResultSet rs = selectStmt.executeQuery()) {
                                System.out.println("====================================================================================");
                                System.out.printf("%-12s| %-15s| %-20s| %-18s| %-30s\n", "customer_id","customer_name","address","phone_number","email"); // 헤더 출력

                                System.out.println("------------------------------------------------------------------------------------");

                                while (rs.next()) {

                                    String id = rs.getString("customer_id");
                                    String name = rs.getString("customer_name");
                                    String addr = rs.getString("address");
                                    String phone = rs.getString("phone_number");
                                    String em = rs.getString("email");
                                    System.out.printf("%-12s| %-14s| %-19s| %-17s| %-30s\n",
                                            id,name, addr, phone, em);

                                }
                                break;
                            }
                        }
                    }}

                else if(role == 2) {
                    //비워두고 나중에 사용자 입력값 넣기
                    PreparedStatement pStmt = conn.prepareStatement(sql_sell);

                    //고객 정보 변경 인풋 받기



                    //케이크 정보 변경 인풋 받기

                    System.out.println("Enter CAKE NAME please.");
                    String ori_name = sc.nextLine();

                    try (PreparedStatement checkStmt = conn.prepareStatement(check_cake)) {
                        checkStmt.setString(1, ori_name);  // 새 이름 기준으로 SELECT

                        try (ResultSet rs = checkStmt.executeQuery()){ //입력한 이름이 디비에 없을 경우
                            if (!rs.next()) {
                                System.out.println("No Cake name in DB");
                                continue;  //다시 이름 물어보기
                            }
                        }catch(SQLException sqle) {
                            System.out.println("SQLException: "+sqle);
                        }



                        System.out.println("~~~Updating starts. Please enter your new information.~~~\r\n");

                        System.out.println("Enter NEW CAKE NAME please.");
                        String cake_name = sc.nextLine();

                        System.out.println("Enter NEW CAKE PRICE please.");
                        int price = sc.nextInt();
                        sc.nextLine();


                        //내용 update(변경) sql
                        try {
                            conn.setAutoCommit(false);
                            pStmt.setString(1, cake_name);
                            pStmt.setInt(2, price);
                            pStmt.setString(3, ori_name);

                            pStmt.executeUpdate(); //함수 실행
                            conn.commit();
                        }catch(SQLException e) {
                            conn.rollback();
                        }finally {
                            conn.setAutoCommit(true);
                        }

                        //터미널 출력

                        String selectSql = "SELECT * FROM Cakes WHERE cake_name = ?";
                        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                            selectStmt.setString(1, cake_name);  // 새 이름 기준으로 SELECT
                            try (ResultSet rs = selectStmt.executeQuery()) {
                                System.out.println("====================================================================");
                                System.out.printf("%-12s| %-28s| %-20s| %-18s \n","cake_id","cake_name","cake_price","cake_rate"); // 헤더 출력"

                                System.out.println("--------------------------------------------------------------------");


                                while (rs.next()) {
                                    String id = rs.getString("cake_id");
                                    String name = rs.getString("cake_name");
                                    int cake_price = rs.getInt("price");
                                    float cake_rate = rs.getFloat("cake_rate");

                                    System.out.printf("%-12s| %-28s| %-20s| %-18s \n",
                                            id, name, cake_price, cake_rate);

                                }
                                break;
                            }
                        }
                    }}
                else {
                    System.out.println("~~~~select between 1 and 2~~~~\r\r");
                    continue;
                }

            }
        }
        catch(SQLException sqle){ //오류 날 경우 처리
            System.out.println("SQLException : "+sqle);
        }
    }





    //select 판매자 - 유진
    public static void selectCustomerMain(Connection conn){
        Scanner scanner = new Scanner(System.in); // 사용자 입력을 위한 Scanner

        System.out.println("===============================");
        System.out.println(" CAKE ORDERING SYSTEM ");
        System.out.println("===============================");
        System.out.println("Application started!");

        System.out.println("Attempting to connect to database...");
        // try-with-resources 구문을 사용하여 Connection 객체 관리
        try { // DB 연결 시도 및 Connection 객체 자동 해제 설정
            System.out.println("Database connection successful!");
            while (true) { // 애플리케이션 종료 선택 전까지 반복
                printMainMenu(); // 메인 메뉴 출력
                int choice = getUserChoice(scanner); // 사용자 선택 입력 받기

                switch (choice) {
                    case 1: // 판매자 메뉴 선택
                        handleSellerMenu(conn, scanner); // 판매자 기능 처리 메서드 호출
                        break;
                    case 2: // 구매자 메뉴 선택
                        handleBuyerMenu(conn, scanner); // 구매자 기능 처리 메서드 호출 (다른 팀원 구현)
                        break;
                    case 0: // 종료 선택
                        System.out.println("Exiting application.");
                        return; // main 메서드 종료 -> 애플리케이션 종료
                    default:
                        System.out.println("Invalid choice. Please enter again.");
                }
                System.out.println(); // 메뉴 선택 후 줄바꿈
            }

        }
        /*catch (SQLException e) {
            // 데이터베이스 연결 또는 SQL 쿼리 실행 중 오류 발생 시 처리
            System.err.println("Database error occurred: " + e.getMessage());
            e.printStackTrace();
        }*/
         catch (InputMismatchException e) {
            // getUserChoice 메서드에서 잘못된 입력(정수가 아닌 값)을 받았을 때 처리
            System.err.println("Invalid input format. Exiting application.");
            e.printStackTrace();
        } catch (Exception e) {
            // 그 외 예상치 못한 오류 발생 시 처리
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        finally { // Scanner 자원 해제
            System.out.println("Initiating application shutdown...");
            System.out.println("Database connection automatically closed by try-with-resources.");
            if (scanner != null) {
                //scanner.close(); // Close scanner
                System.out.println("Scanner resources released.");
            }
            System.out.println("Application terminated.");
            System.out.println("===============================");
        }
    }

    // 메뉴 관련 메서드 (printMainMenu, getUserChoice, printSellerSubMenu, printBuyerSubMenu
    private static void printMainMenu() {
        System.out.println("-------------------------------");
        System.out.println(" MAIN MENU ");
        System.out.println("-------------------------------");
        System.out.println("1. Seller Menu");
        System.out.println("2. Buyer Menu");
        System.out.println("0. Exit");
        System.out.println("-------------------------------");
        System.out.print("Enter your choice: ");
    }

    // 사용자로부터 메뉴 선택 번호를 정수로 입력받는 메서드
    // 유효하지 않은 입력(정수가 아닌 값) 처리
    private static int getUserChoice(Scanner scanner) {
        while (true) {
            try {
                int choice = scanner.nextInt();
                // 정수를 읽은 후 나머지 줄 소비
                scanner.nextLine();
                return choice;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
                System.out.print("Enter your choice: ");
            }
        }
    }
    // 판매자 상세 메뉴를 콘솔에 출력하는 메서드
    private static void printSellerSubMenu() {
        System.out.println("-------------------------------");
        System.out.println(" SELLER MENU ");
        System.out.println("-------------------------------");
        System.out.println("1. View All Orders for My Store");
        System.out.println("2. View All Reviews for My Store");
        System.out.println("3. Back");
        System.out.println("-------------------------------");
        System.out.print("Enter your choice: ");
    }
    private static void printBuyerSubMenu() {
        System.out.println("-------------------------------");
        System.out.println(" BUYER MENU ");
        System.out.println("-------------------------------");
        // TODO: 여기에 구매자 기능 메뉴 추가
        System.out.println("9. Back");
        System.out.println("-------------------------------");
        System.out.print("Enter your choice: ");
    }

    // 역할별 핸들러 메서드 (handleSellerMenu, handleBuyerMenu)
    private static void handleSellerMenu(Connection conn, Scanner scanner) {
        while (true) { // '뒤로가기' 선택 전까지 반복
            printSellerSubMenu(); // 판매자 상세 메뉴 출력
            int sellerChoice = getUserChoice(scanner); // 사용자 선택 받기

            switch (sellerChoice) {
                case 1: // 1. 내 가게 전체 주문 목록 조회 기능
                    // 주문 조회 구현 메서드 호출
                    viewAllOrdersForMyStore(conn, scanner);
                    break;
                case 2: // 2. 내 가게 전체 리뷰 조회 기능
                    // 리뷰 조회 구현 메서드 호출
                    viewAllReviewsForMyStore(conn, scanner);
                    break;
                case 3: // 메인 메뉴로 뒤로가기
                    System.out.println("Returning to Main Menu."); // 영문 출력
                    return; // handleSellerMenu 메서드 종료
                default:
                    System.out.println("Invalid choice. Please enter again."); // 영문 출력
            }
            System.out.println(); // 기능 실행 후 줄바꿈
        }
    }
    // 구매자 메뉴 선택 시 호출될 메서드
    private static void handleBuyerMenu(Connection conn, Scanner scanner) {
        while (true) { // '뒤로가기' 선택 전까지 반복
            printBuyerSubMenu(); // 구매자 상세 메뉴 출력
            int buyerChoice = getUserChoice(scanner); // 사용자 선택 받기

            switch (buyerChoice) {
                // TODO: 여기에 구매자 기능 호출 추가
                case 9: // 메인 메뉴로 뒤로가기
                    System.out.println("Returning to Main Menu.");
                    return; // handleBuyerMenu 메서드 종료
                default:
                    System.out.println("Invalid choice. Please enter again.");
            }
            System.out.println();
        }
    }
    /**
     * 판매자 - 특정 가게의 전체 주문 목록을 조회하고 콘솔에 출력하는 메서드
     * 사용자로부터 가게 ID를 입력받아 해당 가게의 주문 목록을 보여줍니다.
     * @param conn 데이터베이스 연결 Connection 객체
     * @param scanner 사용자 입력을 받기 위한 Scanner 객체
     */
    private static void viewAllOrdersForMyStore(Connection conn, Scanner scanner) {
        // 1. 실행할 SQL 쿼리 문자열 정의
        String sql = "SELECT "
                + "    Orders.order_num, "
                + "    Orders.order_date, "
                + "    Customers.customer_name, "
                // + "    Customers.phone_number, " // 필요하다면 이 컬럼 포함
                + "    Cakes.cake_name, "
                + "    Orders.price AS order_price " // 주문 시점 가격 사용
                + "FROM Orders  "
                + "JOIN Customers  ON Orders.customer_id = Customers.customer_id "
                + "JOIN Cakes  ON Orders.cake_id = Cakes.cake_id "
                + "WHERE Orders.store_id = ?"; // 판매자의 가게 ID를 파라미터로 받을 위치

        // 2. JDBC 작업 수행 (PreparedStatement 사용, try-with-resources)
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 3. SQL 쿼리의 '?' 파라미터에 값 설정 (사용자 입력 또는 판매자 ID)
            System.out.print("Enter the Store ID to view orders for (e.g., HD001): "); // 영문 출력
            String storeId = scanner.nextLine(); // next() -> nextLine()으로 수정 권장

            pstmt.setString(1, storeId); // 첫 번째 '?'에 입력받은 storeId 값(String) 설정

            // 4. SQL 쿼리 실행 및 결과 가져오기
            System.out.println("Fetching order list for store " + storeId + "..."); // 영문 출력

            try (ResultSet rs = pstmt.executeQuery()) {

                // 5. 쿼리 실행 결과(ResultSet) 처리 및 콘솔 출력
                System.out.println("=============================================================");
                System.out.printf(" %-8s | %-10s | %-12s | %-15s | %s\n",
                        "Order #", "Order Date", "Customer Name", "Cake Name", "Price"); // 영문 출력 헤더
                System.out.println("-------------------------------------------------------------");

                boolean foundResults = false; // 조회된 결과가 있는지 확인 플래그
                while (rs.next()) { // 결과 행 반복
                    foundResults = true;
                    int orderNum = rs.getInt("order_num");
                    String orderDate = rs.getString("order_date");
                    String customerName = rs.getString("customer_name");
                    // String phoneNumber = rs.getString("phone_number"); // 포함시
                    String cakeName = rs.getString("cake_name");
                    int orderPrice = rs.getInt("order_price");

                    // 가져온 데이터 출력
                    System.out.printf(" %-8d | %-10s | %-12s | %-15s | %d\n",
                            orderNum, orderDate, customerName, cakeName, orderPrice);
                }

                // 6. 결과가 없을 경우 메시지 출력
                if (!foundResults) {
                    System.out.println(" No order history found for this store."); // 영문 출력
                }

                System.out.println("=============================================================");

            } // 내부 try 블록 종료 시 ResultSet 자동 닫힘

        } catch (SQLException e) {
            // SQL 관련 예외 처리
            System.err.println("Database error while viewing order list: " + e.getMessage()); // 영문 오류 출력
            // e.printStackTrace(); // 개발/디버깅용
        } // 외부 try 블록 종료 시 PreparedStatement 자동 닫힘
        // Connection은 main 메서드의 try-with-resources 블록에서 관리됩니다.
    }

    /**
     * 판매자 - 특정 가게의 전체 리뷰를 조회하고 콘솔에 출력하는 메서드
     * 사용자로부터 가게 ID를 입력받아 해당 가게의 리뷰 목록을 보여줌
     * @param conn 데이터베이스 연결 Connection 객체
     * @param scanner 사용자 입력을 받기 위한 Scanner 객체
     */
    private static void viewAllReviewsForMyStore(Connection conn, Scanner scanner) {
        // 1. 실행할 SQL 쿼리 문자열 정의
        String sql = "SELECT "
                + "    R.review_num, "
                + "    R.review_rate, "
                + "    C.customer_name, "
                + "    CA.cake_name "
                + "FROM Reviews R "
                + "JOIN Customers C ON R.customer_id = C.customer_id "
                + "JOIN Cakes CA ON R.cake_id = CA.cake_id "
                + "WHERE R.store_id = ?"; // 판매자의 가게 ID를 파라미터로 받을 위치

        // 2. JDBC 작업 수행 (PreparedStatement 사용, try-with-resources)
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 3. SQL 쿼리의 '?' 파라미터에 값 설정 (사용자 입력 또는 판매자 ID)
            System.out.print("Enter the Store ID to view reviews for (e.g., HD001): "); // 영문 출력
            String storeId = scanner.nextLine();
            pstmt.setString(1, storeId); // 첫 번째 '?'에 입력받은 storeId 값(String) 설정

            // 4. SQL 쿼리 실행 및 결과 가져오기
            System.out.println("Fetching review list for store " + storeId + "..."); // 영문 출력
            try (ResultSet rs = pstmt.executeQuery()) {

                // 5. 쿼리 실행 결과(ResultSet) 처리 및 콘솔 출력
                System.out.println("===============================================");
                System.out.printf(" %-10s | %-6s | %-10s | %s\n",
                        "Review #", "Rating", "Customer", "Cake Name"); // 영문 출력 헤더
                System.out.println("-----------------------------------------------");

                boolean foundResults = false;
                while (rs.next()) {
                    foundResults = true;
                    String reviewNum = rs.getString("review_num");
                    double reviewRate = rs.getDouble("review_rate");
                    String customerName = rs.getString("customer_name");
                    String cakeName = rs.getString("cake_name");

                    System.out.printf(" %-10s | %.1f  | %-10s | %s\n",
                            reviewNum, reviewRate, customerName, cakeName); // 별점 출력 포맷 조정
                }

                if (!foundResults) {
                    System.out.println(" No reviews found for this store."); // 영문 출력
                }
                System.out.println("===============================================");

            } // 내부 try 블록 종료 시 ResultSet 자동 닫힘

        } catch (SQLException e) {
            System.err.println("Database error while viewing review list: " + e.getMessage()); // 영문 오류 출력
            // e.printStackTrace();
        } // 외부 try 블록 종료 시 PreparedStatement 자동 닫힘
    }



    //select 구매자 - 지인
    
    //cake number을 input으로 받고, 그에 맞는 케이크의 정보를 출력해주는 기능
    public static void viewCakeInfoCustomer(Connection conn) {

        try (Statement stmt=conn.createStatement();)
        {
            Scanner sc=new Scanner(System.in);

            //select 구매자 - 케이크 조회: VIEW, GROUP BY 사용
            System.out.println("=============================================================");
            System.out.println("[Search Cakes Information by number]\n\n~~~~~~ MENU ~~~~~~");

            ResultSet allCake=stmt.executeQuery("SELECT * FROM Cakes;"); //메뉴판 조회 위한 쿼리

            while(allCake.next()){
                int cake_id = allCake.getInt("cake_id"); //cake id 번호로 받아서
                System.out.println(cake_id+": "+allCake.getString("cake_name")); //cake name과 함께 출력
            }

            String inputCakeName = ""; //초기값 빈 문자열 
            ResultSet allCake2=stmt.executeQuery("SELECT * FROM Cakes;"); //cake number와 cake name 매치하기 위한 쿼리 
            System.out.print("\nPlease enter the number of the cake to retrieve: ");
            int cakeNum=sc.nextInt(); //cake number 입력받고 
            sc.nextLine(); //버퍼 비워줌

            while(allCake2.next()){
                int cake_id = Integer.parseInt(allCake2.getString("cake_id").trim()); //cake id 받아서 앞뒤 공백 제거
                if(cakeNum==cake_id){ //user로부터 받은 cake number와 cake id가 같으면
                    inputCakeName= allCake2.getString("cake_name"); //inputCakeName에 cake name 추가
                }
            }

            PreparedStatement pstmt=conn.prepareStatement("SELECT cake_name, price, cake_rate FROM cake_rate_cal WHERE cake_name=?;"); //사용자의 input(cake_id)에 맞는 케이크 정보 출력용 쿼리
            pstmt.setString(1,inputCakeName); //sql의 ? 자리에 inputCakeName 추가
            ResultSet cake_check=pstmt.executeQuery();

            System.out.println("=============================================================");
            System.out.printf(" %-20s | %-6s | %-3s\n", "Cake Name", "Price","Cake Rating");
            System.out.println("-------------------------------------------------------------");
            if(inputCakeName.isEmpty()) //inputCakeName이 비어있다면 cake number에 해당하는 cake id가 없다는 뜻
                System.out.println("Sorry, there is no cake with that number.");
            else {
                while (cake_check.next()) {
                    String cakeName = cake_check.getString("cake_name");
                    int price = cake_check.getInt("price");
                    double cakeRating=cake_check.getDouble("cake_rate");
                    System.out.printf(" %-20s | %-6d | %.1f\n", cakeName, price,cakeRating); //cake name, price, cake rating 출력
                }
            }
            System.out.println("=============================================================");

        }
        catch(SQLException sqle){ //오류 날 경우 처리
            System.out.println("SQLException : "+sqle);
        }
    }

    //stores를 rating에 따라 출력해주는 기능. 오름차순/내림차순 출력 방법을 사용자로부터 input받아 결정.
    public static void viewStoreInfoCustomer(Connection conn) {

        try (Statement stmt=conn.createStatement();)
        {
            Scanner sc=new Scanner(System.in);

            //select 구매자 - 지점 조회 : AGGREGATION, GROUP BY 사용
            System.out.println("[View Stores sorted by store rating]\nAscending order: 1\nDescending order: 2");
            System.out.print("Please enter the number of the option: ");

            int orderNum=sc.nextInt(); //option 선택을 위해 사용자 input 받음
            String sql = "SELECT s.store_name, COUNT(r.review_num) AS review_count, AVG(r.review_rate) AS avg_rating FROM Reviews r JOIN Stores s ON r. store_id=s.store_id GROUP BY s.store_name";
            if (orderNum==1){ //ascending order을 원한다면
                sql=sql+" ORDER BY avg_rating ASC;"; //원래 명시하지 않아도 되지만 코드의 흐름 이해를 위해 명시해줌
            }
            else if (orderNum==2){ //descending order을 원한다면
                sql=sql+" ORDER BY avg_rating DESC;";
            }
            else { //1,2 외의 다른 번호 입력 시, default인 asc로 출력
                System.out.println("=============================================================");
                System.out.println("Sorry, there is no cake with that number.\nWe will show default option: Ascending Order");
                sql=sql+" ORDER BY avg_rating ASC;";
            }

            ResultSet store_check=stmt.executeQuery(sql);
            System.out.println("=============================================================");
            System.out.printf(" %-15s | %-6s | %-6s\n", "Store Name", "Number of Reviews","Store Rating");
            System.out.println("-------------------------------------------------------------");

            while(store_check.next()){
                String storeName=store_check.getString("store_name");
                int reviewCount=store_check.getInt("review_count");
                double avgRating=store_check.getDouble("avg_rating");
                System.out.printf(" %-11s | %-17d | %.1f\n",storeName,reviewCount,avgRating); //store name, number of reviews, store rating 출력
            }
            System.out.println("=============================================================");

        }
        catch(SQLException sqle){ //오류 날 경우 처리
            System.out.println("SQLException : "+sqle);
        }

    }

    //리뷰 작성자의 이름을 input으로 받아 그 사람이 작성한 리뷰를 보여주는 기능
    public static void viewReviewbyNameCustomer(Connection conn) {
        try (Statement stmt=conn.createStatement();)
        {
            Scanner sc=new Scanner(System.in);

            //select 구매자 - 리뷰 조회 : VIEW, JOIN 사용
            System.out.println("[Search Reviews by Customer Name]");
            System.out.print("Please enter the name of the reviewer to retrieve : ");

            String inputName=sc.nextLine();

            PreparedStatement pstmt=conn.prepareStatement("SELECT * FROM review_info WHERE customer_name=?;"); //리뷰 작성자 이름에 매칭되는 리뷰 출력용 쿼리
            pstmt.setString(1,inputName); //? 자리에 input으로 받은 이름 삽입

            ResultSet review_check=pstmt.executeQuery();
            boolean found=false;

            System.out.println("=============================================================");
            System.out.printf(" %-15s | %-20s | %-3s | %-15s\n", "Customer Name", "Cake Name","Rating","Store Name");
            System.out.println("-------------------------------------------------------------");

            while(review_check.next()){
                if(review_check.getString("customer_name").equals(inputName))
                    found=true; //inputName에 매칭되는 customer_name을 찾으면 found.

                String customerName=review_check.getString("customer_name");
                String cakeName=review_check.getString("cake_name");
                double rating=review_check.getDouble("review_rate");
                String storeName=review_check.getString("store_name");
                System.out.printf(" %-12s | %-20s | %6.1f | %-15s\n", customerName,cakeName,rating,storeName); //customer name, cake name, rating, store name 출력

            }
            if(!found){ //inputName에 매칭되는 customer_name을 찾지 못했다면
                System.out.println("Sorry, there are no reviews written by this customer.");
            }
            System.out.println("=============================================================");

        }
        catch(SQLException sqle){ //오류 날 경우 처리
            System.out.println("SQLException : "+sqle);
        }


    }



}
