package ru.edu.nsu.fit.martynov.autoparts.db;
import ru.edu.nsu.fit.martynov.autoparts.dto.UserDto;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;

public class DataBaseController {
    private static final String DATABASE_URL = "jdbc:oracle:thin:@//84.237.50.81:1521/XE";
    private static final String DATABASE_USER = "20204_Martynov";
    private static final String DATABASE_PASSWORD = "6Yv-3Zd-KnZ-kFS";

    private static DataBaseController controller = null;
    private Connection connection;

    private DataBaseController() throws SQLException {
        connection = DriverManager.getConnection(
                DATABASE_URL,
                DATABASE_USER,
                DATABASE_PASSWORD
        );
    }

    public static DataBaseController getInstance() throws SQLException {
        if (controller == null) {
            synchronized (DataBaseController.class) {
                if (controller == null) {
                    controller = new DataBaseController();
                }
            }
        } else {
            if (controller.connection.isClosed()) {
                synchronized (controller.connection) {
                    if (controller.connection.isClosed()) {
                        controller.connection = DriverManager.getConnection(
                                DATABASE_URL,
                                DATABASE_USER,
                                DATABASE_PASSWORD
                        );
                    }
                }
            }
        }
        return controller;
    }

    public DataBaseController init() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            deleteTables(statement);
            createTables(statement);
            fillTables(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

    public UserDto getUserByLogin(String login) {
        PreparedStatement preparedStatement = null;
        ResultSet result = null;

        try {
            preparedStatement = connection.prepareStatement(
                    "SELECT user_id, " +
                        "   login, " +
                        "   password " +
                        "FROM Users " +
                        "WHERE login = ?"
            );
            preparedStatement.setString(1, login);
            result = preparedStatement.executeQuery();
            if (result.next()) {
                String pwd = result.getString("password");
                return new UserDto(
                        result.getInt("user_id"),
                        result.getString("login"),
                        result.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Object executeStatement(String sql) throws SQLException {
        Statement statement = null;
        Object result = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
            result = statement.getResultSet();
        } catch (SQLException e) {
            if (e.getErrorCode() == 17002) throw new SQLException();
            return e.getMessage();
        }
        return result;
    }

    public Object executePreparedStatement(String sql, Object... args) throws SQLException {
        PreparedStatement statement = null;
        Object result = null;
        try {
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            result = statement.executeQuery();
        } catch (SQLException e) {
            if (e.getErrorCode() == 17002) throw new SQLException();
            return e.getMessage();
        }
        return result;
    }

    private Statement getNewStatement() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statement;
    }

    private void closeStatement(Statement statement) {
        try {
            if (statement != null) statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables(Statement statement) throws SQLException {
        statement.execute(
                "CREATE TABLE Users" +
                    "(" +
                    "   user_id INT PRIMARY KEY," +
                    "   login VARCHAR(50) NOT NULL UNIQUE," +
                    "   password VARCHAR(100) NOT NULL" +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Countries" +
                    "(" +
                    "   country_id   INT         PRIMARY KEY," +
                    "   country_name VARCHAR(50) NOT NULL UNIQUE" +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Car_brands " +
                    "( " +
                    "   brand_id INT PRIMARY KEY, " +
                    "   country_id INT CONSTRAINT cbrands_cid REFERENCES Countries(country_id) NOT NULL, " +
                    "   title VARCHAR(50) NOT NULL UNIQUE " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Car_model " +
                    "( " +
                    "   model_id INT PRIMARY KEY, " +
                    "   brand_id INT CONSTRAINT cmodel_bid REFERENCES Car_brands(brand_id) ON DELETE CASCADE NOT NULL," +
                    "   title VARCHAR(50) NOT NULL, CONSTRAINT cmodel_uqe UNIQUE(brand_id, title) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Car " +
                    "(" +
                    "   car_id INT PRIMARY KEY, " +
                    "   model_id INT REFERENCES Car_model(model_id) ON DELETE CASCADE NOT NULL, " +
                    "   generation VARCHAR(50) NOT NULL, CONSTRAINT car_uqe UNIQUE(model_id, generation)" +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Manufacturer " +
                    "( " +
                    "   manufacturer_id INT PRIMARY KEY, " +
                    "   country_id INT CONSTRAINT manufacturer_cid REFERENCES Countries(country_id) ON DELETE CASCADE NOT NULL, " +
                    "   title VARCHAR(50) NOT NULL " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Part_type " +
                    "( " +
                    "   type_id INT PRIMARY KEY, " +
                    "   title VARCHAR(50) NOT NULL UNIQUE " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Parts " +
                    "( " +
                    "   part_id INT PRIMARY KEY, " +
                    "   part_type INT CONSTRAINT parch REFERENCES Part_type(type_id) ON DELETE CASCADE NOT NULL, " +
                    "   car_id INT CONSTRAINT carch REFERENCES Car(car_id), " +
                    "   manufacturer_id INT CONSTRAINT mnfch REFERENCES Manufacturer(manufacturer_id) ON DELETE CASCADE NOT NULL, " +
                    "   price INT NOT NULL CHECK(price > 0) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Supplier_types " +
                    "( " +
                    "   stype_id INT PRIMARY KEY, " +
                    "   type_name VARCHAR(50) NOT NULL " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Suppliers " +
                    "( " +
                    "   supplier_id INT PRIMARY KEY, " +
                    "   stype_id INT CONSTRAINT stpch REFERENCES Supplier_types(stype_id) ON DELETE CASCADE NOT NULL, " +
                    "   country_id INT CONSTRAINT ctrch REFERENCES Countries(country_id) ON DELETE CASCADE NOT NULL, " +
                    "   title VARCHAR(50) NOT NULL " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Dealers " +
                    "( " +
                    "   dealer_id INT PRIMARY KEY CONSTRAINT dlrch REFERENCES Suppliers(supplier_id), " +
                    "   discount INT CHECK(discount >= 0) CHECK(discount < 100) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Price_list " +
                     "( " +
                     "   part_id INT CONSTRAINT prtch REFERENCES Parts(part_id) ON DELETE CASCADE, " +
                     "   supplier_id INT CONSTRAINT splch REFERENCES Suppliers(supplier_id) ON DELETE CASCADE, " +
                     "   price INT CHECK(price > 0) NOT NULL, " +
                     "   delivery_time INT CHECK(delivery_time >= 0) NOT NULL, " +
                     "   CONSTRAINT pskey PRIMARY KEY(part_id, supplier_id) " +
                     ")"
        );

        statement.execute(
                "CREATE TABLE Supply " +
                    "( " +
                    "   supply_id INT PRIMARY KEY, " +
                    "   supplier_id INT CONSTRAINT splrch REFERENCES Suppliers(supplier_id) ON DELETE CASCADE, " +
                    "   order_date DATE NOT NULL, " +
                    "   receipt_date DATE, CONSTRAINT datech CHECK(receipt_date > order_date) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Supply_list " +
                    "( " +
                    "   supply_id INT CONSTRAINT slist_sid REFERENCES Supply(supply_id) ON DELETE CASCADE, " +
                    "   part_id INT CONSTRAINT slist_pid REFERENCES Parts(part_id) ON DELETE CASCADE, " +
                    "   price INT CHECK(price > 0) NOT NULL, " +
                    "   count INT CHECK(count >= 0) NOT NULL, " +
                    "   CONSTRAINT slist_key PRIMARY KEY(supply_id, part_id) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Part_info " +
                    "( " +
                    "   info_id INT PRIMARY KEY, " +
                    "   part_id INT CONSTRAINT pinfo_pid REFERENCES Parts(part_id) ON DELETE CASCADE NOT NULL, " +
                    "   supply_id INT CONSTRAINT pinfo_sid REFERENCES Supply(supply_id) ON DELETE CASCADE NOT NULL " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Stock " +
                    "( " +
                    "   cell_id INT PRIMARY KEY, " +
                    "   part_type INT CONSTRAINT stock_ptp REFERENCES Part_type(type_id) ON DELETE CASCADE NOT NULL, " +
                    "   capacity INT NOT NULL CHECK(capacity > 0) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE In_stock " +
                    "( " +
                    "   cell_id INT CONSTRAINT instock_cid REFERENCES Stock(cell_id) ON DELETE CASCADE, " +
                    "   part_info INT CONSTRAINT instock_pif REFERENCES Part_info(info_id) ON DELETE CASCADE, " +
                    "   count INT CHECK(count >= 0) NOT NULL, " +
                    "   CONSTRAINT instock_key PRIMARY KEY(cell_id, part_info) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Carts " +
                    "( " +
                    "   cart_id INT PRIMARY KEY, " +
                    "   transaction_date DATE " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Customers " +
                    "( " +
                    "   customer_id INT PRIMARY KEY, " +
                    "   phone_number VARCHAR(15) NOT NULL, " +
                    "   email VARCHAR(50), " +
                    "   first_name VARCHAR(20) NOT NULL, " +
                    "   last_name VARCHAR(20) NOT NULL " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Cart_list " +
                    "( " +
                    "   cart_id INT CONSTRAINT clist_cid REFERENCES Carts(cart_id) ON DELETE CASCADE, " +
                    "   part_info INT CONSTRAINT clist_pif REFERENCES Part_info(info_id) ON DELETE CASCADE, " +
                    "   price INT CHECK(price > 0) NOT NULL, " +
                    "   count INT CHECK(count > 0) NOT NULL, " +
                    "   CONSTRAINT clist_key PRIMARY KEY(cart_id, part_info) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Orders " +
                    "( " +
                    "   cart_id INT CONSTRAINT orders_cad REFERENCES Carts(cart_id) ON DELETE CASCADE, " +
                    "   customer_id INT CONSTRAINT orders_cmd REFERENCES Customers(customer_id) ON DELETE CASCADE NOT NULL, " +
                    "   order_date DATE NOT NULL, CONSTRAINT orders_key PRIMARY KEY(cart_id) " +
                    ")"
        );

        statement.execute(
                "CREATE TABLE Reject " +
                    "( " +
                    "   cart_id INT CONSTRAINT reject_cid REFERENCES Carts(cart_id) ON DELETE CASCADE, " +
                    "   part_info INT CONSTRAINT reject_pif REFERENCES Part_info(info_id) ON DELETE CASCADE, " +
                    "   CONSTRAINT reject_key PRIMARY KEY(cart_id, part_info) " +
                    ")"
        );
    }

    private void createTriggers(CallableStatement statement) throws SQLException {
        // Countries - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_country_id START WITH 1;" +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Countries_Id\"" +
                    "BEFORE INSERT ON Countries" +
                    "FOR EACH ROW" +
                    "BEGIN" +
                    "   SELECT seq_country_id.nextval " +
                    "   INTO :new.country_id " +
                    "   FROM dual;" +
                    "END;"
        );

        // Countries - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_country\" " +
                    "AFTER UPDATE OF country_id ON Countries " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Car_brands SET country_id = :new.country_id WHERE country_id = :old.country_id; " +
                    "   UPDATE Manufacturer SET country_id = :new.country_id WHERE country_id = :old.country_id; " +
                    "   UPDATE Suppliers SET country_id = :new.country_id WHERE country_id = :old.country_id; " +
                    "END;"
        );

        // Car brands - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_car_brands_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Car_brands_Id\" " +
                    "BEFORE INSERT ON Car_brands " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_car_brands_id.nextval " +
                    "   INTO :new.brand_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Car brands - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_car_brand\" " +
                    "AFTER UPDATE OF brand_id ON Car_brands " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Car_model SET brand_id = :new.brand_id WHERE brand_id = :old.brand_id; " +
                    "END;"
        );

        // Car model - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_car_model_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Car_model_Id\" " +
                    "BEFORE INSERT ON Car_model " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_car_model_id.nextval " +
                    "   INTO :new.model_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Car model - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_car_model\" " +
                    "AFTER UPDATE OF model_id ON Car_model " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Car SET model_id = :new.model_id WHERE model_id = :old.model_id; " +
                    "END;"
        );

        // Car - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_car_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Car_Id\" " +
                    "BEFORE INSERT ON Car " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_car_id.nextval " +
                    "   INTO :new.car_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Car - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_car\" " +
                    "AFTER UPDATE OF car_id ON Car " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Parts SET car_id = :new.car_id WHERE car_id = :old.car_id; " +
                    "END;"
        );

        // Manufacturer - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_manufacturer_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Manufacturer_Id\" " +
                    "BEFORE INSERT ON Manufacturer " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_manufacturer_id.nextval " +
                    "   INTO :new.manufacturer_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Manufacturer - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_manufacturer\" " +
                    "AFTER UPDATE OF manufacturer_id ON Manufacturer " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Parts SET manufacturer_id = :new.manufacturer_id WHERE manufacturer_id = :old.manufacturer_id; " +
                    "END;"
        );

