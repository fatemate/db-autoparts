package ru.edu.nsu.fit.martynov.autoparts.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class Queries {
    private ObservableList<Query> queriesList;
    private static Queries queries = null;

    public static Queries getInstance() {
        if (queries == null) {
            synchronized (Queries.class) {
                if (queries == null)
                    queries = new Queries();
            }
        }
        return queries;
    }

    public ObservableList<Query> getQueries() {
        return queriesList;
    }

    private Queries() {
        ArrayList<Query> arrayList = new ArrayList<>();

        // Тип поставщика, тип детали, тип поставщика, дата начала, дата конца, ид детали, кол-во деталей
        arrayList.add(new Query(
                        "Запрос 1.1",
                        "SELECT title AS \"Поставщик\", " +
                                " 	 type_name AS \"Тип поставщика\", " +
                                " 	 country_name AS \"Страна\" " +
                                "FROM (" +
                                " 	SELECT DISTINCT supplier_id, " +
                                " 		 title, " +
                                " 	 	 stype_id, " +
                                " 		 country_id" +
                                " 	FROM (" +
                                "	 	SELECT DISTINCT supplier_id, " +
                                " 		 	 title, " +
                                " 			 stype_id, " +
                                " 			 country_id" +
                                "		FROM (" +
                                "			SELECT *" +
                                "			FROM Suppliers" +
                                " 			WHERE stype_id = ?" +
                                "		)" +
                                "		JOIN Price_list USING(supplier_id)" +
                                "		JOIN Parts USING(part_id)" +
                                "		WHERE part_type = ?" +
                                " 	)" +
                                " 	UNION (" +
                                "		SELECT supplier_id, " +
                                " 			 title, " +
                                " 			 stype_id, " +
                                " 			 country_id" +
                                "		FROM (" +
                                "			SELECT *" +
                                "			FROM (" +
                                "				SELECT *" +
                                "				FROM Suppliers" +
                                " 				WHERE stype_id = ?" +
                                "			)" +
                                "			JOIN Supply USING(supplier_id)" +
                                "			WHERE receipt_date BETWEEN ? AND ?" +
                                "		)" +
                                "		JOIN Supply_list USING(supply_id)" +
                                " 		WHERE part_id = ?" +
                                "		GROUP BY part_id, supplier_id, title, stype_id, country_id" +
                                " 		HAVING SUM(count) > ?" +
                                " 	)" +
                                ")" +
                                "JOIN Supplier_types USING(stype_id)" +
                                "JOIN Countries USING(country_id)"
                )
        );

        // Тип поставщика, тип детали, тип поставщика, дата начала, дата конца, ид детали, кол-во деталей
        arrayList.add(new Query(
                        "Запрос 1.2",
                        "SELECT COUNT(*) AS \"Кол-во\"" +
                                "FROM (" +
                                " 	SELECT DISTINCT supplier_id" +
                                " 	FROM (" +
                                "	 	SELECT supplier_id" +
                                "		FROM (" +
                                "			SELECT *" +
                                "			FROM Suppliers" +
                                " 			WHERE stype_id = ?" +
                                "		)" +
                                "		JOIN Price_list USING(supplier_id)" +
                                "		JOIN Parts USING(part_id)" +
                                "		WHERE part_type = ?" +
                                " 	)" +
                                " 	UNION (" +
                                "		SELECT supplier_id" +
                                "		FROM (" +
                                "			SELECT *" +
                                "			FROM (" +
                                "				SELECT *" +
                                "				FROM Suppliers" +
                                " 				WHERE stype_id = ?" +
                                "			)" +
                                "			JOIN Supply USING(supplier_id)" +
                                "			WHERE receipt_date BETWEEN ? AND ?" +
                                "		)" +
                                "		JOIN Supply_list USING(supply_id)" +
                                " 		WHERE part_id = ?" +
                                "		GROUP BY part_id, supplier_id" +
                                " 		HAVING SUM(count) > ?" +
                                " 	)" +
                                ")"
                )
        );

        // Ид детали
        arrayList.add(new Query(
                        "Запрос 2",
                        "SELECT Suppliers.title AS \"Поставщик\"," +
                                " 	 type_name AS \"Тип поставщика\"," +
                                " 	 Part_type.title || ' для ' || car_name AS \"Деталь\"," +
                                " 	 Manufacturer.title AS \"Производитель\"," +
                                " 	 Sorted_price_list.price AS \"Цена\"," +
                                " 	 delivery_time AS \"Время поставки\"" +
                                "FROM (" +
                                " 	SELECT *" +
                                " 	FROM Price_list" +
                                " 	WHERE part_id = ?" +
                                ") Sorted_price_list" +
                                "JOIN Parts USING(part_id)" +
                                "JOIN Part_type ON Parts.part_type = Part_type.type_id" +
                                "JOIN Car_view ON Parts.car_id = Car_view.car_id" +
                                "JOIN Manufacturer USING(manufacturer_id)" +
                                "JOIN Suppliers USING(supplier_id)" +
                                "JOIN Supplier_types USING(stype_id)"
                )
        );

        // Тип детали, дата начала, дата конца, ид детали, кол-во деталей
        arrayList.add(new Query(
                        "Запрос 3.1",
                        "SELECT DISTINCT customer_id, " +
                                " 	 first_name || ' ' || last_name AS \"Покупатель\", " +
                                " 	 email, " +
                                " 	 phone_number AS \"Телефон\"" +
                                "FROM (" +
                                " 	(" +
                                "		SELECT cart_id  " +
                                "		FROM (" +
                                "			SELECT *" +
                                "			FROM Part_type" +
                                "			JOIN Parts ON Part_type.type_id = Parts.part_type" +
                                "			WHERE type_id = ?" +
                                "		) " +
                                "		JOIN (" +
                                "			SELECT part_id, " +
                                " 				 cart_id " +
                                "			FROM (" +
                                "				SELECT *" +
                                "				FROM Carts" +
                                "				WHERE transaction_date BETWEEN ? AND ?" +
                                "			)" +
                                "			JOIN Cart_list USING(cart_id)" +
                                "			JOIN Part_info ON Cart_list.part_info = Part_info.info_id" +
                                "		) USING(part_id)" +
                                "	) UNION (" +
                                "		SELECT cart_id" +
                                "		FROM Carts" +
                                "		JOIN Cart_list USING(cart_id)" +
                                "		JOIN Part_info ON Cart_list.part_info = Part_info.info_id" +
                                "		WHERE part_id = ? AND transaction_date IS NOT NULL" +
                                "		GROUP BY part_id, cart_id" +
                                "		HAVING SUM(count) > ?" +
                                "	)" +
                                ")" +
                                "JOIN Orders USING (cart_id)" +
                                "JOIN Customers USING(customer_id)"
                )
        );

        // Тип детали, дата начала, дата конца, ид детали, кол-во деталей
        arrayList.add(new Query(
                        "Запрос 3.2",
                        "SELECT COUNT(DISTINCT customer_id) AS \"Кол-во\"	 " +
                                "FROM (" +
                                " 	(" +
                                "		SELECT cart_id  " +
                                "		FROM (" +
                                "			SELECT *" +
                                "			FROM Part_type" +
                                "			JOIN Parts ON Part_type.type_id = Parts.part_type" +
                                "			WHERE type_id = ?" +
                                "		) " +
                                "		JOIN (" +
                                "			SELECT part_id, " +
                                " 				 cart_id " +
                                "			FROM (" +
                                "				SELECT *" +
                                "				FROM Carts" +
                                "				WHERE transaction_date BETWEEN ? AND ?" +
                                "			)" +
                                "			JOIN Cart_list USING(cart_id)" +
                                "			JOIN Part_info ON Cart_list.part_info = Part_info.info_id" +
                                "		) USING(part_id)" +
                                "	) UNION (" +
                                "		SELECT cart_id" +
                                "		FROM Carts" +
                                "		JOIN Cart_list USING(cart_id)" +
                                "		JOIN Part_info ON Cart_list.part_info = Part_info.info_id" +
                                "		WHERE part_id = ? AND transaction_date IS NOT NULL" +
                                "		GROUP BY part_id, cart_id" +
                                "		HAVING SUM(count) > ?" +
                                "	)" +
                                ")" +
                                "JOIN Orders USING (cart_id)" +
                                "JOIN Customers USING(customer_id)"
                )
        );

        arrayList.add(new Query(
                        "Запрос 4",
                        "SELECT cell_id AS \"Ячейка\", " +
                                " 	 Part_type.title AS \"Категория\", " +
                                " 	 Manufacturer.title AS \"Производитель\", " +
                                " 	 car_name AS \"Автомобиль\", " +
                                " 	 count AS \"Кол-во\" " +
                                "FROM (" +
                                "SELECT cell_id, " +
                                " 	 	 part_id, " +
                                " 	 	 manufacturer_id, " +
                                " 	 	 car_id, " +
                                " 	 	 part_type," +
                                "  	 	 SUM(count) AS count" +
                                "	FROM In_stock " +
                                "	JOIN Part_info ON In_stock.Part_info = Part_info.info_id " +
                                "	JOIN Parts USING (part_id)" +
                                " 	GROUP BY cell_id, part_id, manufacturer_id, car_id, part_type" +
                                ")" +
                                "JOIN Manufacturer USING (manufacturer_id)" +
                                "JOIN Car_view USING (car_id)" +
                                "JOIN Part_type ON Part_type.type_id = part_type" +
                                "ORDER BY cell_id"
                )
        );

        arrayList.add(new Query(
                        "Запрос 5.1",
                        "SELECT part_id, " +
                                "	 count, " +
                                "	 part_type, " +
                                "	 manufacturer, " +
                                "	 car_name " +
                                "FROM (" +
                                "	SELECT part_id, " +
                                "		 count, " +
                                "		 ROW_NUMBER() OVER (ORDER BY count DESC) AS RowNumber   " +
                                "	FROM (" +
                                "		SELECT part_id, " +
                                "			 SUM(count) AS count" +
                                "		FROM Cart_list " +
                                "		JOIN Part_info ON part_info = Part_info.info_id " +
                                "		JOIN Parts USING (part_id) " +
                                "		GROUP BY part_id" +
                                "	)" +
                                ") " +
                                "JOIN Part_view USING (part_id) " +
                                "JOIN Car_view USING (car_id) " +
                                "WHERE RowNumber <= 10" +
                                "ORDER BY RowNumber"
                )
        );

        arrayList.add(new Query(
                        "Запрос 5.2",
                        "SELECT RowNumber AS \"Price Top\", " +
                                "	 Suppliers.title AS Supplier " +
                                "FROM (" +
                                "	SELECT supplier_id, " +
                                "		 ROW_NUMBER() OVER  (ORDER BY SUM(rank_price) / COUNT(*))" +
                                "        	AS RowNumber " +
                                "	FROM (" +
                                "		SELECT part_id, " +
                                "			 supplier_id, " +
                                "			 price," +
                                "			 DENSE_RANK() OVER (PARTITION BY part_id ORDER BY price) " +
                                "  				AS rank_price" +
                                "		FROM Price_list " +
                                "		GROUP BY part_id, supplier_id, price" +
                                "	)" +
                                "GROUP BY supplier_id" +
                                ") " +
                                "JOIN Suppliers USING (supplier_id)" +
                                "WHERE RowNumber <= 10" +
                                "ORDER BY RowNumber"
                )
        );

        // Дата начала, дата конца, дата начала, дата конца, дата начала, дата конца, тип детали
        arrayList.add(new Query(
                        "Запрос 6",
                        "SELECT title AS \"Деталь\", " +
                                "ROUND(" +
                                "   		SUM(price * count) / MONTHS_BETWEEN(" +
                                " 			?, " +
                                " 			?" +
                                " 		), " +
                                " 		0" +
                                " 	) AS \"Средняя сумма\", " +
                                " 	TO_CHAR(" +
                                " 		ROUND(" +
                                " 			SUM(count) / MONTHS_BETWEEN(" +
                                " 				?, " +
                                " 				?" +
                                " 			), " +
                                " 			2" +
                                " 		), " +
                                " 		'FM9999999990.00'" +
                                " 	) AS \"Среднее кол-во\"" +
                                "FROM (" +
                                "	SELECT cart_id" +
                                "	FROM Carts" +
                                "	WHERE transaction_date BETWEEN ? AND ?" +
                                ")" +
                                "JOIN Cart_list USING(cart_id)" +
                                "JOIN (" +
                                "	SELECT type_id, title, part_id, info_id " +
                                "	FROM (" +
                                "		SELECT * " +
                                "		FROM Part_type  " +
                                "		WHERE type_id = ?" +
                                "	) " +
                                "	JOIN  Parts ON type_id = Parts.part_type" +
                                "	JOIN Part_info ON Parts.part_id = Part_info.part_id" +
                                ") ON Cart_list.part_info = info_id" +
                                "GROUP BY title"
                )
        );

        // Дата начала, дата конца, дата начала, дата конца, ид поставщика
        arrayList.add(new Query(
                        "Запрос 7.1",
                        "SELECT title AS \"Поставщик\", " +
                                " 	 type_name AS \"Тип поставщика\",  " +
                                " 	 country_name AS \"Страна\", " +
                                " 	 ROUND(100 * supplier_count / total_count, 1) || ' %' AS \"Доля в ед., %\", " +
                                " 	 ROUND(100 * supplier_price / total_price, 1) || ' %' AS \"Доля в руб., %\"," +
                                " 	 supplier_count AS \"Доля в ед.\", " +
                                " 	 supplier_price AS \"Доля в руб.\"," +
                                " 	 supplier_count || ' / ' || total_count AS \"Доля в ед., отн.\", " +
                                " 	 supplier_price || ' / ' || total_price AS \"Доля в руб., отн.\"" +
                                "FROM (" +
                                "SELECT supplier_id, " +
                                " 	 SUM(count) AS supplier_count, " +
                                " 	 SUM(count * price) AS supplier_price" +
                                "FROM (" +
                                "		SELECT * " +
                                "		FROM Carts " +
                                "		JOIN Cart_list USING(cart_id)" +
                                "		WHERE transaction_date BETWEEN ? AND ?" +
                                ") " +
                                "JOIN Part_info ON part_info = Part_info.info_id" +
                                "JOIN Supply USING(supply_id)" +
                                "GROUP BY supplier_id" +
                                ") CROSS JOIN (" +
                                "SELECT SUM(count) AS total_count, " +
                                " 		 SUM(count * price) AS total_price" +
                                "FROM (" +
                                "		SELECT * " +
                                "		FROM Carts " +
                                "		JOIN Cart_list USING(cart_id)" +
                                "		WHERE transaction_date BETWEEN ? AND ?" +
                                ") " +
                                "JOIN Part_info ON part_info = Part_info.info_id" +
                                "JOIN Supply USING(supply_id)" +
                                ")" +
                                "JOIN Suppliers USING(supplier_id)" +
                                "JOIN Countries USING(country_id)" +
                                "JOIN Supplier_types USING(stype_id)" +
                                "WHERE supplier_id = ?"
                )
        );

        // Дата начала, дата конца, дата начала, дата конца, дата начала, дата конца
        arrayList.add(new Query(
                        "Запрос 7.2",
                        "SELECT sales - reject_sum - order_sum AS profit" +
                                "FROM (" +
                                "	SELECT SUM(price * count) AS sales" +
                                "	FROM (" +
                                "		SELECT *" +
                                "		FROM Carts" +
                                "		WHERE transaction_date ? AND ?" +
                                "	)" +
                                "	JOIN Cart_list USING(Cart_id)" +
                                "	JOIN Part_info ON Cart_list.part_info = Part_info.info_id" +
                                ")" +
                                "CROSS JOIN (" +
                                "	SELECT SUM(price * Reject_info.count) AS reject_sum" +
                                "	FROM (" +
                                "		SELECT *" +
                                "		FROM Reject" +
                                "		WHERE transaction_date BETWEEN ? AND ?" +
                                "	) Reject_info" +
                                "	JOIN Cart_list ON Reject_info.cart_id = Cart_list.cart_id " +
                                " 		AND Reject_info.part_info = Cart_list.part_info" +
                                ")" +
                                "CROSS JOIN (" +
                                "	SELECT SUM(price * count) AS order_sum" +
                                "	FROM (" +
                                "		SELECT *" +
                                "		FROM Supply" +
                                "		WHERE order_date BETWEEN ? AND ?" +
                                "	) " +
                                "	JOIN Supply_list USING(supply_id)" +
                                ")"
                )
        );

        arrayList.add(new Query(
                        "Запрос 8",
                        "SELECT Revenue AS \"Выручка\", " +
                                "	 Expenses AS \"Расходы\", " +
                                "	 ROUND(Expenses / Revenue * 100, 2) || '%' AS \"Процент расх.\"" +
                                "FROM (" +
                                "SELECT SUM(count * price) AS Revenue" +
                                "	FROM Cart_list" +
                                ")" +
                                "CROSS JOIN (" +
                                "	SELECT SUM(count * price) AS Expenses" +
                                "	FROM Supply_list" +
                                ")"
                )
        );

        // Дата начала
        arrayList.add(new Query(
                        "Запрос 9.1",
                        "SELECT Suppliers.title AS \"Поставщик\", " +
                                "	 receipt_date AS \"Дата поставки\", " +
                                "	 Part_type.title AS \"Тип\", " +
                                "	 Manufacturer.title AS \"Производитель\", " +
                                "	 car_name AS \"Авто\", " +
                                "	 count AS \"Кол-во\"" +
                                "FROM ( " +
                                "SELECT * " +
                                "	FROM In_stock " +
                                "	JOIN Part_info ON Part_info.info_id = In_stock.part_info " +
                                "	JOIN Supply USING (supply_id)" +
                                "	WHERE Receipt_date <= ?" +
                                ")" +
                                "JOIN Parts USING (part_id)" +
                                "JOIN Suppliers USING (supplier_id)" +
                                "JOIN Part_type ON part_type = Part_type.type_id" +
                                "JOIN Car_view USING (car_id) " +
                                "JOIN Manufacturer USING (Manufacturer_id) "
                )
        );

        // Дата начала
        arrayList.add(new Query(
                        "Запрос 9.2",
                        "SELECT Total AS \"Весь товар\", " +
                                "	 Old AS \"Залежалый\", " +
                                "	 ROUND(Old / Total * 100, 2) || '%' AS \"Процент\"" +
                                "FROM (" +
                                "	SELECT SUM(count) AS Total" +
                                "	FROM In_stock " +
                                "	JOIN Part_info ON Part_info.info_id = In_stock.part_info " +
                                ") " +
                                "CROSS JOIN (" +
                                "	SELECT SUM(count) AS Old" +
                                "	FROM In_stock " +
                                "	JOIN Part_info ON Part_info.info_id = In_stock.part_info " +
                                "	JOIN Supply USING (supply_id)" +
                                "	WHERE Receipt_date <= ?" +
                                ")"
                )
        );

        // Дата начала, дата конца
        arrayList.add(new Query(
                        "Запрос 10",
                        "SELECT Suppliers.title AS \"Поставщик\", " +
                                "	 receipt_date AS \"Дата поставки\", " +
                                "	 Part_type.title AS \"Тип\", " +
                                "	 car_name AS \"Авто\", " +
                                "	 count AS \"Кол-во\", " +
                                "	 cart_id AS \"Номер корзины\", " +
                                "	 Carts.transaction_date AS \"Дата покупки\", " +
                                "	 reject_date AS \"Дата возврата\"" +
                                "FROM (" +
                                "	SELECT cart_id, " +
                                "            	 transaction_date AS reject_date, " +
                                "            	 supplier_id, " +
                                "            	 car_id, " +
                                "            	 part_type, " +
                                "            	 reject.count AS count, " +
                                "           	 receipt_date" +
                                "	FROM Reject " +
                                "	JOIN Part_info ON Reject.part_info = Part_info.info_id" +
                                "	JOIN Parts ON Parts.part_id = Part_info.part_id " +
                                "	JOIN Supply ON Part_info.supply_id = Supply.supply_id" +
                                "	WHERE receipt_date >= ? AND receipt_date <= ?" +
                                ")" +
                                "JOIN Suppliers USING (supplier_id)" +
                                "JOIN Part_type ON part_type = Part_type.type_id" +
                                "JOIN Car_view USING (car_id)" +
                                "JOIN Carts USING (Cart_id)"
                )
        );

        // Дата транзакции
        arrayList.add(new Query(
                        "Запрос 11",
                        "SELECT transaction_date   AS \"Дата продажи\", " +
                                "	 Manufacturer.title AS \"Производитель\"," +
                                "	 Part_type.title || ' для ' || car_name AS \"Деталь\"," +
                                "	 total_count        AS \"Количество\", " +
                                "	 total_price        AS \"Стоимость\"" +
                                "FROM (" +
                                "	SELECT part_id, " +
                                " 		 transaction_date, " +
                                " 		 SUM(count) AS total_count, " +
                                " 		 SUM(count * price) AS total_price" +
                                "	FROM (" +
                                "		SELECT cart_id, " +
                                "      			 transaction_date" +
                                "		FROM Carts" +
                                "		WHERE transaction_date IS NOT NULL " +
                                "           		AND transaction_date = ? " +
                                "	) " +
                                "JOIN Cart_list USING (cart_id)" +
                                "JOIN Part_info ON Cart_list.part_info = Part_info.info_id" +
                                "GROUP BY part_id, transaction_date" +
                                ")" +
                                "JOIN Parts USING (Part_id) " +
                                "JOIN Part_type ON Parts.part_type = Part_type.type_id" +
                                "JOIN Car_View USING (car_id) " +
                                "JOIN Manufacturer USING (Manufacturer_id) "
                )
        );

        // Дата начала, дата конца, дата начала, дата конца
        arrayList.add(new Query(
                        "Запрос 12",
                        "SELECT type 				AS \"Вид операции\", " +
                                "      	 cart_id 			AS \"Номер корзины\", " +
                                " transaction_date   	AS \"Дата продажи\", " +
                                "	 Manufacturer.title 	AS \"Производитель\"," +
                                "	 Part_type.title || ' для ' || car_name AS \"Деталь\"," +
                                "	 Count              	AS \"Количество\", " +
                                "	 sell_price              AS \"Цена\", " +
                                "	 count * sell_price 	AS \"Стоимость\"" +
                                "FROM (" +
                                "	SELECT type, " +
                                "         	 cart_id, " +
                                "            	 transaction_date, " +
                                "            	 part_info, " +
                                "         	 count, price AS sell_price" +
                                "	FROM (" +
                                "		SELECT 'Продажа' AS type, " +
                                "       		 cart_id, " +
                                "      			 transaction_date" +
                                "		FROM Carts" +
                                "		WHERE transaction_date IS NOT NULL " +
                                "           AND transaction_date >= ? " +
                                "           AND transaction_date <= ?" +
                                "	) " +
                                "JOIN Cart_list USING (cart_id)" +
                                "UNION (" +
                                "		SELECT 'Возврат' AS type, " +
                                "           	 	 Reject.cart_id, " +
                                "       	 	 Reject.transaction_date, " +
                                "         	 	 Reject.part_info, " +
                                "         	 	 Reject.count, price * -1 AS sell_price" +
                                "		FROM Reject " +
                                "      		JOIN Cart_list ON " +
                                "      			Reject.cart_id = Cart_list.cart_id " +
                                "        		AND Reject.part_info = Cart_list.part_info" +
                                "		WHERE transaction_date >= ? " +
                                "     		AND transaction_date <= ?" +
                                "	)" +
                                ")" +
                                "JOIN Part_info ON part_info = Part_info.info_id" +
                                "JOIN Parts USING (Part_id) " +
                                "JOIN Part_type ON Parts.part_type = Part_type.type_id" +
                                "JOIN Car_View USING (car_id) " +
                                "JOIN Manufacturer USING (Manufacturer_id) " +
                                "ORDER BY transaction_date"
                )
        );

        arrayList.add(new Query(
                        "Запрос 13",
                        "SELECT Manufacturer.title AS \"Производитель\", " +
                                " 	 Part_type.title AS \"Тип\", " +
                                " 	 car_name AS \"Авто\", " +
                                " 	 count AS \"Кол-во\"" +
                                "	FROM (" +
                                "		SELECT part_id, " +
                                "  			 SUM(count) AS count " +
                                "		FROM In_stock " +
                                "		JOIN Part_info ON In_stock.part_info = Part_info.info_id" +
                                "		JOIN Parts USING (part_id)" +
                                "		GROUP BY part_id" +
                                "	)" +
                                "	JOIN Parts USING (part_id)" +
                                "	JOIN Manufacturer USING (manufacturer_id)" +
                                "	JOIN Part_type ON Parts.part_type = Part_type.type_id" +
                                "	JOIN Car_view USING (car_id)"
                )
        );

        // Дата конца, дата конца, дата конца, дата конца, дата конца, дата конца,
        arrayList.add(new Query(
                        "Запрос 14",
                        "SELECT ROUND((count_before + count_sale_old + count_now) / (2 * count_sale), 2) " +
                                " 		AS count_ratio,  " +
                                " 	 ROUND((price_before + price_sale_old + price_now) / (2 * price_sale), 2) " +
                                " 		AS price_ratio" +
                                "FROM (  " +
                                " 	SELECT SUM(count_in_stock) AS count_before, " +
                                " 		 SUM(price * count_in_stock * 1.6) AS price_before" +
                                "  	FROM (" +
                                "    		SELECT part_id, " +
                                " 			 supply_id, " +
                                " 			 count_in_stock," +
                                " 			 receipt_date" +
                                "    		FROM (" +
                                "      			SELECT part_info, " +
                                " 				 SUM(count) AS count_in_stock" +
                                "      			FROM In_stock" +
                                "      			GROUP BY part_info" +
                                "    		) Stock " +
                                "    		JOIN Part_info ON Stock.part_info = Part_info.info_id" +
                                "    		JOIN Supply USING(supply_id)" +
                                "    		WHERE receipt_date < " +
                                " 			ADD_MONTHS(?, -1)" +
                                "  	)" +
                                "  	JOIN Supply_list USING(supply_id, part_id)" +
                                ") " +
                                "CROSS JOIN (" +
                                "SELECT SUM(count) AS count_sale, " +
                                " 	 SUM(price * count) AS price_sale" +
                                "FROM (" +
                                "  		SELECT * " +
                                "  		FROM Carts" +
                                "  		WHERE transaction_date BETWEEN " +
                                " 			ADD_MONTHS(?, -1) AND ?" +
                                " 	) " +
                                "JOIN Cart_list USING(cart_id)" +
                                ") " +
                                "CROSS JOIN (" +
                                "  	SELECT SUM(count_in_stock) AS count_now, " +
                                " 		 SUM(price * count_in_stock * 1.6) AS price_now" +
                                "  	FROM (" +
                                "    		SELECT part_id, " +
                                " 			 supply_id, " +
                                " 			 count_in_stock, " +
                                " 			 receipt_date" +
                                "    		FROM (" +
                                "      			SELECT part_info, " +
                                " 				 SUM(count) AS count_in_stock" +
                                "      			FROM In_stock" +
                                "      			GROUP BY part_info" +
                                "    		) Stock " +
                                "    		JOIN Part_info ON Stock.part_info = Part_info.info_id" +
                                "    		JOIN Supply USING(supply_id)" +
                                "  	)" +
                                "  	JOIN Supply_list USING(supply_id, part_id)" +
                                ") " +
                                "CROSS JOIN (" +
                                "SELECT SUM(count) AS count_sale_old, " +
                                " 	 SUM(price * count) AS price_sale_old" +
                                " 	FROM (" +
                                " 	SELECT * " +
                                " 	FROM (" +
                                "   		SELECT * " +
                                "   		FROM Carts" +
                                "   		WHERE transaction_date BETWEEN " +
                                " 			ADD_MONTHS(?, -1) " +
                                " 			AND ?" +
                                ") " +
                                "JOIN Cart_list USING(cart_id)" +
                                "JOIN Part_info ON Cart_list.part_info = Part_info.info_id" +
                                "JOIN Supply USING(supply_id)" +
                                "WHERE ADD_MONTHS(receipt_date < ?, -1)" +
                                ")" +
                                ")"
                )
        );

        arrayList.add(new Query(
                        "Запрос 15.1",
                        "SELECT cell_id AS \"Пустые ячейки\"" +
                                "FROM Stock" +
                                "LEFT JOIN In_stock USING (cell_id)" +
                                "WHERE count IS NULL"
                )
        );

        arrayList.add(new Query(
                        "Запрос 15.2",
                        "SELECT title AS \"Деталь\", " +
                                " 	 empty AS \"Свободно мест\"" +
                                "FROM (" +
                                "	SELECT part_type, " +
                                " 		 SUM(capacity - count) AS Empty" +
                                "	FROM (" +
                                "		SELECT cell_id, " +
                                " 			 SUM(count) AS count" +
                                "		FROM In_stock" +
                                "		GROUP BY cell_id" +
                                "		) " +
                                "	JOIN Stock USING (cell_id)" +
                                "	GROUP BY part_type" +
                                ") " +
                                "JOIN Part_type ON part_type = Part_type.type_id"
                )
        );

        arrayList.add(new Query(
                        "Запрос 16.1",
                        "SELECT cart_id AS \"Номер заказа\", " +
                                "	 first_name || ' ' || last_name AS  \"Покупатель\", " +
                                "	 phone_number AS \"Телефон\", " +
                                "	 price AS \"Сумма заявки\"" +
                                "FROM (" +
                                "	SELECT *" +
                                "	FROM Orders JOIN Carts USING (cart_id)" +
                                "	WHERE transaction_date IS NULL" +
                                ") " +
                                "JOIN Customers USING (customer_id) " +
                                "JOIN Cart_list USING (Cart_id)"
                )
        );

        arrayList.add(new Query(
                        "Запрос 16.2",
                        "SELECT SUM(count * price) AS \"Сумма заявок\", " +
                                " 	 COUNT(cart_id) AS \"Кол-во заявок\"" +
                                "FROM (" +
                                "	SELECT *" +
                                "	FROM Orders " +
                                "	JOIN Carts USING (cart_id)" +
                                "	WHERE transaction_date IS NULL" +
                                ")" +
                                "JOIN Cart_list USING (cart_id)"
                )
        );

        queriesList = FXCollections.observableList(arrayList);
    }
}