        // Part_type - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_part_type_id START WITH 1; " +
                        "CREATE OR REPLACE TRIGGER \"Trigger_Part_type_Id\" " +
                        "BEFORE INSERT ON Part_type " +
                        "FOR EACH ROW " +
                        "BEGIN " +
                        "   SELECT seq_part_type_id.nextval " +
                        "   INTO :new.type_id " +
                        "   FROM dual; " +
                        "END;"
        );

        // Part_type - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_part_type\" " +
                    "AFTER UPDATE OF type_id ON Part_type " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Parts SET part_type = :new.type_id WHERE part_type = :old.type_id; " +
                    "   UPDATE Stock SET part_type = :new.type_id WHERE part_type = :old.type_id; " +
                    "END;"
        );

        // Parts - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_parts_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Parts_Id\" " +
                    "BEFORE INSERT ON Parts " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_parts_id.nextval " +
                    "   INTO :new.part_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Parts - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_parts\" " +
                    "AFTER UPDATE OF part_id ON Parts " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Part_info SET part_id = :new.part_id WHERE part_id = :old.part_id; " +
                    "   UPDATE Supply_list SET part_id = :new.part_id WHERE part_id = :old.part_id; " +
                    "   UPDATE Price_list SET part_id = :new.part_id WHERE part_id = :old.part_id; " +
                    "END;"
        );

        // Supplier_types - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_supplier_types_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Supplier_types_Id\" " +
                    "BEFORE INSERT ON Supplier_types " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_supplier_types_id.nextval " +
                    "   INTO :new.stype_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Supplier_types - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_supplier_types\" " +
                    "AFTER UPDATE OF stype_id ON Supplier_types " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Suppliers SET stype_id = :new.stype_id WHERE stype_id = :old.stype_id; " +
                    "END;"
        );

        // Suppliers - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_suppliers_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Suppliers_Id\" " +
                    "BEFORE INSERT ON Suppliers " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_suppliers_id.nextval " +
                    "   INTO :new.supplier_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Suppliers - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_suppliers\" " +
                    "AFTER UPDATE OF supplier_id ON Suppliers " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Supply SET supplier_id = :new.supplier_id WHERE supplier_id = :old.supplier_id; " +
                    "   UPDATE Dealers SET dealer_id = :new.supplier_id WHERE dealer_id = :old.supplier_id; " +
                    "   UPDATE Price_list SET supplier_id = :new.supplier_id WHERE supplier_id = :old.supplier_id; " +
                    "END;"
        );

        // Suppliers - Adding dealer
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_add_dealer\" " +
                    "AFTER INSERT ON Suppliers " +
                    "FOR EACH ROW " +
                    "WHEN (new.stype_id = 2) " +
                    "BEGIN " +
                    "   INSERT INTO Dealers VALUES(:new.supplier_id, 0); " +
                    "END;"
        );

        // Supply - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_supply_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Supply_Id\" " +
                    "BEFORE INSERT ON Supply " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_supply_id.nextval " +
                    "   INTO :new.supply_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Supply - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_supply\" " +
                    "AFTER UPDATE OF supply_id ON Supply " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Part_info SET supply_id = :new.supply_id WHERE supply_id = :old.supply_id; " +
                    "   UPDATE Supply_list SET supply_id = :new.supply_id WHERE supply_id = :old.supply_id; " +
                    "END;"
        );

        // Supply - Insert order date
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_Supply_insert\" " +
                    "BEFORE INSERT ON Supply " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   :new.order_date := SYSDATE; " +
                    "END;"
        );

        // Supply - Update receipt date
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_Supply_receipt\" " +
                        "BEFORE UPDATE OF receipt_date ON Supply " +
                        "FOR EACH ROW " +
                        "BEGIN " +
                        "   IF :old.receipt_date IS NULL " +
                        "       THEN :new.receipt_date := SYSDATE; " +
                        "       ELSE :new.receipt_date := :old.receipt_date; " +
                        "   END IF; " +
                        "END;"
        );

        // Part_info - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_part_info_id START WITH 1; " +
                        "CREATE OR REPLACE TRIGGER \"Trigger_Part_info_Id\" " +
                        "BEFORE INSERT ON Part_info " +
                        "FOR EACH ROW " +
                        "BEGIN " +
                        "   SELECT seq_part_info_id.nextval " +
                        "   INTO :new.info_id " +
                        "   FROM dual; " +
                        "END;"
        );

        // Part_info - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_part_info\" " +
                    "AFTER UPDATE OF info_id ON Part_info " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Cart_list SET part_info = :new.info_id WHERE part_info = :old.info_id; " +
                    "   UPDATE In_stock SET part_info = :new.info_id WHERE part_info = :old.info_id; " +
                    "   UPDATE Reject SET part_info = :new.info_id WHERE part_info = :old.info_id; " +
                    "END;"
        );

        // Stock - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_stock_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Stock_Id\" " +
                    "BEFORE INSERT ON Stock " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_stock_id.nextval " +
                    "   INTO :new.cell_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Stock - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_stock\" " +
                    "AFTER UPDATE OF cell_id ON Stock " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE IN_stock SET cell_id = :new.cell_id WHERE cell_id = :old.cell_id; " +
                    "END;"
        );

        // Carts - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_carts_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Carts_Id\" " +
                    "BEFORE INSERT ON Carts " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_carts_id.nextval " +
                    "   INTO :new.cart_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Carts - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_carts\" " +
                    "AFTER UPDATE OF cart_id ON Carts " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Orders SET cart_id = :new.cart_id WHERE cart_id = :old.cart_id; " +
                    "   UPDATE Cart_list SET cart_id = :new.cart_id WHERE cart_id = :old.cart_id; " +
                    "   UPDATE Reject SET cart_id = :new.cart_id WHERE cart_id = :old.cart_id; " +
                    "END;"
        );

        // Carts - Insert transaction date
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_Carts_insert\" " +
                    "BEFORE INSERT ON Carts " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   IF :new.transaction_date IS NULL " +
                    "       THEN :new.transaction_date := NULL; " +
                    "       ELSE :new.transaction_date := SYSDATE; " +
                    "   END IF; " +
                    "END;"
        );

        // Carts - Update transaction date
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_Cart_finish\" " +
                    "BEFORE UPDATE OF transaction_date ON Carts " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   IF :old.transaction_date IS NULL " +
                    "       THEN :new.transaction_date := SYSDATE; " +
                    "       ELSE IF :old.transaction_date != :new.transaction_date " +
                    "           THEN RAISE_APPLICATION_ERROR(-20003, 'Bad operation! Changing transaction date forbiden!'); " +
                    "       END IF; " +
                    "   END IF; " +
                    "END;"
        );

        // Customers - Autoincrement
        statement.execute(
                "CREATE SEQUENCE seq_customers_id START WITH 1; " +
                    "CREATE OR REPLACE TRIGGER \"Trigger_Customers_Id\" " +
                    "BEFORE INSERT ON Customers " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   SELECT seq_customers_id.nextval " +
                    "   INTO :new.customer_id " +
                    "   FROM dual; " +
                    "END;"
        );

        // Customers - Cascade update
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_update_customers\" " +
                    "AFTER UPDATE OF customer_id ON Customers " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   UPDATE Orders SET customer_id = :new.customer_id WHERE customer_id = :old.customer_id; " +
                    "END;"
        );

        // Orders - Autoincrement
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_Orders_insert\" " +
                    "BEFORE INSERT ON Orders " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   :new.order_date := SYSDATE; " +
                    "END;"
        );

        // Orders - Update order date
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_Orders_order_date\" " +
                    "BEFORE UPDATE OF order_date ON Orders " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "   IF :old.order_date != :new.order_date " +
                    "       THEN RAISE_APPLICATION_ERROR(-20002, 'Bad operation! Changing order date forbidden!'); " +
                    "   END IF; " +
                    "END;"
        );

        // Reject - Reject inspection
        statement.execute(
                "CREATE OR REPLACE TRIGGER \"Trigger_Reject_insert\" " +
                        "BEFORE INSERT ON Reject " +
                        "FOR EACH ROW " +
                        "DECLARE " +
                        "   bought_count INT; " +
                        "BEGIN " +
                        "   SELECT count INTO bought_count " +
                        "   FROM Cart_list " +
                        "   WHERE cart_id = :new.cart_id AND part_info = :new.part_info; " +
                        "   IF bought_count < :new.count " +
                        "       THEN RAISE_APPLICATION_ERROR(-20000, 'Reject count > Cart count!'); " +
                        "   END IF; :new.transaction_date := SYSDATE; " +
                        "   EXCEPTION WHEN NO_DATA_FOUND " +
                        "       THEN RAISE_APPLICATION_ERROR(-20001, 'Not found this product in current cart'); " +
                        "END;"
        );
    }

    private void createViews(Statement statement) throws SQLException {
        statement.execute(
                "CREATE OR REPLACE VIEW Car_view AS " +
                    "   SELECT Car_brands.title || ' ' || Car_model.title || ' (' || Car.generation || ')' AS car_name, " +
                    "       car_id, " +
                    "       model_id, " +
                    "       brand_id, " +
                    "       country_id AS brand_country_id " +
                    "   FROM Car " +
                    "   JOIN Car_model USING (model_id) " +
                    "   JOIN Car_brands USING (brand_id);"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Part_view AS " +
                    "   SELECT Part_type.title AS part_type, " +
                    "       Manufacturer.title AS manufacturer, " +
                    "       price AS retail_price, " +
                    "       part_id, " +
                    "       car_id, " +
                    "       type_id, " +
                    "       manufacturer_id, " +
                    "       country_id AS manufacturer_country_id " +
                    "   FROM Parts " +
                    "   JOIN Manufacturer USING (manufacturer_id) " +
                    "   JOIN Part_type ON part_type = Part_type.type_id;"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Price_list_view AS " +
                    "   SELECT Suppliers.title AS Supplier, " +
                    "       part_id, " +
                    "       price, " +
                    "       ROUND(price * ((100 - COALESCE(discount, 0)) / 100), 0) AS final_price, " +
                    "       delivery_time, " +
                    "       supplier_id, " +
                    "       stype_id, " +
                    "       country_id AS supplier_country_id " +
                    "   FROM Price_list " +
                    "   JOIN Suppliers USING (supplier_id) " +
                    "   JOIN Dealers ON supplier_id = Dealers.dealer_id"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Supply_view AS " +
                    "   SELECT Suppliers.title AS supplier, " +
                    "       order_date, " +
                    "       receipt_date, " +
                    "       Supply_list.price AS price, " +
                    "       Supply_list.count AS count, " +
                    "       price * count AS total, " +
                    "       part_id, info_id, " +
                    "       supplier_id, " +
                    "       supply_id, " +
                    "       stype_id, " +
                    "       country_id AS supplier_country_id " +
                    "   FROM Part_info " +
                    "   JOIN Supply USING (supply_id) " +
                    "   JOIN Suppliers USING (Supplier_id) " +
                    "   JOIN Supply_list USING (supply_id, part_id)"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Part_info_view AS " +
                    "   SELECT info_id AS part, " +
                    "       part_type, " +
                    "       manufacturer, " +
                    "       car_name, " +
                    "       price AS wholesale_price, " +
                    "       retail_price, " +
                    "       supplier, " +
                    "       order_date, " +
                    "       receipt_date, " +
                    "       part_id, " +
                    "       car_id, " +
                    "       supply_id, " +
                    "       count, " +
                    "       supplier_id, " +
                    "       stype_id, " +
                    "       supplier_country_id, " +
                    "       type_id, " +
                    "       manufacturer_id, " +
                    "       manufacturer_country_id, " +
                    "       model_id, " +
                    "       brand_id, " +
                    "       brand_country_id " +
                    "   FROM Part_info " +
                    "   JOIN Supply_view USING (info_id, part_id, supply_id) " +
                    "   JOIN Part_view USING (part_id) " +
                    "   JOIN Car_view USING (car_id)"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Stock_view AS " +
                    "   SELECT cell_id, " +
                    "       SUM(count) AS occupied, " +
                    "       capacity - SUM(count) AS free " +
                    "   FROM In_stock " +
                    "   JOIN Stock USING (cell_id) " +
                    "   GROUP BY cell_id, capacity"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW In_stock_view AS " +
                    "   SELECT cell_id, " +
                    "       capacity, " +
                    "       part_info, " +
                    "       part_type, " +
                    "       count " +
                    "   FROM In_stock " +
                    "   JOIN Stock USING (cell_id)"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Customers_view AS " +
                    "   SELECT last_name || ' ' || first_name AS customer, " +
                    "       phone_number || (CASE WHEN email IS NULL THEN '' ELSE ', email: ' || email END) AS contacts, " +
                    "       customer_id " +
                    "   FROM Customers"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Cart_list_view AS " +
                    "   SELECT cart_id, " +
                    "       transaction_date, " +
                    "       part_info, " +
                    "       count, " +
                    "       price, " +
                    "       count * price AS total_price " +
                    "   FROM Carts " +
                    "   JOIN Cart_list USING (cart_id)"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Order_list_view AS " +
                    "   SELECT customer_id, " +
                    "       cart_id, " +
                    "       order_date, " +
                    "       transaction_date, " +
                    "       part_info, " +
                    "       count, " +
                    "       price, " +
                    "       price * count AS total_price " +
                    "   FROM Orders " +
                    "   JOIN Carts USING (cart_id) " +
                    "   JOIN Cart_list USING (cart_id)"
        );

        statement.execute(
                "CREATE OR REPLACE VIEW Reject_view AS " +
                    "   SELECT Reject.transaction_date AS reject_date, " +
                    "       part_info, " +
                    "       Reject.count AS count, " +
                    "       price, " +
                    "       Reject.count * price AS total_price, " +
                    "       cart_id, " +
                    "       Carts.transaction_date AS purchase_date " +
                    "   FROM Reject " +
                    "   JOIN Cart_list USING (cart_id, part_info) " +
                    "   JOIN Carts USING (cart_id)"
        );
    }

    private void fillTables(Statement statement) throws SQLException {
        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Users VALUES(1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918'); " +
                    "   INSERT INTO Users VALUES(2, 'seller', 'a4279eae47aaa7417da62434795a011ccb0ec870f7f56646d181b5500a892a9a'); " +
                    "   INSERT INTO Users VALUES(3, 'user', '04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb'); " +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Countries VALUES(1, 'Россия'); " +
                    "   INSERT INTO Countries VALUES(2, 'Германия'); " +
                    "   INSERT INTO Countries VALUES(3, 'Китай'); " +
                    "   INSERT INTO Countries VALUES(4, 'Япония'); " +
                    "   INSERT INTO Countries VALUES(5, 'США'); " +
                    "   INSERT INTO Countries VALUES(6, 'Франция'); " +
                    "   INSERT INTO Countries VALUES(7, 'Чешская республика'); " +
                    "   INSERT INTO Countries VALUES(8, 'Нидерланды'); " +
                    "   INSERT INTO Countries VALUES(9, 'Бельгия'); " +
                    "   INSERT INTO Countries VALUES(10, 'Дания'); " +
                    "   INSERT INTO Countries VALUES(11, 'Польша'); " +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Car_brands VALUES(1, 1, 'Lada'); " +
                    "   INSERT INTO Car_brands VALUES(2, 2, 'Volkswagen'); " +
                    "   INSERT INTO Car_brands VALUES(3, 2, 'Mercedes-benz'); " +
                    "   INSERT INTO Car_brands VALUES(4, 2, 'Audi'); " +
                    "   INSERT INTO Car_brands VALUES(5, 2, 'Porsche'); " +
                    "   INSERT INTO Car_brands VALUES(6, 3, 'Haval'); " +
                    "   INSERT INTO Car_brands VALUES(7, 3, 'Chery'); " +
                    "   INSERT INTO Car_brands VALUES(8, 4, 'Toyota'); " +
                    "   INSERT INTO Car_brands VALUES(9, 4, 'Lexus'); " +
                    "   INSERT INTO Car_brands VALUES(10, 4, 'Nissan'); " +
                    "   INSERT INTO Car_brands VALUES(11, 4, 'Honda'); " +
                    "   INSERT INTO Car_brands VALUES(12, 5, 'Ford'); " +
                    "   INSERT INTO Car_brands VALUES(13, 5, 'Tesla'); " +
                    "   INSERT INTO Car_brands VALUES(14, 6, 'Renault'); " +
                    "   INSERT INTO Car_brands VALUES(15, 7, 'Skoda'); " +
                    "END;"
        );

        statement.execute(
                "BEGIN" +
                    "   INSERT INTO Car_model VALUES(1, 1, 'Vesta'); " +
                    "   INSERT INTO Car_model VALUES(2, 1, 'Granta'); " +
                    "   INSERT INTO Car_model VALUES(3, 1, 'XRAY'); " +
                    "   INSERT INTO Car_model VALUES(4, 1, 'Largus'); " +
                    "   INSERT INTO Car_model VALUES(5, 2, 'Polo'); " +
                    "   INSERT INTO Car_model VALUES(6, 2, 'Passat'); " +
                    "   INSERT INTO Car_model VALUES(7, 2, 'Taos'); " +
                    "   INSERT INTO Car_model VALUES(8, 2, 'Tiguan'); " +
                    "   INSERT INTO Car_model VALUES(9, 2, 'Touareg'); " +
                    "   INSERT INTO Car_model VALUES(10, 3, 'S-class'); " +
                    "   INSERT INTO Car_model VALUES(11, 3, 'E-class'); " +
                    "   INSERT INTO Car_model VALUES(12, 3, 'GLA'); " +
                    "   INSERT INTO Car_model VALUES(13, 3, 'GLB'); " +
                    "   INSERT INTO Car_model VALUES(14, 3, 'GLC'); " +
                    "   INSERT INTO Car_model VALUES(15, 4, 'Q3'); " +
                    "   INSERT INTO Car_model VALUES(16, 4, 'Q5'); " +
                    "   INSERT INTO Car_model VALUES(17, 4, 'Q7'); " +
                    "   INSERT INTO Car_model VALUES(18, 4, 'A3'); " +
                    "   INSERT INTO Car_model VALUES(19, 4, 'A5'); " +
                    "   INSERT INTO Car_model VALUES(20, 4, 'A7'); " +
                    "   INSERT INTO Car_model VALUES(21, 5, '911'); " +
                    "   INSERT INTO Car_model VALUES(22, 5, 'Cayenne'); " +
                    "   INSERT INTO Car_model VALUES(23, 5, 'Panamera'); " +
                    "   INSERT INTO Car_model VALUES(24, 6, 'Jolion'); " +
                    "   INSERT INTO Car_model VALUES(25, 6, 'F7'); " +
                    "   INSERT INTO Car_model VALUES(26, 7, 'Tiggo 4'); " +
                    "   INSERT INTO Car_model VALUES(27, 7, 'Tiggo 7 PRO'); " +
                    "   INSERT INTO Car_model VALUES(28, 8, 'Corolla'); " +
                    "   INSERT INTO Car_model VALUES(29, 8, 'Camry'); " +
                    "   INSERT INTO Car_model VALUES(30, 8, 'RAV 4'); " +
                    "   INSERT INTO Car_model VALUES(31, 8, 'Land Cruiser'); " +
                    "   INSERT INTO Car_model VALUES(32, 9, 'LS'); " +
                    "   INSERT INTO Car_model VALUES(33, 9, 'RX'); " +
                    "   INSERT INTO Car_model VALUES(34, 9, 'LX'); " +
                    "   INSERT INTO Car_model VALUES(35, 10, 'Pathfinder'); " +
                    "   INSERT INTO Car_model VALUES(36, 10, 'Qashqai'); " +
                    "   INSERT INTO Car_model VALUES(37, 10, 'Murano'); " +
                    "   INSERT INTO Car_model VALUES(38, 11, 'CR-V'); " +
                    "   INSERT INTO Car_model VALUES(39, 12, 'Mustang'); " +
                    "   INSERT INTO Car_model VALUES(40, 12, 'Focus'); " +
                    "   INSERT INTO Car_model VALUES(41, 12, 'Mondeo'); " +
                    "   INSERT INTO Car_model VALUES(42, 13, 'Model S'); " +
                    "   INSERT INTO Car_model VALUES(43, 13, 'Model 3'); " +
                    "   INSERT INTO Car_model VALUES(44, 13, 'Model Y'); " +
                    "   INSERT INTO Car_model VALUES(45, 14, 'Kaptur'); " +
                    "   INSERT INTO Car_model VALUES(46, 14, 'Arkana'); " +
                    "   INSERT INTO Car_model VALUES(47, 14, 'Duster'); " +
                    "   INSERT INTO Car_model VALUES(48, 15, 'Rapid'); " +
                    "   INSERT INTO Car_model VALUES(49, 15, 'Octavia'); " +
                    "   INSERT INTO Car_model VALUES(50, 15, 'Karoq'); " +
                    "   INSERT INTO Car_model VALUES(51, 15, 'Kodiaq'); " +
                    "   INSERT INTO Car_model VALUES(52, 15, 'Superb');" +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Car VALUES(1, 1, 'I'); " +
                    "   INSERT INTO Car VALUES(2, 1, 'I - restayling'); " +
                    "   INSERT INTO Car VALUES(3, 2, 'I'); " +
                    "   INSERT INTO Car VALUES(4, 2, 'I - restayling'); " +
                    "   INSERT INTO Car VALUES(5, 3, 'I'); " +
                    "   INSERT INTO Car VALUES(6, 4, 'I'); " +
                    "   INSERT INTO Car VALUES(7, 4, 'I - restayling'); " +
                    "   INSERT INTO Car VALUES(8, 5, 'V'); " +
                    "   INSERT INTO Car VALUES(9, 5, 'V - restayling'); " +
                    "   INSERT INTO Car VALUES(10, 5, 'VI'); " +
                    "   INSERT INTO Car VALUES(11, 6, 'B7'); " +
                    "   INSERT INTO Car VALUES(12, 6, 'B8'); " +
                    "   INSERT INTO Car VALUES(13, 6, 'B8 - restayling'); " +
                    "   INSERT INTO Car VALUES(14, 7, 'I'); " +
                    "   INSERT INTO Car VALUES(15, 8, 'I'); " +
                    "   INSERT INTO Car VALUES(16, 8, 'I - restayling'); " +
                    "   INSERT INTO Car VALUES(17, 8, 'II'); " +
                    "   INSERT INTO Car VALUES(18, 8, 'II - restayling'); " +
                    "   INSERT INTO Car VALUES(19, 9, 'II'); " +
                    "   INSERT INTO Car VALUES(20, 9, 'II - restayling'); " +
                    "   INSERT INTO Car VALUES(21, 9, 'III'); " +
                    "   INSERT INTO Car VALUES(22, 10, 'IV (W205)'); " +
                    "   INSERT INTO Car VALUES(23, 10, 'IV - restayling (W205)'); " +
                    "   INSERT INTO Car VALUES(24, 10, 'V (W206)'); " +
                    "   INSERT INTO Car VALUES(25, 11, 'V'); " +
                    "   INSERT INTO Car VALUES(26, 11, 'V - restayling'); " +
                    "   INSERT INTO Car VALUES(27, 12, 'I - restayling (X156)'); " +
                    "   INSERT INTO Car VALUES(28, 12, 'II (H247)'); " +
                    "   INSERT INTO Car VALUES(29, 13, 'I (X247)'); " +
                    "   INSERT INTO Car VALUES(30, 14, 'I (X253)'); " +
                    "   INSERT INTO Car VALUES(31, 14, 'I - restayling (X253)'); " +
                    "   INSERT INTO Car VALUES(32, 15, 'I - restayling (8U)'); " +
                    "   INSERT INTO Car VALUES(33, 15, 'II (F3)'); " +
                    "   INSERT INTO Car VALUES(34, 16, 'II (FY)'); " +
                    "   INSERT INTO Car VALUES(35, 16, 'II - restayling (FY)'); " +
                    "   INSERT INTO Car VALUES(36, 17, 'II (4M)'); " +
                    "   INSERT INTO Car VALUES(37, 17, 'II - restayling (4M)'); " +
                    "   INSERT INTO Car VALUES(38, 18, 'III - restayling (8V)'); " +
                    "   INSERT INTO Car VALUES(39, 18, 'IV (8Y)'); " +
                    "   INSERT INTO Car VALUES(40, 19, 'I - restayling (8T)'); " +
                    "   INSERT INTO Car VALUES(41, 19, 'II (F5)'); " +
                    "   INSERT INTO Car VALUES(42, 20, 'I - restayling (4G)'); " +
                    "   INSERT INTO Car VALUES(43, 20, 'II (4K)'); " +
                    "   INSERT INTO Car VALUES(44, 21, 'VI - restayling (997)'); " +
                    "   INSERT INTO Car VALUES(45, 21, 'VII (991)'); " +
                    "   INSERT INTO Car VALUES(46, 21, 'VII - restayling (991)'); " +
                    "   INSERT INTO Car VALUES(47, 22, 'II - restayling (958)'); " +
                    "   INSERT INTO Car VALUES(48, 22, 'III'); " +
                    "   INSERT INTO Car VALUES(49, 23, 'II'); " +
                    "   INSERT INTO Car VALUES(50, 23, 'II - restayling'); " +
                    "   INSERT INTO Car VALUES(51, 24, 'I'); " +
                    "   INSERT INTO Car VALUES(52, 25, 'I'); " +
                    "   INSERT INTO Car VALUES(53, 26, 'I'); " +
                    "   INSERT INTO Car VALUES(54, 27, 'I'); " +
                    "   INSERT INTO Car VALUES(55, 28, 'X (E140, E150)'); " +
                    "   INSERT INTO Car VALUES(56, 28, 'X - restayling (E140, E150)'); " +
                    "   INSERT INTO Car VALUES(57, 28, 'XI (E160, E170)'); " +
                    "   INSERT INTO Car VALUES(58, 28, 'XI - restayling (E160, E170)'); " +
                    "   INSERT INTO Car VALUES(59, 29, 'VIII (XV70)'); " +
                    "   INSERT INTO Car VALUES(60, 29, 'VIII - restayling (XV70)'); " +
                    "   INSERT INTO Car VALUES(61, 30, 'IV - restayling (CA40)'); " +
                    "   INSERT INTO Car VALUES(62, 30, 'V (XA50)'); " +
                    "   INSERT INTO Car VALUES(63, 31, '200 Series - restayling'); " +
                    "   INSERT INTO Car VALUES(64, 31, '300 Series'); " +
                    "   INSERT INTO Car VALUES(65, 32, 'IV - restayling 1'); " +
                    "   INSERT INTO Car VALUES(66, 32, 'IV - restayling 2'); " +
                    "   INSERT INTO Car VALUES(67, 32, 'V'); " +
                    "   INSERT INTO Car VALUES(68, 33, 'III - restayling'); " +
                    "   INSERT INTO Car VALUES(69, 33, 'IV'); " +
                    "   INSERT INTO Car VALUES(70, 34, 'III - restayling 1'); " +
                    "   INSERT INTO Car VALUES(71, 34, 'III - restayling 2'); " +
                    "   INSERT INTO Car VALUES(72, 34, 'IV'); " +
                    "   INSERT INTO Car VALUES(73, 35, 'IV - restayling'); " +
                    "   INSERT INTO Car VALUES(74, 35, 'V'); " +
                    "   INSERT INTO Car VALUES(75, 36, 'II - restayling'); " +
                    "   INSERT INTO Car VALUES(76, 36, 'III'); " +
                    "   INSERT INTO Car VALUES(77, 37, 'II - restayling 2 (Z51)'); " +
                    "   INSERT INTO Car VALUES(78, 37, 'III (Z52)'); " +
                    "   INSERT INTO Car VALUES(79, 38, 'IV - restayling'); " +
                    "   INSERT INTO Car VALUES(80, 38, 'V'); " +
                    "   INSERT INTO Car VALUES(81, 39, 'V - restayling'); " +
                    "   INSERT INTO Car VALUES(82, 39, 'VI'); " +
                    "   INSERT INTO Car VALUES(83, 40, 'IV'); " +
                    "   INSERT INTO Car VALUES(84, 40, 'IV - restayling'); " +
                    "   INSERT INTO Car VALUES(85, 41, 'IV - restayling'); " +
                    "   INSERT INTO Car VALUES(86, 41, 'V'); " +
                    "   INSERT INTO Car VALUES(87, 42, 'I'); " +
                    "   INSERT INTO Car VALUES(88, 43, 'I'); " +
                    "   INSERT INTO Car VALUES(89, 44, 'I'); " +
                    "   INSERT INTO Car VALUES(90, 45, 'I'); " +
                    "   INSERT INTO Car VALUES(91, 45, 'I - restayling'); " +
                    "   INSERT INTO Car VALUES(92, 46, 'I'); " +
                    "   INSERT INTO Car VALUES(93, 47, 'I - restayling'); " +
                    "   INSERT INTO Car VALUES(94, 47, 'II'); " +
                    "   INSERT INTO Car VALUES(95, 48, 'I - restayling'); " +
                    "   INSERT INTO Car VALUES(96, 48, 'II'); " +
                    "   INSERT INTO Car VALUES(97, 49, 'III (A7)'); " +
                    "   INSERT INTO Car VALUES(98, 49, 'III - restayling (A7)'); " +
                    "   INSERT INTO Car VALUES(99, 49, 'IV (A8)'); " +
                    "   INSERT INTO Car VALUES(100, 50, 'I'); " +
                    "   INSERT INTO Car VALUES(101, 51, 'I'); " +
                    "   INSERT INTO Car VALUES(102, 51, 'I - restayling'); " +
                    "   INSERT INTO Car VALUES(103, 52, 'II - restayling'); " +
                    "   INSERT INTO Car VALUES(104, 52, 'III'); " +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Manufacturer VALUES(1, 1, 'Lada'); " +
                    "   INSERT INTO Manufacturer VALUES(2, 2, 'Volkswagen'); " +
                    "   INSERT INTO Manufacturer VALUES(3, 2, 'Mercedes-benz'); " +
                    "   INSERT INTO Manufacturer VALUES(4, 2, 'Audi'); " +
                    "   INSERT INTO Manufacturer VALUES(5, 2, 'Porsche'); " +
                    "   INSERT INTO Manufacturer VALUES(6, 3, 'Haval'); " +
                    "   INSERT INTO Manufacturer VALUES(7, 3, 'Chery'); " +
                    "   INSERT INTO Manufacturer VALUES(8, 4, 'Toyota'); " +
                    "   INSERT INTO Manufacturer VALUES(9, 4, 'Lexus'); " +
                    "   INSERT INTO Manufacturer VALUES(10, 4, 'Nissan'); " +
                    "   INSERT INTO Manufacturer VALUES(11, 4, 'Honda'); " +
                    "   INSERT INTO Manufacturer VALUES(12, 5, 'Ford'); " +
                    "   INSERT INTO Manufacturer VALUES(13, 5, 'Tesla'); " +
                    "   INSERT INTO Manufacturer VALUES(14, 6, 'Renault'); " +
                    "   INSERT INTO Manufacturer VALUES(15, 7, 'Skoda'); " +
                    "   INSERT INTO Manufacturer VALUES(16, 1, 'СААЗ'); " +
                    "   INSERT INTO Manufacturer VALUES(17, 2, 'SWAG'); " +
                    "   INSERT INTO Manufacturer VALUES(18, 2, 'Bosch'); " +
                    "   INSERT INTO Manufacturer VALUES(19, 3, 'Китай'); " +
                    "   INSERT INTO Manufacturer VALUES(20, 4, 'GMB'); " +
                    "   INSERT INTO Manufacturer VALUES(21, 5, 'TRW'); " +
                    "   INSERT INTO Manufacturer VALUES(22, 8, 'Nipparts'); " +
                    "   INSERT INTO Manufacturer VALUES(23, 9, 'Van Wezel'); " +
                    "   INSERT INTO Manufacturer VALUES(24, 10, 'Klokkerholm'); " +
                    "   INSERT INTO Manufacturer VALUES(25, 11, 'Krosno'); " +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Part_type VALUES(1, 'Передний бампер'); " +
                    "   INSERT INTO Part_type VALUES(2, 'Задний бампер'); " +
                    "   INSERT INTO Part_type VALUES(3, 'Капот'); " +
                    "   INSERT INTO Part_type VALUES(4, 'Цепь/ремень ГРМ'); " +
                    "   INSERT INTO Part_type VALUES(5, 'Лобовое стекло'); " +
                    "   INSERT INTO Part_type VALUES(6, 'Стойки'); " +
                    "   INSERT INTO Part_type VALUES(7, 'Фары передние'); " +
                    "   INSERT INTO Part_type VALUES(8, 'Фары задние'); " +
                    "   INSERT INTO Part_type VALUES(9, 'Боковые зеркала'); " +
                    "   INSERT INTO Part_type VALUES(10, 'Щётки стеклоочистителя'); " +
                    "   INSERT INTO Part_type VALUES(11, 'Усилители руля'); " +
                    "   INSERT INTO Part_type VALUES(12, 'Усилитель тормозов'); " +
                    "   INSERT INTO Part_type VALUES(13, 'Радиатор охлаждения'); " +
                    "   INSERT INTO Part_type VALUES(14, 'Интеркулер'); " +
                    "   INSERT INTO Part_type VALUES(15, 'Генератор'); " +
                    "   INSERT INTO Part_type VALUES(16, 'ЭБУ'); " +
                    "   INSERT INTO Part_type VALUES(17, 'Помпа охлаждения'); " +
                    "   INSERT INTO Part_type VALUES(18, 'Аккумуляторная батарея'); " +
                    "   INSERT INTO Part_type VALUES(19, 'Лямбда-зонд'); " +
                    "   INSERT INTO Part_type VALUES(20, 'Поршневые кольца'); " +
                    "   INSERT INTO Part_type VALUES(21, 'Воздушный фильтр'); " +
                    "   INSERT INTO Part_type VALUES(22, 'Тормозной цилиндр'); " +
                    "   INSERT INTO Part_type VALUES(23, 'Диск сцепления'); " +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Parts VALUES(1, 3, 1, 1, 5400); " +
                    "   INSERT INTO Parts VALUES(2, 3, 2, 1, 6800); " +
                    "   INSERT INTO Parts VALUES(3, 3, 3, 1, 4890); " +
                    "   INSERT INTO Parts VALUES(4, 3, 4, 1, 7250); " +
                    "   INSERT INTO Parts VALUES(5, 3, 5, 1, 5590); " +
                    "   INSERT INTO Parts VALUES(6, 3, 6, 1, 8690); " +
                    "   INSERT INTO Parts VALUES(7, 3, 7, 1, 4890); " +
                    "   INSERT INTO Parts VALUES(8, 13, 2, 1, 5400); " +
                    "   INSERT INTO Parts VALUES(9, 13, 3, 1, 6800); " +
                    "   INSERT INTO Parts VALUES(10, 13, 4, 1, 4890); " +
                    "   INSERT INTO Parts VALUES(11, 13, 5, 1, 7250); " +
                    "   INSERT INTO Parts VALUES(12, 13, 6, 1, 5590); " +
                    "   INSERT INTO Parts VALUES(13, 13, 7, 1, 8690); " +
                    "   INSERT INTO Parts VALUES(14, 13, 1, 1, 4890); " +
                    "   INSERT INTO Parts VALUES(15, 6, 3, 1, 5400); " +
                    "   INSERT INTO Parts VALUES(16, 6, 4, 1, 6800); " +
                    "   INSERT INTO Parts VALUES(17, 6, 5, 1, 4890); " +
                    "   INSERT INTO Parts VALUES(18, 6, 6, 1, 7250); " +
                    "   INSERT INTO Parts VALUES(19, 6, 7, 1, 5590); " +
                    "   INSERT INTO Parts VALUES(20, 6, 1, 1, 8690); " +
                    "   INSERT INTO Parts VALUES(21, 6, 2, 1, 4890); " +
                    "   INSERT INTO Parts VALUES(22, 3, 8, 2, 13490); " +
                    "   INSERT INTO Parts VALUES(23, 3, 8, 9, 9790); " +
                    "   INSERT INTO Parts VALUES(24, 3, 9, 2, 15990); " +
                    "   INSERT INTO Parts VALUES(98, 3, 9, 10, 12100); " +
                    "   INSERT INTO Parts VALUES(25, 3, 10, 2, 19990); " +
                    "   INSERT INTO Parts VALUES(26, 3, 10, 9, 16890); " +
                    "   INSERT INTO Parts VALUES(27, 3, 11, 2, 17590); " +
                    "   INSERT INTO Parts VALUES(28, 3, 11, 10, 9990); " +
                    "   INSERT INTO Parts VALUES(29, 3, 12, 2, 15390); " +
                    "   INSERT INTO Parts VALUES(30, 3, 12, 9, 11390); " +
                    "   INSERT INTO Parts VALUES(31, 3, 13, 2, 28790); " +
                    "   INSERT INTO Parts VALUES(32, 3, 13, 10, 13990); " +
                    "   INSERT INTO Parts VALUES(33, 3, 14, 2, 18250); " +
                    "   INSERT INTO Parts VALUES(34, 3, 14, 9, 14990); " +
                    "   INSERT INTO Parts VALUES(35, 3, 15, 2, 23190); " +
                    "   INSERT INTO Parts VALUES(36, 3, 15, 10, 15900); " +
                    "   INSERT INTO Parts VALUES(37, 3, 16, 2, 25550); " +
                    "   INSERT INTO Parts VALUES(38, 3, 16, 9, 17990); " +
                    "   INSERT INTO Parts VALUES(39, 3, 17, 2, 31990); " +
                    "   INSERT INTO Parts VALUES(40, 3, 17, 10, 19990); " +
                    "   INSERT INTO Parts VALUES(41, 3, 18, 2, 18899); " +
                    "   INSERT INTO Parts VALUES(42, 3, 18, 9, 13789); " +
                    "   INSERT INTO Parts VALUES(43, 3, 19, 2, 25990); " +
                    "   INSERT INTO Parts VALUES(44, 3, 19, 10, 15650); " +
                    "   INSERT INTO Parts VALUES(45, 3, 20, 2, 19340); " +
                    "   INSERT INTO Parts VALUES(46, 3, 20, 9, 11900); " +
                    "   INSERT INTO Parts VALUES(47, 3, 21, 2, 28100); " +
                    "   INSERT INTO Parts VALUES(48, 3, 21, 10, 15900); " +
                    "   INSERT INTO Parts VALUES(49, 6, 21, 2, 13490); " +
                    "   INSERT INTO Parts VALUES(50, 6, 21, 9, 9790); " +
                    "   INSERT INTO Parts VALUES(51, 6, 10, 2, 15990); " +
                    "   INSERT INTO Parts VALUES(52, 6, 10, 10, 12100); " +
                    "   INSERT INTO Parts VALUES(53, 6, 11, 2, 19990); " +
                    "   INSERT INTO Parts VALUES(54, 6, 11, 9, 16890); " +
                    "   INSERT INTO Parts VALUES(55, 6, 12, 2, 17590); " +
                    "   INSERT INTO Parts VALUES(56, 6, 12, 10, 9990); " +
                    "   INSERT INTO Parts VALUES(57, 6, 13, 2, 15390); " +
                    "   INSERT INTO Parts VALUES(58, 6, 13, 9, 11390); " +
                    "   INSERT INTO Parts VALUES(59, 6, 14, 2, 28790); " +
                    "   INSERT INTO Parts VALUES(60, 6, 14, 10, 13990); " +
                    "   INSERT INTO Parts VALUES(61, 6, 15, 2, 18250); " +
                    "   INSERT INTO Parts VALUES(62, 6, 15, 9, 14990); " +
                    "   INSERT INTO Parts VALUES(63, 6, 16, 2, 23190); " +
                    "   INSERT INTO Parts VALUES(64, 6, 16, 10, 15900); " +
                    "   INSERT INTO Parts VALUES(65, 6, 17, 2, 25550); " +
                    "   INSERT INTO Parts VALUES(66, 6, 17, 9, 17990); " +
                    "   INSERT INTO Parts VALUES(67, 6, 18, 2, 31990); " +
                    "   INSERT INTO Parts VALUES(68, 6, 18, 10, 19990); " +
                    "   INSERT INTO Parts VALUES(69, 6, 19, 2, 18899); " +
                    "   INSERT INTO Parts VALUES(70, 6, 19, 9, 13789); " +
                    "   INSERT INTO Parts VALUES(71, 6, 20, 2, 25990); " +
                    "   INSERT INTO Parts VALUES(72, 6, 20, 10, 15650); " +
                    "   INSERT INTO Parts VALUES(73, 6, 9, 2, 19340); " +
                    "   INSERT INTO Parts VALUES(74, 6, 9, 9, 11900); " +
                    "   INSERT INTO Parts VALUES(75, 6, 8, 2, 28100); " +
                    "   INSERT INTO Parts VALUES(76, 6, 8, 10, 15900); " +
                    "   INSERT INTO Parts VALUES(77, 3, 55, 23, 12000); " +
                    "   INSERT INTO Parts VALUES(78, 3, 55, 24, 12800); " +
                    "   INSERT INTO Parts VALUES(79, 3, 55, 8, 19990); " +
                    "   INSERT INTO Parts VALUES(80, 3, 56, 8, 23990); " +
                    "   INSERT INTO Parts VALUES(81, 3, 56, 24, 14579); " +
                    "   INSERT INTO Parts VALUES(82, 3, 57, 8, 19990); " +
                    "   INSERT INTO Parts VALUES(83, 3, 57, 23, 12490); " +
                    "   INSERT INTO Parts VALUES(84, 3, 58, 8, 23249); " +
                    "   INSERT INTO Parts VALUES(85, 3, 58, 24, 16490); " +
                    "   INSERT INTO Parts VALUES(86, 3, 59, 8, 32880); " +
                    "   INSERT INTO Parts VALUES(87, 3, 59, 23, 19890); " +
                    "   INSERT INTO Parts VALUES(88, 3, 60, 8, 23569); " +
                    "   INSERT INTO Parts VALUES(89, 3, 60, 24, 16990); " +
                    "   INSERT INTO Parts VALUES(90, 3, 61, 8, 22550); " +
                    "   INSERT INTO Parts VALUES(91, 3, 61, 23, 15290); " +
                    "   INSERT INTO Parts VALUES(92, 3, 62, 8, 26990); " +
                    "   INSERT INTO Parts VALUES(93, 3, 62, 24, 16990); " +
                    "   INSERT INTO Parts VALUES(94, 3, 63, 8, 19790); " +
                    "   INSERT INTO Parts VALUES(95, 3, 63, 23, 13790); " +
                    "   INSERT INTO Parts VALUES(96, 3, 64, 8, 16590); " +
                    "   INSERT INTO Parts VALUES(97, 3, 64, 24, 10390); " +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Supplier_types VALUES(1, 'Фирма'); " +
                    "   INSERT INTO Supplier_types VALUES(2, 'Дилер'); " +
                    "   INSERT INTO Supplier_types VALUES(3, 'Небольшое производство'); " +
                    "   INSERT INTO Supplier_types VALUES(4, 'Мелкий магазин'); " +
                    "END;"
        );

        statement.execute(
                "BEGIN INSERT " +
                    "   INTO Suppliers VALUES(1, 1, 1, 'Lada'); " +
                    "   INSERT INTO Suppliers VALUES(2, 1, 2, 'Volkswagen'); " +
                    "   INSERT INTO Suppliers VALUES(3, 1, 2, 'Mercedes-benz'); " +
                    "   INSERT INTO Suppliers VALUES(4, 1, 2, 'Audi'); " +
                    "   INSERT INTO Suppliers VALUES(5, 1, 2, 'Porsche'); " +
                    "   INSERT INTO Suppliers VALUES(6, 1, 3, 'Haval'); " +
                    "   INSERT INTO Suppliers VALUES(7, 1, 3, 'Chery'); " +
                    "   INSERT INTO Suppliers VALUES(8, 1, 4, 'Toyota'); " +
                    "   INSERT INTO Suppliers VALUES(9, 1, 4, 'Lexus'); " +
                    "   INSERT INTO Suppliers VALUES(10, 1, 4, 'Nissan'); " +
                    "   INSERT INTO Suppliers VALUES(11, 1, 4, 'Honda'); " +
                    "   INSERT INTO Suppliers VALUES(12, 1, 5, 'Ford'); " +
                    "   INSERT INTO Suppliers VALUES(13, 1, 5, 'Tesla'); " +
                    "   INSERT INTO Suppliers VALUES(14, 1, 6, 'Renault'); " +
                    "   INSERT INTO Suppliers VALUES(15, 1, 7, 'Skoda'); " +
                    "   INSERT INTO Suppliers VALUES(16, 2, 1, 'Автомир'); " +
                    "   INSERT INTO Suppliers VALUES(17, 2, 4, 'Kuoshi'); " +
                    "   INSERT INTO Suppliers VALUES(18, 2, 2, 'Das welt auto'); " +
                    "   INSERT INTO Suppliers VALUES(19, 3, 1, 'Моторинвест'); " +
                    "   INSERT INTO Suppliers VALUES(20, 3, 4, 'Mijao Koujou'); " +
                    "   INSERT INTO Suppliers VALUES(21, 3, 2, 'Meier autoteile'); " +
                    "   INSERT INTO Suppliers VALUES(22, 4, 1, 'АвтоВосток'); " +
                    "   INSERT INTO Suppliers VALUES(23, 4, 1, 'ВагАвто'); " +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   INSERT INTO Dealers VALUES(16, 8); " +
                    "   INSERT INTO Dealers VALUES(17, 3); " +
                    "   INSERT INTO Dealers VALUES(18, 4); " +
                    "END;"
        );

        statement.execute(
                "BEGIN " +
                    "   -- Lada \n" +
                    "   INSERT INTO Price_list VALUES(1, 1, 3400, 14); " +
                    "   INSERT INTO Price_list VALUES(2, 1, 3800, 14); " +
                    "   INSERT INTO Price_list VALUES(3, 1, 2890, 14); " +
                    "   INSERT INTO Price_list VALUES(4, 1, 4250, 14); " +
                    "   INSERT INTO Price_list VALUES(5, 1, 3500, 14); " +
                    "   INSERT INTO Price_list VALUES(6, 1, 4390, 14); " +
                    "   INSERT INTO Price_list VALUES(7, 1, 2490, 14); " +
                    "   INSERT INTO Price_list VALUES(8, 1, 2200, 30); " +
                    "   INSERT INTO Price_list VALUES(9, 1, 3600, 30); " +
                    "   INSERT INTO Price_list VALUES(10, 1, 2290, 30); " +
                    "   INSERT INTO Price_list VALUES(11, 1, 3450, 14); " +
                    "   INSERT INTO Price_list VALUES(12, 1, 2690, 30); " +
                    "   INSERT INTO Price_list VALUES(13, 1, 4290, 14); " +
                    "   INSERT INTO Price_list VALUES(14, 1, 2390, 14); " +
                    "   INSERT INTO Price_list VALUES(15, 1, 2100, 30); " +
                    "   INSERT INTO Price_list VALUES(16, 1, 3700, 30); " +
                    "   INSERT INTO Price_list VALUES(17, 1, 2190, 30); " +
                    "   INSERT INTO Price_list VALUES(18, 1, 3950, 14); " +
                    "   INSERT INTO Price_list VALUES(19, 1, 2890, 30); " +
                    "   INSERT INTO Price_list VALUES(20, 1, 4290, 30); " +
                    "   INSERT INTO Price_list VALUES(21, 1, 2990, 20); " +
                    "   -- Volkswagen \n" +
                    "   INSERT INTO Price_list VALUES(22, 2, 7490, 14); " +
                    "   INSERT INTO Price_list VALUES(24, 2, 8990, 14); " +
                    "   INSERT INTO Price_list VALUES(25, 2, 10990, 30); " +
                    "   INSERT INTO Price_list VALUES(27, 2, 9590, 20); " +
                    "   INSERT INTO Price_list VALUES(29, 2, 7990, 14); " +
                    "   INSERT INTO Price_list VALUES(31, 2, 15790, 30); " +
                    "   INSERT INTO Price_list VALUES(33, 2, 10250, 20); " +
                    "   INSERT INTO Price_list VALUES(35, 2, 11190, 30); " +
                    "   INSERT INTO Price_list VALUES(37, 2, 13550, 14); " +
                    "   INSERT INTO Price_list VALUES(39, 2, 18790, 14); " +
                    "   INSERT INTO Price_list VALUES(41, 2, 9899, 30); " +
                    "   INSERT INTO Price_list VALUES(43, 2, 12990, 14); " +
                    "   INSERT INTO Price_list VALUES(45, 2, 10540, 30); " +
                    "   INSERT INTO Price_list VALUES(47, 2, 14900, 14); " +
                    "   INSERT INTO Price_list VALUES(49, 2, 6490, 20); " +
                    "   INSERT INTO Price_list VALUES(51, 2, 7990, 30); " +
                    "   INSERT INTO Price_list VALUES(53, 2, 9590, 14); " +
                    "   INSERT INTO Price_list VALUES(55, 2, 9590, 14); " +
                    "   INSERT INTO Price_list VALUES(57, 2, 8390, 30); " +
                    "   INSERT INTO Price_list VALUES(59, 2, 14190, 14); " +
                    "   INSERT INTO Price_list VALUES(61, 2, 9350, 30); " +
                    "   INSERT INTO Price_list VALUES(63, 2, 11590, 14); " +
                    "   INSERT INTO Price_list VALUES(65, 2, 14250, 14); " +
                    "   INSERT INTO Price_list VALUES(67, 2, 14990, 14); " +
                    "   INSERT INTO Price_list VALUES(69, 2, 9790, 20); " +
                    "   INSERT INTO Price_list VALUES(71, 2, 13990, 30); " +
                    "   INSERT INTO Price_list VALUES(73, 2, 10300, 14); " +
                    "   INSERT INTO Price_list VALUES(75, 2, 16000, 20); " +
                    "END;"
        );

        statement.execute(
                "BEGIN" +
                    "   INSERT INTO Customers VALUES(1, 'Иван', 'Иванов', 'ivanov@mail.com', '8-800-555-35-35');" +
                    "   INSERT INTO Customers VALUES(2, 'Петр', 'Петров', 'petrov@mail.com', '8-800-555-35-36');" +
                    "   INSERT INTO Customers VALUES(3, 'Анна', 'Сидорова', 'sidorova@mail.com', '8-800-555-35-37');" +
                    "   INSERT INTO Customers VALUES(4, 'Мария', 'Кузнецова', 'kuznetsova@mail.com', '8-800-555-35-38');" +
                    "   INSERT INTO Customers VALUES(5, 'Дмитрий', 'Смирнов', 'smirnov@mail.com', '8-800-555-35-39');" +
                    "   INSERT INTO Customers VALUES(6, 'Елена', 'Иванова', 'ivanova@mail.com', '8-800-555-35-40');" +
                    "   INSERT INTO Customers VALUES(7, 'Алексей', 'Петров', 'alekseev@mail.com', '8-800-555-35-41');" +
                    "   INSERT INTO Customers VALUES(8, 'Ольга', 'Сидорова', 'sidorova2@mail.com', '8-800-555-35-42');" +
                    "   INSERT INTO Customers VALUES(9, 'Ирина', 'Кузнецова', 'kuznetsova2@mail.com', '8-800-555-35-43');" +
                    "   INSERT INTO Customers VALUES(10, 'Андрей', 'Смирнов', 'smirnov2@mail.com', '8-800-555-35-44');" +
                    "END;"
        );

        statement.execute(
                ""
        );
    }

    private void deleteTables(Statement statement) throws SQLException {
        try { statement.execute("DROP TABLE Users"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Reject"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Orders"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Cart_list"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Customers"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Carts"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE In_stock"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Stock"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Part_info"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Supply_list"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Supply"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Price_list"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Dealers"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Suppliers"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Supplier_types"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Parts"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Part_type"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Manufacturer"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Car"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Car_model"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Car_brands"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}

        try { statement.execute("DROP TABLE Countries"); }
        catch (SQLException e) { if (e.getErrorCode() != 942) throw e;}
    }
}

